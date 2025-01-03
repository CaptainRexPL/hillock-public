package dev.codeclub.hillock;

import dev.codeclub.hillock.database.service.InviteService;
import dev.codeclub.hillock.database.service.UserService;
import dev.codeclub.hillock.discord.DiscordBot;
import dev.codeclub.hillock.discord.DiscordConfiguration;
import dev.codeclub.hillock.http.AppUrlProvider;
import dev.codeclub.hillock.mail.Email;
import dev.codeclub.hillock.mail.LocalEmail;
import dev.codeclub.hillock.mail.MailgunEmail;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.http.HttpClient;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableConfigurationProperties(DiscordConfiguration.class)
@SecurityScheme(
		name = "Authentication Token",
		scheme = "",
		type = SecuritySchemeType.APIKEY,
		in = SecuritySchemeIn.HEADER,
		paramName = "X-Brutus-Token"
)
public class HillockApplication {

	public static void main(String[] args) {
		SpringApplication.run(HillockApplication.class, args);
	}

	@Value("${mailing.service:local}")
	private String mailingService;

	@Value("${hillock.baseurl:http://localhost:8080}")
	private String serverUrl;

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new io.swagger.v3.oas.models.info.Info()
						.title("Hillock")
						.description("No description provided")
						.version("1.0.0-dev"))
				.addServersItem(new io.swagger.v3.oas.models.servers.Server().url(serverUrl));
	}

	@Bean(name = "discordBotExecutor")
	public Executor discordBotExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(1);
		executor.setQueueCapacity(0);
		executor.setThreadNamePrefix("DiscordBot-");
		executor.initialize();
		return executor;
	}

	@Bean
	@ConditionalOnProperty(name = "discord.enabled", havingValue = "true", matchIfMissing = false)
	public DiscordBot discordBot(DiscordConfiguration discordConfiguration,
								 InviteService inviteService,
								 UserService userService,
								 @Qualifier("discordBotExecutor") Executor discordBotExecutor) {
		return new DiscordBot(discordConfiguration, inviteService, userService, discordBotExecutor);
	}

	@Bean
	public HttpClient httpClient() {
		return HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.build();
	}

	@Bean
	public Email emailService(AppUrlProvider urlProvider, HttpClient httpClient,
							  @Value("${mailgun.domain}") String domain,
							  @Value("${mailgun.api-key}") String apiKey) {
		if ("mailgun".equalsIgnoreCase(mailingService)) {
			return new MailgunEmail(urlProvider, domain, apiKey, httpClient);
		} else {
			return new LocalEmail(urlProvider);
		}
	}
}
