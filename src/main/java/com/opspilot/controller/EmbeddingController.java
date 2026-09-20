package com.opspilot.controller;

import com.opspilot.common.result.Result;
import com.opspilot.service.EmbeddingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/embedding")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping
    public Result<String> getEmbedding(@RequestParam("text") String text) {
        return Result.ok(embeddingService.getEmbedding(text));
    }
}
