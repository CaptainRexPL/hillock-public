package dev.codeclub.hillock.database.repository;

import dev.codeclub.hillock.database.model.RefreshToken;
import dev.codeclub.hillock.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>, JpaSpecificationExecutor<RefreshToken> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    void deleteByUser(User user);

    void deleteAllByExpiresAtBefore(LocalDateTime now);
}
