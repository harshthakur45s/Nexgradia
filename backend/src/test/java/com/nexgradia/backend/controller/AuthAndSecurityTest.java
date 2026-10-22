package com.nexgradia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexgradia.backend.dto.LoginRequest;
import com.nexgradia.backend.dto.RegisterRequest;
import com.nexgradia.backend.model.Role;
import com.nexgradia.backend.model.User;
import com.nexgradia.backend.repository.AcademicRecordRepository;
import com.nexgradia.backend.repository.ResumeRepository;
import com.nexgradia.backend.repository.StudentProfileRepository;
import com.nexgradia.backend.repository.UserRepository;
import com.nexgradia.backend.security.JwtTokenProvider;
import com.nexgradia.backend.security.UserPrincipal;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository profileRepository;

    @Autowired
    private AcademicRecordRepository academicRecordRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        resumeRepository.deleteAll();
        academicRecordRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Public health endpoints remain accessible without authentication")
    void publicHealthEndpointRemainsAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Registration succeeds with valid input and hashes password in database")
    void registrationSucceedsWithValidInput() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "student1@campus.edu",
                "StrongPassword123!",
                "Alice Smith",
                Role.STUDENT
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("student1@campus.edu"))
                .andExpect(jsonPath("$.fullName").value("Alice Smith"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        // Verify database storage
        User savedUser = userRepository.findByEmailIgnoreCase("student1@campus.edu").orElseThrow();
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("StrongPassword123!");
        assertThat(savedUser.getPasswordHash()).startsWith("$2a$"); // BCrypt signature
    }

    @Test
    @DisplayName("Invalid registration inputs are rejected with BAD_REQUEST")
    void invalidRegistrationInputIsRejected() throws Exception {
        // Invalid email, short password, empty name, null role
        RegisterRequest badRequest = new RegisterRequest(
                "not-an-email",
                "short",
                "",
                null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("Duplicate account emails are rejected with CONFLICT")
    void duplicateAccountEmailIsRejected() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "duplicate@campus.edu",
                "Password123!",
                "John Doe",
                Role.STUDENT
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second attempt with same email (case-insensitive)
        RegisterRequest duplicateRequest = new RegisterRequest(
                "DUPLICATE@campus.edu",
                "DifferentPassword123!",
                "Jane Doe",
                Role.RECRUITER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("Public registration cannot create PLACEMENT_ADMIN")
    void publicRegistrationCannotCreatePlacementAdmin() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "admin_wannabe@campus.edu",
                "Password123!",
                "Sneaky Admin",
                Role.PLACEMENT_ADMIN
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Public registration cannot grant privileged role: PLACEMENT_ADMIN"));
    }

    @Test
    @DisplayName("Login succeeds with valid credentials and fails with invalid credentials")
    void loginSucceedsWithValidCredentialsAndFailsWithInvalid() throws Exception {
        RegisterRequest register = new RegisterRequest(
                "login_test@campus.edu",
                "ValidPassword123!",
                "Bob Jones",
                Role.RECRUITER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // Valid login
        LoginRequest validLogin = new LoginRequest("login_test@campus.edu", "ValidPassword123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("login_test@campus.edu"))
                .andExpect(jsonPath("$.role").value("RECRUITER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        // Invalid password
        LoginRequest badPassword = new LoginRequest("login_test@campus.edu", "WrongPassword!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badPassword)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        // Non-existent user
        LoginRequest nonExistent = new LoginRequest("ghost@campus.edu", "ValidPassword123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistent)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Unauthenticated requests to protected endpoints return 401 Unauthorized")
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/recruiter/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/student/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Role-based access rules enforce permissions correctly")
    void roleBasedAccessRulesEnforcedCorrectly() throws Exception {
        // Register Student
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("student_role@campus.edu", "Password123!", "Student User", Role.STUDENT))))
                .andExpect(status().isCreated());

        // Register Recruiter
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("recruiter_role@campus.edu", "Password123!", "Recruiter User", Role.RECRUITER))))
                .andExpect(status().isCreated());

        // Login as Student
        String studentToken = authenticateAndGetToken("student_role@campus.edu", "Password123!");
        // Login as Recruiter
        String recruiterToken = authenticateAndGetToken("recruiter_role@campus.edu", "Password123!");

        // Student can access student dashboard
        mockMvc.perform(get("/api/v1/student/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        // Student CANNOT access recruiter dashboard (403 Forbidden)
        mockMvc.perform(get("/api/v1/recruiter/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // Student CANNOT access admin dashboard (403 Forbidden)
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // Recruiter can access recruiter dashboard
        mockMvc.perform(get("/api/v1/recruiter/dashboard")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk());

        // Recruiter CANNOT access student dashboard
        mockMvc.perform(get("/api/v1/student/dashboard")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Users cannot view other user's account data through manipulated identifiers")
    void preventAccountAccessThroughManipulatedIdentifiers() throws Exception {
        // Create user 1
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("user1@campus.edu", "Password123!", "User One", Role.STUDENT))))
                .andExpect(status().isCreated());

        // Create user 2
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("user2@campus.edu", "Password123!", "User Two", Role.STUDENT))))
                .andExpect(status().isCreated());

        User user1 = userRepository.findByEmailIgnoreCase("user1@campus.edu").orElseThrow();
        User user2 = userRepository.findByEmailIgnoreCase("user2@campus.edu").orElseThrow();

        String user1Token = authenticateAndGetToken("user1@campus.edu", "Password123!");

        // User 1 accesses own profile by ID -> 200 OK
        mockMvc.perform(get("/api/v1/users/" + user1.getId())
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user1@campus.edu"));

        // User 1 attempts to access user 2's profile by manipulated ID -> 403 Forbidden
        mockMvc.perform(get("/api/v1/users/" + user2.getId())
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Expired JWT token is rejected with 401 Unauthorized")
    void expiredTokenIsRejected() throws Exception {
        RegisterRequest register = new RegisterRequest(
                "expired_user@campus.edu",
                "Password123!",
                "Expired User",
                Role.STUDENT
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        User user = userRepository.findByEmailIgnoreCase("expired_user@campus.edu").orElseThrow();
        UserPrincipal principal = UserPrincipal.create(user);

        // Generate an expired token (-10 seconds)
        String expiredToken = jwtTokenProvider.generateTokenWithCustomExpiration(principal, -10000L);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Malformed JWT token is rejected with 401 Unauthorized")
    void malformedTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer not.a.valid.jwt.payload"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("JWT token signed with different secret is rejected with 401 Unauthorized")
    void tokenWithInvalidSignatureIsRejected() throws Exception {
        SecretKey invalidKey = Keys.hmacShaKeyFor(
                "an_entirely_different_secret_key_that_is_at_least_32_bytes_long_1234".getBytes(StandardCharsets.UTF_8));
        String tamperedToken = io.jsonwebtoken.Jwts.builder()
                .subject("tampered@campus.edu")
                .claim("id", 9999L)
                .claim("roles", List.of("ROLE_STUDENT"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(invalidKey)
                .compact();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Placement Admin can view any user's profile by ID")
    void adminCanAccessAnyUserById() throws Exception {
        // Register student
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("student_target@campus.edu", "Password123!", "Student Target", Role.STUDENT))))
                .andExpect(status().isCreated());

        User student = userRepository.findByEmailIgnoreCase("student_target@campus.edu").orElseThrow();

        // Create PLACEMENT_ADMIN directly in repository (internal/staff provisioning)
        User adminUser = new User(
                "admin_super@campus.edu",
                passwordEncoder.encode("AdminPassword123!"),
                "Placement Officer",
                Role.PLACEMENT_ADMIN
        );
        userRepository.save(adminUser);

        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);
        String adminToken = jwtTokenProvider.generateToken(adminPrincipal);

        // Admin retrieves student's account by ID -> 200 OK
        mockMvc.perform(get("/api/v1/users/" + student.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student_target@campus.edu"))
                .andExpect(jsonPath("$.fullName").value("Student Target"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("Disabled user account cannot authenticate and existing tokens are rejected")
    void disabledUserCannotAuthenticateOrAccess() throws Exception {
        RegisterRequest register = new RegisterRequest(
                "disabled@campus.edu",
                "Password123!",
                "Disabled User",
                Role.STUDENT
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        User user = userRepository.findByEmailIgnoreCase("disabled@campus.edu").orElseThrow();
        String validTokenBeforeDisable = jwtTokenProvider.generateToken(UserPrincipal.create(user));

        // Disable user
        user.setEnabled(false);
        userRepository.save(user);

        // 1. Attempt login with valid credentials -> Rejected with 401
        LoginRequest loginRequest = new LoginRequest("disabled@campus.edu", "Password123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        // 2. Attempt protected endpoint with token created prior to disablement -> Rejected with 401
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + validTokenBeforeDisable))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("JWT secret with less than 32 bytes throws IllegalArgumentException")
    void weakJwtSecretIsRejected() {
        assertThatThrownBy(() ->
                new JwtTokenProvider("short_secret", 3600000, null)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("at least 32 bytes");
    }

    @Test
    @DisplayName("Production profile with default secret throws IllegalStateException")
    void productionProfileWithDefaultSecretIsRejected() {
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setActiveProfiles("production");

        assertThatThrownBy(() ->
                new JwtTokenProvider(JwtTokenProvider.DEFAULT_DEV_SECRET, 3600000, mockEnv)
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("Production environment must not use the default development JWT secret");
    }

    private String authenticateAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("token").asText();
    }
}
