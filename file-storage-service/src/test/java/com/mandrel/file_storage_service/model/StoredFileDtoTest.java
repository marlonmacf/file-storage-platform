package com.mandrel.file_storage_service.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class StoredFileDtoTest {

    @Test
    void builder_shouldPopulateAllFields() {
        byte[] data = {1,2};
        LocalDateTime now = LocalDateTime.now();
        StoredFileDto dto = StoredFileDto.builder()
                .id(7L)
                .filename("x")
                .contentType("t")
                .size(2L)
                .uploadDate(now)
                .content(data)
                .build();

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getFilename()).isEqualTo("x");
        assertThat(dto.getContentType()).isEqualTo("t");
        assertThat(dto.getSize()).isEqualTo(2L);
        assertThat(dto.getUploadDate()).isEqualTo(now);
        assertThat(dto.getContent()).isEqualTo(data);
    }
}
