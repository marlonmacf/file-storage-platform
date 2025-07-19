package com.mandrel.file_storage_service.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Path ROOT = Paths.get("uploaded-files");

    public void store(MultipartFile file) {
        try {
            if (Files.notExists(ROOT)) {
                Files.createDirectories(ROOT);
            }
            Path destination = ROOT.resolve(file.getOriginalFilename());
            Files.write(destination, file.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }
}
