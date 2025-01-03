package dev.codeclub.hillock.database.service;

import dev.codeclub.hillock.database.model.Invite;
import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.database.repository.InviteRepository;
import dev.codeclub.hillock.database.repository.UserRepository;
import dev.codeclub.hillock.enums.Role;
import dev.codeclub.hillock.http.AppUrlProvider;
import dev.codeclub.hillock.http.HttpException;
import dev.codeclub.hillock.http.model.*;
import dev.codeclub.hillock.mail.Email;
import dev.codeclub.hillock.security.ApiToken;
import dev.codeclub.hillock.security.PasswordHashing;
import dev.codeclub.hillock.security.TokenCrypter;
import dev.codeclub.hillock.security.VerificationToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Service
public class AuthenticationService {

    private static Logger LOGGER = LogManager.getLogger(AuthenticationService.class);

    private final UserService userService;
    private final PasswordHashing passwordHashing;
    private final InviteRepository inviteRepository;
    private final TokenCrypter tokenCrypter;
    private final BrutusService brutusService;
    private final UserRepository userRepository;
    private final AppUrlProvider urlProvider;
    private final Email emailService;

    @Autowired
    public AuthenticationService(UserService userService, PasswordHashing passwordHashing, InviteRepository inviteRepository, TokenCrypter tokenCrypter, BrutusService brutusService,
                                 UserRepository userRepository, Email emailService, AppUrlProvider urlProvider) {
        this.userService = userService;
        this.passwordHashing = passwordHashing;
        this.inviteRepository = inviteRepository;
        this.tokenCrypter = tokenCrypter;
        this.brutusService = brutusService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.urlProvider = urlProvider;
    }

    public VerifyResponse verify(String namespace, String token) {
        if (token == null || token.isBlank()) {
            throw new HttpException.BadRequestException("token missing");
        }
        try {
            VerificationToken verificationToken = VerificationToken.from(tokenCrypter, token).orElseThrow(() -> new HttpException.BadRequestException("invalid token"));
            if (verificationToken.isExpired()) {
                return new VerifyResponse(false, "token expired");
            }
            if (verificationToken.getUUID().toString().equals(namespace)) {
                User user = userService.getUserById(verificationToken.userId).orElseThrow(() -> new HttpException.BadRequestException("user not found"));
               if (user.getEmailverified()) {
                    return new VerifyResponse(false, "email already confirmed");
                }
                user.setEmailverified(true);
                userRepository.save(user);
                return new VerifyResponse(true, "email confirmed");
            } else {
                throw new HttpException.BadRequestException("invalid token");
            }
        } catch (IllegalArgumentException e) {
            throw new HttpException.BadRequestException("invalid token");
        }
    }

    public CreateAccountResponse createAccount(CreateAccountRequest req) throws HttpException.BadRequestException {
        if (req.username() == null) {
            throw new HttpException.BadRequestException("name missing");
        } else if (!isNicknameValid(req.username())) {
            throw new HttpException.BadRequestException("name invalid");
        } else if (req.email() == null) {
            throw new HttpException.BadRequestException("email missing");
        } else if (!isEmailValid(req.email())) {
            throw new HttpException.BadRequestException("email invalid");
        } else if (req.password() == null) {
            throw new HttpException.BadRequestException("password missing");
        } else if (!isPasswordValid(req.password())) {
            throw new HttpException.BadRequestException("password invalid");
        } else if (req.confirmPassword() == null) {
            throw new HttpException.BadRequestException("confirm password missing");
        } else if (!req.password().equals(req.confirmPassword())) {
            throw new HttpException.BadRequestException("passwords do not match");
        } else if (req.invite() == null) {
            throw new HttpException.BadRequestException("invite missing");
        }
        Optional<Invite> invite = inviteRepository.findByInvite(req.invite());
        if (!invite.isPresent() || invite.get().getRedeemed()) {
            throw new IllegalArgumentException("Invalid or already redeemed invite code");
        }
        Optional<User> user = userService.getUserByEmail(req.email());
        if (user.isPresent()) {
            throw new HttpException.BadRequestException("email already in use");
        }
        user = userService.getUserByUsername(req.username());
        if (user.isPresent()) {
            throw new HttpException.BadRequestException("name already in use");
        }
        User newUser = userService.createUser(req.username(), req.email(), req.password(), invite.get());
        sendVerificationEmail(newUser);
        return new CreateAccountResponse(newUser.toPublic(), createToken(newUser));
    }

    public static boolean isNicknameValid(String nickname) {
        int length = nickname.length();
        return length >= 3 && length <= 100 && nickname.matches("^[a-zA-Z0-9_]+$");
    }

    public static boolean isEmailValid(String email) {
        return email.length() >= 3 && email.length() <= 100 && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isPasswordValid(String password) {
        int length = password.codePointCount(0, password.length());
        return length >= 8 && length <= 255;
    }

    public LoginResponse login(LoginRequest req) {
        if (req.email() == null || req.email().isBlank()) {
            brutusService.handleFailedLoginAttempt(null);
            throw new HttpException.BadRequestException("email missing");
        } else if (req.password() == null || req.password().isBlank()) {
            brutusService.handleFailedLoginAttempt(req.email());
            throw new HttpException.BadRequestException("password missing");
        }
        Optional<User> user;
        user = userService.getUserByEmail(req.email());
        if (user.isEmpty()) {
            brutusService.handleFailedLoginAttempt(req.email());
            throw new HttpException.BadRequestException("invalid email or password");
        }
        User currentUser = user.get();
        if (passwordHashing.verify(req.password().toCharArray(), currentUser.getHashedpassword().toCharArray())) {
            if (!currentUser.getEmailverified()) {
                sendVerificationEmail(currentUser);
                throw new HttpException.BadRequestException("Email not verified");
            }
            if (currentUser.getDisabled()) {
                throw new HttpException.ForbiddenException("Account banned");
            }
            currentUser.setNewLogin(brutusService.getClientIp());
            userRepository.save(currentUser);
            return new LoginResponse(UserResponse.fromDbUser(user.get()), createToken(user.get()));
        } else {
            currentUser.setNewFailedLogin(brutusService.getClientIp());
            brutusService.handleFailedLoginAttempt(req.email());
            userRepository.save(currentUser);
            throw new HttpException.BadRequestException("invalid email or password");
        }
    }

    /**
     * Create an API token
     * @param user to authenticate with
     * @return token
     */
    public String createToken(User user) {
        ApiToken apiToken = new ApiToken(user.getId(), Role.valueOf(user.getRole()), userService);
        String token;
        try {
            token = apiToken.serialize(tokenCrypter);
        } catch (IOException e) {
            throw new HttpException.ServerErrorException("token generation error", e);
        }
        return token;
    }

    private void sendVerificationEmail(User user) {
        ForkJoinPool.commonPool().submit(() -> {
            VerificationToken apiToken = new VerificationToken(user.getId(), new Date().getTime() + TimeUnit.HOURS.toMillis(24));
            String url;
            try {
                url = urlProvider.emailVerificationUrl(apiToken.serialize(tokenCrypter), apiToken.getUUID());
            } catch (IOException e) {
                throw new IllegalArgumentException("token could not be serialized", e);
            }
            try {
                emailService.send(user, "Verify your Hillock account", "verify-account", Map.of("verificationUrl", url));
            } catch (Throwable e) {
                LOGGER.info("could not send verification email", e);
            }
        });
    }
}
