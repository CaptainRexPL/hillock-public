package dev.codeclub.hillock.event;

import dev.codeclub.hillock.database.model.FailedLoginAttempt;
import org.springframework.context.ApplicationEvent;

public class FailedLoginAttemptEvent extends ApplicationEvent {
    private final FailedLoginAttempt failedLoginAttempt;

    public FailedLoginAttemptEvent(Object source, FailedLoginAttempt failedLoginAttempt) {
        super(source);
        this.failedLoginAttempt = failedLoginAttempt;
    }


    public FailedLoginAttempt getFailedLoginAttempt() {
        return failedLoginAttempt;
    }
}
