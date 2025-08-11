package com.mandrel.file_storage_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mandrel.file_storage_service.model.StoredFileDto;
import com.mandrel.file_storage_service.service.FileStorageService;

@WebMvcTest(FileStorageController.class)
class FileStorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    void listAllFilesFromMemory_shouldReturnOkWithJson() throws Exception {
        StoredFileDto dto = StoredFileDto.builder()
                .id(1L)
                .filename("doc.txt")
                .contentType("text/plain")
                .size(4L)
                .uploadDate(LocalDateTime.now())
                .content("test".getBytes(StandardCharsets.UTF_8))
                .build();

        given(fileStorageService.listAll()).willReturn(List.of(dto));

        mockMvc.perform(get("/files/memory"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].filename").value("doc.txt"));
    }

    @Test
    void downloadFileById_found_shouldReturnAttachment() throws Exception {
        byte[] bytes = "data".getBytes(StandardCharsets.UTF_8);
        StoredFileDto dto = StoredFileDto.builder()
                .id(2L)
                .filename("file.bin")
                .contentType("application/octet-stream")
                .size((long) bytes.length)
                .content(bytes)
                .build();

        given(fileStorageService.getFileById(2L)).willReturn(Optional.of(dto));

        mockMvc.perform(get("/files/memory/2"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"file.bin\""))
                .andExpect(header().string("Content-Length", String.valueOf(bytes.length)))
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(content().bytes(bytes));
    }

    @Test
    void downloadFileById_notFound_shouldReturn404() throws Exception {
        given(fileStorageService.getFileById(99L)).willReturn(Optional.empty());

        mockMvc.perform(get("/files/memory/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void storeFileInMemory_shouldReturnOkWithId() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "hello.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));

        StoredFileDto saved = StoredFileDto.builder()
                .id(10L)
                .filename("hello.txt")
                .contentType("text/plain")
                .size(5L)
                .content("hello".getBytes(StandardCharsets.UTF_8))
                .build();

        given(fileStorageService.store(any(StoredFileDto.class))).willReturn(saved);

        mockMvc.perform(multipart("/files/memory/save").file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Stored file with ID: 10")));
    }

    @Test
    void deleteFile_shouldReturnOkWhenRemoved() throws Exception {
        given(fileStorageService.deleteById(5L)).willReturn(true);

        mockMvc.perform(delete("/files/memory/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted file: 5"));
    }

    @Test
    void deleteFile_shouldReturnNotFoundWhenMissing() throws Exception {
        given(fileStorageService.deleteById(6L)).willReturn(false);

        mockMvc.perform(delete("/files/memory/6"))
                .andExpect(status().isNotFound());
    }
}
