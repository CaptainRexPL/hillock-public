package dev.codeclub.hillock.database.repository;

import dev.codeclub.hillock.database.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByDiscordid(Long discordid);

    @Query("SELECT COUNT(u) + 1 " +
            "FROM User u " +
            "WHERE u.pokerscore > (SELECT u2.pokerscore FROM User u2 WHERE u2.id = :userId)")
    Long getLeaderboardRank(@Param("userId") Long userId);

    @Query("SELECT u FROM User u ORDER BY u.pokerscore DESC, u.username ASC")
    List<User> getLeaderboard(Pageable pageable);
}