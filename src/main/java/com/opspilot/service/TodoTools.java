package com.opspilot.service;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class TodoTools {

    private final List<String> tasks = new ArrayList<>();
    private final List<Boolean> done = new ArrayList<>();
    private final ToolTraceCollector collector;

    public TodoTools(ToolTraceCollector collector) {
        this.collector = collector;
    }

    @Tool
    public void addTodo(String task) {
        tasks.add(task);
        done.add(false);
        collector.record("default", "addTodo", task, "已添加", 0);
    }

    @Tool
    public String listTodos() {
        long start = System.currentTimeMillis();
        String result = "";
        for (int i = 0; i < tasks.size(); i++) {
            result = result + (i + 1) + ". " + tasks.get(i) + "\n";
        }
        collector.record("default", "listTodos", "", result, System.currentTimeMillis() - start);
        return result;
    }

    @Tool
    public String completeTodo(int index) {
        long start = System.currentTimeMillis();
        if (index < 0 || index >= tasks.size()) {
            String r = "序号无效，请输入正确的序号";
            collector.record("default", "completeTodo", String.valueOf(index), r, System.currentTimeMillis() - start);
            return r;
        }
        done.set(index, true);
        String r = "已完成：" + tasks.get(index);
        collector.record("default", "completeTodo", String.valueOf(index), r, System.currentTimeMillis() - start);
        return r;
    }
}
