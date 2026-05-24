package nl.inholland.codegen.bankingapp.policies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authorization.AuthorizationDeniedException;

import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.models.User;

import static org.junit.jupiter.api.Assertions.*;

class ApproveUsersPolicyTest {
    private ApproveUsersPolicy approveUsersPolicy;
    private User nonEmployeeUser;
    private User employeeUser;

    @BeforeEach
    void setUp() {
        approveUsersPolicy = new ApproveUsersPolicy();

        nonEmployeeUser = new User();
        nonEmployeeUser.setRole(User.Role.Customer);

        employeeUser = new User();
        employeeUser.setRole(User.Role.Employee);
    }

    @Test
    void enforceApproverIsEmployee_throwsWithNonEmployeeAprover() {
        assertThrows(AuthorizationDeniedException.class,
                () -> approveUsersPolicy.enforceApproverIsEmployee(nonEmployeeUser));
    }

    @Test
    void enforceApproverIsEmployee_successWithNonEmployeeAprover() {
        assertDoesNotThrow(() -> approveUsersPolicy.enforceApproverIsEmployee(employeeUser));
    }


    @Test
    void enforceUserIsNotClosed_throwsWithClosedUser() {
        nonEmployeeUser.setClosed(true);

        assertThrows(BadRequestException.class,
                () -> approveUsersPolicy.enforceUserIsNotClosed(nonEmployeeUser));
    }

    @Test
    void enforceUserIsNotClosed_successWithNonClosedUser() {
        nonEmployeeUser.setClosed(false);

        assertDoesNotThrow(() -> approveUsersPolicy.enforceUserIsNotClosed(nonEmployeeUser));
    }

    @Test
    void enforceUserIsNotApproved_throwsWithApprovedUser() {
        nonEmployeeUser.setApprovedBy(employeeUser);

        assertThrows(BadRequestException.class,
                () -> approveUsersPolicy.enforceUserIsNotApproved(nonEmployeeUser));
    }

    @Test
    void enforceUserIsApproved_successWithNonApprovedUser() {
        nonEmployeeUser.setApprovedBy(null);

        assertDoesNotThrow(() -> approveUsersPolicy.enforceUserIsNotApproved(nonEmployeeUser));
    }
}
