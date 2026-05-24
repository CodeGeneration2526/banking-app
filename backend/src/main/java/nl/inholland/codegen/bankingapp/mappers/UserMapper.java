package nl.inholland.codegen.bankingapp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import nl.inholland.codegen.bankingapp.dtos.RegisterRequest;
import nl.inholland.codegen.bankingapp.dtos.UserResponse;
import nl.inholland.codegen.bankingapp.models.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    public UserResponse toUserResponse(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "closed", ignore = true)
    public User toModel(RegisterRequest user);
}
