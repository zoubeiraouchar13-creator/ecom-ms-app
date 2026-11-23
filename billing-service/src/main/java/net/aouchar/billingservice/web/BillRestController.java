package net.aouchar.billingservice.web;

import net.aouchar.billingservice.entities.Bill;
import net.aouchar.billingservice.service.BillService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BillRestController {

    private final BillService billService;

    public BillRestController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/bills/{id}")
    public Bill getBillById(@PathVariable Long id){
        return billService.getFullBill(id);
    }

    // Q11 - utilisés par le client Angular (factures complètes avec client et produits)
    @GetMapping("/fullBills")
    public List<Bill> getAllFullBills(){
        return billService.getAllFullBills();
    }

    @GetMapping("/fullBills/{id}")
    public Bill getFullBill(@PathVariable Long id){
        return billService.getFullBill(id);
    }
}
