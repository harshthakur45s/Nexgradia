package com.nexgradia.backend.controller;

import com.nexgradia.backend.dto.ResumeResponse;
import com.nexgradia.backend.model.Resume;
import com.nexgradia.backend.security.UserPrincipal;
import com.nexgradia.backend.service.ResumeService;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/students/me/resumes")
@PreAuthorize("hasRole('STUDENT')")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> uploadResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file) {
        ResumeResponse response = resumeService.uploadResume(userPrincipal.getId(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getResumes(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ResumeResponse> responses = resumeService.getResumes(userPrincipal.getId());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/current")
    public ResponseEntity<ResumeResponse> getCurrentResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ResumeResponse response = resumeService.getCurrentResume(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{resumeId}/download")
    public ResponseEntity<Resource> downloadResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long resumeId) {
        ResumeService.DownloadedResume downloaded = resumeService.getResumeForDownload(userPrincipal.getId(), resumeId);
        Resume resume = downloaded.resume();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resume.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(resume.getOriginalFilename()).build().toString())
                .contentLength(resume.getFileSizeBytes())
                .body(downloaded.resource());
    }
}
