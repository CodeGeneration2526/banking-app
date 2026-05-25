package nl.inholland.codegen.bankingapp.utils;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import nl.inholland.codegen.bankingapp.models.User;

@Component
public class GetAuthUser {
    public Optional<User> getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return Optional.empty();
        }

        User user = (User)authentication.getPrincipal();
        return Optional.of(user);
    }
}
