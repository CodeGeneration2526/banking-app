package nl.inholland.codegen.bankingapp.models;

import jakarta.persistence.*;
import java.util.Date;

import lombok.*;

@Entity
@Table(name = "accounts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Account {
    public static Long DEFAULT_DAILY_LIMIT = 5_000L * 100L; // 5000.00 EUR
    public static Long DEFAULT_ABSOLUTE_LIMIT = 0L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(nullable = false, unique = true, updatable = false)
    private Long accountNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(nullable = false)
    @Builder.Default
    private Long storedAmountInCents = 0L; // starting balance will always be 0

    @Column(nullable = false)
    @Builder.Default
    private Long dailyLimitInCents = DEFAULT_DAILY_LIMIT;

    @Column(nullable = false)
    @Builder.Default
    private Long absoluteLimitInCents = DEFAULT_ABSOLUTE_LIMIT;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Date creationDate = new Date();

    @Column(nullable = true, unique = true, updatable = false)
    private String iban;

    @ManyToOne
    private User owner;

    @Column(nullable = false)
    @Builder.Default
    private Boolean closed = false;

    public static enum AccountType {
        Checking,
        Savings,
        Atm
    }
}

