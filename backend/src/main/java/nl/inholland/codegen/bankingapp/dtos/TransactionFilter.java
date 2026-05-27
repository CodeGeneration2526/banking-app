package nl.inholland.codegen.bankingapp.dtos;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import nl.inholland.codegen.bankingapp.models.Transaction;
import nl.inholland.codegen.bankingapp.services.TransactionSpecifications;

public record TransactionFilter(
    LocalDate dateFrom,
    LocalDate dateTo,
    Long accountNumber,
    Long amountInCents,
    TransactionSpecifications.AmountFilter amountFilter
) {
    public Specification<Transaction> toSpecification() {
        Specification<Transaction> spec = (root, query, cb) -> cb.conjunction();
        if (dateFrom != null)      spec = spec.and(TransactionSpecifications.timestampOnOrAfter(dateFrom.atStartOfDay()));
        if (dateTo != null)        spec = spec.and(TransactionSpecifications.timestampBefore(dateTo.plusDays(1).atStartOfDay()));
        if (accountNumber != null) spec = spec.and(TransactionSpecifications.involvesAccountNumber(accountNumber));
        if (amountInCents != null) spec = spec.and(TransactionSpecifications.amountCompare(amountInCents, amountFilter));
        return spec;
    }
}
