package com.nexgradia.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class StudentProfileRequest {

    @Pattern(
            regexp = "^(\\+?[0-9]{10,15})?$",
            message = "Phone number must be valid with 10 to 15 digits"
    )
    private String phoneNumber;

    @NotBlank(message = "Institution is required")
    @Size(max = 255, message = "Institution must not exceed 255 characters")
    private String institution;

    @NotBlank(message = "Program is required")
    @Size(max = 100, message = "Program must not exceed 100 characters")
    private String program;

    @NotBlank(message = "Branch is required")
    @Size(max = 100, message = "Branch must not exceed 100 characters")
    private String branch;

    @NotNull(message = "Graduation year is required")
    @Min(value = 2000, message = "Graduation year must be 2000 or later")
    @Max(value = 2035, message = "Graduation year must be 2035 or earlier")
    private Integer graduationYear;

    @NotNull(message = "Current semester is required")
    @Min(value = 1, message = "Current semester must be at least 1")
    @Max(value = 10, message = "Current semester must not exceed 10")
    private Integer currentSemester;

    public StudentProfileRequest() {
    }

    public StudentProfileRequest(String phoneNumber, String institution, String program,
                                 String branch, Integer graduationYear, Integer currentSemester) {
        this.phoneNumber = phoneNumber;
        this.institution = institution;
        this.program = program;
        this.branch = branch;
        this.graduationYear = graduationYear;
        this.currentSemester = currentSemester;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public Integer getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(Integer currentSemester) {
        this.currentSemester = currentSemester;
    }
}
