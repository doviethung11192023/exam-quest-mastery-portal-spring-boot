package com.example.demo.dto;

import java.time.Instant;

public class BackupFileDTO {
    private String fileName;
    private String fileSize;
    private Instant createdAt;

    public BackupFileDTO(String fileName, String fileSize, Instant createdAt) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    // No-args constructor
    public BackupFileDTO() {
    }

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "BackupFileDTO{" +
                "fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}