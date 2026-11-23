package net.aouchar.customerservice.repository;

import net.aouchar.customerservice.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Utilisée par l'outil MCP searchCustomersByName
    List<Customer> findByNameContainsIgnoreCase(String keyword);
}
