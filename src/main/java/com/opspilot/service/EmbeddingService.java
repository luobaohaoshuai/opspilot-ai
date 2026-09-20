package com.opspilot.service;


import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class EmbeddingService {

    private final OpenAiEmbeddingModel embeddingModel;

    public EmbeddingService(OpenAiEmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    // 调智谱 Embedding API
    private List<float[]> getEmbeddings(List<String> texts) {
        List<float[]> result = new ArrayList<>();
        for (String text : texts) {
            float[] vec = embeddingModel.embed(text).content().vector();
            result.add(vec);
        }
        return result;
    }


    // 余弦相似度
    public double cosineSimilarity(float[] a, float[] b) {
         double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // 知识库
    private static final List<String> KNOWLEDGE_BASE = List.of(
            "温度超过35℃时系统自动生成高温告警，运维人员需立即检查设备散热情况。",
            "ESP32-001 设备安装于3号机房，负责监测机房温湿度。",
            "设备告警分为三个等级：温度过高、湿度过高、设备离线，告警状态默认未处理。",
            "运维平台支持通过 Agent 自然语言查询设备状态、告警历史和知识库文档。",
            "employee表的id是主键，name是姓名，age是年龄，department是部门。"
    );

    // 检索最相关的知识库片段
    public String searchBest(String question) {
        List<String> all = new ArrayList<>();
        all.add(question);
        all.addAll(KNOWLEDGE_BASE);
        List<float[]> embeddings = getEmbeddings(all);

        float[] qVec = embeddings.get(0);
        int bestIdx = 0;
        double bestScore = -1;
        for (int i = 0; i < KNOWLEDGE_BASE.size(); i++) {
            double score = cosineSimilarity(qVec, embeddings.get(i + 1));
            if (score > bestScore) {
                bestScore = score;
                bestIdx = i;
            }
        }
        return KNOWLEDGE_BASE.get(bestIdx);
    }



    // 对外开放的简单接口
    public String getEmbedding(String text) {
        try {
            List<float[]> embeddings = getEmbeddings(List.of(text));
            return "文本: " + text + "，向量维度: " + embeddings.get(0).length;
        } catch (Exception e) {
            return "调用失败: " + e.getMessage();
        }
    }
}
