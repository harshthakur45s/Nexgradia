package com.nexgradia.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "semester_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_semester_records_academic_semester",
                        columnNames = {"academic_record_id", "semester_number"}
                )
        }
)
public class SemesterRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_record_id", nullable = false)
    private AcademicRecord academicRecord;

    @Column(name = "semester_number", nullable = false)
    private Integer semesterNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false, length = 20)
    private AcademicResultType resultType;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "backlogs_count", nullable = false)
    private Integer backlogsCount = 0;

    @Column
    private Integer credits;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SemesterRecord() {
    }

    public SemesterRecord(AcademicRecord academicRecord, Integer semesterNumber,
                          AcademicResultType resultType, BigDecimal score,
                          Integer backlogsCount, Integer credits) {
        this.academicRecord = academicRecord;
        this.semesterNumber = semesterNumber;
        this.resultType = resultType;
        this.score = score;
        this.backlogsCount = backlogsCount != null ? backlogsCount : 0;
        this.credits = credits;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AcademicRecord getAcademicRecord() {
        return academicRecord;
    }

    public void setAcademicRecord(AcademicRecord academicRecord) {
        this.academicRecord = academicRecord;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
