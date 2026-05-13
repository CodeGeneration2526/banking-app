package nl.inholland.codegen.bankingapp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import nl.inholland.codegen.bankingapp.dtos.AccountDetailResponse;
import nl.inholland.codegen.bankingapp.dtos.AccountSummaryResponse;
import nl.inholland.codegen.bankingapp.models.Account;

@Mapper
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    AccountDetailResponse toAccountDetailResponse(Account account);

    @Mapping(target = "accountType", expression = "java(account.getType().name())")
    AccountSummaryResponse toAccountSummaryResponse(Account account);
}
