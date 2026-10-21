package com.nexgradia.backend.dto;

import com.nexgradia.backend.model.StudentProfile;
import java.time.Instant;

public class StudentProfileResponse {

    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String institution;
    private String program;
    private String branch;
    private Integer graduationYear;
    private Integer currentSemester;
    private boolean profileCompleted;
    private Instant createdAt;
    private Instant updatedAt;

    public StudentProfileResponse() {
    }

    public StudentProfileResponse(Long id, Long userId, String email, String fullName,
                                  String phoneNumber, String institution, String program,
                                  String branch, Integer graduationYear, Integer currentSemester,
                                  boolean profileCompleted, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.institution = institution;
        this.program = program;
        this.branch = branch;
        this.graduationYear = graduationYear;
        this.currentSemester = currentSemester;
        this.profileCompleted = profileCompleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static StudentProfileResponse fromEntity(StudentProfile profile) {
        return new StudentProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getUser().getFullName(),
                profile.getPhoneNumber(),
                profile.getInstitution(),
                profile.getProgram(),
                profile.getBranch(),
                profile.getGraduationYear(),
                profile.getCurrentSemester(),
                profile.isProfileCompleted(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getInstitution() {
        return institution;
    }

    public String getProgram() {
        return program;
    }

    public String getBranch() {
        return branch;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public Integer getCurrentSemester() {
        return currentSemester;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
