package com.opspilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final ChatLanguageModel chatModel;
    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiService(ChatLanguageModel chatModel, JdbcTemplate jdbcTemplate, EmbeddingService embeddingService) {
        this.chatModel = chatModel;
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingService = embeddingService;
    }

    private static final String NL2SQL_PROMPT = """
            你是一个 MySQL SQL 生成器。用户说人话，你输出 SQL。

            当前数据库有一个 employee 表，列名：
            - id (BIGINT, 主键)
            - name (VARCHAR, 姓名)
            - age (INT, 年龄)
            - department (VARCHAR, 部门)

            规则：
            1. SQL 中所有关键字和运算符前后必须有空格
            2. 只生成 SELECT 语句，任何 INSERT/UPDATE/DELETE/DROP 请求都返回 sql 空字符串
            3. 与数据库无关的问题，sql 返回空字符串

            Few-shot 例子：
            用户："查所有员工" → {"sql": "SELECT * FROM employee", "explain": "查询全部员工"}
            用户："年龄大于20的员工" → {"sql": "SELECT * FROM employee WHERE age > 20", "explain":"查询年龄大于20的员工"}
            用户："查张三的部门" → {"sql": "SELECT department FROM employee WHERE name = '张三'", "explain":"查询张三所在的部门"}

            严格按 JSON 格式返回，不要代码块：
            {"sql": "生成的SQL", "explain": "这条SQL做了什么"}
            """;

    public String chat(String question) {
        String content = chatModel.generate(
                SystemMessage.from(NL2SQL_PROMPT),
                UserMessage.from(question)
        ).content().text();

        try {
            JsonNode parsed = objectMapper.readTree(content);
            String sql = parsed.get("sql").asText();
            String explain = parsed.get("explain").asText();

            if (sql.isEmpty()) {
                return "与数据库无关的问题";
            }

            String safeResult = validateSql(sql);
            if (safeResult != null) {
                return safeResult;
            }

            String finalSql = addLimit(sql);
            List<Map<String, Object>> result = jdbcTemplate.queryForList(finalSql);
            return "SQL: " + sql + "\n解释: " + explain + "\n结果: " + result;
        } catch (Exception e) {
            return "解析失败，原始返回:\n" + content;
        }
    }

    private String validateSql(String sql) {
        String upper = sql.trim().toUpperCase();

        // 1. 禁止多语句
        if (sql.contains(";")) {
            return "安全拦截：禁止多语句查询";
        }

        // 2. 必须 SELECT 开头
        if (!upper.startsWith("SELECT")) {
            return "安全拦截：只允许 SELECT 查询，禁止 " + sql;
        }

        // 3. 表名白名单
        if (!upper.contains("EMPLOYEE") && !upper.contains("DEVICE")
                && !upper.contains("DEVICE_DATA") && !upper.contains("ALARM")
                && !upper.contains("KNOWLEDGE_CHUNK")) {
            return "安全拦截：不允许查询的表";
        }

        return null;  // 通过
    }

    private String addLimit(String sql) {
        if (!sql.trim().toUpperCase().contains("LIMIT")) {
            return sql.trim() + " LIMIT 100";
        }
        return sql;
    }

    public String rag(String question) {
        String context = embeddingService.searchBest(question);
        String ragPrompt = "已知以下信息：" + context + " 请根据信息回答用户问题。如果信息不足以回答，就说不知道。";

        String content = chatModel.generate(
                SystemMessage.from(ragPrompt),
                UserMessage.from(question)
        ).content().text();

        return "检索到: " + context + "\n回答: " + content;
    }
}
