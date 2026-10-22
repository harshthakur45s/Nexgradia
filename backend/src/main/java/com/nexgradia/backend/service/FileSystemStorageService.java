package com.nexgradia.backend.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileSystemStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);

    private final Path rootLocation;

    public FileSystemStorageService(@Value("${app.storage.resume-dir:storage/resumes}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.rootLocation);
            logger.info("Resume storage directory initialized at: {}", this.rootLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not initialize storage directory at " + this.rootLocation, ex);
        }
    }

    @Override
    public String store(InputStream inputStream, String extension) {
        String cleanExtension = extension.startsWith(".") ? extension.substring(1) : extension;
        String storageKey = UUID.randomUUID() + "." + cleanExtension.toLowerCase();

        try {
            Path destinationFile = this.rootLocation.resolve(storageKey).normalize().toAbsolutePath();

            // Guard against path traversal attacks
            if (!destinationFile.startsWith(this.rootLocation) || !destinationFile.getParent().equals(this.rootLocation)) {
                throw new SecurityException("Cannot store file outside target directory");
            }

            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return storageKey;
        } catch (IOException ex) {
            logger.error("Failed to store file with storageKey: {}", storageKey, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file safely");
        }
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        try {
            Path file = this.rootLocation.resolve(storageKey).normalize().toAbsolutePath();

            if (!file.startsWith(this.rootLocation) || !file.getParent().equals(this.rootLocation)) {
                throw new SecurityException("Cannot access file outside target directory");
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume file not found on storage");
            }
        } catch (MalformedURLException ex) {
            logger.error("Malformed URL for storageKey: {}", storageKey, ex);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume file not found on storage");
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path file = this.rootLocation.resolve(storageKey).normalize().toAbsolutePath();
            if (file.startsWith(this.rootLocation) && file.getParent().equals(this.rootLocation)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException ex) {
            logger.warn("Could not delete storage file: {}", storageKey, ex);
        }
    }
}
