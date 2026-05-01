package nl.inholland.codegen.bankingapp.dtos;

import nl.inholland.codegen.bankingapp.models.Role;

public class UserResponseDTO {

    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;

    public UserResponseDTO(Long userId, String firstName, String lastName, String email, Role role) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
}