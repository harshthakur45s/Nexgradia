package com.nexgradia.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ResumeValidator {

    public static final String CONTENT_TYPE_PDF = "application/pdf";
    public static final String CONTENT_TYPE_DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private static final int MAX_ZIP_ENTRIES = 500;

    public record ValidatedFileMetadata(String sanitizedFilename, String contentType, String extension) {}

    public ValidatedFileMetadata validate(MultipartFile file, long maxFileSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resume file cannot be empty");
        }

        if (file.getSize() > maxFileSizeBytes) {
            long maxMb = maxFileSizeBytes / (1024 * 1024);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File size exceeds the maximum allowed limit of " + maxMb + "MB");
        }

        String rawFilename = file.getOriginalFilename();
        if (rawFilename == null || rawFilename.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filename is required");
        }

        // Sanitize filename to prevent path traversal and CRLF/header injection
        Path path = Paths.get(rawFilename).getFileName();
        String baseName = path != null ? path.toString().trim() : "resume";
        // Strip control characters, quotes, and newlines to prevent HTTP response splitting
        String sanitizedFilename = baseName.replaceAll("[\\r\\n\\t\"';]", "_");
        if (sanitizedFilename.length() > 255) {
            sanitizedFilename = sanitizedFilename.substring(0, 255);
        }

        int dotIndex = sanitizedFilename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == sanitizedFilename.length() - 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File must have a valid .pdf or .docx extension");
        }

        String extension = sanitizedFilename.substring(dotIndex + 1).toLowerCase();
        if (!"pdf".equals(extension) && !"docx".equals(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unsupported file extension. Only .pdf and .docx formats are allowed");
        }

        String declaredContentType = file.getContentType();
        validateDeclaredContentType(extension, declaredContentType);

        // Verify magic bytes / file signature
        validateFileSignature(file, extension);

        String canonicalContentType = "pdf".equals(extension) ? CONTENT_TYPE_PDF : CONTENT_TYPE_DOCX;
        return new ValidatedFileMetadata(sanitizedFilename, canonicalContentType, extension);
    }

    private void validateDeclaredContentType(String extension, String declaredContentType) {
        if (declaredContentType == null) {
            return; // Fallback to extension and magic bytes
        }

        String lowerType = declaredContentType.toLowerCase();
        if ("pdf".equals(extension)) {
            if (!lowerType.contains("pdf") && !lowerType.equals("application/octet-stream")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Declared content-type does not match .pdf file extension");
            }
        } else if ("docx".equals(extension)) {
            boolean validDocxType = lowerType.contains("wordprocessingml")
                    || lowerType.contains("officedocument")
                    || lowerType.equals("application/octet-stream")
                    || lowerType.equals("application/zip")
                    || lowerType.equals("application/x-zip-compressed");
            if (!validDocxType) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Declared content-type does not match .docx file extension");
            }
        }
    }

    private void validateFileSignature(MultipartFile file, String extension) {
        byte[] header = new byte[4];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header);
            if (bytesRead < 4) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Malformed file: file content is too small to be valid");
            }
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read file content for validation");
        }

        if ("pdf".equals(extension)) {
            // PDF Magic Bytes: %PDF (0x25, 0x50, 0x44, 0x46)
            if (header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid file content: file signature does not match PDF format");
            }
        } else if ("docx".equals(extension)) {
            // DOCX is a ZIP container: PK\x03\x04 (0x50, 0x4B, 0x03, 0x04)
            if (header[0] != 0x50 || header[1] != 0x4B || header[2] != 0x03 || header[3] != 0x04) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid file content: file signature does not match DOCX format");
            }

            // Verify valid ZIP structure containing OOXML entries without extracting to disk
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Malformed DOCX file: archive contains no valid entries");
                }
                boolean hasOoxmlStructure = false;
                int entryCount = 0;
                while (entry != null) {
                    entryCount++;
                    if (entryCount > MAX_ZIP_ENTRIES) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Malformed DOCX file: archive contains excessive entries");
                    }
                    String name = entry.getName();
                    if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Malformed DOCX file: invalid entry path detected in archive");
                    }
                    if ("[Content_Types].xml".equalsIgnoreCase(name) || name.startsWith("word/")) {
                        hasOoxmlStructure = true;
                        break;
                    }
                    entry = zis.getNextEntry();
                }
                if (!hasOoxmlStructure) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Invalid file content: archive does not contain valid Word document structure");
                }
            } catch (IOException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Malformed DOCX file: unable to read archive structure");
            }
        }
    }
}
