package dev.codeclub.hillock.mail;

import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.http.AppUrlProvider;
import dev.codeclub.hillock.http.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEmail implements Email {
    private final HandlebarsTemplateEngine handlebars = new HandlebarsTemplateEngine("/emails");
    private final AppUrlProvider urlProvider;

    public AbstractEmail(AppUrlProvider urlProvider) {
        this.urlProvider = urlProvider;
    }

    protected Map<String, Object> prepareVariables(User user, Map<String, Object> variables) {
        variables = new HashMap<>(variables);
        variables.put("name", user.getUsername());
        variables.put("email", user.getEmail());
        variables.put("homeUrl", urlProvider.homeUrl());
        variables.put("loginUrl", urlProvider.loginUrl());
        variables.put("signupUrl", urlProvider.signupUrl());
        variables.put("logoUrl", urlProvider.logoUrl(128));
        return variables;
    }

    protected String renderHtml(String templateName, Map<String, Object> variables) {
        return renderTemplate(templateName + "-html", variables);
    }

    protected String renderText(String templateName, Map<String, Object> variables) {
        return renderTemplate(templateName + "-txt", variables);
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        return handlebars.render(templateName, variables);
    }
}