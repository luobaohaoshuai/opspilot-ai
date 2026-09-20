package com.opspilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class WebSearchService {

    private static final Logger log = LoggerFactory.getLogger(WebSearchService.class);

    public enum SearchStatus {
        OK,
        DISABLED,
        NO_RESULTS,
        FAILED
    }

    public record SearchItem(String title, String url, String snippet) {
    }

    public record SearchResponse(SearchStatus status, List<SearchItem> items, String message) {
    }

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final boolean enabled;
    private final String endpoint;
    private final int maxResults;

    public WebSearchService(RestTemplateBuilder restTemplateBuilder,
                            ObjectMapper mapper,
                            @Value("${app.web-search.enabled:true}") boolean enabled,
                            @Value("${app.web-search.endpoint:https://api.duckduckgo.com/}") String endpoint,
                            @Value("${app.web-search.timeout-ms:6000}") int timeoutMs,
                            @Value("${app.web-search.max-results:5}") int maxResults) {
        Duration timeout = Duration.ofMillis(Math.max(1000, timeoutMs));
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
        this.mapper = mapper;
        this.enabled = enabled;
        this.endpoint = endpoint;
        this.maxResults = Math.max(1, Math.min(10, maxResults));
    }

    public SearchResponse search(String query) {
        if (!enabled) {
            return new SearchResponse(SearchStatus.DISABLED, List.of(), "Web search is disabled.");
        }

        String cleanQuery = sanitizeQuery(query);
        if (cleanQuery.isBlank()) {
            return new SearchResponse(SearchStatus.NO_RESULTS, List.of(), "Search query is empty after sanitization.");
        }

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(endpoint)
                    .queryParam("q", cleanQuery)
                    .queryParam("format", "json")
                    .queryParam("no_html", "1")
                    .queryParam("skip_disambig", "1")
                    .build()
                    .encode()
                    .toUri();

            String body = restTemplate.getForObject(uri, String.class);
            return parseDuckDuckGoResponse(body, maxResults);
        } catch (RestClientException | IllegalArgumentException e) {
            log.warn("Web search failed for query '{}': {}", cleanQuery, e.getMessage());
            return new SearchResponse(SearchStatus.FAILED, List.of(), e.getMessage());
        }
    }

    public String sanitizeQuery(String query) {
        if (query == null) {
            return "";
        }

        String sanitized = query
                .replaceAll("(?i)\\bESP32-\\d{3,}\\b", "ESP32 device")
                .replaceAll("(?i)(api[_-]?key|token|password|secret)\\s*[:=]\\s*\\S+", "$1=<redacted>")
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (sanitized.length() > 180) {
            return sanitized.substring(0, 180).trim();
        }
        return sanitized;
    }

    SearchResponse parseDuckDuckGoResponse(String body, int limit) {
        if (body == null || body.isBlank()) {
            return new SearchResponse(SearchStatus.NO_RESULTS, List.of(), "Search provider returned an empty response.");
        }

        try {
            JsonNode root = mapper.readTree(body);
            List<SearchItem> items = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();

            addIfPresent(
                    items,
                    seen,
                    root.path("Heading").asText(""),
                    root.path("AbstractURL").asText(""),
                    root.path("AbstractText").asText(""),
                    limit
            );

            collectTopicItems(root.path("Results"), items, seen, limit);
            collectTopicItems(root.path("RelatedTopics"), items, seen, limit);

            if (items.isEmpty()) {
                return new SearchResponse(SearchStatus.NO_RESULTS, List.of(), "No usable public web result was found.");
            }
            return new SearchResponse(SearchStatus.OK, items, "OK");
        } catch (Exception e) {
            log.warn("Failed to parse web search response: {}", e.getMessage());
            return new SearchResponse(SearchStatus.FAILED, List.of(), e.getMessage());
        }
    }

    private void collectTopicItems(JsonNode node, List<SearchItem> items, Set<String> seen, int limit) {
        if (node == null || !node.isArray() || items.size() >= limit) {
            return;
        }

        for (JsonNode item : node) {
            if (items.size() >= limit) {
                return;
            }

            if (item.has("Topics")) {
                collectTopicItems(item.path("Topics"), items, seen, limit);
                continue;
            }

            String text = item.path("Text").asText("");
            String url = item.path("FirstURL").asText("");
            String title = titleFrom("", text, url);
            addIfPresent(items, seen, title, url, text, limit);
        }
    }

    private void addIfPresent(List<SearchItem> items,
                              Set<String> seen,
                              String title,
                              String url,
                              String snippet,
                              int limit) {
        if (items.size() >= limit || snippet == null || snippet.isBlank()) {
            return;
        }

        String cleanUrl = url == null ? "" : url.trim();
        String dedupeKey = cleanUrl.isBlank() ? snippet.trim() : cleanUrl;
        if (!seen.add(dedupeKey)) {
            return;
        }

        items.add(new SearchItem(
                valueOrFallback(title, snippet, cleanUrl),
                cleanUrl,
                truncate(snippet.trim(), 320)
        ));
    }

    private String valueOrFallback(String title, String snippet, String url) {
        if (title != null && !title.isBlank()) {
            return truncate(title.trim(), 90);
        }
        return titleFrom("", snippet, url);
    }

    private String titleFrom(String heading, String text, String url) {
        if (heading != null && !heading.isBlank()) {
            return truncate(heading.trim(), 90);
        }
        if (text != null) {
            int separator = text.indexOf(" - ");
            if (separator > 0) {
                return truncate(text.substring(0, separator).trim(), 90);
            }
            if (!text.isBlank()) {
                return truncate(text.trim(), 90);
            }
        }
        if (url != null && !url.isBlank()) {
            try {
                String host = URI.create(url).getHost();
                if (host != null && !host.isBlank()) {
                    return host;
                }
            } catch (IllegalArgumentException ignored) {
                return url;
            }
        }
        return "Web result";
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)).trim() + "...";
    }
}
