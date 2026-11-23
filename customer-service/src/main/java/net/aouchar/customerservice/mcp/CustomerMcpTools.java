package net.aouchar.customerservice.mcp;

import net.aouchar.customerservice.entities.Customer;
import net.aouchar.customerservice.repository.CustomerRepository;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerMcpTools {

    private final CustomerRepository customerRepository;

    public CustomerMcpTools(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @McpTool(name = "getAllCustomers",
            description = "Retourne la liste de tous les clients (id, name, email)")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @McpTool(name = "getCustomerById",
            description = "Retourne un client à partir de son identifiant")
    public Customer getCustomerById(@McpToolParam(description = "Identifiant du client") Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable avec l'id " + id));
    }

    @McpTool(name = "searchCustomersByName",
            description = "Recherche les clients dont le nom contient le mot-clé (insensible à la casse)")
    public List<Customer> searchCustomersByName(@McpToolParam(description = "Mot-clé à chercher dans le nom") String keyword) {
        return customerRepository.findByNameContainsIgnoreCase(keyword);
    }

    @McpTool(name = "createCustomer",
            description = "Crée un nouveau client et retourne le client enregistré")
    public Customer createCustomer(@McpToolParam(description = "Nom du client") String name,
                                   @McpToolParam(description = "Email du client") String email) {
        return customerRepository.save(Customer.builder().name(name).email(email).build());
    }
}
