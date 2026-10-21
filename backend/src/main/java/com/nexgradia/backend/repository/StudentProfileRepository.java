package com.nexgradia.backend.repository;

import com.nexgradia.backend.model.StudentProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
