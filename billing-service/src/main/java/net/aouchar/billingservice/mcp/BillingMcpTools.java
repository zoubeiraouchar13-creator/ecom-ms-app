package net.aouchar.billingservice.mcp;

import net.aouchar.billingservice.service.BillService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillingMcpTools {

    private final BillService billService;

    public BillingMcpTools(BillService billService) {
        this.billService = billService;
    }

    @McpTool(name = "getAllBills",
            description = "Retourne toutes les factures (client, produits, montants et total en MAD déjà calculés)")
    public List<BillView> getAllBills() {
        return billService.getAllFullBills().stream().map(BillView::of).toList();
    }

    @McpTool(name = "getBillById",
            description = "Retourne une facture (date, client, produits, quantités, prix, total en MAD) à partir de son identifiant")
    public BillView getBillById(@McpToolParam(description = "Identifiant de la facture") Long id) {
        return BillView.of(billService.getFullBill(id));
    }

    @McpTool(name = "getBillsByCustomerId",
            description = "Retourne uniquement les factures du client dont l'identifiant est donné (montants en MAD)")
    public List<BillView> getBillsByCustomerId(@McpToolParam(description = "Identifiant du client") Long customerId) {
        return billService.getFullBillsByCustomer(customerId).stream().map(BillView::of).toList();
    }

    @McpTool(name = "getBillTotal",
            description = "Retourne le montant total d'une facture, en dirhams marocains (MAD)")
    public double getBillTotal(@McpToolParam(description = "Identifiant de la facture") Long id) {
        return BillView.of(billService.getFullBill(id)).total();
    }
}
