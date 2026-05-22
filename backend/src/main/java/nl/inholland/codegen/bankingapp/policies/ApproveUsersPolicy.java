package nl.inholland.codegen.bankingapp.policies;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;

import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.models.User;

@Component
public class ApproveUsersPolicy {
    public void enforceApproveUsersPolicy(User user, User approver)
            throws BadRequestException, AuthorizationDeniedException
    {
        enforceApproverIsEmployee(approver);
        enforceUserIsNotClosed(user);
        enforceUserIsApproved(user);
    }

    public void enforceApproverIsEmployee(User approver) throws AuthorizationDeniedException {
        if (approver.getRole() != User.Role.Employee) {
            throw new AuthorizationDeniedException("Only employees can approve other users");
        }
    }

    public void enforceUserIsNotClosed(User user) throws BadRequestException {
        if (user.isClosed()) {
            throw new BadRequestException("Cannot approve a closed account");
        }
    }

    public void enforceUserIsApproved(User user) throws BadRequestException {
        if (user.getApprovedBy() != null) {
            throw new BadRequestException("Customer already approved");
        }
    }
}
