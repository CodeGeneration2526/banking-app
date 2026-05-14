package nl.inholland.codegen.bankingapp.mappers;

import org.mapstruct.Mapper;

import nl.inholland.codegen.bankingapp.dtos.UserResponse;
import nl.inholland.codegen.bankingapp.models.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    public UserResponse toUserResponse(User user);
}
