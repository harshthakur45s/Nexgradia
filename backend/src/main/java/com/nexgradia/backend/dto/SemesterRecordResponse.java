package com.nexgradia.backend.dto;

import com.nexgradia.backend.model.AcademicResultType;
import com.nexgradia.backend.model.SemesterRecord;
import java.math.BigDecimal;

public class SemesterRecordResponse {

    private Long id;
    private Integer semesterNumber;
    private AcademicResultType resultType;
    private BigDecimal score;
    private Integer backlogsCount;
    private Integer credits;

    public SemesterRecordResponse() {
    }

    public SemesterRecordResponse(Long id, Integer semesterNumber, AcademicResultType resultType,
                                  BigDecimal score, Integer backlogsCount, Integer credits) {
        this.id = id;
        this.semesterNumber = semesterNumber;
        this.resultType = resultType;
        this.score = score;
        this.backlogsCount = backlogsCount;
        this.credits = credits;
    }

    public static SemesterRecordResponse fromEntity(SemesterRecord entity) {
        return new SemesterRecordResponse(
                entity.getId(),
                entity.getSemesterNumber(),
                entity.getResultType(),
                entity.getScore(),
                entity.getBacklogsCount(),
                entity.getCredits()
        );
    }

    public Long getId() {
        return id;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public AcademicResultType getResultType() {
        return resultType;
    }

    public BigDecimal getScore() {
        return score;
    }

    public Integer getBacklogsCount() {
        return backlogsCount;
    }

    public Integer getCredits() {
        return credits;
    }
}
