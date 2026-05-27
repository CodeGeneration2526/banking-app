package nl.inholland.codegen.bankingapp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import nl.inholland.codegen.bankingapp.dtos.TransactionResponse;
import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "from",        expression = "java(identifier(transaction.getSenderAccount()))")
    @Mapping(target = "to",          expression = "java(identifier(transaction.getReceiverAccount()))")
    @Mapping(target = "initiatedBy", source = "initiatedBy.email")
    TransactionResponse toTransactionResponse(Transaction transaction);

    default String identifier(Account account) {
        //Only return the IBAN in the response if it's a checkings account, else return the regular number (savings account)
        if(account.getIban() != null) {
            return account.getIban();
        } else {
            return String.valueOf(account.getAccountNumber());
        }
    }
}
