package com.opspilot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class WebSearchServiceTest {

    private final WebSearchService service = new WebSearchService(
            new RestTemplateBuilder(),
            new ObjectMapper(),
            true,
            "https://api.duckduckgo.com/",
            1000,
            3
    );

    @Test
    void shouldParseDuckDuckGoResults() {
        String body = """
                {
                  "Heading": "ESP32",
                  "AbstractText": "ESP32 is a family of low-cost, low-power microcontrollers.",
                  "AbstractURL": "https://example.com/esp32",
                  "Results": [
                    {
                      "Text": "DHT22 - A digital temperature and humidity sensor.",
                      "FirstURL": "https://example.com/dht22"
                    }
                  ],
                  "RelatedTopics": [
                    {
                      "Text": "Overheating - Common causes include poor ventilation.",
                      "FirstURL": "https://example.com/overheating"
                    },
                    {
                      "Topics": [
                        {
                          "Text": "WiFi RSSI - Signal strength indicator.",
                          "FirstURL": "https://example.com/rssi"
                        }
                      ]
                    }
                  ]
                }
                """;

        WebSearchService.SearchResponse response = service.parseDuckDuckGoResponse(body, 3);

        assertEquals(WebSearchService.SearchStatus.OK, response.status());
        assertEquals(3, response.items().size());
        assertEquals("ESP32", response.items().get(0).title());
        assertEquals("DHT22", response.items().get(1).title());
    }

    @Test
    void shouldReturnNoResultsForEmptyProviderPayload() {
        WebSearchService.SearchResponse response = service.parseDuckDuckGoResponse("{}", 3);

        assertEquals(WebSearchService.SearchStatus.NO_RESULTS, response.status());
        assertEquals(0, response.items().size());
    }

    @Test
    void shouldSanitizeInternalIdentifiersAndSecrets() {
        String sanitized = service.sanitizeQuery("ESP32-001 token=secret-value high temperature troubleshooting");

        assertFalse(sanitized.contains("ESP32-001"));
        assertFalse(sanitized.contains("secret-value"));
    }
}
