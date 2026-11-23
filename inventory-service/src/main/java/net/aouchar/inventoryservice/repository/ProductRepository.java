package net.aouchar.inventoryservice.repository;

import net.aouchar.inventoryservice.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Utilisée par l'outil MCP searchProductsByName
    List<Product> findByNameContainsIgnoreCase(String keyword);
}
