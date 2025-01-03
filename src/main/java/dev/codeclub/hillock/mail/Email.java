package dev.codeclub.hillock.mail;

import dev.codeclub.hillock.database.model.User;

import java.util.Map;

public interface Email {
    void send(User user, String subject, String templateName, Map<String, Object> variables);
}
