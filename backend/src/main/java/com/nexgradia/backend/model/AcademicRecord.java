package com.nexgradia.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "academic_records")
public class AcademicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_profile_id", nullable = false, unique = true)
    private StudentProfile studentProfile;

    @Column(name = "tenth_board", nullable = false, length = 100)
    private String tenthBoard;

    @Column(name = "tenth_school", nullable = false)
    private String tenthSchool;

    @Column(name = "tenth_passing_year", nullable = false)
    private Integer tenthPassingYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenth_result_type", nullable = false, length = 20)
    private AcademicResultType tenthResultType;

    @Column(name = "tenth_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal tenthScore;

    @Column(name = "twelfth_board", nullable = false, length = 100)
    private String twelfthBoard;

    @Column(name = "twelfth_school", nullable = false)
    private String twelfthSchool;

    @Column(name = "twelfth_passing_year", nullable = false)
    private Integer twelfthPassingYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "twelfth_result_type", nullable = false, length = 20)
    private AcademicResultType twelfthResultType;

    @Column(name = "twelfth_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal twelfthScore;

    @Column(name = "cumulative_cgpa", nullable = false, precision = 5, scale = 2)
    private BigDecimal cumulativeCgpa;

    @Column(name = "active_backlogs", nullable = false)
    private Integer activeBacklogs = 0;

    @Column(name = "cleared_backlogs", nullable = false)
    private Integer clearedBacklogs = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @OneToMany(mappedBy = "academicRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("semesterNumber ASC")
    private List<SemesterRecord> semesters = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AcademicRecord() {
    }

    public AcademicRecord(StudentProfile studentProfile, String tenthBoard, String tenthSchool,
                          Integer tenthPassingYear, AcademicResultType tenthResultType, BigDecimal tenthScore,
                          String twelfthBoard, String twelfthSchool, Integer twelfthPassingYear,
                          AcademicResultType twelfthResultType, BigDecimal twelfthScore,
                          BigDecimal cumulativeCgpa, Integer activeBacklogs, Integer clearedBacklogs) {
        this.studentProfile = studentProfile;
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
        this.verificationStatus = VerificationStatus.UNVERIFIED;
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

    public void addSemesterRecord(SemesterRecord semesterRecord) {
        semesters.add(semesterRecord);
        semesterRecord.setAcademicRecord(this);
    }

    public void clearSemesterRecords() {
        semesters.clear();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentProfile getStudentProfile() {
        return studentProfile;
    }

    public void setStudentProfile(StudentProfile studentProfile) {
        this.studentProfile = studentProfile;
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

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public List<SemesterRecord> getSemesters() {
        return semesters;
    }

    public void setSemesters(List<SemesterRecord> semesters) {
        this.semesters = semesters;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
