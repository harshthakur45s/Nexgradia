package com.nexgradia.backend.controller;

import com.nexgradia.backend.dto.AcademicRecordRequest;
import com.nexgradia.backend.dto.AcademicRecordResponse;
import com.nexgradia.backend.dto.StudentProfileRequest;
import com.nexgradia.backend.dto.StudentProfileResponse;
import com.nexgradia.backend.security.UserPrincipal;
import com.nexgradia.backend.service.AcademicRecordService;
import com.nexgradia.backend.service.StudentProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final StudentProfileService studentProfileService;
    private final AcademicRecordService academicRecordService;

    public StudentController(StudentProfileService studentProfileService,
                             AcademicRecordService academicRecordService) {
        this.studentProfileService = studentProfileService;
        this.academicRecordService = academicRecordService;
    }

    @GetMapping("/me")
    public ResponseEntity<StudentProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        StudentProfileResponse profile = studentProfileService.getProfileByUserId(userPrincipal.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<StudentProfileResponse> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StudentProfileRequest request) {
        StudentProfileResponse updatedProfile = studentProfileService.upsertProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/me/academics")
    public ResponseEntity<AcademicRecordResponse> getMyAcademics(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        AcademicRecordResponse response = academicRecordService.getAcademicRecordByUserId(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/academics")
    public ResponseEntity<AcademicRecordResponse> updateMyAcademics(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AcademicRecordRequest request) {
        AcademicRecordResponse updatedRecord = academicRecordService.upsertAcademicRecord(userPrincipal.getId(), request);
        return ResponseEntity.ok(updatedRecord);
    }
}
