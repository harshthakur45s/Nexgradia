package com.nexgradia.backend.dto;

import com.nexgradia.backend.model.Resume;
import java.time.Instant;

public class ResumeResponse {

    private Long id;
    private Long studentProfileId;
    private String originalFilename;
    private String contentType;
    private Long fileSizeBytes;
    private Integer versionNumber;
    private boolean active;
    private Instant createdAt;

    public ResumeResponse() {
    }

    public ResumeResponse(Long id, Long studentProfileId, String originalFilename,
                          String contentType, Long fileSizeBytes, Integer versionNumber,
                          boolean active, Instant createdAt) {
        this.id = id;
        this.studentProfileId = studentProfileId;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.versionNumber = versionNumber;
        this.active = active;
        this.createdAt = createdAt;
    }

    public static ResumeResponse fromEntity(Resume entity) {
        return new ResumeResponse(
                entity.getId(),
                entity.getStudentProfile().getId(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getFileSizeBytes(),
                entity.getVersionNumber(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getStudentProfileId() {
        return studentProfileId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
