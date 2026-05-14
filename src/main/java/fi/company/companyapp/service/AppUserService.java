package fi.company.companyapp.service;

import fi.company.companyapp.entity.AppUser;
import fi.company.companyapp.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    public AppUser register(String username, String email, String rawPassword,
                            String firstName, String lastName, Set<String> roles) {
        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoles(roles);
        user.setEnabled(true);
        AppUser saved = appUserRepository.save(user);

        // Notify admin on new user registration
        emailService.notifyAdminNewUser(saved);

        return saved;
    }

    public AppUser save(AppUser user) {
        return appUserRepository.save(user);
    }

    public void delete(AppUser user) {
        appUserRepository.delete(user);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        return appUserRepository.findByUsername(username).map(user -> {
            if (passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                appUserRepository.save(user);
                return true;
            }
            return false;
        }).orElse(false);
    }

    public void resetPasswordByEmail(String email, String newPassword) {
        appUserRepository.findByEmail(email).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            appUserRepository.save(user);
        });
    }

    public AppUser findOrCreateOAuthUser(String provider, String providerId,
                                         String email, String name) {
        return appUserRepository.findByOauthProviderAndOauthProviderId(provider, providerId)
            .orElseGet(() -> {
                AppUser user = new AppUser();
                user.setOauthProvider(provider);
                user.setOauthProviderId(providerId);
                user.setEmail(email);
                String baseUsername = email != null ? email.split("@")[0] : providerId;
                user.setUsername(baseUsername);
                user.setFirstName(name != null ? name.split(" ")[0] : "");
                user.setLastName(name != null && name.contains(" ") ? name.split(" ")[1] : "");
                user.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                user.setRoles(Set.of("ROLE_USER"));
                user.setEnabled(true);
                return appUserRepository.save(user);
            });
    }
}
