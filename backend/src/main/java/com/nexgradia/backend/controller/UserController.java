package com.nexgradia.backend.controller;

import com.nexgradia.backend.dto.UserResponse;
import com.nexgradia.backend.model.User;
import com.nexgradia.backend.repository.UserRepository;
import com.nexgradia.backend.security.UserPrincipal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticated user retrieves their own profile.
     */
    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * User retrieves an account by ID:
     * - Admin can view any user's profile.
     * - Student/Recruiter can only view their own profile. Manipulated ID is rejected.
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('PLACEMENT_ADMIN') or #id == principal.id")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * Admin-only endpoint.
     */
    @GetMapping("/admin/dashboard")
    public ResponseEntity<Map<String, String>> adminDashboard() {
        return ResponseEntity.ok(Map.of("message", "Welcome to the Placement Admin dashboard"));
    }

    /**
     * Recruiter-only endpoint.
     */
    @GetMapping("/recruiter/dashboard")
    public ResponseEntity<Map<String, String>> recruiterDashboard() {
        return ResponseEntity.ok(Map.of("message", "Welcome to the Recruiter dashboard"));
    }

    /**
     * Student-only endpoint.
     */
    @GetMapping("/student/dashboard")
    public ResponseEntity<Map<String, String>> studentDashboard() {
        return ResponseEntity.ok(Map.of("message", "Welcome to the Student dashboard"));
    }
}
