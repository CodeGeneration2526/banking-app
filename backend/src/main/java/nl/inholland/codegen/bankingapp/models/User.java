package nl.inholland.codegen.bankingapp.models;

import java.util.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
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

    public enum Role {
        Customer,
        Employee,
    }
}
