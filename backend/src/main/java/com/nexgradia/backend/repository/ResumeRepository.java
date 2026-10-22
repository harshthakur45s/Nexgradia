package com.nexgradia.backend.repository;

import com.nexgradia.backend.model.Resume;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByStudentProfileIdOrderByVersionNumberDesc(Long studentProfileId);

    Optional<Resume> findByStudentProfileIdAndActiveTrue(Long studentProfileId);

    Optional<Resume> findByIdAndStudentProfileId(Long id, Long studentProfileId);

    @Query("SELECT COALESCE(MAX(r.versionNumber), 0) FROM Resume r WHERE r.studentProfile.id = :profileId")
    Integer findMaxVersionNumberByStudentProfileId(@Param("profileId") Long profileId);

    @Modifying
    @Query("UPDATE Resume r SET r.active = false WHERE r.studentProfile.id = :profileId AND r.active = true")
    void deactivateCurrentActiveByStudentProfileId(@Param("profileId") Long profileId);
}
