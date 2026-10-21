package com.nexgradia.backend.repository;

import com.nexgradia.backend.model.AcademicRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> {

    Optional<AcademicRecord> findByStudentProfileId(Long studentProfileId);

    Optional<AcademicRecord> findByStudentProfileUserId(Long userId);
}
