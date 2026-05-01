package nl.inholland.codegen.bankingapp.repositories;

import nl.inholland.codegen.bankingapp.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}