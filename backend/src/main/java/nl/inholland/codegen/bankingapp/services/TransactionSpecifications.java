package nl.inholland.codegen.bankingapp.services;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import nl.inholland.codegen.bankingapp.models.Transaction;

public final class TransactionSpecifications {

    private TransactionSpecifications() {}

    public static Specification<Transaction> ownerIs(long userId) {
        return (root, query, cb) -> cb.or(
            cb.equal(root.get("senderAccount").get("owner").get("userId"), userId),
            cb.equal(root.get("receiverAccount").get("owner").get("userId"), userId));
    }

    public static Specification<Transaction> timestampOnOrAfter(LocalDateTime start) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), start);
    }

    public static Specification<Transaction> timestampBefore(LocalDateTime end) {
        return (root, query, cb) -> cb.lessThan(root.get("timestamp"), end);
    }

    public static Specification<Transaction> involvesIban(String iban) {
        return (root, query, cb) -> cb.or(
            cb.equal(root.get("senderAccount").get("iban"), iban),
            cb.equal(root.get("receiverAccount").get("iban"), iban));
    }
}
