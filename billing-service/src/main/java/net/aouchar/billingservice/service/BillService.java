package net.aouchar.billingservice.service;

import net.aouchar.billingservice.entities.Bill;
import net.aouchar.billingservice.feign.CustomerServiceRestClient;
import net.aouchar.billingservice.feign.InventoryServiceRestClient;
import net.aouchar.billingservice.repository.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Construit des factures "complètes" (client + produits récupérés via OpenFeign).
 * Partagé par le contrôleur REST (client Angular) et les outils MCP (agent IA).
 * @Transactional permet de charger la collection lazy productItems
 * même quand l'appel ne vient pas d'une requête HTTP classique (cas des outils MCP).
 */
@Service
@Transactional(readOnly = true)
public class BillService {

    private final BillRepository billRepository;
    private final CustomerServiceRestClient customerServiceRestClient;
    private final InventoryServiceRestClient inventoryServiceRestClient;

    public BillService(BillRepository billRepository,
                       CustomerServiceRestClient customerServiceRestClient,
                       InventoryServiceRestClient inventoryServiceRestClient) {
        this.billRepository = billRepository;
        this.customerServiceRestClient = customerServiceRestClient;
        this.inventoryServiceRestClient = inventoryServiceRestClient;
    }

    public Bill getFullBill(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable avec l'id " + id));
        return enrich(bill);
    }

    public List<Bill> getAllFullBills() {
        return billRepository.findAll().stream().map(this::enrich).toList();
    }

    public List<Bill> getFullBillsByCustomer(Long customerId) {
        return billRepository.findByCustomerId(customerId).stream().map(this::enrich).toList();
    }

    public double computeTotal(Bill bill) {
        return bill.getProductItems().stream()
                .mapToDouble(pi -> pi.getPrice() * pi.getQuantity())
                .sum();
    }

    private Bill enrich(Bill bill) {
        bill.setCustomer(customerServiceRestClient.findCustomerById(bill.getCustomerId()));
        bill.getProductItems().forEach(pi ->
                pi.setProduct(inventoryServiceRestClient.getProduct(pi.getProductId())));
        return bill;
    }
}
