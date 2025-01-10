package dev.codeclub.hillock.controller;

import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.database.service.UserService;
import dev.codeclub.hillock.http.HttpException;
import dev.codeclub.hillock.http.model.*;
import dev.codeclub.hillock.model.UpdateUserResult;
import dev.codeclub.hillock.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    private static User getUserFromAuthentication(Authentication authentication) {
        return ((CustomUserDetails)authentication.getPrincipal()).getUser();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update someone's profile", description = "Allows the administrator to modify someone's profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile update successfully", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Failed to update the profile, check error message for details", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class)))
    })
    public ResponseEntity<UpdateUserResult> updateUser(Authentication authentication, @PathVariable Long id, @RequestBody UpdateUserRequest userRequest) {
        UpdateUserResult result = userService.updateUser(getUserFromAuthentication(authentication), id, userRequest);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PutMapping("/")
    @Operation(summary = "Update profile", description = "Allows you to update your profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Failed to update your profile", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class)))
    })
    public ResponseEntity<UpdateUserResult> updateUser(Authentication authentication, @RequestBody UpdateUserRequest userRequest) {
        User user = getUserFromAuthentication(authentication);
        UpdateUserResult result = userService.updateUser(user, user.getId(), userRequest);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get your own profile", description = "Get your own profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class)))
    })
    public ResponseEntity<?> getUser(Authentication authentication) {
        try {
            UserResponse response = userService.getUser(null, getUserFromAuthentication(authentication));
            return ResponseEntity.ok(response);
        } catch (HttpException.NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (HttpException.BadRequestException e) {
            return ResponseEntity.badRequest().body(new FailureInfo(e.getMessage()));
        }
    }

    @GetMapping("/")
    @Operation(summary = "Get profile", description = "Get someone else's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class)))
    })
    public ResponseEntity<?> getUser(Authentication authentication, @ModelAttribute GetUserRequest getUserRequest) {
        try {
            UserResponse response = userService.getUser(getUserRequest, getUserFromAuthentication(authentication));
            return ResponseEntity.ok(UserResponse.toPublic(response));
        } catch (HttpException.NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (HttpException.BadRequestException e) {
            return ResponseEntity.badRequest().body(new FailureInfo(e.getMessage()));
        }
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Leaderboard", description = "Allows you to get the leaderboard.")
    public ResponseEntity<GetLeaderboardResponse> getLeaderboard(@RequestParam Integer limit) {
        List<User> users = userService.getLeaderboard(limit);
        List<GetLeaderboardResponse.ProfileScore> leaderboard = users.stream()
                .map(user -> new GetLeaderboardResponse.ProfileScore(user.getUsername(), user.getPokerscore()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new GetLeaderboardResponse(leaderboard));
    }
}