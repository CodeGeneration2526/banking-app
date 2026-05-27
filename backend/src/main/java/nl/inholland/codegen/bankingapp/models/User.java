package nl.inholland.codegen.bankingapp.models;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    // NOTE: it will perhaps be more realistic for a banking system to use UUIDv4 as the pk
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String bsn;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Date registrationDate = new Date();

    @ManyToOne
    @JoinColumn(name = "user_approved_by")
    private User approvedBy;

    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.Customer;

    @Column(nullable = false)
    @Builder.Default
    private boolean closed = false;

    public enum Role implements GrantedAuthority {
        Customer,
        Employee;

		@Override
		public String getAuthority() {
            return "ROLE_" + name();
		}
    }

	@Override
	public Collection<Role> getAuthorities() {
        return List.of(role);
	}

	@Override
	public String getUsername() {
        return email;
	}
}
