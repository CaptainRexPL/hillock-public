package dev.codeclub.hillock.event;

import dev.codeclub.hillock.database.model.UnauthorizedAttempt;
import org.springframework.context.ApplicationEvent;

public class UnauthorizedAttemptEvent extends ApplicationEvent {
    private  final UnauthorizedAttempt unauthorizedAttempt;

    public UnauthorizedAttemptEvent(Object source, UnauthorizedAttempt unauthorizedAttempt) {
        super(source);
        this.unauthorizedAttempt = unauthorizedAttempt;
    }

    public UnauthorizedAttempt getUnauthorizedAttempt() {
        return unauthorizedAttempt;
    }
}
