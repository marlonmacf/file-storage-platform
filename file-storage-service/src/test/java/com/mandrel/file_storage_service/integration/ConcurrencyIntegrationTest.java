package com.mandrel.file_storage_service.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import java.nio.file.*;
import java.io.IOException;
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
class ConcurrencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanupLocal() throws Exception {
        Path root = Path.of("uploaded-files");
        if (Files.exists(root)) {
            try (var s = Files.list(root)) {
                s.forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            }
            Files.deleteIfExists(root);
        }
    }

    @Test
    void parallel_uploads_to_memory_should_all_succeed() throws Exception {
        int tasks = 20;
        var pool = Executors.newFixedThreadPool(8);
        List<Callable<Long>> callables = new ArrayList<>();
        for (int i = 0; i < tasks; i++) {
            final int idx = i;
            callables.add(() -> {
                byte[] bytes = ("mem-" + idx).getBytes(StandardCharsets.UTF_8);
                MockMultipartFile mf = new MockMultipartFile("file", "m" + idx + ".txt", MediaType.TEXT_PLAIN_VALUE, bytes);
                MvcResult res = mockMvc.perform(multipart("/files/memory/save").file(mf))
                        .andExpect(status().isOk())
                        .andReturn();
                String body = res.getResponse().getContentAsString();
                String digits = body.replaceAll("[^0-9]", "");
                return Long.parseLong(digits);
            });
        }
        List<Future<Long>> futures = pool.invokeAll(callables);
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        List<Long> ids = new ArrayList<>();
        for (Future<Long> f : futures) ids.add(f.get(5, TimeUnit.SECONDS));
        assertThat(ids).hasSize(tasks);

        // Validate each uploaded file can be downloaded
        for (Long id : ids) {
            mockMvc.perform(get("/files/memory/" + id))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void parallel_downloads_from_memory_should_return_correct_content() throws Exception {
        int files = 10;
        Map<Long, byte[]> idToBytes = new java.util.concurrent.ConcurrentHashMap<>();
        for (int i = 0; i < files; i++) {
            byte[] bytes = ("data-" + i).getBytes(StandardCharsets.UTF_8);
            MockMultipartFile mf = new MockMultipartFile("file", "d" + i + ".txt", MediaType.TEXT_PLAIN_VALUE, bytes);
            MvcResult res = mockMvc.perform(multipart("/files/memory/save").file(mf))
                    .andExpect(status().isOk())
                    .andReturn();
            Long id = Long.parseLong(res.getResponse().getContentAsString().replaceAll("[^0-9]", ""));
            idToBytes.put(id, bytes);
        }

        var pool = Executors.newFixedThreadPool(8);
        List<Callable<Boolean>> downloads = new ArrayList<>();
        for (Map.Entry<Long, byte[]> e : idToBytes.entrySet()) {
            downloads.add(() -> {
                MvcResult res = mockMvc.perform(get("/files/memory/" + e.getKey()))
                        .andExpect(status().isOk())
                        .andReturn();
                byte[] content = res.getResponse().getContentAsByteArray();
                return java.util.Arrays.equals(content, e.getValue());
            });
        }
        for (Future<Boolean> f : pool.invokeAll(downloads)) {
            assertThat(f.get(5, TimeUnit.SECONDS)).isTrue();
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    void parallel_uploads_to_local_should_all_succeed() throws Exception {
        int tasks = 16;
        var pool = Executors.newFixedThreadPool(8);
        CopyOnWriteArrayList<String> filenames = new CopyOnWriteArrayList<>();

        List<Callable<String>> callables = new ArrayList<>();
        for (int i = 0; i < tasks; i++) {
            final int idx = i;
            callables.add(() -> {
                String name = "concurrent-" + idx + ".txt";
                byte[] bytes = ("local-" + idx).getBytes(StandardCharsets.UTF_8);
                MockMultipartFile mf = new MockMultipartFile("file", name, MediaType.TEXT_PLAIN_VALUE, bytes);
                mockMvc.perform(multipart("/files/local/save").file(mf))
                        .andExpect(status().isOk());
                return name;
            });
        }
        List<Future<String>> futures = pool.invokeAll(callables);
        for (Future<String> f : futures) filenames.add(f.get(5, TimeUnit.SECONDS));
        pool.shutdown();
        pool.awaitTermination(15, TimeUnit.SECONDS);

        // Validate each uploaded file exists and content matches
        for (int i = 0; i < filenames.size(); i++) {
            String name = filenames.get(i);
            String expected = "local-" + i;
            MvcResult res = mockMvc.perform(get("/files/local/" + name))
                    .andExpect(status().isOk())
                    .andReturn();
            assertThat(new String(res.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8))
                    .isEqualTo(expected);
        }
    }
}
