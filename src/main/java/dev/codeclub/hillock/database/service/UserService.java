package dev.codeclub.hillock.database.service;
import de.mkammerer.snowflakeid.SnowflakeIdGenerator;
import de.mkammerer.snowflakeid.options.Options;
import de.mkammerer.snowflakeid.structure.Structure;
import de.mkammerer.snowflakeid.time.MonotonicTimeSource;
import de.mkammerer.snowflakeid.time.TimeSource;
import dev.codeclub.hillock.database.model.Invite;
import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.database.repository.UserRepository;
import dev.codeclub.hillock.enums.Role;
import dev.codeclub.hillock.http.AppUrlProvider;
import dev.codeclub.hillock.http.HttpException;
import dev.codeclub.hillock.http.model.GetUserRequest;
import dev.codeclub.hillock.http.model.GetUserResponse;
import dev.codeclub.hillock.http.model.UpdateUserRequest;
import dev.codeclub.hillock.http.model.UserResponse;
import dev.codeclub.hillock.mail.Email;
import dev.codeclub.hillock.model.UpdateUserResult;
import dev.codeclub.hillock.security.PasswordHashing;
import dev.codeclub.hillock.security.TokenCrypter;
import dev.codeclub.hillock.security.VerificationToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

import java.io.IOException;

@Service
public class UserService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ADVANCED_EMAIL_REGEX =
            "^(?=.{1,254}$)(?=.{1,64}@)[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final UserRepository userRepository;
    private final InviteService inviteService;
    private final PasswordHashing passwordHashing;

    @Autowired
    public UserService(UserRepository userRepository, InviteService inviteService, PasswordHashing passwordHashing) {
        this.userRepository = userRepository;
        this.inviteService = inviteService;
        this.passwordHashing = passwordHashing;
    }

    // CREATE
    public User createUser(String username, String email, String password, Invite invite) {
        String hashedPassword = passwordHashing.hash(password);
        Random random = new Random();
        int generatorId = random.nextInt(64);
        System.out.println("Generator ID: " + generatorId);

        // Use Unix epoch as the custom epoch
        TimeSource timeSource = new MonotonicTimeSource(Instant.parse("1970-01-01T00:00:00Z"));
        // Twitter snowflake structure: 41 bits timestamp, 10 bits machine ID, 12 bits sequence number
        Structure structure = new Structure(41, 10, 12);

        // If the sequence number overflows, throw an exception
        Options options = new Options(Options.SequenceOverflowStrategy.THROW_EXCEPTION);
        SnowflakeIdGenerator generator = SnowflakeIdGenerator.createCustom(generatorId, timeSource, structure, options);

        // Generate a new Snowflake ID
        long userId = generator.next();
        User user = new User(userId, username, email, false, hashedPassword, Role.GUEST.name(), false, invite, 0);
        userRepository.save(user);
        inviteService.redeemInvite(invite.getId());
        return user;
    }

    public UserResponse getUser(GetUserRequest req, User reqUser) {
        Optional<User> user;
        if (req == null) {
            return UserResponse.fromDbUser(reqUser);
        } else if (req.email() != null) {
            user = getUserByEmail(req.email());
            if (user.isPresent()) {
                return UserResponse.fromDbUser(user.get());
            } else {
                throw new HttpException.NotFoundException("User not found");
            }
        } else if (req.userId() != null) {
            user = getUserById(req.userId());
            if (user.isPresent()) {
                return UserResponse.fromDbUser(user.get());
            } else {
                throw new HttpException.NotFoundException("User not found");
            }
        } else if (req.username() != null) {
            user = getUserByUsername(req.username());
            if (user.isPresent()) {
                return UserResponse.fromDbUser(user.get());
            } else {
                throw new HttpException.NotFoundException("User not found");
            }
        } else if (req.discordId() != null) {
            user = getUserByDiscordId(req.discordId());
            if (user.isPresent()) {
                return UserResponse.fromDbUser(user.get());
            } else {
                throw new HttpException.NotFoundException("User not found");
            }
        } else {
            throw new HttpException.BadRequestException("Invalid request");
        }
    }

    // READ (find one by id)
    @Cacheable(value = "users", key = "#id")
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // READ (find one by email)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByDiscordId(Long discordId) {
        return userRepository.findByDiscordid(discordId);
    }

    // READ (find all)
    public List<User> getAllUses() {
        return userRepository.findAll();
    }

    // UPDATE
    @CacheEvict(value = "users", key = "#id")
    public User updateUser(Long id, User updatedUser) {
        if (userRepository.existsById(id)) {
            updatedUser.setId(id); 
            return userRepository.save(updatedUser);
        }
        return null;
    }

    // DELETE
    @CacheEvict(value = "users", key = "#id")
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private UpdateUserResult updateUser(Long userId, UpdateUserRequest req, Function<User, UpdateUserResult> updateFunction) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return new UpdateUserResult(false, "User not found", null);
        }
        User updatedUser = optionalUser.get();
        return updateFunction.apply(updatedUser);
    }

    private UpdateUserResult updateUserName(User user, String username) {
        if (username != null) {
            if (username.isBlank()) {
                return new UpdateUserResult(false, "Username cannot be empty or white space only", null);
            } else {
                Optional<User> optionalUser = userRepository.findByUsername(username);
                if (optionalUser.isPresent() && !optionalUser.get().getId().equals(user.getId())) {
                    return new UpdateUserResult(false, "Username already taken", null);
                }
                user.setUsername(username);
                userRepository.save(user);
            }
        }
        return new UpdateUserResult(true, null, user);
    }

    private UpdateUserResult updateEmail(User user, String email) {
        if (email != null) {
            if (!Pattern.matches(ADVANCED_EMAIL_REGEX, email)) {
                return new UpdateUserResult(false, "Incorrect email format", null);
            } else {
                Optional<User> optionalUser = userRepository.findByEmail(email);
                if (optionalUser.isPresent() && !optionalUser.get().getId().equals(user.getId())) {
                    return new UpdateUserResult(false, "Email already taken", null);
                }
                user.setEmail(email);
                user.setEmailverified(false);
                userRepository.save(user);
            }
        }
        return new UpdateUserResult(true, null, user);
    }

    private UpdateUserResult updatePassword(User user, String password) {
        if (password != null) {
            if (password.isBlank()) {
                return new UpdateUserResult(false, "Password cannot be empty or white space only", null);
            } else {
                user.setHashedpassword(passwordHashing.hash(password));
                userRepository.save(user);
            }
        }
        return new UpdateUserResult(true, null, user);
    }

    private UpdateUserResult updateRole(User user, String role) {
        if (role != null) {
            if (Role.contains(role)) {
                user.setRole(role);
                userRepository.save(user);
            } else {
                return new UpdateUserResult(false, "Incorrect role name", null);
            }
        }
        return new UpdateUserResult(true, null, user);
    }

    private UpdateUserResult updateDisabled(User user, Boolean disabled) {
        if (disabled != null) {
            user.setDisabled(disabled);
            userRepository.save(user);
        }
        return new UpdateUserResult(true, null, user);
    }

    private UpdateUserResult updateDiscordid(User user, Long discordid) {
        if (discordid != null) {
            Optional<User> optionalUser = userRepository.findByDiscordid(discordid);
            if (optionalUser.isPresent() && !optionalUser.get().getId().equals(user.getId())) {
                return new UpdateUserResult(false, "Discord id already taken", null);
            }
            user.setDiscordid(discordid);
            userRepository.save(user);
        }
        return new UpdateUserResult(true, null, user);
    }

    private UpdateUserResult updatePokerscore(User user, Integer pokerscore) {
        if (pokerscore != null) {
            user.setPokerscore(pokerscore);
            userRepository.save(user);
        }
        return new UpdateUserResult(true, null, user);
    }

    private UpdateUserResult updateUser(User targetUser, UpdateUserRequest req, UpdateAction... actions) {
        for (UpdateAction action : actions) {
            UpdateUserResult result = action.update();
            if (!result.isSuccess()) {
                return result;
            }
        }
        return new UpdateUserResult(true, "User updated successfully", targetUser);
    }

    private UpdateUserResult updateForBot(User targetUser, UpdateUserRequest req) {
        return updateUser(targetUser, req,
                () -> updateDiscordid(targetUser, req.discordId()),
                () -> updatePokerscore(targetUser, req.pokerScore()),
                () -> updateDisabled(targetUser, req.disabled())
        );
    }

    private UpdateUserResult updateOwnProfile(User targetUser, UpdateUserRequest req) {
        return updateUser(targetUser, req,
                () -> updateUserName(targetUser, req.username()),
                () -> updateEmail(targetUser, req.email()),
                () -> updatePassword(targetUser, req.password()),
                () -> updateDiscordid(targetUser, req.discordId()),
                () -> updatePokerscore(targetUser, req.pokerScore())
        );
    }

    private UpdateUserResult updateByAdmin(User targetUser, UpdateUserRequest req) {
        return updateUser(targetUser, req,
                () -> updateUserName(targetUser, req.username()),
                () -> updateEmail(targetUser, req.email()),
                () -> updatePassword(targetUser, req.password()),
                () -> updateRole(targetUser, req.role()),
                () -> updateDisabled(targetUser, req.disabled()),
                () -> updateDiscordid(targetUser, req.discordId()),
                () -> updatePokerscore(targetUser, req.pokerScore())
        );
    }



    public UpdateUserResult updateUser(User authUser, Long userId, UpdateUserRequest req) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new UpdateUserResult(false, "User not found", null);
        }
        User targetUser = user.get();
        if (authUser == null) {
            return updateForBot(targetUser, req);
        } else {
            if (authUser.getId().equals(userId)) {
                return updateOwnProfile(targetUser, req);
            } else {
                if (authUser.isAtLeastInRole(Role.ADMIN)) {
                    return updateByAdmin(targetUser, req);
                } else {
                    return new UpdateUserResult(false, "You are not authorized to update other users", null);
                }
            }
        }
    }

    public Long getLeaderboardRank(Long userId) {
        return userRepository.getLeaderboardRank(userId);
    }

    public List<User> getLeaderboard(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<User> leaderboard = userRepository.getLeaderboard(pageable);
        return leaderboard;
    }

    public boolean isAuthorized(User user, Role role) {
        return Role.valueOf(user.getRole()).level >= role.level;
    }

    @FunctionalInterface
    private interface UpdateAction {
        UpdateUserResult update();
    }
}