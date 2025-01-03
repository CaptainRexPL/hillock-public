package dev.codeclub.hillock.http.model;

public class FailureInfo {
    private final String message;

    public FailureInfo(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
