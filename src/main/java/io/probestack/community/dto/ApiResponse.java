package io.probestack.community.dto;

import java.time.Instant;

public class ApiResponse<T> {

    private final Instant timestamp;
    private final String status;
    private final String message;
    private final T data;

    private ApiResponse(Builder<T> builder) {
        this.timestamp = Instant.now();
        this.status = builder.status;
        this.message = builder.message;
        this.data = builder.data;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private String status;
        private String message;
        private T data;

        public Builder<T> status(String status) { this.status = status; return this; }
        public Builder<T> message(String message) { this.message = message; return this; }
        public Builder<T> data(T data) { this.data = data; return this; }
        public ApiResponse<T> build() { return new ApiResponse<>(this); }
    }

    public Instant getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
