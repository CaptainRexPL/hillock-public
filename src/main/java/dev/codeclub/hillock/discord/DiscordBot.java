package dev.codeclub.hillock.discord;

import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.database.service.InviteService;
import dev.codeclub.hillock.database.service.UserService;
import dev.codeclub.hillock.enums.Role;
import dev.codeclub.hillock.event.FailedLoginAttemptEvent;
import dev.codeclub.hillock.event.UnauthorizedAttemptEvent;
import dev.codeclub.hillock.http.HttpException;
import dev.codeclub.hillock.http.model.GetUserRequest;
import dev.codeclub.hillock.http.model.UpdateUserRequest;
import dev.codeclub.hillock.http.model.UserResponse;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.util.Color;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import reactor.core.publisher.Mono;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;


public class DiscordBot {

    private static final Logger LOGGER = LogManager.getLogger(DiscordBot.class.getName());

    private final DiscordConfiguration discordConfiguration;
    private final InviteService inviteService;
    private final UserService userService;
    private final Executor discordBotExecutor;
    private RestClient restClient;
    private GatewayDiscordClient client;


    public DiscordBot(DiscordConfiguration discordConfiguration, InviteService inviteService, UserService userService, Executor discordBotExecutor) {
        this.discordConfiguration = discordConfiguration;
        this.inviteService = inviteService;
        this.userService = userService;
        this.discordBotExecutor = discordBotExecutor;
    }

    @PostConstruct
    public void initializeBot() {
        LOGGER.info("Scheduling Discord bot start...");
        discordBotExecutor.execute(this::start);
    }

    public void start() {
        try {
            LOGGER.info("Starting Discord bot asynchronously...");
            client = DiscordClient.create(discordConfiguration.getToken())
                    .login()
                    .block();
            registerCommands();
            client.on(ChatInputInteractionEvent.class, this::handleCommand).subscribe();
            LOGGER.info("Discord bot started successfully.");
        } catch (Exception e) {
            LOGGER.error("Failed to start Discord bot", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (client != null) {
            client.logout().block();
            LOGGER.info("Discord bot stopped.");
        }
    }

    @EventListener
    public void handleFailedLoginAttemptEvent(FailedLoginAttemptEvent event) {
        restClient.getChannelById(Snowflake.of(discordConfiguration.getAlertsChannel())).createMessage(createFailedLoginEmbed(event).build().asRequest()).block();
    }

    @EventListener
    public void handleUnauthorizedAttemptEvent(UnauthorizedAttemptEvent event) {
        restClient.getChannelById(Snowflake.of(discordConfiguration.getAlertsChannel())).createMessage(createUnauthorizedAttemptEmbed(event).build().asRequest()).block();
    }

    private void registerCommands() {
        LOGGER.info("Registering Discord commands...");
        long startTime = System.nanoTime(); 
        ApplicationCommandRequest inviteCode = ApplicationCommandRequest.builder()
                .name("invite")
                .description("Stwórz zaproszenie do systemu Hillock")
                .build();

        ApplicationCommandOptionData usernameArgument = ApplicationCommandOptionData.builder()
                .name("username")
                .description("Nazwa użytkownika w systemie Hillock")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .build();

        ApplicationCommandOptionData emailArgument = ApplicationCommandOptionData.builder()
                .name("email")
                .description("Email przypisany do twojego konta Hillock")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .build();

        ApplicationCommandOptionData disableArgument = ApplicationCommandOptionData.builder()
                .name("disable")
                .description("Zablokuj użytkownika")
                .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                .required(true)
                .build();

        ApplicationCommandOptionData scoreArgument = ApplicationCommandOptionData.builder()
                .name("score")
                .description("Punkty pokerowe")
                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                .required(true)
                .build();

        ApplicationCommandRequest connectDiscord = ApplicationCommandRequest.builder()
                .name("connect")
                .description("Połącz konto Discord z kontem Hillock")
                .build()
                .withOptions(usernameArgument);

        ApplicationCommandRequest disconnectDiscord = ApplicationCommandRequest.builder()
                .name("disconnect")
                .description("Rozłącz konto Discord z kontem Hillock")
                .build()
                .withOptions(usernameArgument);

        ApplicationCommandRequest profileLookup = ApplicationCommandRequest.builder()
                .name("profile")
                .description("Wyszukaj konto Hillock")
                .build()
                .withOptions(usernameArgument, emailArgument);

        ApplicationCommandRequest disable = ApplicationCommandRequest.builder()
                .name("disable")
                .description("Zablokuj/odblokuj konto użytkownika")
                .build()
                .withOptions(disableArgument, usernameArgument, emailArgument);

        ApplicationCommandRequest leaderboard = ApplicationCommandRequest.builder()
                .name("leaderboard")
                .description("Wyświetl top 10 hazardzistów")
                .build();

        ApplicationCommandRequest updateScore = ApplicationCommandRequest.builder()
                .name("updatescore")
                .description("Zaktualizuj wynik")
                .build()
                .withOptions(scoreArgument, usernameArgument, emailArgument);

        ApplicationCommandRequest pay = ApplicationCommandRequest.builder()
                .name("pay")
                .description("Przelej daną sumę żetonów pokerowych")
                .build()
                .withOptions(scoreArgument, usernameArgument, emailArgument);


        restClient = client.getRestClient();
        long applicationId = restClient.getApplicationId().block();
        registerCommand(inviteCode, applicationId);
        registerCommand(connectDiscord, applicationId);
        registerCommand(disconnectDiscord, applicationId);
        registerCommand(profileLookup, applicationId);
        registerCommand(disable, applicationId);
        registerCommand(leaderboard, applicationId);
        registerCommand(updateScore, applicationId);
        registerCommand(pay, applicationId);

        long endTime = System.nanoTime();
        double elapsedTimeInSeconds = (endTime - startTime) / 1_000_000_000.0;
        DecimalFormat df = new DecimalFormat("#.###");
        String formattedTime = df.format(elapsedTimeInSeconds);

        LOGGER.info("Registering Discord commands completed in {} seconds", formattedTime);
    }

    private void registerCommand(ApplicationCommandRequest command, long applicationId) {
        try {
            restClient.getApplicationService()
                    .createGuildApplicationCommand(applicationId, Long.parseLong(discordConfiguration.getGuild()), command)
                    .doOnError(e -> LOGGER.log(Level.FATAL, "Unable to create global command", e))
                    .doOnSuccess(applicationCommandData -> LOGGER.log(Level.TRACE, "Created command"))
                    .onErrorResume(e -> Mono.empty())
                    .block();
        } catch (Throwable e) {
            LOGGER.error("Failed to add command " + command.name(), e);
        }
    }

    private Mono<Void> handleCommand(ChatInputInteractionEvent event) {
        RestChannel channel = restClient.getChannelById(event.getInteraction().getChannelId());
        if (!validateAccessRole(event.getInteraction().getMember())) {
            return event.reply("You don't have access");
        }
        switch (event.getCommandName()) {
            case "invite":
                return processInviteCommand(event, channel);
            case "connect":
                return processConnectCommand(event, channel);
            case "disconnect":
                return processDisconnectCommand(event, channel);
            case "profile":
                return processLookupCommand(event, channel);
            case "disable":
                return processDisableCommand(event, channel);
            case "leaderboard":
                return processLeaderboardCommand(event, channel);
            case "updatescore":
                return processUpdateScore(event, channel);
            case "pay":
                return processPay(event, channel);
            default:
                return event.reply("unhandled command");
        }
    }

    private Mono<Void> processInviteCommand(ChatInputInteractionEvent event, RestChannel channel) {
        if (!validateAdminRole(event.getInteraction().getMember())) {
            channel.createMessage("You don't have access").block();
            return event.reply("You don't have access");
        } else {
            String code = inviteService.createInviteCode(event.getInteraction().getUser().getId().asLong());
            return event.reply("Wygenerowany kod to: `" + code + "`. Aby go wykorzystać, zarejestruj się.").withEphemeral(true);
        }
    }

    private Mono<Void> processConnectCommand(ChatInputInteractionEvent event, RestChannel channel) {
        UserResponse updatedUser = updateUserWithDiscordId(event, event.getInteraction().getUser().getId().asLong());
        if (updatedUser != null) {
            return event.reply("Pomyślnie przypisano do konta " + updatedUser.getUsername()).withEphemeral(true);
        } else {
            return event.reply("Nie udało się przypisać konta").withEphemeral(true);
        }
    }

    private Mono<Void> processDisconnectCommand(ChatInputInteractionEvent event, RestChannel channel) {
        UserResponse updatedProfile = updateUserWithDiscordId(event, -1);
        if (updatedProfile != null) {
            return event.reply("Pomyślnie odłączono od konta " + updatedProfile.getUsername()).withEphemeral(true);
        } else {
            return event.reply("Nie udało się odłączyć od konta").withEphemeral(true);
        }
    }

    private Mono<Void> processLookupCommand(ChatInputInteractionEvent event, RestChannel channel) {
        UserResponse user = getUser(event);
        if (user != null) {
            channel.createMessage(createProfileEmbed(user).build().asRequest()).block();
            return event.reply("Znaleziono profil").withEphemeral(false);
        } else {
            return event.reply("Nie znaleziono profilu").withEphemeral(false);
        }
    }

    private Mono<Void> processDisableCommand(ChatInputInteractionEvent event, RestChannel channel) {
        String email = event.getOption("email").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asString)).orElse(null);
        String username = event.getOption("username").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asString)).orElse(null);
        Boolean disable = event.getOption("disable").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asBoolean)).orElse(null);
        Optional<User> usersProfile = userService.getUserByDiscordId(event.getInteraction().getUser().getId().asLong());
        if (usersProfile.isEmpty() || usersProfile.get().getDisabled()) {
            return event.reply().withEmbeds(createFailedDisableEmbed("Twoje konto nie jest skojarzone z żadnym kontem Hillock lub konto jest zablokowane").build());
        } else if (!userService.isAuthorized(usersProfile.get(), Role.ADMIN) && !validateAdminRole(event.getInteraction().getMember()))  {
            return event.reply().withEmbeds(createFailedDisableEmbed("Nie masz uprawnień do blokowania użytkowników").build());
        }
        if (email == null && username == null) {
            return event.reply().withEmbeds(createFailedDisableEmbed("Nie podano użytkownika").build());
        }
        if (disable == null) {
            return event.reply().withEmbeds(createFailedDisableEmbed(null).build());
        }
        UserResponse user = getUser(event);
        if (user == null) {
            event.reply().withEmbeds(createFailedDisableEmbed("Użytkownik o podanym emailu/id nie istnieje").build());
        } else if (user.getUsername().equals(usersProfile.get().getUsername())) {
            return event.reply().withEmbeds(createFailedDisableEmbed("Nie możesz zablokować swojego konta. Jeśli to konieczne, zgłoś to adminowi").build());
        }
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(null, null, null, null, null, null, true);
        userService.updateUser(null, user.getId(), updateUserRequest);
        return event.reply().withEmbeds(createSuccessfulDisableEmbed(user.getUsername(), disable).build());
    }

    private Mono<Void> processLeaderboardCommand(ChatInputInteractionEvent event, RestChannel channel) {
        return event.reply("Remember, house always wins...").doOnSuccess(unused -> {
            channel.createMessage(createLeaderboardEmbed().build().asRequest()).block();
        });
    }

    private Mono<Void> processUpdateScore(ChatInputInteractionEvent event, RestChannel channel) {
        String email = event.getOption("email").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asString)).orElse(null);
        String username = event.getOption("username").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asString)).orElse(null);
        Optional<User> usersProfile = userService.getUserByDiscordId(event.getInteraction().getUser().getId().asLong());
        if (usersProfile.isPresent() && !usersProfile.get().getDisabled()) {
            if (email == null && username == null) {
                UpdateUserRequest updateUserRequest = new UpdateUserRequest(null, null, null, event.getOption("score").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asLong)).map(Long::intValue).orElse(null), null, null, null);
                userService.updateUser(null, usersProfile.get().getId(), updateUserRequest);
                return event.reply().withEmbeds(createSuccessfulScoreUpdateEmbed(usersProfile.get().getUsername(), updateUserRequest.pokerScore()).build());
            } else {
                UserResponse user = getUser(event);
                if (userService.isAuthorized(usersProfile.get(), Role.POKER_MANAGER) || validateAdminRole(event.getInteraction().getMember()) || (user != null && user.getId().equals(usersProfile.get().getId()))) {
                    if (user != null) {
                        UpdateUserRequest updateUserRequest = new UpdateUserRequest(null, null, null, event.getOption("score").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asLong)).map(Long::intValue).orElse(null), null, null, null);
                        userService.updateUser(null, user.getId(), updateUserRequest);
                        return event.reply().withEmbeds(createSuccessfulScoreUpdateEmbed(user.getUsername(), updateUserRequest.pokerScore()).build());
                    } else {
                        return event.reply().withEmbeds(createFailedScoreUpdateEmbed("Nie znaleziono użytkownika").build());
                    }
                } else {
                    return event.reply().withEmbeds(createFailedScoreUpdateEmbed("Nie masz uprawnień do zmiany punktów innych użytkowników").build());
                }
            }
        } else {
            return event.reply().withEmbeds(createFailedScoreUpdateEmbed("Twoje konto nie jest skojarzone z żadnym kontem Hillock lub jest zablokowane").build());
        }
    }

    private Mono<Void> processPay(ChatInputInteractionEvent event, RestChannel channel) {
        String email = event.getOption("email").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asString)).orElse(null);
        String username = event.getOption("username").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asString)).orElse(null);
        Integer score = event.getOption("score").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asLong)).map(Long::intValue).orElse(null);
        Optional<User> usersProfile = userService.getUserByDiscordId(event.getInteraction().getUser().getId().asLong());
        if (usersProfile.isEmpty() || usersProfile.get().getDisabled()) {
            return event.reply().withEmbeds(createFailedPayEmbed("Twoje konto nie jest skojarzone z żadnym kontem Hillock lub jest zablokowane").build());
        } else if (email == null && username == null) {
            return event.reply().withEmbeds(createFailedPayEmbed("Nie podano odbiorcy").build());
        }
        UserResponse user = getUser(event);
        if (user == null) {
            return event.reply().withEmbeds(createFailedPayEmbed("Użytkownik o podanym emailu/id nie istnieje").build());
        }
        if (Objects.equals(user.getUsername(), usersProfile.get().getUsername())) {
            return event.reply().withEmbeds(createFailedPayEmbed("Nie możesz przelać żetonów na swoje własne konto!").build());
        }
        if (score == null || score < 0) {
            return event.reply().withEmbeds(createFailedPayEmbed("Nie możesz przelać ujemnej kwoty").build());
        } else if (usersProfile.get().getPokerscore() < score) {
            return event.reply().withEmbeds(createFailedPayEmbed("Nie możesz przelać więcej żetonów niż posiadasz (" + usersProfile.get().getPokerscore() + ")").build());
        }

        UpdateUserRequest updateSenderRequest = new UpdateUserRequest(null, null, null, usersProfile.get().getPokerscore() - score, null, null, null);
        userService.updateUser(null, user.getId(), updateSenderRequest);
        UpdateUserRequest updateRecipientRequest = new UpdateUserRequest(null, null, null, user.getPokerscore() + score, null, null, null);
        userService.updateUser(null, user.getId(), updateRecipientRequest);
        return event.reply().withEmbeds(createSuccessfulPayEmbed(user.getUsername(), score).build());
    }

    private UserResponse getUser(ChatInputInteractionEvent event) {
        String email = event.getOption("email").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asString)).orElse(null);
        String username = event.getOption("username").flatMap(option -> option.getValue().map(ApplicationCommandInteractionOptionValue::asString)).orElse(null);
        if (email == null && username == null) {
            return null;
        }
        try {
            GetUserRequest getUserRequest = new GetUserRequest(
                    null,
                    email,
                    username,
                    null
            );
            return userService.getUser(getUserRequest, null);
        } catch (HttpException.NotFoundException e) {
            return null;
        }
    }

    private UserResponse updateUserWithDiscordId(ChatInputInteractionEvent event, long discordId) {
        UserResponse user = getUser(event);
        if (user == null) {
            return null;
        }
        UpdateUserRequest updateProfileRequest = new UpdateUserRequest(null, null, null, null, discordId, null, null);
        return userService.updateUser(null, user.getId(), updateProfileRequest).getUser();
    }

    private EmbedCreateSpec.Builder createSuccessfulDisableEmbed(String username, boolean disabled) {
        return EmbedCreateSpec.builder()
                .title(disabled ? "Zablokowano użytkownika" : "Odblokowano użytkownika")
                .color(disabled ? Color.ORANGE : Color.GREEN)
                .footer("Hillock made with <3 by CodeClub", null)
                .addField("Nazwa użytkownika: ", username, true);
    }

    private EmbedCreateSpec.Builder createFailedDisableEmbed(String reason) {
        return EmbedCreateSpec.builder()
                .title("Nie udało się zablokować użytkownika")
                .color(Color.RED)
                .footer("Hillock made with <3 by CodeClub", null)
                .addField("Powód: ", reason != null ? reason : "Błąd systemu", false);
    }

    private EmbedCreateSpec.Builder createSuccessfulScoreUpdateEmbed(String username, long score) {
        return EmbedCreateSpec.builder()
                .title("Zaktualizowano wynik")
                .color(Color.GREEN)
                .footer("Hillock made with <3 by CodeClub", null)
                .addField("Nazwa użytkownika: ", username, true)
                .addField("Nowy wynik: ", String.valueOf(score), true);
    }

    private EmbedCreateSpec.Builder createFailedScoreUpdateEmbed(String reason) {
        return EmbedCreateSpec.builder()
                .title("Nie udało się zaktualizować wyniku")
                .color(Color.RED)
                .footer("Hillock made with <3 by CodeClub", null)
                .addField("Powód: ", reason != null ? reason : "Błąd systemu", false);
    }

    private EmbedCreateSpec.Builder createSuccessfulPayEmbed(String recipient, long score) {
        return EmbedCreateSpec.builder()
                .title("Przelano pieniądze")
                .color(Color.GREEN)
                .footer("Hillock made with <3 by CodeClub", null)
                .addField("Odbiorca: ", recipient, true)
                .addField("Przelana kwota", String.valueOf(score), true);
    }

    private EmbedCreateSpec.Builder createFailedPayEmbed(String reason) {
        return EmbedCreateSpec.builder()
                .title("Nie udało się przelać kwoty")
                .color(Color.RED)
                .footer("Hillock made with <3 by CodeClub", null)
                .addField("Powód: ", reason != null ? reason : "Błąd systemu", false);
    }

    private EmbedCreateSpec.Builder createProfileEmbed(UserResponse user) {
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .title(user.getUsername())
                .color(Color.BLUE)
                .footer("Hillock made with <3 by CodeClub", null)
                .addField("id konta: ", user.getId().toString(), true)
                .addField("Rola: ", user.getRole(), true)
                .addField("Punkty pokerowe: ", user.getPokerscore().toString(), true)
                .addField("Pozycja w rankingu: ", userService.getLeaderboardRank(user.getId()).toString(), true)
                .addField("Discord", user.getDiscordid() == null || user.getDiscordid() == -1 ? "Brak połączenia" : "Połączono", true)
                .addField("Czy zablokowane: ", user.getDisabled().toString(), true);
        return builder;
    }

    private EmbedCreateSpec.Builder createLeaderboardEmbed() {
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .title("Top 10 Poker Players")
                .color(Color.RUBY)
                .footer("Hillock made with <3 by CodeClub", null);
        for (User user : userService.getLeaderboard(10)) {
            builder.addField(user.getUsername(), user.getPokerscore().toString(), false);
        }
        return builder;
    }

    private EmbedCreateSpec.Builder createFailedLoginEmbed(FailedLoginAttemptEvent event) {
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .title("BRUTUS: Failed login attempt detected")
                .color(Color.RED)
                .footer("Hillock made with <3 by CodeClub", null)
                .addField("Email: ", event.getFailedLoginAttempt().getEmail() != null ? event.getFailedLoginAttempt().getEmail() : "UNKNOWN", false)
                .addField("IP Address: ", event.getFailedLoginAttempt().getIpAddress(), false)
                .addField("Time: ", Instant.now().toString(), false);
        return builder;
    }

    private EmbedCreateSpec.Builder createUnauthorizedAttemptEmbed(UnauthorizedAttemptEvent event) {
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .title("BRUTUS: Unauthorized access attempt detected")
                .color(Color.RED)
                .footer("Hillock made with <3 by CodeClub", null)
                .addField("IP Address: ", event.getUnauthorizedAttempt().getIpAddress(), false)
                .addField("Profile id: ", event.getUnauthorizedAttempt().getProfileId() != null ? event.getUnauthorizedAttempt().getProfileId().toString() : "UNKNOWN", false)
                .addField("Time: ", Instant.now().toString(), false);
        return builder;
    }

    private boolean validateAccessRole(Optional<Member> member) {
        return member.map(value -> value.getRoleIds().stream()
                        .map(Snowflake::asString)
                        .anyMatch(roleId -> roleId.equals(discordConfiguration.getAccessRole())))
                .orElse(false);
    }

    private boolean validateAdminRole(Optional<Member> member) {
        return member.map(value -> value.getRoleIds().stream()
                        .map(Snowflake::asString)
                        .anyMatch(roleId -> roleId.equals(discordConfiguration.getAdminRole())))
                .orElse(false);
    }
}


