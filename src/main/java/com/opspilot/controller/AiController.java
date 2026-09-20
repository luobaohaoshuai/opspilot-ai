package com.opspilot.controller;

import com.opspilot.common.result.Result;
import com.opspilot.service.AiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/chat")
    public Result<String> chat(@RequestParam(name = "question", defaultValue = "你好") String question) {
        return Result.ok(aiService.chat(question));
    }

    @GetMapping("/rag")
    public Result<String> rag(@RequestParam(name = "question", defaultValue = "张三得过什么奖") String question) {
        return Result.ok(aiService.rag(question));
    }
}
