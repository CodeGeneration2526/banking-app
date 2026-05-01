package nl.inholland.codegen.bankingapp.models;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    CUSTOMER,
    EMPLOYEE;

    @Override
    public String getAuthority() {
        return name();
    }
}