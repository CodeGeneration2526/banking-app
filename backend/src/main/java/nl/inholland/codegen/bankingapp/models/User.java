package nl.inholland.codegen.bankingapp.models;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
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
    private int phoneNumber;

    @Column(nullable = false, unique = true)
    private int bsn;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)
    private Date registrationDate = new Date();

    @ManyToOne
    @JoinColumn(name = "user_approved_by")
    private User approvedBy;

    @Column(nullable = false)
    private Role role = Role.Customer;

    @Column(nullable = false)
    private boolean closed = false;

    public enum Role {
        Customer,
        Employee,
    }
}
