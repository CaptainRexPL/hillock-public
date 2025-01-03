package dev.codeclub.hillock.controller;

import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.database.service.UserService;
import dev.codeclub.hillock.http.HttpException;
import dev.codeclub.hillock.http.model.*;
import dev.codeclub.hillock.model.PublicUserProfile;
import dev.codeclub.hillock.model.UpdateUserResult;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profile")
@SecurityRequirement(name = "Authentication Token")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateUserResult> updateUser(@RequestAttribute("user") User user, @PathVariable Long id, @RequestBody UpdateUserRequest userRequest) {
        UpdateUserResult result = userService.updateUser(user, id, userRequest);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PutMapping("/")
    public ResponseEntity<UpdateUserResult> updateUser(@RequestAttribute("user") User user, @RequestBody UpdateUserRequest userRequest) {
        UpdateUserResult result = userService.updateUser(user, user.getId(), userRequest);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/me")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Udało się znaleźć profil", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Niepoprawny format rządania", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class))),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono profilu", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class)))
    })
    public ResponseEntity<?> getUser(@RequestAttribute("user") User user) {
        try {
            UserResponse response = userService.getUser(null, user);
            return ResponseEntity.ok(response);
        } catch (HttpException.NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (HttpException.BadRequestException e) {
            return ResponseEntity.badRequest().body(new FailureInfo(e.getMessage()));
        }
    }

    @GetMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Udało się znaleźć profil", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Niepoprawny format rządania", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class))),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono profilu", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class)))
    })
    public ResponseEntity<?> getUser(@RequestAttribute("user") User user, @ModelAttribute GetUserRequest getUserRequest) {
        try {
            UserResponse response = userService.getUser(getUserRequest, user);
            return ResponseEntity.ok(UserResponse.toPublic(response));
        } catch (HttpException.NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (HttpException.BadRequestException e) {
            return ResponseEntity.badRequest().body(new FailureInfo(e.getMessage()));
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<GetLeaderboardResponse> getLeaderboard(@RequestParam Integer limit) {
        List<User> users = userService.getLeaderboard(limit);
        List<GetLeaderboardResponse.ProfileScore> leaderboard = users.stream()
                .map(user -> new GetLeaderboardResponse.ProfileScore(user.getUsername(), user.getPokerscore()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new GetLeaderboardResponse(leaderboard));
    }

}