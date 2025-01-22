package dev.codeclub.hillock.controller;

import dev.codeclub.hillock.annotations.NoAuth;
import dev.codeclub.hillock.database.model.Invite;
import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.database.service.InviteService;
import dev.codeclub.hillock.enums.Role;
import dev.codeclub.hillock.http.HttpException;
import dev.codeclub.hillock.http.model.CreateInviteRequest;
import dev.codeclub.hillock.http.model.FailureInfo;
import dev.codeclub.hillock.http.model.UpdateInviteRequest;
import dev.codeclub.hillock.http.model.InviteResponse;
import dev.codeclub.hillock.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@SecurityRequirement(name = "Authentication Token")
@RequestMapping("/api/invites")
public class InviteController {

    private final InviteService inviteService;

    @Autowired
    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @Operation(summary = "Create a new invite", description = "Creates a new invite and returns the created invite object.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Invite created successfully"),
            @ApiResponse(responseCode = "400", description = "User not found; you have to link your discord account first to create an invite.")
    })
    @PostMapping
    //@PreAuthorize("@userDetailsService.isAtLeastInRole('ADMIN')")
    @NoAuth
    public ResponseEntity<?> createInvite(Authentication authentication, @RequestBody CreateInviteRequest createInviteRequest) {
        try {
            User user = null;
            if (authentication != null) {
                user = ((CustomUserDetails)authentication.getPrincipal()).getUser();
            }
            InviteResponse inviteResponse = inviteService.createInvite(createInviteRequest.invite(), user);
            return new ResponseEntity<>(inviteResponse, HttpStatus.CREATED);
        } catch (HttpException.BadRequestException e) {
            return ResponseEntity.badRequest().body(new FailureInfo(e.getMessage()));
        }
    }

    @Operation(summary = "Get invite by id", description = "Returns the invite object for the specified id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite found"),
            @ApiResponse(responseCode = "404", description = "Invite not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@userDetailsService.isAtLeastInRole('ADMIN')")
    public ResponseEntity<?> getInviteById(Authentication authentication, @PathVariable Long id) {
        User user = ((CustomUserDetails)authentication.getPrincipal()).getUser();
        Optional<Invite> invite = inviteService.getInviteById(id);
        return invite
                .map(dbInvite -> ResponseEntity.ok(InviteResponse.FromDbInvite(dbInvite)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get invite by code", description = "Returns the invite object for the specified invite code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite found"),
            @ApiResponse(responseCode = "404", description = "Invite not found")
    })
    @GetMapping("/code/{inviteCode}")
    public ResponseEntity<?> getInviteByCode(Authentication authentication, @PathVariable String inviteCode) {
        User user = ((CustomUserDetails)authentication.getPrincipal()).getUser();
        Optional<Invite> invite = inviteService.getInviteByCode(inviteCode);
        return invite
                .map(dbInvite -> ResponseEntity.ok(InviteResponse.FromDbInvite(dbInvite)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all invites", description = "Returns a list of all invites.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invites found")
    })
    @GetMapping
    @PreAuthorize("@userDetailsService.isAtLeastInRole('ADMIN')")
    public ResponseEntity<?> getAllInvites() {
        List<Invite> invites = inviteService.getAllInvites();
        List<InviteResponse> inviteResponses = invites.stream()
                .map(InviteResponse::FromDbInvite)
                .toList();
        return ResponseEntity.ok(inviteResponses);
    }

    @Operation(summary = "Update an invite", description = "Updates an existing invite and returns the updated invite.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite updated successfully"),
            @ApiResponse(responseCode = "404", description = "Invite not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@userDetailsService.isAtLeastInRole('ADMIN')")
    public ResponseEntity<?> updateInvite(Authentication authentication, @PathVariable Long id, @RequestBody UpdateInviteRequest updateInviteRequest) {
        Invite inviteToUpdate = updateInviteRequest.toDbInvite();
        Invite updatedInvite = inviteService.updateInvite(id, inviteToUpdate);
        if (updatedInvite != null) {
            InviteResponse response = InviteResponse.FromDbInvite(updatedInvite);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete an invite", description = "Deletes an invite by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Invite deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Invite not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvite(@RequestAttribute("user") User user, @PathVariable Long id) {
        if (!user.isAtLeastInRole(Role.ADMIN)) {
            return ResponseEntity.badRequest().body(new FailureInfo("You do not have permission to use this endpoint."));
        }
        return inviteService.deleteInvite(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
