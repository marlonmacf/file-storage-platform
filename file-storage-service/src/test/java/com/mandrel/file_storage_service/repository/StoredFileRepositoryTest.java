package com.mandrel.file_storage_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mandrel.file_storage_service.model.StoredFile;

@DataJpaTest
class StoredFileRepositoryTest {

    @Autowired
    private StoredFileRepository repository;

    @Test
    void save_and_findAllMetadata_shouldReturnDtoWithFields() {
        byte[] content = new byte[] {1,2,3};
        StoredFile entity = new StoredFile(null, "a.txt", "text/plain", 3L, LocalDateTime.now(), content);
        repository.save(entity);

        var dtos = repository.findAllMetadata();
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getFilename()).isEqualTo("a.txt");
        assertThat(dtos.get(0).getContent()).isNotNull();
    }
}
