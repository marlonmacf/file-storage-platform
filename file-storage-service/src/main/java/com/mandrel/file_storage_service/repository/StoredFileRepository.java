package com.mandrel.file_storage_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mandrel.file_storage_service.model.StoredFile;
import com.mandrel.file_storage_service.model.StoredFileDto;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

    @Query("""
        SELECT new com.mandrel.file_storage_service.model.StoredFileDto(
            f.id, f.filename, f.contentType, f.size, f.uploadDate, f.content)
        FROM StoredFile f
    """)
    List<StoredFileDto> findAllMetadata();
}
