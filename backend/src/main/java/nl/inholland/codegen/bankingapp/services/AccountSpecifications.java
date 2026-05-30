package nl.inholland.codegen.bankingapp.services;

import org.springframework.data.jpa.domain.Specification;

import nl.inholland.codegen.bankingapp.models.Account;

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

    public static Specification<Account> isCheckingAccount() {
        return (root, query, cb) -> cb.equal(root.get("accountType"), Account.AccountType.Checking);
    }
}
