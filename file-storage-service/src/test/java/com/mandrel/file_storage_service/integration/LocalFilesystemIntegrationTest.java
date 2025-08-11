package com.mandrel.file_storage_service.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
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
class LocalFilesystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanup() throws IOException {
        Path root = Path.of("uploaded-files");
        if (Files.exists(root)) {
            try (var s = Files.list(root)) {
                s.forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            }
            Files.deleteIfExists(root);
        }
    }

    @Test
    void upload_list_download_delete_flow_in_local_filesystem() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "alpha.txt", MediaType.TEXT_PLAIN_VALUE,
                "alpha".getBytes(StandardCharsets.UTF_8));

        // Upload
        mockMvc.perform(multipart("/files/local/save").file(file))
                .andExpect(status().isOk());

        // List
        MvcResult list = mockMvc.perform(get("/files/local"))
                .andExpect(status().isOk())
                .andReturn();
        String listBody = list.getResponse().getContentAsString();
        assertThat(listBody).contains("alpha.txt");

        // Download
        MvcResult download = mockMvc.perform(get("/files/local/alpha.txt"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(new String(download.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8))
                .isEqualTo("alpha");

        // Delete
        mockMvc.perform(delete("/files/local/alpha.txt")).andExpect(status().isOk());

        // Confirm deletion
        mockMvc.perform(get("/files/local/alpha.txt")).andExpect(status().isNotFound());
    }
}
