package com.nexgradia.backend.service;

import com.nexgradia.backend.dto.StudentProfileRequest;
import com.nexgradia.backend.dto.StudentProfileResponse;
import com.nexgradia.backend.model.Role;
import com.nexgradia.backend.model.StudentProfile;
import com.nexgradia.backend.model.User;
import com.nexgradia.backend.repository.StudentProfileRepository;
import com.nexgradia.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class StudentProfileService {

    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;

    public StudentProfileService(StudentProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfileByUserId(Long userId) {
        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student profile has not been created yet"));
        return StudentProfileResponse.fromEntity(profile);
    }

    public StudentProfileResponse upsertProfile(Long userId, StudentProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can manage student profiles");
        }

        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    StudentProfile newProfile = new StudentProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        profile.setInstitution(request.getInstitution().trim());
        profile.setProgram(request.getProgram().trim());
        profile.setBranch(request.getBranch().trim());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setCurrentSemester(request.getCurrentSemester());
        profile.setProfileCompleted(true);

        StudentProfile saved = profileRepository.save(profile);
        return StudentProfileResponse.fromEntity(saved);
    }
}
