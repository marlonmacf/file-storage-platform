package com.mandrel.file_storage_service.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
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
import com.mandrel.file_storage_service.service.FileUploadService;

@WebMvcTest(FileUploadController.class)
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileUploadService fileUploadService;

    @Test
    void listAllFilesFromLocal_shouldReturnOkWithJson() throws Exception {
        StoredFileDto dto = StoredFileDto.builder()
                .filename("img.png")
                .content("x".getBytes(StandardCharsets.UTF_8))
                .build();

        given(fileUploadService.listAll()).willReturn(List.of(dto));

        mockMvc.perform(get("/files/local"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].filename").value("img.png"));
    }

    @Test
    void downloadFileByFilename_found_shouldReturnAttachment() throws Exception {
        byte[] bytes = new byte[] {1, 2, 3};
        StoredFileDto dto = StoredFileDto.builder()
                .filename("img.png")
                .contentType("image/png")
                .size((long) bytes.length)
                .content(bytes)
                .build();

        given(fileUploadService.getFileByFilename("img.png")).willReturn(Optional.of(dto));

        mockMvc.perform(get("/files/local/img.png"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"img.png\""))
                .andExpect(header().string("Content-Length", String.valueOf(bytes.length)))
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(content().bytes(bytes));
    }

    @Test
    void downloadFileByFilename_notFound_shouldReturn404() throws Exception {
        given(fileUploadService.getFileByFilename("missing.bin")).willReturn(Optional.empty());

        mockMvc.perform(get("/files/local/missing.bin"))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadFileInLocal_shouldReturnOk() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "foo.txt", "text/plain", "hi".getBytes(StandardCharsets.UTF_8));

        given(fileUploadService.listAll()).willReturn(List.of());

        mockMvc.perform(multipart("/files/local/save").file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("File uploaded successfully: foo.txt")));
    }

    @Test
    void deleteFile_shouldReturnOkWhenRemoved() throws Exception {
        given(fileUploadService.deleteByFilename("foo.txt")).willReturn(true);

        mockMvc.perform(delete("/files/local/foo.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted file: foo.txt"));
    }

    @Test
    void deleteFile_shouldReturnNotFoundWhenMissing() throws Exception {
        given(fileUploadService.deleteByFilename("bar.txt")).willReturn(false);

        mockMvc.perform(delete("/files/local/bar.txt"))
                .andExpect(status().isNotFound());
    }
}
