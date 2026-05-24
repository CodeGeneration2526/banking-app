package nl.inholland.codegen.bankingapp.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(nullable = false, unique = true)
    private Long accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(nullable = false)
    private Long storedAmountInCents;

    @Column(nullable = false)
    private Long dailyLimitInCents;

    @Column(nullable = false)
    private Long absoluteLimitInCents;

    @Column(nullable = false)
    private LocalDateTime creationDate;

    @Column(nullable = false, unique = true)
    private String iban;

    @ManyToOne
    private User owner;

    @Column(nullable = false)
    private Boolean closed = false;

    public enum AccountType {
        Checking,
        Savings,
        Atm
    }
}

