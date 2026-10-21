package com.nexgradia.backend.dto;

import java.time.Instant;
import java.util.List;

public class ApiErrorResponse {

    private int status;
    private String error;
    private String message;
    private List<String> details;
    private String timestamp;

    public ApiErrorResponse() {
        this.timestamp = Instant.now().toString();
    }

    public ApiErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }

    public ApiErrorResponse(int status, String error, String message, List<String> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
        this.timestamp = Instant.now().toString();
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getDetails() {
        return details;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
