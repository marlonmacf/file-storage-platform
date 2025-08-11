package com.mandrel.file_storage_service.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class MemoryStorageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void upload_and_download_flow_in_memory_storage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "greeting.txt", MediaType.TEXT_PLAIN_VALUE,
                "hello world".getBytes(StandardCharsets.UTF_8));

        // Upload
        MvcResult upload = mockMvc.perform(multipart("/files/memory/save").file(file))
                .andExpect(status().isOk())
                .andReturn();

        String body = upload.getResponse().getContentAsString();
        assertThat(body).contains("Stored file with ID:");
        Long id = Long.parseLong(body.replaceAll("[^0-9]", ""));

        // List (optional check it appears)
        MvcResult list = mockMvc.perform(get("/files/memory"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(list.getResponse().getContentAsString()).contains("greeting.txt");

        // Download by id
        MvcResult download = mockMvc.perform(get("/files/memory/" + id))
                .andExpect(status().isOk())
                .andReturn();
        byte[] downloaded = download.getResponse().getContentAsByteArray();
        assertThat(new String(downloaded, StandardCharsets.UTF_8)).isEqualTo("hello world");
    }
}
