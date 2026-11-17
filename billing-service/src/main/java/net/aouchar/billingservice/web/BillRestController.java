package net.aouchar.billingservice.web;

import net.aouchar.billingservice.entities.Bill;
import net.aouchar.billingservice.feign.CustomerServiceRestClient;
import net.aouchar.billingservice.feign.InventoryServiceRestClient;
import net.aouchar.billingservice.model.Customer;
import net.aouchar.billingservice.repository.BillRepository;
import net.aouchar.billingservice.repository.ProductItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BillRestController {
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private ProductItemRepository productItemRepository;
    @Autowired
    private CustomerServiceRestClient customerServiceRestClient;
    @Autowired
    private InventoryServiceRestClient inventoryServiceRestClient;

    @GetMapping("/bills/{id}")
    public Bill getBillById(@PathVariable Long id){
        Bill bill = billRepository.findById(id).get();
        Customer customer = customerServiceRestClient.findCustomerById(bill.getCustomerId());
        bill.setCustomer(customer);
        bill.getProductItems().forEach(pi ->{
            pi.setProduct(
                    inventoryServiceRestClient.getProduct(pi.getProductId()));
        });
        return bill;
    }
}
