package dev.codeclub.hillock.database.repository;

import dev.codeclub.hillock.database.model.UnauthorizedAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface UnathorizedAttemptRepository extends JpaRepository<UnauthorizedAttempt, Long> {
}