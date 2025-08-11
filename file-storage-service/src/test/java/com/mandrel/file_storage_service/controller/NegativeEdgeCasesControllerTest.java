package com.mandrel.file_storage_service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.servlet.multipart.max-file-size=1KB",
    "spring.servlet.multipart.max-request-size=1KB"
})
class NegativeEdgeCasesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void upload_empty_file_memory_should_400() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.txt", MediaType.TEXT_PLAIN_VALUE, new byte[0]);
        mockMvc.perform(multipart("/files/memory/save").file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File must be provided"));
    }

    @Test
    void upload_missing_file_part_memory_should_400() throws Exception {
        mockMvc.perform(multipart("/files/memory/save"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_unknown_content_type_memory_should_400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "x.bin", null, "abc".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/files/memory/save").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Content-Type must be provided"));
    }

    @Test
    void upload_empty_file_local_should_400() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.txt", MediaType.TEXT_PLAIN_VALUE, new byte[0]);
        mockMvc.perform(multipart("/files/local/save").file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File must be provided"));
    }

    @Test
    void upload_missing_file_part_local_should_400() throws Exception {
        mockMvc.perform(multipart("/files/local/save"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_path_traversal_filename_local_should_400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "../evil.txt", MediaType.TEXT_PLAIN_VALUE, "bad".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/files/local/save").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid filename"));
    }

    @Test
    void upload_oversized_should_413() throws Exception {
        // Build ~20MB payload to exceed the configured 10MB limit
        byte[] big = new byte[20 * 1024 * 1024];
        MockMultipartFile large = new MockMultipartFile("file", "big.bin", MediaType.APPLICATION_OCTET_STREAM_VALUE, big);
        mockMvc.perform(multipart("/files/memory/save").file(large))
                .andExpect(status().isPayloadTooLarge());
    }
}
