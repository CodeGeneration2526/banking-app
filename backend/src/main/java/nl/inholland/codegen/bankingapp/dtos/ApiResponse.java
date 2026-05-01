package nl.inholland.codegen.bankingapp.dtos;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private String message;
    private int status;
    private LocalDateTime timestamp;
    private T data;

    public ApiResponse() {}

    public ApiResponse(String message, int status, T data) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }

    // getters & setters

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}