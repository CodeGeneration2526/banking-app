package nl.inholland.codegen.bankingapp.repositories;

import nl.inholland.codegen.bankingapp.models.Customer;
import nl.inholland.codegen.bankingapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser(User user);
}
