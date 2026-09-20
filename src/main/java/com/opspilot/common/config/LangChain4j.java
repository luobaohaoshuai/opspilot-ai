package com.opspilot.common.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4j {

    @Value("${deepseek.api.key}")
    private String deepseekKey;

    @Value("${deepseek.api.base-url}")
    private String deepseekUrl;

    @Value("${deepseek.api.model}")
    private String deepseekModel;

    @Value("${zhipu.api.key}")
    private String zhipuKey;

    @Value("${zhipu.api.base-url}")
    private String zhipuUrl;

    @Value("${zhipu.api.model}")
    private String zhipuModel;

    @Bean
    public OpenAiChatModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(deepseekUrl)
                .apiKey(deepseekKey)
                .modelName(deepseekModel)
                .build();
    }

    @Bean
    public  OpenAiEmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(zhipuUrl)
                .apiKey(zhipuKey)
                .modelName(zhipuModel)
                .build();
    }
}
