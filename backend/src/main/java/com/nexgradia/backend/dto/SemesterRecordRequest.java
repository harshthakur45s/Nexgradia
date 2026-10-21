package com.nexgradia.backend.dto;

import com.nexgradia.backend.model.AcademicResultType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SemesterRecordRequest {

    @NotNull(message = "Semester number is required")
    @Min(value = 1, message = "Semester number must be at least 1")
    @Max(value = 10, message = "Semester number must not exceed 10")
    private Integer semesterNumber;

    @NotNull(message = "Result type is required")
    private AcademicResultType resultType;

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score cannot be negative")
    @DecimalMax(value = "100.0", message = "Score cannot exceed 100")
    private BigDecimal score;

    @Min(value = 0, message = "Backlogs count cannot be negative")
    private Integer backlogsCount = 0;

    @Min(value = 0, message = "Credits cannot be negative")
    private Integer credits;

    public SemesterRecordRequest() {
    }

    public SemesterRecordRequest(Integer semesterNumber, AcademicResultType resultType,
                                 BigDecimal score, Integer backlogsCount, Integer credits) {
        this.semesterNumber = semesterNumber;
        this.resultType = resultType;
        this.score = score;
        this.backlogsCount = backlogsCount != null ? backlogsCount : 0;
        this.credits = credits;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public AcademicResultType getResultType() {
        return resultType;
    }

    public void setResultType(AcademicResultType resultType) {
        this.resultType = resultType;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getBacklogsCount() {
        return backlogsCount;
    }

    public void setBacklogsCount(Integer backlogsCount) {
        this.backlogsCount = backlogsCount;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }
}
