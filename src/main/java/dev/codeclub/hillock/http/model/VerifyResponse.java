package dev.codeclub.hillock.http.model;

public class VerifyResponse {
    private final boolean success;
    private final String message;

    public VerifyResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
