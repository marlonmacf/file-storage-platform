package com.mandrel.file_storage_service.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mandrel.file_storage_service.model.StoredFileDto;
import com.mandrel.file_storage_service.service.FileStorageService;

@RestController
@RequestMapping("/files/memory")
public class FileStorageController {

    private final FileStorageService fileStorageService;
    @Value("${spring.servlet.multipart.max-file-size:}")
    private String maxFileSizeProperty;

    @Autowired
    public FileStorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public List<StoredFileDto> listAllFilesFromMemory() {
        return fileStorageService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadFileById(@PathVariable Long id) {
        Optional<StoredFileDto> response = fileStorageService.getFileById(id);
        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        StoredFileDto dto = response.get();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(dto.getContentType()))
                .contentLength(dto.getSize())
                .body(new ByteArrayResource(dto.getContent()));
    }

    @PostMapping("/save")
    public ResponseEntity<String> storeFileInMemory(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File must be provided");
        }
        String content = file.getContentType();
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body("Content-Type must be provided");
        }
        DataSize max = (maxFileSizeProperty == null || maxFileSizeProperty.isBlank())
                ? null
                : DataSize.parse(maxFileSizeProperty);
        if (max != null && file.getSize() > max.toBytes()) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File too large");
        }
        StoredFileDto dto = StoredFileDto.builder()
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .content(file.getBytes())
                .build();
        StoredFileDto saved = fileStorageService.store(dto);
        return ResponseEntity.ok("Stored file with ID: " + saved.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        boolean removed = fileStorageService.deleteById(id);
        if (removed) {
            return ResponseEntity.ok("Deleted file: " + id);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + id);
        }
    }
}
