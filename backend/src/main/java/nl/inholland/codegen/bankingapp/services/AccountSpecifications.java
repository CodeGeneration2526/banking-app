package nl.inholland.codegen.bankingapp.services;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.User;

public final class AccountSpecifications {

    private AccountSpecifications() {}

    public static Specification<Account> firstNameContains(String firstName) {
        return (root, query, cb) -> firstName == null || firstName.isBlank()
            ? cb.conjunction()
            : cb.like(cb.lower(root.get("owner").get("firstName")), "%" + firstName.toLowerCase() + "%");
    }

    public static Specification<Account> lastNameContains(String lastName) {
        return (root, query, cb) -> lastName == null || lastName.isBlank()
            ? cb.conjunction()
            : cb.like(cb.lower(root.get("owner").get("lastName")), "%" + lastName.toLowerCase() + "%");
    }

    public static Specification<Account> ibanEquals(String iban) {
        return (root, query, cb) -> iban == null || iban.isBlank()
            ? cb.conjunction()
            : cb.equal(root.get("iban"), iban);
    }

    public static Specification<Account> visibleTo(User user) {
        return (root, query, cb) -> {
            if (user.getRole() == User.Role.Employee) {
                return cb.conjunction(); // no restriction
            }

            Predicate checkingAccounts = cb.equal(root.get("accountType"), Account.AccountType.Checking);

            Predicate ownSavingsAccounts = cb.and(
                    cb.equal(root.get("accountType"), Account.AccountType.Savings),
                    cb.equal(root.get("owner").get("id"), user.getUserId())
            );

            return cb.or(checkingAccounts, ownSavingsAccounts);
        };
    }

    public static Specification<Account> accountTypeEquals(Account.AccountType accountType) {
        return (root, query, cb) -> accountType == null
            ? cb.conjunction()
            : cb.equal(root.get("accountType"), accountType);
    }


    public static Specification<Account> ownerUserId(Long ownerUserId) {
        return (root, query, cb) -> ownerUserId == null
            ? cb.conjunction()
            : cb.equal(root.get("owner").get("userId"), ownerUserId);
    }
}
