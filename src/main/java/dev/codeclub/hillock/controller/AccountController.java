package dev.codeclub.hillock.controller;

import dev.codeclub.hillock.annotations.NoAuth;
import dev.codeclub.hillock.database.service.AuthenticationService;
import dev.codeclub.hillock.http.HttpException;
import dev.codeclub.hillock.http.model.*;
import dev.codeclub.hillock.http.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private static Logger LOGGER = LogManager.getLogger(AccountController.class);

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    public AccountController(AuthenticationService authenticationService, RefreshTokenService refreshTokenService) {
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
    }

    @Operation(summary = "Create a new Hillock account", description = "Creates a new account.")
    @PostMapping("/create")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account created successfully", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Failed to create an account, check the error message for details", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class)))
    })
    @NoAuth
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest accountRequest){
        try {
            CreateAccountResponse createAccountResponse = authenticationService.createAccount(accountRequest);
            return new ResponseEntity<>(createAccountResponse, HttpStatus.CREATED);
        } catch (HttpException.BadRequestException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new FailureInfo(e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login to your Hillock account", description = "Logs in to your account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged in successfully", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "403", description = "Failed to log in", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class))),
            @ApiResponse(responseCode = "400", description = "Unexpected error, most likely due to invalid request body", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class)))
    })
    @NoAuth
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        try {
            LoginResponse loginResponse = authenticationService.login(loginRequest);
            return new ResponseEntity<>(loginResponse, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new FailureInfo("Invalid credentials"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new FailureInfo(e.getMessage()));
        }
    }

    @NoAuth
    @Hidden
    @GetMapping("/verify/{namespace}/{token}")
    public ResponseEntity<?> verify(@PathVariable String namespace, @PathVariable String token){
        try {
            VerifyResponse verifyResponse = authenticationService.verify(namespace, token);
            return new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        } catch (HttpException.BadRequestException e) {
            return ResponseEntity.badRequest().body(new FailureInfo(e.getMessage()));
        }
    }

    @Operation(summary = "refresh token", description = "Refreshes your token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refreshed token successfully", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "403", description = "Failed to refresh the token", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class)))
     })
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody String refreshToken) {
        LOGGER.info("Refreshing token: {}", refreshToken);
        AuthResponse response = refreshTokenService.generateRefreshToken(refreshToken);
        if (RefreshTokenService.TOKEN_EXPIRED.equals(response.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new FailureInfo("Invalid refresh token (expired)"));
        }
        if (RefreshTokenService.TOKEN_NOT_FOUND.equals(response.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new FailureInfo("Invalid refresh token (not found)"));
        }

       return ResponseEntity.ok(response);
    }
}
