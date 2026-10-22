package com.nexgradia.backend.service;

import com.nexgradia.backend.dto.ResumeResponse;
import com.nexgradia.backend.model.Resume;
import com.nexgradia.backend.model.StudentProfile;
import com.nexgradia.backend.repository.ResumeRepository;
import com.nexgradia.backend.repository.StudentProfileRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ResumeService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeService.class);

    private final ResumeRepository resumeRepository;
    private final StudentProfileRepository profileRepository;
    private final StorageService storageService;
    private final ResumeValidator resumeValidator;
    private final long maxFileSizeBytes;

    public record DownloadedResume(Resource resource, Resume resume) {}

    public ResumeService(
            ResumeRepository resumeRepository,
            StudentProfileRepository profileRepository,
            StorageService storageService,
            ResumeValidator resumeValidator,
            @Value("${app.storage.max-file-size-bytes:5242880}") long maxFileSizeBytes) {
        this.resumeRepository = resumeRepository;
        this.profileRepository = profileRepository;
        this.storageService = storageService;
        this.resumeValidator = resumeValidator;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public ResumeResponse uploadResume(Long userId, MultipartFile file) {
        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Please create your student profile before uploading a resume"));

        ResumeValidator.ValidatedFileMetadata metadata = resumeValidator.validate(file, maxFileSizeBytes);

        String storageKey;
        try (InputStream inputStream = file.getInputStream()) {
            storageKey = storageService.store(inputStream, metadata.extension());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read upload stream");
        }

        try {
            Integer maxVersion = resumeRepository.findMaxVersionNumberByStudentProfileId(profile.getId());
            int newVersion = (maxVersion != null ? maxVersion : 0) + 1;

            // Deactivate any currently active resume
            resumeRepository.deactivateCurrentActiveByStudentProfileId(profile.getId());

            Resume resume = new Resume(
                    profile,
                    storageKey,
                    metadata.sanitizedFilename(),
                    metadata.contentType(),
                    file.getSize(),
                    newVersion,
                    true
            );

            Resume saved = resumeRepository.save(resume);
            return ResumeResponse.fromEntity(saved);
        } catch (Exception ex) {
            // Clean up stored file to prevent orphaned files
            try {
                storageService.delete(storageKey);
            } catch (Exception deleteEx) {
                logger.error("Failed to delete orphan file: {} after database failure", storageKey, deleteEx);
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getResumes(Long userId) {
        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student profile has not been created yet"));

        return resumeRepository.findByStudentProfileIdOrderByVersionNumberDesc(profile.getId())
                .stream()
                .map(ResumeResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumeResponse getCurrentResume(Long userId) {
        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student profile has not been created yet"));

        Resume resume = resumeRepository.findByStudentProfileIdAndActiveTrue(profile.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No active resume found for this student"));

        return ResumeResponse.fromEntity(resume);
    }

    @Transactional(readOnly = true)
    public DownloadedResume getResumeForDownload(Long userId, Long resumeId) {
        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Resume not found"));

        Resume resume = resumeRepository.findByIdAndStudentProfileId(resumeId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Resume not found"));

        Resource resource = storageService.loadAsResource(resume.getStorageKey());
        return new DownloadedResume(resource, resume);
    }
}
