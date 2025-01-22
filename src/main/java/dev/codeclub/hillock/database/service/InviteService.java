package dev.codeclub.hillock.database.service;
import dev.codeclub.hillock.database.model.Invite;
import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.database.repository.InviteRepository;
import dev.codeclub.hillock.http.HttpException;
import dev.codeclub.hillock.http.model.InviteResponse;
import dev.codeclub.hillock.security.TokenCrypter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InviteService {

    private final InviteRepository inviteRepository;

    @Autowired
    public InviteService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    // CREATE
    public Invite createInvite(Invite invite) {
        return inviteRepository.save(invite);
    }

    public InviteResponse createInvite(String inviteCode, User user) {
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new HttpException.BadRequestException("Invite code missing");
        }
        if (getInviteByCode(inviteCode).isPresent()) {
            throw new HttpException.BadRequestException("Invite code already exists");
        }
        //TODO: remove this once the event is over
        if (user == null) {
            Invite invite = new Invite(inviteCode, -1L, false);
            createInvite(invite);
            return InviteResponse.FromDbInvite(invite);
        }
        if (user.getDiscordid() == null) {
            throw new HttpException.NotFoundException("User not found");
        }
        Invite invite = new Invite(inviteCode, user.getDiscordid(), false);
        createInvite(invite);
        return InviteResponse.FromDbInvite(invite);
    }

    public String createInviteCode(long discordUserId) {
        String code = TokenCrypter.inviteCode();
        Invite invite = new Invite(code, discordUserId, false);
        createInvite(invite);
        return code;
    }

    // READ (find one by id)
    public Optional<Invite> getInviteById(Long id) {
        return inviteRepository.findById(id);
    }

    // READ (find one by invite code)
    public Optional<Invite> getInviteByCode(String invite) {
        return inviteRepository.findByInvite(invite);
    }

    // READ (find all)
    public List<Invite> getAllInvites() {
        return inviteRepository.findAll();
    }

    // UPDATE
    public Invite updateInvite(Long id, Invite updatedInvite) {
        if (inviteRepository.existsById(id)) {
            updatedInvite.setId(id);  // Set the ID to the provided ID for the update
            return inviteRepository.save(updatedInvite);
        }
        return null;  // or throw an exception
    }

    public Invite redeemInvite(Long id) {
        if (inviteRepository.existsById(id)) {
            Invite invite = inviteRepository.findById(id).get();
            invite.setRedeemed(true);
            return inviteRepository.save(invite);
        }
        return null;
    }

    // DELETE
    public boolean deleteInvite(Long id) {
        if (inviteRepository.existsById(id)) {
            inviteRepository.deleteById(id);
            return true;
        }
        return false;  // or throw an exception
    }
}