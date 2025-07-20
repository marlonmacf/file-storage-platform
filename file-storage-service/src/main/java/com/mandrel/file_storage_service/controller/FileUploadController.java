package com.mandrel.file_storage_service.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mandrel.file_storage_service.model.StoredFileDto;
import com.mandrel.file_storage_service.service.FileUploadService;

@RestController
@RequestMapping("/files/local")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @Autowired
    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @GetMapping
    public List<StoredFileDto> listAllFilesFromLocal() {
        return fileUploadService.listAll();
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> downloadFileByFilename(@PathVariable String filename) {
        Optional<StoredFileDto> response = fileUploadService.getFileByFilename(filename);
        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        StoredFileDto file = response.get();
        ByteArrayResource resource = new ByteArrayResource(file.getContent());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getSize())
                .body(resource);
    }

    @PostMapping("/save")
    public ResponseEntity<String> uploadFileInLocal(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File must be provided");
            }
            fileUploadService.store(StoredFileDto.builder()
                    .filename(file.getOriginalFilename())
                    .content(file.getBytes())
                    .build());
            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        }
    }

    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        boolean removed = fileUploadService.deleteByFilename(filename);
        if (removed) {
            return ResponseEntity.ok("Deleted file: " + filename);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + filename);
        }
    }
}
