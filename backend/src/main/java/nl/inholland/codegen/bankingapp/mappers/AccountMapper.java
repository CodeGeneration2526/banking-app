package nl.inholland.codegen.bankingapp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import nl.inholland.codegen.bankingapp.dtos.AccountDetailResponse;
import nl.inholland.codegen.bankingapp.dtos.AccountSummaryResponse;
import nl.inholland.codegen.bankingapp.dtos.NewAccountRequest;
import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.User;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountSummaryResponse toAccountSummaryResponse(Account account);

    @Mapping(target = "userId", source = "owner.userId")
    AccountDetailResponse toAccountDetailResponse(Account account);

    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "storedAmountInCents", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "iban", ignore = true)
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "closed", source = "newAccountRequest.closed")
    Account toModel(NewAccountRequest newAccountRequest, User owner);
}
