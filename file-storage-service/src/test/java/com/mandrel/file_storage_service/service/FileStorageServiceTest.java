package com.mandrel.file_storage_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mandrel.file_storage_service.model.StoredFile;
import com.mandrel.file_storage_service.model.StoredFileDto;
import com.mandrel.file_storage_service.repository.StoredFileRepository;

class FileStorageServiceTest {

    @Mock
    private StoredFileRepository storedFileRepository;

    @InjectMocks
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void store_shouldPersistAndReturnDto() {
        byte[] content = new byte[] {1, 2, 3};
        StoredFileDto input = StoredFileDto.builder()
                .filename("a.bin")
                .contentType("application/octet-stream")
                .content(content)
                .build();

        StoredFile saved = new StoredFile(1L, "a.bin", "application/octet-stream", 3L, LocalDateTime.now(), content);
        given(storedFileRepository.save(any(StoredFile.class))).willReturn(saved);

        StoredFileDto result = fileStorageService.store(input);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFilename()).isEqualTo("a.bin");
        assertThat(result.getSize()).isEqualTo(3L);
        assertThat(result.getContent()).isEqualTo(content);
    }

    @Test
    void listAll_shouldDelegateToRepository() {
        given(storedFileRepository.findAllMetadata()).willReturn(List.of());
        assertThat(fileStorageService.listAll()).isEmpty();
    }

    @Test
    void getFileById_shouldReturnDtoWhenPresent() {
        byte[] content = new byte[] {1};
        StoredFile entity = new StoredFile(2L, "b.txt", "text/plain", 1L, LocalDateTime.now(), content);
        given(storedFileRepository.findById(2L)).willReturn(Optional.of(entity));

        Optional<StoredFileDto> result = fileStorageService.getFileById(2L);
        assertThat(result).isPresent();
        assertThat(result.get().getFilename()).isEqualTo("b.txt");
    }

    @Test
    void deleteById_shouldDeleteAndReturnTrueWhenExists() {
        StoredFile entity = new StoredFile();
        entity.setId(3L);
        given(storedFileRepository.findById(3L)).willReturn(Optional.of(entity));

        boolean removed = fileStorageService.deleteById(3L);
        assertThat(removed).isTrue();
    }

    @Test
    void deleteById_shouldReturnFalseWhenMissing() {
        given(storedFileRepository.findById(4L)).willReturn(Optional.empty());
        boolean removed = fileStorageService.deleteById(4L);
        assertThat(removed).isFalse();
    }
}
