package com.opspilot.service;

import com.opspilot.entity.KnowledgeChunk;
import com.opspilot.mapper.KnowledgeChunkMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfServiceTest {

    @Test
    void Rag回答应返回真实文档来源而不是模拟来源() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        ChatLanguageModel chatModel = mock(ChatLanguageModel.class);
        KnowledgeChunkMapper chunkMapper = mock(KnowledgeChunkMapper.class);

        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setDocumentName("设备说明书.pdf");
        chunk.setChunkIndex(2);
        chunk.setContent("DHT22 数据引脚连接到 GPIO4。");
        chunk.setVectorJson("[1.0,0.0]");

        when(chunkMapper.selectList(isNull())).thenReturn(List.of(chunk));
        when(embeddingModel.embed("设备怎么接线？"))
                .thenReturn(Response.from(Embedding.from(new float[]{1.0f, 0.0f})));
        when(chatModel.generate(any(ChatMessage[].class)))
                .thenReturn(Response.from(AiMessage.from("数据引脚应连接到 GPIO4。")));

        PdfService service = new PdfService(embeddingModel, chatModel, chunkMapper);
        PdfService.RagAnswer answer = service.askWithLLM("设备怎么接线？");

        assertEquals("数据引脚应连接到 GPIO4。", answer.answer());
        assertFalse(answer.sources().isEmpty());
        assertEquals("设备说明书.pdf", answer.sources().getFirst().title());
        assertEquals(3, answer.sources().getFirst().page());
        assertEquals(1.0, answer.sources().getFirst().score(), 0.0001);
    }
}
