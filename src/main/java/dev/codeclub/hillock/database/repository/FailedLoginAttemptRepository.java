package dev.codeclub.hillock.database.repository;

import dev.codeclub.hillock.database.model.FailedLoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface FailedLoginAttemptRepository extends JpaRepository<FailedLoginAttempt, Long> {
}