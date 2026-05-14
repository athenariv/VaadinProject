package fi.company.companyapp.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import lombok.*;

import java.util.Set;

/**
 * AppUser entity with roles for authentication
 */
@Entity
@Table(name = "app_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(exclude = "passwordHash")
public class AppUser extends BaseEntity {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Email is required")
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @Column(nullable = false)
    private boolean enabled = true;

    // Profile image stored as file path
    private String profileImagePath;

    // OAuth2 provider info
    private String oauthProvider;
    private String oauthProviderId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles;
}
