package com.nexgradia.backend.dto;

import com.nexgradia.backend.model.Role;
import com.nexgradia.backend.model.User;

public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private boolean enabled;

    public UserResponse() {
    }

    public UserResponse(Long id, String email, String fullName, Role role, boolean enabled) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.enabled = enabled;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isEnabled()
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
