package com.nexgradia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexgradia.backend.dto.LoginRequest;
import com.nexgradia.backend.dto.RegisterRequest;
import com.nexgradia.backend.dto.StudentProfileRequest;
import com.nexgradia.backend.model.Role;
import com.nexgradia.backend.model.User;
import com.nexgradia.backend.repository.AcademicRecordRepository;
import com.nexgradia.backend.repository.ResumeRepository;
import com.nexgradia.backend.repository.StudentProfileRepository;
import com.nexgradia.backend.repository.UserRepository;
import com.nexgradia.backend.security.JwtTokenProvider;
import com.nexgradia.backend.security.UserPrincipal;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResumeManagementTest {

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

    @Value("${app.storage.resume-dir:target/test-storage/resumes}")
    private String storageDir;

    @BeforeEach
    void setUp() throws IOException {
        resumeRepository.deleteAll();
        academicRecordRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        // Clean up test storage directory
        Path storagePath = Path.of(storageDir).toAbsolutePath().normalize();
        if (Files.exists(storagePath)) {
            try (var stream = Files.walk(storagePath)) {
                stream.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .filter(f -> !f.equals(storagePath.toFile()))
                        .forEach(File::delete);
            }
        }
    }

    @Test
    @DisplayName("Upload valid PDF resume succeeds, sets version 1 active, and stores privately")
    void uploadValidPdfResumeSucceeds() throws Exception {
        String token = registerStudentAndCreateProfile("student_pdf@campus.edu", "Alice Student");

        byte[] validPdf = createMinimalValidPdf();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Alice_Resume_2026.pdf",
                "application/pdf",
                validPdf
        );

        mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFilename").value("Alice_Resume_2026.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSizeBytes").value(validPdf.length))
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.storageKey").doesNotExist()); // Confirms internal storageKey is not exposed
    }

    @Test
    @DisplayName("Upload valid DOCX resume succeeds and verifies OOXML zip container structure")
    void uploadValidDocxResumeSucceeds() throws Exception {
        String token = registerStudentAndCreateProfile("student_docx@campus.edu", "Bob Word");

        byte[] validDocx = createMinimalValidDocx();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Bob_Resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                validDocx
        );

        mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFilename").value("Bob_Resume.docx"))
                .andExpect(jsonPath("$.contentType").value("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Unsupported file extension is rejected with 400 Bad Request")
    void unsupportedFileExtensionIsRejected() throws Exception {
        String token = registerStudentAndCreateProfile("unsupported_ext@campus.edu", "Charlie");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.png",
                "image/png",
                new byte[]{1, 2, 3, 4, 5}
        );

        mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported file extension. Only .pdf and .docx formats are allowed"));
    }

    @Test
    @DisplayName("Spoofed file: .pdf extension with plain text or non-PDF signature is rejected")
    void spoofedPdfSignatureIsRejected() throws Exception {
        String token = registerStudentAndCreateProfile("spoofed_pdf@campus.edu", "David Spoof");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.pdf",
                "application/pdf",
                "This is not a PDF file at all, just plain text.".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid file content: file signature does not match PDF format"));
    }

    @Test
    @DisplayName("Spoofed file: .docx extension with non-Word zip structure is rejected")
    void spoofedDocxSignatureIsRejected() throws Exception {
        String token = registerStudentAndCreateProfile("spoofed_docx@campus.edu", "Emma Spoof");

        // Create a ZIP without OOXML entries (e.g. just a text file inside)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("notes.txt"));
            zos.write("Some notes".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                baos.toByteArray()
        );

        mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid file content: archive does not contain valid Word document structure"));
    }

    @Test
    @DisplayName("Empty file is rejected with 400 Bad Request")
    void emptyFileIsRejected() throws Exception {
        String token = registerStudentAndCreateProfile("empty_file@campus.edu", "Frank");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Resume file cannot be empty"));
    }

    @Test
    @DisplayName("Oversized file exceeding 5MB is rejected with 400 Bad Request")
    void oversizedFileIsRejected() throws Exception {
        String token = registerStudentAndCreateProfile("oversized@campus.edu", "Grace");

        // 5.5 MB payload starting with valid PDF header
        byte[] largeBytes = new byte[5 * 1024 * 1024 + 500 * 1024];
        byte[] pdfHeader = "%PDF".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(pdfHeader, 0, largeBytes, 0, pdfHeader.length);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "oversized.pdf",
                "application/pdf",
                largeBytes
        );

        mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Version history preserves past resumes, updates version numbers, and tracks active version")
    void versionHistoryAndCurrentVersionTrackedProperly() throws Exception {
        String token = registerStudentAndCreateProfile("versioning@campus.edu", "Hannah");

        // 1. Upload Version 1 (PDF)
        MockMultipartFile v1File = new MockMultipartFile(
                "file",
                "Hannah_v1.pdf",
                "application/pdf",
                createMinimalValidPdf()
        );
        mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(v1File)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.active").value(true));

        // 2. Upload Version 2 (DOCX)
        MockMultipartFile v2File = new MockMultipartFile(
                "file",
                "Hannah_v2.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                createMinimalValidDocx()
        );
        mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(v2File)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(2))
                .andExpect(jsonPath("$.active").value(true));

        // 3. List all versions -> both are listed in descending order (v2 first, then v1)
        mockMvc.perform(get("/api/v1/students/me/resumes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].versionNumber").value(2))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].versionNumber").value(1))
                .andExpect(jsonPath("$[1].active").value(false));

        // 4. GET /current -> returns Version 2
        mockMvc.perform(get("/api/v1/students/me/resumes/current")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionNumber").value(2))
                .andExpect(jsonPath("$.originalFilename").value("Hannah_v2.docx"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Download of owned resume returns exact file bytes, content headers, and original filename")
    void downloadOwnedResumeSucceeds() throws Exception {
        String token = registerStudentAndCreateProfile("downloader@campus.edu", "Ian Downloader");

        byte[] originalContent = createMinimalValidPdf();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Ian_Official_Resume.pdf",
                "application/pdf",
                originalContent
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();

        Long resumeId = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get("id").asLong();

        // Download
        MvcResult downloadResult = mockMvc.perform(get("/api/v1/students/me/resumes/" + resumeId + "/download")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Ian_Official_Resume.pdf\""))
                .andReturn();

        byte[] downloadedBytes = downloadResult.getResponse().getContentAsByteArray();
        assertThat(downloadedBytes).isEqualTo(originalContent);
    }

    @Test
    @DisplayName("Cross-user ownership protection: student cannot download another student's resume")
    void crossUserDownloadIsRejectedWith404() throws Exception {
        // Student A uploads a resume
        String tokenA = registerStudentAndCreateProfile("student_a_res@campus.edu", "Student A");
        MockMultipartFile fileA = new MockMultipartFile("file", "A_Resume.pdf", "application/pdf", createMinimalValidPdf());
        MvcResult resultA = mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(fileA)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andReturn();

        Long resumeIdA = objectMapper.readTree(resultA.getResponse().getContentAsString()).get("id").asLong();

        // Student B attempts to download Student A's resume
        String tokenB = registerStudentAndCreateProfile("student_b_res@campus.edu", "Student B");
        mockMvc.perform(get("/api/v1/students/me/resumes/" + resumeIdA + "/download")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resume not found"));
    }

    @Test
    @DisplayName("Unauthenticated, Recruiter, and Admin requests to resume endpoints are rejected")
    void unauthenticatedAndNonStudentRolesAreForbidden() throws Exception {
        // 1. Unauthenticated
        mockMvc.perform(get("/api/v1/students/me/resumes"))
                .andExpect(status().isUnauthorized());

        // 2. Recruiter
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("recruiter_res@campus.edu", "Password123!", "Recruiter User", Role.RECRUITER))))
                .andExpect(status().isCreated());

        String recruiterToken = authenticateAndGetToken("recruiter_res@campus.edu", "Password123!");

        mockMvc.perform(get("/api/v1/students/me/resumes")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());

        // 3. Placement Admin
        User adminUser = new User("admin_res@campus.edu", "hash", "Admin User", Role.PLACEMENT_ADMIN);
        userRepository.save(adminUser);
        String adminToken = jwtTokenProvider.generateToken(UserPrincipal.create(adminUser));

        mockMvc.perform(get("/api/v1/students/me/resumes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Missing physical file on storage returns 404 Not Found safely")
    void missingStorageFileReturns404() throws Exception {
        String token = registerStudentAndCreateProfile("missing_file@campus.edu", "Jack");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Jack_Resume.pdf",
                "application/pdf",
                createMinimalValidPdf()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/students/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();

        Long resumeId = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get("id").asLong();

        // Simulate physical file deletion
        var resume = resumeRepository.findById(resumeId).orElseThrow();
        Path physicalFile = Path.of(storageDir, resume.getStorageKey()).toAbsolutePath().normalize();
        Files.deleteIfExists(physicalFile);

        // Attempt download
        mockMvc.perform(get("/api/v1/students/me/resumes/" + resumeId + "/download")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resume file not found on storage"));
    }

    // Helper methods
    private byte[] createMinimalValidPdf() {
        return "%PDF-1.4\n1 0 obj\n<< /Type /Catalog >>\nendobj\ntrailer\n<< /Root 1 0 R >>\n%%EOF".getBytes(StandardCharsets.UTF_8);
    }

    private byte[] createMinimalValidDocx() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zos.write("<Types></Types>".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("word/document.xml"));
            zos.write("<w:document></w:document>".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    private String registerStudentAndCreateProfile(String email, String name) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest(email, "Password123!", name, Role.STUDENT))))
                .andExpect(status().isCreated());

        String token = authenticateAndGetToken(email, "Password123!");

        StudentProfileRequest profileRequest = new StudentProfileRequest(
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
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isOk());

        return token;
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
