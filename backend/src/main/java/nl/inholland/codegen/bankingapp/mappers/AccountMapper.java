package nl.inholland.codegen.bankingapp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import nl.inholland.codegen.bankingapp.dtos.AccountDetailResponse;
import nl.inholland.codegen.bankingapp.dtos.AccountSummaryResponse;
import nl.inholland.codegen.bankingapp.models.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountSummaryResponse toAccountSummaryResponse(Account account);

    @Mapping(target = "userId", source = "owner.userId")
    AccountDetailResponse toAccountDetailResponse(Account account);
}
