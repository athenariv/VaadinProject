package fi.company.companyapp.repository;

import fi.company.companyapp.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByOauthProviderAndOauthProviderId(String provider, String providerId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
