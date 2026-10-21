package com.nexgradia.backend.dto;

import com.nexgradia.backend.model.AcademicRecord;
import com.nexgradia.backend.model.AcademicResultType;
import com.nexgradia.backend.model.VerificationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class AcademicRecordResponse {

    private Long id;
    private Long studentProfileId;
    private String tenthBoard;
    private String tenthSchool;
    private Integer tenthPassingYear;
    private AcademicResultType tenthResultType;
    private BigDecimal tenthScore;
    private String twelfthBoard;
    private String twelfthSchool;
    private Integer twelfthPassingYear;
    private AcademicResultType twelfthResultType;
    private BigDecimal twelfthScore;
    private BigDecimal cumulativeCgpa;
    private Integer activeBacklogs;
    private Integer clearedBacklogs;
    private VerificationStatus verificationStatus;
    private Instant verifiedAt;
    private List<SemesterRecordResponse> semesters;
    private Instant createdAt;
    private Instant updatedAt;

    public AcademicRecordResponse() {
    }

    public AcademicRecordResponse(Long id, Long studentProfileId, String tenthBoard, String tenthSchool,
                                  Integer tenthPassingYear, AcademicResultType tenthResultType, BigDecimal tenthScore,
                                  String twelfthBoard, String twelfthSchool, Integer twelfthPassingYear,
                                  AcademicResultType twelfthResultType, BigDecimal twelfthScore,
                                  BigDecimal cumulativeCgpa, Integer activeBacklogs, Integer clearedBacklogs,
                                  VerificationStatus verificationStatus, Instant verifiedAt,
                                  List<SemesterRecordResponse> semesters, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.studentProfileId = studentProfileId;
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
        this.activeBacklogs = activeBacklogs;
        this.clearedBacklogs = clearedBacklogs;
        this.verificationStatus = verificationStatus;
        this.verifiedAt = verifiedAt;
        this.semesters = semesters;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AcademicRecordResponse fromEntity(AcademicRecord entity) {
        List<SemesterRecordResponse> semesterResponses = entity.getSemesters() != null
                ? entity.getSemesters().stream().map(SemesterRecordResponse::fromEntity).toList()
                : List.of();

        return new AcademicRecordResponse(
                entity.getId(),
                entity.getStudentProfile().getId(),
                entity.getTenthBoard(),
                entity.getTenthSchool(),
                entity.getTenthPassingYear(),
                entity.getTenthResultType(),
                entity.getTenthScore(),
                entity.getTwelfthBoard(),
                entity.getTwelfthSchool(),
                entity.getTwelfthPassingYear(),
                entity.getTwelfthResultType(),
                entity.getTwelfthScore(),
                entity.getCumulativeCgpa(),
                entity.getActiveBacklogs(),
                entity.getClearedBacklogs(),
                entity.getVerificationStatus(),
                entity.getVerifiedAt(),
                semesterResponses,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getStudentProfileId() {
        return studentProfileId;
    }

    public String getTenthBoard() {
        return tenthBoard;
    }

    public String getTenthSchool() {
        return tenthSchool;
    }

    public Integer getTenthPassingYear() {
        return tenthPassingYear;
    }

    public AcademicResultType getTenthResultType() {
        return tenthResultType;
    }

    public BigDecimal getTenthScore() {
        return tenthScore;
    }

    public String getTwelfthBoard() {
        return twelfthBoard;
    }

    public String getTwelfthSchool() {
        return twelfthSchool;
    }

    public Integer getTwelfthPassingYear() {
        return twelfthPassingYear;
    }

    public AcademicResultType getTwelfthResultType() {
        return twelfthResultType;
    }

    public BigDecimal getTwelfthScore() {
        return twelfthScore;
    }

    public BigDecimal getCumulativeCgpa() {
        return cumulativeCgpa;
    }

    public Integer getActiveBacklogs() {
        return activeBacklogs;
    }

    public Integer getClearedBacklogs() {
        return clearedBacklogs;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public List<SemesterRecordResponse> getSemesters() {
        return semesters;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
