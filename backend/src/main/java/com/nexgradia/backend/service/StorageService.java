package com.nexgradia.backend.service;

import java.io.InputStream;
import org.springframework.core.io.Resource;

public interface StorageService {

    String store(InputStream inputStream, String extension);

    Resource loadAsResource(String storageKey);

    void delete(String storageKey);
}
