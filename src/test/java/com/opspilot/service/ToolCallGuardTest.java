package com.opspilot.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolCallGuardTest {

    private final ToolCallGuard guard = new ToolCallGuard();

    @Test
    void 前三次相同调用应通过() {
        // 第1次：设基线
        assertNull(guard.check("mem1", "getDeviceStatus", "ESP32-001"));
        // 第2次：重复1，count=1
        assertNull(guard.check("mem1", "getDeviceStatus", "ESP32-001"));
        // 第3次：重复2，count=2
        assertNull(guard.check("mem1", "getDeviceStatus", "ESP32-001"));
    }

    @Test
    void 第四次相同调用应拒绝() {
        guard.check("mem2", "getDeviceStatus", "ESP32-001"); // 基线
        guard.check("mem2", "getDeviceStatus", "ESP32-001"); // count=1
        guard.check("mem2", "getDeviceStatus", "ESP32-001"); // count=2
        // 第4次：重复3，count=3 → REJECT
        String result = guard.check("mem2", "getDeviceStatus", "ESP32-001");
        assertNotNull(result);
        assertTrue(result.contains("REJECT"));
        assertTrue(result.contains("getDeviceStatus"));
    }

    @Test
    void 不同参数应重置计数() {
        guard.check("mem3", "getDeviceStatus", "ESP32-001"); // 基线
        guard.check("mem3", "getDeviceStatus", "ESP32-001"); // count=1
        // 换一个参数，重置
        assertNull(guard.check("mem3", "getDeviceStatus", "ESP32-002")); // 新基线
        // 新参数的第1次重复，count=1
        assertNull(guard.check("mem3", "getDeviceStatus", "ESP32-002")); // count=1
    }

    @Test
    void 不同memoryId应互不干扰() {
        guard.check("memA", "getDeviceStatus", "ESP32-001"); // memA 基线
        guard.check("memA", "getDeviceStatus", "ESP32-001"); // memA count=1
        guard.check("memA", "getDeviceStatus", "ESP32-001"); // memA count=2

        // memB 是另一个会话，完全独立
        assertNull(guard.check("memB", "getDeviceStatus", "ESP32-001")); // memB 基线

        // memA 第4次 → count=3 → REJECT
        assertNotNull(guard.check("memA", "getDeviceStatus", "ESP32-001"));
    }

    @Test
    void 不同工具名用相同参数也应独立计数() {
        guard.check("mem4", "getDeviceStatus", "ESP32-001"); // 基线
        guard.check("mem4", "getDeviceStatus", "ESP32-001"); // count=1

        // 不同工具，key 不同（mem4:getAlarmHistory vs mem4:getDeviceStatus），不受影响
        assertNull(guard.check("mem4", "getAlarmHistory", "ESP32-001"));
    }

    @Test
    void 重复两次后第三次重复被拦截() {
        // 验证：第2次重复（count=2）仍通过
        assertNull(guard.check("mem6", "searchKnowledge", "温度过高")); // 基线
        assertNull(guard.check("mem6", "searchKnowledge", "温度过高")); // count=1
        assertNull(guard.check("mem6", "searchKnowledge", "温度过高")); // count=2
        // 第3次重复（count=3→REJECT）
        assertNotNull(guard.check("mem6", "searchKnowledge", "温度过高"));
    }
}
