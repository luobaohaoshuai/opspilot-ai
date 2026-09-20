package com.opspilot.service;

import com.opspilot.entity.KnowledgeChunk;
import com.opspilot.mapper.KnowledgeChunkMapper;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfService {

    private static final double MIN_CONFIDENCE_SCORE = 0.50;

    public enum SearchStatus {
        OK,
        NO_KNOWLEDGE,
        LOW_CONFIDENCE
    }


    private final ObjectMapper mapper = new ObjectMapper();

    public record SearchResult(SearchStatus status,
                               String source,
                               String documentName,
                               Integer chunkIndex,
                               String content,
                               double score) {
    }

    public record RagSource(String title,
                            String snippet,
                            String documentId,
                            Integer page,
                            double score) {
    }

    public record RagAnswer(String answer, List<RagSource> sources) {
    }

    private final EmbeddingModel embeddingModel;
    private final ChatLanguageModel chatModel;

    private final KnowledgeChunkMapper chunkMapper;

    public PdfService(EmbeddingModel embeddingModel,
                      ChatLanguageModel chatModel,
                      KnowledgeChunkMapper chunkMapper) {
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.chunkMapper = chunkMapper;
    }

    public String extractText(MultipartFile file) throws Exception {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += (chunkSize - overlap);
        }
        return chunks;
    }

    @Transactional
    public void embedAndStore(List<String> chunks, String documentName) throws Exception {
        List<KnowledgeChunk> preparedChunks = new ArrayList<>();
        for (int index = 0; index < chunks.size(); index++) {
            String chunk = chunks.get(index);
            KnowledgeChunk kc = new KnowledgeChunk();
            kc.setDocumentName(documentName);
            kc.setChunkIndex(index);
            kc.setContent(chunk);
            float[] vector = embeddingModel.embed(chunk).content().vector();
            kc.setVectorJson(mapper.writeValueAsString(vector));
            preparedChunks.add(kc);
        }

        QueryWrapper<KnowledgeChunk> existingDocument = new QueryWrapper<>();
        existingDocument.eq("document_name", documentName);
        chunkMapper.delete(existingDocument);
        preparedChunks.forEach(chunkMapper::insert);
    }


    public SearchResult searchResult(String question) {
        List<KnowledgeChunk> allChunks = chunkMapper.selectList(null);

        if (allChunks.isEmpty()) {
            return new SearchResult(SearchStatus.NO_KNOWLEDGE, null, null, null, null, 0.0);
        }

        float[] questionVector = embeddingModel.embed(question).content().vector();

        double bestScore = -1.0;
        int bestIndex = 0;
        try {
            for (int i = 0; i < allChunks.size(); i++) {
                float[] vec = mapper.readValue(allChunks.get(i).getVectorJson(), float[].class);
                double score = cosineSimilarity(questionVector, vec);
                if (score > bestScore) {
                    bestScore = score;
                    bestIndex = i;
                }

            }
        } catch(Exception e){
            return new SearchResult(SearchStatus.NO_KNOWLEDGE, null, null, null, null, 0.0);
        }

        KnowledgeChunk best = allChunks.get(bestIndex);
        String bestChunk = best.getContent();
        if (bestScore < MIN_CONFIDENCE_SCORE) {
            return new SearchResult(
                    SearchStatus.LOW_CONFIDENCE,
                    null,
                    best.getDocumentName(),
                    best.getChunkIndex(),
                    bestChunk,
                    bestScore
            );
        }

        return new SearchResult(
                SearchStatus.OK,
                formatSource(best, allChunks.size()),
                best.getDocumentName(),
                best.getChunkIndex(),
                bestChunk,
                bestScore
        );
    }

    public String search(String question) {
        SearchResult result = searchResult(question);
        return switch (result.status()) {
            case NO_KNOWLEDGE -> "NO_KNOWLEDGE";
            case LOW_CONFIDENCE -> "LOW_CONFIDENCE";
            case OK -> "SOURCE: " + result.source() + "\nCONTENT: " + result.content();
        };
    }

    public RagAnswer askWithLLM(String question) {
        try {
            SearchResult result = searchResult(question);
            if (result.status() == SearchStatus.NO_KNOWLEDGE) {
                return new RagAnswer("知识库为空，请先上传 PDF 文档。", List.of());
            }
            if (result.status() == SearchStatus.LOW_CONFIDENCE) {
                return new RagAnswer("资料中未找到足够可信的依据。", List.of());
            }

            String ragPrompt = """
                    You are answering a question with information from an uploaded document.
                    Use only the document content below.
                    If the content is insufficient, say you do not know.

                    Source: %s
                    Document content:
                    %s
                    """.formatted(result.source(), result.content());

            String content = chatModel.generate(
                    SystemMessage.from(ragPrompt),
                    UserMessage.from(question)
            ).content().text();

            RagSource source = new RagSource(
                    result.documentName(),
                    result.content(),
                    result.documentName(),
                    result.chunkIndex() == null ? null : result.chunkIndex() + 1,
                    result.score()
            );
            return new RagAnswer(content, List.of(source));
        } catch (Exception e) {
            throw new IllegalStateException("RAG 服务暂时不可用", e);
        }
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private String formatSource(KnowledgeChunk chunk, int total) {
        return chunk.getDocumentName() + " · 第" + (chunk.getChunkIndex() + 1) + "段/共" + total + "段";
    }

    public List<String> getDocumentList() {
        QueryWrapper<KnowledgeChunk> wrapper = new QueryWrapper<>();
        wrapper.select("document_name").groupBy("document_name").orderByAsc("document_name");
        return chunkMapper.selectList(wrapper).stream()
                .map(KnowledgeChunk::getDocumentName)
                .collect(java.util.stream.Collectors.toList());
    }

    public void deleteDocument(String documentName) {
        QueryWrapper<KnowledgeChunk> wrapper = new QueryWrapper<>();
        wrapper.eq("document_name", documentName);
        chunkMapper.delete(wrapper);
    }

    public long countDocuments() {
        return getDocumentList().size();
    }
}
