package com.nexgradia.backend.service;

import com.nexgradia.backend.dto.AcademicRecordRequest;
import com.nexgradia.backend.dto.AcademicRecordResponse;
import com.nexgradia.backend.dto.SemesterRecordRequest;
import com.nexgradia.backend.model.AcademicRecord;
import com.nexgradia.backend.model.AcademicResultType;
import com.nexgradia.backend.model.SemesterRecord;
import com.nexgradia.backend.model.StudentProfile;
import com.nexgradia.backend.model.VerificationStatus;
import com.nexgradia.backend.repository.AcademicRecordRepository;
import com.nexgradia.backend.repository.StudentProfileRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AcademicRecordService {

    private static final BigDecimal MAX_GPA = BigDecimal.valueOf(10.0);
    private static final BigDecimal MAX_PERCENTAGE = BigDecimal.valueOf(100.0);
    private static final BigDecimal MIN_SCORE = BigDecimal.ZERO;

    private final AcademicRecordRepository academicRecordRepository;
    private final StudentProfileRepository profileRepository;

    public AcademicRecordService(AcademicRecordRepository academicRecordRepository,
                                 StudentProfileRepository profileRepository) {
        this.academicRecordRepository = academicRecordRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public AcademicRecordResponse getAcademicRecordByUserId(Long userId) {
        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student profile has not been created yet"));

        AcademicRecord record = academicRecordRepository.findByStudentProfileId(profile.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Academic record has not been created yet"));

        return AcademicRecordResponse.fromEntity(record);
    }

    public AcademicRecordResponse upsertAcademicRecord(Long userId, AcademicRecordRequest request) {
        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Please create your student profile before adding academic records"));

        // Validate passing year chronological consistency
        if (request.getTwelfthPassingYear() < request.getTenthPassingYear()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Class 12 passing year cannot be earlier than Class 10 passing year");
        }

        // Validate cumulative CGPA range
        if (request.getCumulativeCgpa().compareTo(MIN_SCORE) < 0 || request.getCumulativeCgpa().compareTo(MAX_GPA) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cumulative CGPA must be between 0.0 and 10.0");
        }

        // Validate score boundaries according to ResultType
        validateResultTypeAndScore("Class 10", request.getTenthResultType(), request.getTenthScore());
        validateResultTypeAndScore("Class 12", request.getTwelfthResultType(), request.getTwelfthScore());

        // Validate semester entries for duplicates and score constraints
        if (request.getSemesters() != null && !request.getSemesters().isEmpty()) {
            Set<Integer> seenSemesters = new HashSet<>();
            for (SemesterRecordRequest semReq : request.getSemesters()) {
                if (!seenSemesters.add(semReq.getSemesterNumber())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Duplicate semester entry for semester " + semReq.getSemesterNumber());
                }
                validateResultTypeAndScore("Semester " + semReq.getSemesterNumber(),
                        semReq.getResultType(), semReq.getScore());

                if (semReq.getBacklogsCount() != null && semReq.getBacklogsCount() < 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Semester " + semReq.getSemesterNumber() + " backlogs count cannot be negative");
                }
                if (semReq.getCredits() != null && semReq.getCredits() < 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Semester " + semReq.getSemesterNumber() + " credits cannot be negative");
                }
            }
        }

        AcademicRecord record = academicRecordRepository.findByStudentProfileId(profile.getId())
                .orElseGet(() -> {
                    AcademicRecord newRecord = new AcademicRecord();
                    newRecord.setStudentProfile(profile);
                    return newRecord;
                });

        record.setTenthBoard(request.getTenthBoard().trim());
        record.setTenthSchool(request.getTenthSchool().trim());
        record.setTenthPassingYear(request.getTenthPassingYear());
        record.setTenthResultType(request.getTenthResultType());
        record.setTenthScore(request.getTenthScore());

        record.setTwelfthBoard(request.getTwelfthBoard().trim());
        record.setTwelfthSchool(request.getTwelfthSchool().trim());
        record.setTwelfthPassingYear(request.getTwelfthPassingYear());
        record.setTwelfthResultType(request.getTwelfthResultType());
        record.setTwelfthScore(request.getTwelfthScore());

        record.setCumulativeCgpa(request.getCumulativeCgpa());
        record.setActiveBacklogs(request.getActiveBacklogs() != null ? request.getActiveBacklogs() : 0);
        record.setClearedBacklogs(request.getClearedBacklogs() != null ? request.getClearedBacklogs() : 0);

        // Security rule: Student modifications always force UNVERIFIED status. Self-verification is impossible.
        record.setVerificationStatus(VerificationStatus.UNVERIFIED);
        record.setVerifiedAt(null);

        // Update semester records safely: merge/upsert by semester number to avoid accidental deletion
        if (request.getSemesters() != null && !request.getSemesters().isEmpty()) {
            Map<Integer, SemesterRecord> existingBySemester = record.getSemesters().stream()
                    .collect(Collectors.toMap(SemesterRecord::getSemesterNumber, s -> s));

            for (SemesterRecordRequest semReq : request.getSemesters()) {
                SemesterRecord existing = existingBySemester.get(semReq.getSemesterNumber());
                if (existing != null) {
                    existing.setResultType(semReq.getResultType());
                    existing.setScore(semReq.getScore());
                    existing.setBacklogsCount(semReq.getBacklogsCount() != null ? semReq.getBacklogsCount() : 0);
                    existing.setCredits(semReq.getCredits());
                } else {
                    SemesterRecord newSem = new SemesterRecord(
                            record,
                            semReq.getSemesterNumber(),
                            semReq.getResultType(),
                            semReq.getScore(),
                            semReq.getBacklogsCount(),
                            semReq.getCredits()
                    );
                    record.addSemesterRecord(newSem);
                }
            }
        }

        AcademicRecord saved = academicRecordRepository.save(record);
        return AcademicRecordResponse.fromEntity(saved);
    }

    private void validateResultTypeAndScore(String fieldLabel, AcademicResultType resultType, BigDecimal score) {
        if (score == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldLabel + " score cannot be null");
        }
        if (score.compareTo(MIN_SCORE) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldLabel + " score cannot be negative");
        }

        if (resultType == AcademicResultType.CGPA || resultType == AcademicResultType.SGPA) {
            if (score.compareTo(MAX_GPA) > 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        fieldLabel + " score cannot exceed 10.0 when result type is " + resultType);
            }
        } else if (resultType == AcademicResultType.PERCENTAGE) {
            if (score.compareTo(MAX_PERCENTAGE) > 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        fieldLabel + " percentage cannot exceed 100.0");
            }
        }
    }
}
