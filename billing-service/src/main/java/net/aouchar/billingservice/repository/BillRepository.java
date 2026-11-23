package net.aouchar.billingservice.repository;

import net.aouchar.billingservice.entities.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface BillRepository extends JpaRepository<Bill,Long> {
    // Utilisée par l'outil MCP getBillsByCustomerId
    List<Bill> findByCustomerId(Long customerId);
}
