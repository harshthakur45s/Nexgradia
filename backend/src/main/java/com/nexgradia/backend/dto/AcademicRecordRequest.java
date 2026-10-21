package com.nexgradia.backend.dto;

import com.nexgradia.backend.model.AcademicResultType;
import com.nexgradia.backend.model.VerificationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AcademicRecordRequest {

    @NotBlank(message = "Class 10 board is required")
    @Size(max = 100, message = "Class 10 board must not exceed 100 characters")
    private String tenthBoard;

    @NotBlank(message = "Class 10 school is required")
    @Size(max = 255, message = "Class 10 school must not exceed 255 characters")
    private String tenthSchool;

    @NotNull(message = "Class 10 passing year is required")
    @Min(value = 2000, message = "Class 10 passing year must be 2000 or later")
    @Max(value = 2035, message = "Class 10 passing year must be 2035 or earlier")
    private Integer tenthPassingYear;

    @NotNull(message = "Class 10 result type is required")
    private AcademicResultType tenthResultType;

    @NotNull(message = "Class 10 score is required")
    @DecimalMin(value = "0.0", message = "Class 10 score cannot be negative")
    @DecimalMax(value = "100.0", message = "Class 10 score cannot exceed 100")
    private BigDecimal tenthScore;

    @NotBlank(message = "Class 12 board is required")
    @Size(max = 100, message = "Class 12 board must not exceed 100 characters")
    private String twelfthBoard;

    @NotBlank(message = "Class 12 school is required")
    @Size(max = 255, message = "Class 12 school must not exceed 255 characters")
    private String twelfthSchool;

    @NotNull(message = "Class 12 passing year is required")
    @Min(value = 2000, message = "Class 12 passing year must be 2000 or later")
    @Max(value = 2035, message = "Class 12 passing year must be 2035 or earlier")
    private Integer twelfthPassingYear;

    @NotNull(message = "Class 12 result type is required")
    private AcademicResultType twelfthResultType;

    @NotNull(message = "Class 12 score is required")
    @DecimalMin(value = "0.0", message = "Class 12 score cannot be negative")
    @DecimalMax(value = "100.0", message = "Class 12 score cannot exceed 100")
    private BigDecimal twelfthScore;

    @NotNull(message = "Cumulative CGPA is required")
    @DecimalMin(value = "0.0", message = "Cumulative CGPA cannot be negative")
    @DecimalMax(value = "10.0", message = "Cumulative CGPA cannot exceed 10.0")
    private BigDecimal cumulativeCgpa;

    @Min(value = 0, message = "Active backlogs cannot be negative")
    private Integer activeBacklogs = 0;

    @Min(value = 0, message = "Cleared backlogs cannot be negative")
    private Integer clearedBacklogs = 0;

    // Optional field in request payload - will always be set to UNVERIFIED for student requests
    private VerificationStatus verificationStatus;

    @Valid
    private List<SemesterRecordRequest> semesters = new ArrayList<>();

    public AcademicRecordRequest() {
    }

    public AcademicRecordRequest(String tenthBoard, String tenthSchool, Integer tenthPassingYear,
                                 AcademicResultType tenthResultType, BigDecimal tenthScore,
                                 String twelfthBoard, String twelfthSchool, Integer twelfthPassingYear,
                                 AcademicResultType twelfthResultType, BigDecimal twelfthScore,
                                 BigDecimal cumulativeCgpa, Integer activeBacklogs, Integer clearedBacklogs,
                                 List<SemesterRecordRequest> semesters) {
        this.tenthBoard = tenthBoard;
        this.tenthSchool = tenthSchool;
        this.tenthPassingYear = tenthPassingYear;
        this.tenthResultType = tenthResultType;
        this.tenthScore = tenthScore;
        this.twelfthBoard = twelfthBoard;
        this.twelfthSchool = twelfthSchool;
        this.twelfthPassingYear = twelfthPassingYear;
        this.twelfthResultType = twelfthResultType;
        this.twelfthScore = twelfthScore;
        this.cumulativeCgpa = cumulativeCgpa;
        this.activeBacklogs = activeBacklogs != null ? activeBacklogs : 0;
        this.clearedBacklogs = clearedBacklogs != null ? clearedBacklogs : 0;
        this.semesters = semesters != null ? semesters : new ArrayList<>();
    }

    public String getTenthBoard() {
        return tenthBoard;
    }

    public void setTenthBoard(String tenthBoard) {
        this.tenthBoard = tenthBoard;
    }

    public String getTenthSchool() {
        return tenthSchool;
    }

    public void setTenthSchool(String tenthSchool) {
        this.tenthSchool = tenthSchool;
    }

    public Integer getTenthPassingYear() {
        return tenthPassingYear;
    }

    public void setTenthPassingYear(Integer tenthPassingYear) {
        this.tenthPassingYear = tenthPassingYear;
    }

    public AcademicResultType getTenthResultType() {
        return tenthResultType;
    }

    public void setTenthResultType(AcademicResultType tenthResultType) {
        this.tenthResultType = tenthResultType;
    }

    public BigDecimal getTenthScore() {
        return tenthScore;
    }

    public void setTenthScore(BigDecimal tenthScore) {
        this.tenthScore = tenthScore;
    }

    public String getTwelfthBoard() {
        return twelfthBoard;
    }

    public void setTwelfthBoard(String twelfthBoard) {
        this.twelfthBoard = twelfthBoard;
    }

    public String getTwelfthSchool() {
        return twelfthSchool;
    }

    public void setTwelfthSchool(String twelfthSchool) {
        this.twelfthSchool = twelfthSchool;
    }

    public Integer getTwelfthPassingYear() {
        return twelfthPassingYear;
    }

    public void setTwelfthPassingYear(Integer twelfthPassingYear) {
        this.twelfthPassingYear = twelfthPassingYear;
    }

    public AcademicResultType getTwelfthResultType() {
        return twelfthResultType;
    }

    public void setTwelfthResultType(AcademicResultType twelfthResultType) {
        this.twelfthResultType = twelfthResultType;
    }

    public BigDecimal getTwelfthScore() {
        return twelfthScore;
    }

    public void setTwelfthScore(BigDecimal twelfthScore) {
        this.twelfthScore = twelfthScore;
    }

    public BigDecimal getCumulativeCgpa() {
        return cumulativeCgpa;
    }

    public void setCumulativeCgpa(BigDecimal cumulativeCgpa) {
        this.cumulativeCgpa = cumulativeCgpa;
    }

    public Integer getActiveBacklogs() {
        return activeBacklogs;
    }

    public void setActiveBacklogs(Integer activeBacklogs) {
        this.activeBacklogs = activeBacklogs;
    }

    public Integer getClearedBacklogs() {
        return clearedBacklogs;
    }

    public void setClearedBacklogs(Integer clearedBacklogs) {
        this.clearedBacklogs = clearedBacklogs;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public List<SemesterRecordRequest> getSemesters() {
        return semesters;
    }

    public void setSemesters(List<SemesterRecordRequest> semesters) {
        this.semesters = semesters;
    }
}
