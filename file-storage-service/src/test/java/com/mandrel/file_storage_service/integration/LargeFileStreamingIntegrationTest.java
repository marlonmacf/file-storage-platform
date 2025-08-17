package com.mandrel.file_storage_service.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class LargeFileStreamingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanupLocal() throws Exception {
        Path root = Path.of("uploaded-files");
        if (Files.exists(root)) {
            try (var s = Files.list(root)) {
                s.forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });
            }
            Files.deleteIfExists(root);
        }
    }

    private static byte[] buildLargeBytes(int sizeBytes) {
        byte[] data = new byte[sizeBytes];
        for (int i = 0; i < sizeBytes; i++) {
            data[i] = (byte) (i % 251); // deterministic pattern
        }
        return data;
    }

    private static long usedMemory() {
        System.gc();
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    @Test
    void large_memory_upload_download_validates_headers_and_content() throws Exception {
        int size = 5 * 1024 * 1024; // 5MB under 10MB limit
        byte[] payload = buildLargeBytes(size);
        String filename = "large-mem.bin";

        long before = usedMemory();

        MockMultipartFile mf = new MockMultipartFile(
                "file", filename, MediaType.APPLICATION_OCTET_STREAM_VALUE, payload);

        MvcResult upload = mockMvc.perform(multipart("/files/memory/save").file(mf))
                .andExpect(status().isOk())
                .andReturn();

        Long id = Long.parseLong(upload.getResponse().getContentAsString().replaceAll("[^0-9]", ""));

        MvcResult download = mockMvc.perform(get("/files/memory/" + id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + filename + "\""))
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string("Content-Length", String.valueOf(size)))
                .andReturn();

        byte[] downloaded = download.getResponse().getContentAsByteArray();
        assertThat(downloaded.length).isEqualTo(size);
        assertThat(downloaded).isEqualTo(payload);

        long after = usedMemory();
        // Heuristic: allow generous headroom due to JVM/GC variability
        assertThat(after - before).isLessThan((long) size * 10);
    }

    @Test
    void large_local_upload_download_validates_headers_and_content() throws Exception {
        int size = 4 * 1024 * 1024; // 4MB
        byte[] payload = buildLargeBytes(size);
        String filename = "large-local.bin";

        MockMultipartFile mf = new MockMultipartFile(
                "file", filename, MediaType.APPLICATION_OCTET_STREAM_VALUE, payload);

        mockMvc.perform(multipart("/files/local/save").file(mf))
                .andExpect(status().isOk());

        MvcResult download = mockMvc.perform(get("/files/local/" + filename))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + filename + "\""))
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string("Content-Length", String.valueOf(size)))
                .andReturn();

        byte[] downloaded = download.getResponse().getContentAsByteArray();
        assertThat(downloaded.length).isEqualTo(size);
        assertThat(downloaded).isEqualTo(payload);
    }
}
