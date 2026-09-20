package com.opspilot.controller;

import com.opspilot.common.RateLimit;
import com.opspilot.common.result.Result;
import com.opspilot.service.AgentResponse;
import com.opspilot.service.AgentService;
import com.opspilot.entity.AgentChatRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }
    @RateLimit(maxRequests = 10)
    @PostMapping("/chat")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AgentResponse> chat(@Valid @RequestBody AgentChatRequest request,
                                      Authentication authentication) {
        String username = authentication == null ? "anonymous" : authentication.getName();
        return Result.ok(agentService.chat(username, request.getMemoryId(), request.getMessage()));
    }
}
