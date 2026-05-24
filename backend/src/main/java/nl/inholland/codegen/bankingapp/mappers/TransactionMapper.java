package nl.inholland.codegen.bankingapp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import nl.inholland.codegen.bankingapp.dtos.TransactionResponse;
import nl.inholland.codegen.bankingapp.models.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "fromIban",    source = "senderAccount.iban")
    @Mapping(target = "toIban",      source = "receiverAccount.iban")
    @Mapping(target = "initiatedBy", source = "initiatedBy.email")
    TransactionResponse toTransactionResponse(Transaction transaction);
}
