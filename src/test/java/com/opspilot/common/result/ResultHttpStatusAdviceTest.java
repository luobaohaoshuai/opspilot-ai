package com.opspilot.common.result;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ResultHttpStatusAdviceTest {

    @Test
    void 业务失败应同步设置真实Http状态码() {
        ResultHttpStatusAdvice advice = new ResultHttpStatusAdvice();
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        advice.beforeBodyWrite(
                Result.fail(ErrorCode.DEVICE_NOT_FOUND),
                null,
                null,
                null,
                null,
                response
        );

        verify(response).setStatusCode(HttpStatus.NOT_FOUND);
    }
}
