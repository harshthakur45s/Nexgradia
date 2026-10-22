package com.nexgradia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexgradia.backend.dto.AcademicRecordRequest;
import com.nexgradia.backend.dto.LoginRequest;
import com.nexgradia.backend.dto.RegisterRequest;
import com.nexgradia.backend.dto.SemesterRecordRequest;
import com.nexgradia.backend.dto.StudentProfileRequest;
import com.nexgradia.backend.model.AcademicResultType;
import com.nexgradia.backend.model.Role;
import com.nexgradia.backend.model.User;
import com.nexgradia.backend.model.VerificationStatus;
import com.nexgradia.backend.repository.AcademicRecordRepository;
import com.nexgradia.backend.repository.ResumeRepository;
import com.nexgradia.backend.repository.StudentProfileRepository;
import com.nexgradia.backend.repository.UserRepository;
import com.nexgradia.backend.security.JwtTokenProvider;
import com.nexgradia.backend.security.UserPrincipal;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudentProfileAndAcademicTest {

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
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        resumeRepository.deleteAll();
        academicRecordRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/v1/students/me returns 404 when profile has not been created yet")
    void getMyProfileReturns404WhenNotCreated() throws Exception {
        String token = registerAndGetStudentToken("new_student@campus.edu", "Password123!", "New Student");

        mockMvc.perform(get("/api/v1/students/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Student profile has not been created yet"));
    }

    @Test
    @DisplayName("PUT /api/v1/students/me creates and subsequently updates student profile")
    void createAndUpdateStudentProfileSucceeds() throws Exception {
        String token = registerAndGetStudentToken("alice@campus.edu", "Password123!", "Alice Wonder");

        StudentProfileRequest request = new StudentProfileRequest(
                "+919876543210",
                "National Institute of Technology",
                "B.Tech",
                "Computer Science and Engineering",
                2026,
                6
        );

        // 1. Create Profile
        mockMvc.perform(put("/api/v1/students/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institution").value("National Institute of Technology"))
                .andExpect(jsonPath("$.program").value("B.Tech"))
                .andExpect(jsonPath("$.branch").value("Computer Science and Engineering"))
                .andExpect(jsonPath("$.graduationYear").value(2026))
                .andExpect(jsonPath("$.currentSemester").value(6))
                .andExpect(jsonPath("$.phoneNumber").value("+919876543210"))
                .andExpect(jsonPath("$.profileCompleted").value(true))
                .andExpect(jsonPath("$.email").value("alice@campus.edu"))
                .andExpect(jsonPath("$.fullName").value("Alice Wonder"));

        // 2. Retrieve Profile via GET
        mockMvc.perform(get("/api/v1/students/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institution").value("National Institute of Technology"))
                .andExpect(jsonPath("$.graduationYear").value(2026));

        // 3. Update existing profile (e.g. advanced to semester 7)
        request.setCurrentSemester(7);
        mockMvc.perform(put("/api/v1/students/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSemester").value(7));
    }

    @Test
    @DisplayName("Invalid profile fields are rejected with 400 Bad Request")
    void invalidProfileInputsAreRejected() throws Exception {
        String token = registerAndGetStudentToken("bad_input@campus.edu", "Password123!", "Bad Input");

        // Invalid phone number format, blank program, year out of bounds, semester out of bounds
        StudentProfileRequest badRequest = new StudentProfileRequest(
                "invalid-phone",
                "",
                "",
                "",
                1980,
                15
        );

        mockMvc.perform(put("/api/v1/students/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/students/me/academics returns 404 when academics have not been added yet")
    void getMyAcademicsReturns404WhenNotCreated() throws Exception {
        String token = registerAndGetStudentToken("no_academics@campus.edu", "Password123!", "No Academics");

        // Profile created first
        createProfileForStudent(token);

        mockMvc.perform(get("/api/v1/students/me/academics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Academic record has not been created yet"));
    }

    @Test
    @DisplayName("PUT /api/v1/students/me/academics succeeds and forces UNVERIFIED status")
    void createAcademicRecordSucceedsAndForcesUnverified() throws Exception {
        String token = registerAndGetStudentToken("scholar@campus.edu", "Password123!", "Scholar Student");
        createProfileForStudent(token);

        List<SemesterRecordRequest> semesters = List.of(
                new SemesterRecordRequest(1, AcademicResultType.SGPA, BigDecimal.valueOf(8.50), 0, 22),
                new SemesterRecordRequest(2, AcademicResultType.SGPA, BigDecimal.valueOf(8.75), 0, 24),
                new SemesterRecordRequest(3, AcademicResultType.SGPA, BigDecimal.valueOf(9.10), 0, 24)
        );

        AcademicRecordRequest request = new AcademicRecordRequest(
                "CBSE",
                "Delhi Public School",
                2020,
                AcademicResultType.PERCENTAGE,
                BigDecimal.valueOf(94.20),
                "CBSE",
                "Delhi Public School",
                2022,
                AcademicResultType.PERCENTAGE,
                BigDecimal.valueOf(91.80),
                BigDecimal.valueOf(8.78),
                0,
                0,
                semesters
        );
        // Malicious attempt: student tries to send VERIFIED
        request.setVerificationStatus(VerificationStatus.VERIFIED);

        mockMvc.perform(put("/api/v1/students/me/academics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenthBoard").value("CBSE"))
                .andExpect(jsonPath("$.tenthScore").value(94.20))
                .andExpect(jsonPath("$.twelfthBoard").value("CBSE"))
                .andExpect(jsonPath("$.twelfthScore").value(91.80))
                .andExpect(jsonPath("$.cumulativeCgpa").value(8.78))
                .andExpect(jsonPath("$.verificationStatus").value("UNVERIFIED"))
                .andExpect(jsonPath("$.verifiedAt").doesNotExist())
                .andExpect(jsonPath("$.semesters").isArray())
                .andExpect(jsonPath("$.semesters.length()").value(3))
                .andExpect(jsonPath("$.semesters[0].semesterNumber").value(1))
                .andExpect(jsonPath("$.semesters[0].score").value(8.50));

        // GET confirms UNVERIFIED persistence
        mockMvc.perform(get("/api/v1/students/me/academics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("UNVERIFIED"));
    }

    @Test
    @DisplayName("Duplicate semester numbers in academic record payload are rejected with 400 Bad Request")
    void duplicateSemesterEntriesAreRejected() throws Exception {
        String token = registerAndGetStudentToken("dupe_sem@campus.edu", "Password123!", "Dupe Sem");
        createProfileForStudent(token);

        List<SemesterRecordRequest> duplicateSemesters = List.of(
                new SemesterRecordRequest(1, AcademicResultType.SGPA, BigDecimal.valueOf(8.50), 0, 22),
                new SemesterRecordRequest(1, AcademicResultType.SGPA, BigDecimal.valueOf(9.00), 0, 22) // Duplicate sem 1
        );

        AcademicRecordRequest request = new AcademicRecordRequest(
                "CBSE", "DPS", 2020, AcademicResultType.PERCENTAGE, BigDecimal.valueOf(90.0),
                "CBSE", "DPS", 2022, AcademicResultType.PERCENTAGE, BigDecimal.valueOf(90.0),
                BigDecimal.valueOf(8.5), 0, 0, duplicateSemesters
        );

        mockMvc.perform(put("/api/v1/students/me/academics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate semester entry for semester 1"));
    }

    @Test
    @DisplayName("CGPA or SGPA scores exceeding 10.0 are rejected with 400 Bad Request")
    void cgpaExceeding10IsRejected() throws Exception {
        String token = registerAndGetStudentToken("cgpa_exceed@campus.edu", "Password123!", "CGPA Exceed");
        createProfileForStudent(token);

        // Tenth result type CGPA with score 12.0
        AcademicRecordRequest request = new AcademicRecordRequest(
                "CBSE", "DPS", 2020, AcademicResultType.CGPA, BigDecimal.valueOf(12.0),
                "CBSE", "DPS", 2022, AcademicResultType.PERCENTAGE, BigDecimal.valueOf(90.0),
                BigDecimal.valueOf(8.5), 0, 0, List.of()
        );

        mockMvc.perform(put("/api/v1/students/me/academics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Class 10 score cannot exceed 10.0 when result type is CGPA"));
    }

    @Test
    @DisplayName("Class 12 passing year earlier than Class 10 is rejected with 400 Bad Request")
    void class12PassingYearEarlierThanClass10IsRejected() throws Exception {
        String token = registerAndGetStudentToken("chronological@campus.edu", "Password123!", "Chrono Test");
        createProfileForStudent(token);

        // Tenth passing year 2022, Twelfth passing year 2020 -> chronologically invalid
        AcademicRecordRequest request = new AcademicRecordRequest(
                "CBSE", "DPS", 2022, AcademicResultType.PERCENTAGE, BigDecimal.valueOf(90.0),
                "CBSE", "DPS", 2020, AcademicResultType.PERCENTAGE, BigDecimal.valueOf(90.0),
                BigDecimal.valueOf(8.5), 0, 0, List.of()
        );

        mockMvc.perform(put("/api/v1/students/me/academics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Class 12 passing year cannot be earlier than Class 10 passing year"));
    }

    @Test
    @DisplayName("Academic updates merge semester records without accidentally deleting existing records")
    void academicUpdateDoesNotDeleteExistingSemesterRecords() throws Exception {
        String token = registerAndGetStudentToken("merge_sem@campus.edu", "Password123!", "Merge Sem");
        createProfileForStudent(token);

        // Initial submission with Semesters 1 and 2
        List<SemesterRecordRequest> initialSemesters = List.of(
                new SemesterRecordRequest(1, AcademicResultType.SGPA, BigDecimal.valueOf(8.00), 0, 20),
                new SemesterRecordRequest(2, AcademicResultType.SGPA, BigDecimal.valueOf(8.50), 0, 22)
        );

        AcademicRecordRequest initialRequest = new AcademicRecordRequest(
                "CBSE", "DPS", 2020, AcademicResultType.PERCENTAGE, BigDecimal.valueOf(90.0),
                "CBSE", "DPS", 2022, AcademicResultType.PERCENTAGE, BigDecimal.valueOf(90.0),
                BigDecimal.valueOf(8.25), 0, 0, initialSemesters
        );

        mockMvc.perform(put("/api/v1/students/me/academics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesters.length()").value(2));

        // Subsequent submission with Semester 3 only
        List<SemesterRecordRequest> newSemesters = List.of(
                new SemesterRecordRequest(3, AcademicResultType.SGPA, BigDecimal.valueOf(9.00), 0, 24)
        );

        AcademicRecordRequest updateRequest = new AcademicRecordRequest(
                "CBSE", "DPS", 2020, AcademicResultType.PERCENTAGE, BigDecimal.valueOf(90.0),
                "CBSE", "DPS", 2022, AcademicResultType.PERCENTAGE, BigDecimal.valueOf(90.0),
                BigDecimal.valueOf(8.50), 0, 0, newSemesters
        );

        mockMvc.perform(put("/api/v1/students/me/academics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesters.length()").value(3))
                .andExpect(jsonPath("$.semesters[0].semesterNumber").value(1))
                .andExpect(jsonPath("$.semesters[1].semesterNumber").value(2))
                .andExpect(jsonPath("$.semesters[2].semesterNumber").value(3));
    }

    @Test
    @DisplayName("Unauthenticated requests to student endpoints return 401 Unauthorized")
    void unauthenticatedStudentRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/students/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/students/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/students/me/academics"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/students/me/academics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Recruiters and Placement Admins cannot access student profile or academic endpoints")
    void nonStudentRolesAreForbiddenFromStudentEndpoints() throws Exception {
        // Register Recruiter
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("recruiter_probe@campus.edu", "Password123!", "Recruiter Probe", Role.RECRUITER))))
                .andExpect(status().isCreated());

        String recruiterToken = authenticateAndGetToken("recruiter_probe@campus.edu", "Password123!");

        // Recruiter access -> 403 Forbidden
        mockMvc.perform(get("/api/v1/students/me")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/students/me/academics")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());

        // Placement Admin access -> 403 Forbidden
        User adminUser = new User("admin_probe@campus.edu", "hash", "Admin Probe", Role.PLACEMENT_ADMIN);
        userRepository.save(adminUser);
        String adminToken = jwtTokenProvider.generateToken(UserPrincipal.create(adminUser));

        mockMvc.perform(get("/api/v1/students/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/students/me/academics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cross-user ownership protection: students can only access their own profile and academics")
    void crossUserOwnershipEnforcedStrictly() throws Exception {
        // Student A
        String tokenA = registerAndGetStudentToken("student_a@campus.edu", "Password123!", "Student A");
        createProfileForStudent(tokenA);

        // Student B
        String tokenB = registerAndGetStudentToken("student_b@campus.edu", "Password123!", "Student B");

        // Student B calls GET /api/v1/students/me -> Gets 404 (their own profile is not yet created, NOT Student A's)
        mockMvc.perform(get("/api/v1/students/me")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student profile has not been created yet"));

        // Student A calls GET /api/v1/students/me -> Gets Student A's profile
        mockMvc.perform(get("/api/v1/students/me")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student_a@campus.edu"))
                .andExpect(jsonPath("$.fullName").value("Student A"));
    }

    private String registerAndGetStudentToken(String email, String password, String name) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest(email, password, name, Role.STUDENT))))
                .andExpect(status().isCreated());

        return authenticateAndGetToken(email, password);
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

    private void createProfileForStudent(String token) throws Exception {
        StudentProfileRequest request = new StudentProfileRequest(
                "+919876543210",
                "National Institute of Technology",
                "B.Tech",
                "Computer Science",
                2026,
                6
        );

        mockMvc.perform(put("/api/v1/students/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
