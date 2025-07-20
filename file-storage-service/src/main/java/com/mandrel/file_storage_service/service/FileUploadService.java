package com.mandrel.file_storage_service.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mandrel.file_storage_service.model.StoredFileDto;

@Service
public class FileUploadService {

    private static final Path ROOT_DIR = Paths.get("uploaded-files");

    public void store(StoredFileDto file) {
        try {
            if (Files.notExists(ROOT_DIR)) {
                Files.createDirectories(ROOT_DIR);
            }
            Path destination = ROOT_DIR.resolve(file.getFilename());
            Files.write(destination, file.getContent());
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file " + file.getFilename(), e);
        }
    }

    public List<StoredFileDto> listAll() {
        if (Files.notExists(ROOT_DIR)) {
            return List.of();
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(ROOT_DIR)) {
            List<StoredFileDto> list = new ArrayList<>();
            for (Path path : ds) {
                if (Files.isRegularFile(path)) {
                    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

                    list.add(StoredFileDto.builder()
                            .id(null)
                            .filename(path.getFileName().toString())
                            .contentType(Files.probeContentType(path))
                            .size(attrs.size())
                            .content(Files.readAllBytes(path))
                            .uploadDate(
                                    LocalDateTime.ofInstant(
                                            attrs.creationTime().toInstant(), ZoneId.systemDefault()))
                            .build());
                }
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list all files", e);
        }
    }

    public Optional<StoredFileDto> getFileByFilename(String filename) {
        Path filePath = ROOT_DIR.resolve(filename);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return Optional.empty();
        }

        try {
            return Optional.of(StoredFileDto.builder()
                    .id(null)
                    .filename(filename)
                    .contentType(Files.probeContentType(filePath))
                    .content(Files.readAllBytes(filePath))
                    .size(Files.size(filePath))
                    .build());
        } catch (IOException e) {
            throw new RuntimeException("Could not read file " + filename, e);
        }
    }

    public boolean deleteByFilename(String filename) {
        Path filePath = ROOT_DIR.resolve(filename).normalize();
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file " + filename, e);
        }
    }
}
