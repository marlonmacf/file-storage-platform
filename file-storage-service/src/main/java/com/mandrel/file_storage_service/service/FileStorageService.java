package com.mandrel.file_storage_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mandrel.file_storage_service.model.StoredFile;
import com.mandrel.file_storage_service.model.StoredFileDto;
import com.mandrel.file_storage_service.repository.StoredFileRepository;

@Service
public class FileStorageService {

    private final StoredFileRepository storedFileRepository;

    @Autowired
    public FileStorageService(StoredFileRepository storedFileRepository) {
        this.storedFileRepository = storedFileRepository;
    }

    @Transactional
    public StoredFileDto store(StoredFileDto dto) {
        StoredFile entity = new StoredFile(
                null,
                dto.getFilename(),
                dto.getContentType(),
                (long) dto.getContent().length,
                LocalDateTime.now(),
                dto.getContent());
        StoredFile saved = storedFileRepository.save(entity);
        return StoredFileDto.builder().id(saved.getId())
                .filename(saved.getFilename())
                .contentType(saved.getContentType())
                .size(saved.getSize())
                .content(saved.getContent())
                .build();
    }

    @Transactional(readOnly = true)
    public List<StoredFileDto> listAll() {
        return storedFileRepository.findAllMetadata();
    }

    @Transactional(readOnly = true)
    public Optional<StoredFileDto> getFileById(Long id) {
        return storedFileRepository.findById(id).map(file -> StoredFileDto.builder().id(file.getId())
                .filename(file.getFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .content(file.getContent())
                .build());
    }

    @Transactional
    public boolean deleteById(Long id) {
        Optional<StoredFile> file = storedFileRepository.findById(id);
        if (file.isEmpty()) {
            return false;
        }
        storedFileRepository.delete(file.get());
        return true;
    }
}