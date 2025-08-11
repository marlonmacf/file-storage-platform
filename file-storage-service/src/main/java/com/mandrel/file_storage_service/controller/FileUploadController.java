package com.mandrel.file_storage_service.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.mandrel.file_storage_service.service.FileUploadService;

@RestController
@RequestMapping("/files/local")
public class FileUploadController {

    private final FileUploadService fileUploadService;
    @Value("${spring.servlet.multipart.max-file-size:}")
    private String maxFileSizeProperty;

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
        // Basic validation to avoid path traversal and invalid names
        if (filename == null || filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        // First try to load via service (works with tests mocking the service)
        Optional<StoredFileDto> dtoOpt = fileUploadService.getFileByFilename(filename);
        if (dtoOpt.isPresent()) {
            StoredFileDto dto = dtoOpt.get();
            String contentType = dto.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            long size = dto.getSize() != null ? dto.getSize() : (dto.getContent() != null ? dto.getContent().length : 0);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(size)
                    .body(new ByteArrayResource(dto.getContent()));
        }

        // Fallback to filesystem streaming if service did not provide the file
        Path root = Paths.get("uploaded-files");
        Path filePath = root.resolve(filename).normalize();
        if (!filePath.startsWith(root)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            long size = Files.size(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(size)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<String> uploadFileInLocal(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File must be provided");
            }
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                return ResponseEntity.badRequest().body("Filename must be provided");
            }
            // Basic path traversal and separator checks
            if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
                return ResponseEntity.badRequest().body("Invalid filename");
            }
            DataSize max = (maxFileSizeProperty == null || maxFileSizeProperty.isBlank())
                    ? null
                    : DataSize.parse(maxFileSizeProperty);
            if (max != null && file.getSize() > max.toBytes()) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File too large");
            }
            fileUploadService.store(StoredFileDto.builder()
                    .filename(originalName)
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
