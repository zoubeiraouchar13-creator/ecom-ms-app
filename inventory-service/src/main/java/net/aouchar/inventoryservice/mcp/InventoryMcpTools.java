package net.aouchar.inventoryservice.mcp;

import net.aouchar.inventoryservice.entities.Product;
import net.aouchar.inventoryservice.repository.ProductRepository;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Q9 - Outils MCP exposés par inventory-service (endpoint http://localhost:8082/mcp).
 */
@Component
public class InventoryMcpTools {

    private final ProductRepository productRepository;

    public InventoryMcpTools(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @McpTool(name = "getAllProducts",
            description = "Retourne la liste de tous les produits (id, name, price, quantity en stock)")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @McpTool(name = "getProductById",
            description = "Retourne un produit à partir de son identifiant")
    public Product getProductById(@McpToolParam(description = "Identifiant du produit") Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable avec l'id " + id));
    }

    @McpTool(name = "searchProductsByName",
            description = "Recherche les produits dont le nom contient le mot-clé (insensible à la casse)")
    public List<Product> searchProductsByName(@McpToolParam(description = "Mot-clé à chercher dans le nom du produit") String keyword) {
        return productRepository.findByNameContainsIgnoreCase(keyword);
    }

    @McpTool(name = "getLowStockProducts",
            description = "Retourne les produits dont la quantité en stock est inférieure ou égale au seuil donné")
    public List<Product> getLowStockProducts(@McpToolParam(description = "Seuil de quantité") int threshold) {
        return productRepository.findAll().stream()
                .filter(p -> p.getQuantity() <= threshold)
                .toList();
    }
}
