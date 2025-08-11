package com.mandrel.file_storage_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.mandrel.file_storage_service.model.StoredFileDto;

class FileUploadServiceTest {

    private final FileUploadService service = new FileUploadService();

    @AfterEach
    void cleanUp() throws IOException {
        // Clean the 'uploaded-files' directory created by FileUploadService if present
        Path root = Path.of("uploaded-files");
        if (Files.exists(root)) {
            try (var s = Files.list(root)) {
                s.forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            }
            Files.deleteIfExists(root);
        }
    }

    @Test
    void store_and_get_and_delete_roundtrip() throws IOException {
        byte[] data = new byte[] {1,2,3,4};
        service.store(StoredFileDto.builder()
                .filename("t.bin")
                .content(data)
                .build());

        List<StoredFileDto> all = service.listAll();
        assertThat(all).isNotEmpty();

        Optional<StoredFileDto> loaded = service.getFileByFilename("t.bin");
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getContent()).isEqualTo(data);

        boolean removed = service.deleteByFilename("t.bin");
        assertThat(removed).isTrue();
    }

    @Test
    void getFileByFilename_missing_shouldReturnEmpty() {
        assertThat(service.getFileByFilename("missing")).isEmpty();
    }

    @Test
    void listAll_whenEmpty_shouldReturnEmptyList() throws IOException {
        // Ensure directory is clean
        java.nio.file.Path root = java.nio.file.Path.of("uploaded-files");
        if (java.nio.file.Files.exists(root)) {
            try (var s = java.nio.file.Files.list(root)) {
                s.forEach(p -> { try { java.nio.file.Files.deleteIfExists(p); } catch (IOException ignored) {} });
            }
            java.nio.file.Files.deleteIfExists(root);
        }
        assertThat(service.listAll()).isEmpty();
    }
}
