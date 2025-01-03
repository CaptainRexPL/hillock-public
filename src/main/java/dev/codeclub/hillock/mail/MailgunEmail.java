package dev.codeclub.hillock.mail;

import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.http.AppUrlProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class MailgunEmail extends AbstractEmail {

    private static final Logger LOGGER = LogManager.getLogger(MailgunEmail.class.getName());
    private final String domain;
    private final String apiKey;
    private final HttpClient httpClient;

    public MailgunEmail(AppUrlProvider urlProvider,
                        @Value("${mailgun.domain}") String domain,
                        @Value("${mailgun.api-key}") String apiKey,
                        HttpClient httpClient) {
        super(urlProvider);
        this.domain = domain;
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    @Override
    public void send(User user, String subject, String templateName, Map<String, Object> variables) {
        variables = prepareVariables(user, variables);
        Map<Object, Object> formBody = new HashMap<>();
        formBody.put("from", "Hillock support <no-reply@" + domain + ">"); // Mailgun requires the sender to be a verified domain
        formBody.put("to", user.getEmail());
        formBody.put("subject", subject);
        formBody.put("html", renderHtml(templateName, variables));
        formBody.put("text", renderText(templateName, variables));

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(("api" + ":" + apiKey).getBytes(StandardCharsets.UTF_8)))
                .POST(ofFormData(formBody))
                .uri(URI.create(String.format("https://api.eu.mailgun.net/v3/%s/messages", domain))) //if you are using US region, remove `eu` from the url
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                LOGGER.info("Email sent successfully to {}", user.getEmail());
            } else {
                LOGGER.error("Failed to send email to {}: {}", user.getEmail(), response.body());
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Connection issue", e);
        }
    }

    private static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}
