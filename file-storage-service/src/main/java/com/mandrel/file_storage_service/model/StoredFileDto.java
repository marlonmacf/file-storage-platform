package com.mandrel.file_storage_service.model;

import java.time.LocalDateTime;

public class StoredFileDto {
    private Long id;
    private String filename;
    private String contentType;
    private Long size;
    private LocalDateTime uploadDate;
    private byte[] content;

    public StoredFileDto(Long id, String filename, String contentType, Long size, LocalDateTime uploadDate, byte[] content) {
        this.id = id;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.uploadDate = uploadDate;
        this.content = content;
    }

    public StoredFileDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public static StoredFileMetadataDtoBuilder builder() {
        return new StoredFileMetadataDtoBuilder();
    }

    public static class StoredFileMetadataDtoBuilder {
        private Long id;
        private String filename;
        private String contentType;
        private Long size;
        private LocalDateTime uploadDate;
        private byte[] content;

        public StoredFileMetadataDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public StoredFileMetadataDtoBuilder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public StoredFileMetadataDtoBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public StoredFileMetadataDtoBuilder size(Long size) {
            this.size = size;
            return this;
        }

        public StoredFileMetadataDtoBuilder uploadDate(LocalDateTime uploadDate) {
            this.uploadDate = uploadDate;
            return this;
        }

        public StoredFileMetadataDtoBuilder content(byte[] content) {
            this.content = content;
            return this;
        }

        public StoredFileDto build() {
            StoredFileDto dto = new StoredFileDto();
            dto.setId(id);
            dto.setFilename(filename);
            dto.setContentType(contentType);
            dto.setSize(size);
            dto.setUploadDate(uploadDate);
            dto.setContent(content);
            return dto;
        }
    }
}