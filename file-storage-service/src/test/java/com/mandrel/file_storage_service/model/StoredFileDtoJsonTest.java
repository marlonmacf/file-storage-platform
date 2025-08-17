package com.mandrel.file_storage_service.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonTest
class StoredFileDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serialize_fullObject_shouldMatchExpectedShape() throws Exception {
        byte[] content = new byte[] {1, 2, 3};
        LocalDateTime when = LocalDateTime.of(2023, 1, 2, 3, 4, 5);

        StoredFileDto dto = StoredFileDto.builder()
                .id(42L)
                .filename("report.pdf")
                .contentType("application/pdf")
                .size((long) content.length)
                .uploadDate(when)
                .content(content)
                .build();

        String json = objectMapper.writeValueAsString(dto);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.get("id").asLong()).isEqualTo(42L);
        assertThat(node.get("filename").asText()).isEqualTo("report.pdf");
        assertThat(node.get("contentType").asText()).isEqualTo("application/pdf");
        assertThat(node.get("size").asLong()).isEqualTo(content.length);
        // Expect ISO-8601 without zone for LocalDateTime
        assertThat(node.get("uploadDate").asText()).isEqualTo("2023-01-02T03:04:05");
        // byte[] content should be Base64 encoded
        assertThat(node.get("content").asText()).isEqualTo(Base64.getEncoder().encodeToString(content));
    }

    @Test
    void serialize_withNulls_shouldIncludeNullFields_andCorrectNonNulls() throws Exception {
        StoredFileDto dto = StoredFileDto.builder()
                .id(null)
                .filename("only-name.txt")
                .contentType(null)
                .size(null)
                .uploadDate(null)
                .content(null)
                .build();

        String json = objectMapper.writeValueAsString(dto);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.has("id")).isTrue();
        assertThat(node.get("id").isNull()).isTrue();
        assertThat(node.get("filename").asText()).isEqualTo("only-name.txt");
        assertThat(node.has("contentType")).isTrue();
        assertThat(node.get("contentType").isNull()).isTrue();
        assertThat(node.has("size")).isTrue();
        assertThat(node.get("size").isNull()).isTrue();
        assertThat(node.has("uploadDate")).isTrue();
        assertThat(node.get("uploadDate").isNull()).isTrue();
        assertThat(node.has("content")).isTrue();
        assertThat(node.get("content").isNull()).isTrue();
    }
}
