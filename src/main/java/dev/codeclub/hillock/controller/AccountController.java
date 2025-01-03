package dev.codeclub.hillock.controller;

import dev.codeclub.hillock.annotations.NoAuth;
import dev.codeclub.hillock.database.model.Invite;
import dev.codeclub.hillock.database.service.AuthenticationService;
import dev.codeclub.hillock.database.service.UserService;
import dev.codeclub.hillock.http.HttpException;
import dev.codeclub.hillock.http.model.*;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.servlet.function.ServerResponse.badRequest;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AuthenticationService authenticationServicee;

    public AccountController(AuthenticationService authenticationService) {
        this.authenticationServicee = authenticationService;
    }
    @PostMapping("/create")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Udało się stworzyć konto", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nie udało się utworzyć konta", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class)))
    })
    @NoAuth
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest accountRequest){
        try {
            CreateAccountResponse createAccountResponse = authenticationServicee.createAccount(accountRequest);
            return new ResponseEntity<>(createAccountResponse, HttpStatus.CREATED);
        } catch (HttpException.BadRequestException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new FailureInfo(e.getMessage()));
        }
    }

    @PostMapping("/login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Poprawne zalogowanie", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Błędne logowanie", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FailureInfo.class)))
    })
    @NoAuth
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        try {
            LoginResponse loginResponse = authenticationServicee.login(loginRequest);
            return new ResponseEntity<>(loginResponse, HttpStatus.OK);
        } catch (HttpException.BadRequestException e) {
            return ResponseEntity.badRequest().body(new FailureInfo(e.getMessage()));
        }
    }

    @NoAuth
    @Hidden
    @GetMapping("/verify/{namespace}/{token}")
    public ResponseEntity<?> verify(@PathVariable String namespace, @PathVariable String token){
        try {
            VerifyResponse verifyResponse = authenticationServicee.verify(namespace, token);
            return new ResponseEntity<>(verifyResponse, HttpStatus.OK);
        } catch (HttpException.BadRequestException e) {
            return ResponseEntity.badRequest().body(new FailureInfo(e.getMessage()));
        }
    }


}
