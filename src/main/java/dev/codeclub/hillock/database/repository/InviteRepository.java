package dev.codeclub.hillock.database.repository;

import dev.codeclub.hillock.database.model.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;
@RepositoryRestResource(exported = false)
public interface InviteRepository extends JpaRepository<Invite, Long> {
    Optional<Invite> findByInvite(String invite);

    Optional<Invite> findById(Long id);


}