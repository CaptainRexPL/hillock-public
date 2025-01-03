package dev.codeclub.hillock.model;
import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.http.model.UserResponse;

public class UpdateUserResult {
    private final boolean success;
    private final String message;
    private final UserResponse user;

    public UpdateUserResult(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = UserResponse.fromDbUser(user);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserResponse getUser() {
        return user;
    }
}
