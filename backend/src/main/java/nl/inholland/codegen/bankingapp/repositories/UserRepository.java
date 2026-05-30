package nl.inholland.codegen.bankingapp.repositories;

import nl.inholland.codegen.bankingapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByBsn(int bsn);

    Page<User> findByRole(User.Role role, Pageable pageable);
    Page<User> findByRoleAndApprovedByIsNull(User.Role role, Pageable pageable);
    Page<User> findByRoleAndApprovedByIsNotNull(User.Role role, Pageable pageable);


    Page<User> findByApprovedByIsNull(Pageable pageable);
    Page<User> findByApprovedByIsNotNull(Pageable pageable);
}
