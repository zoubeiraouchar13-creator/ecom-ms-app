package net.aouchar.billingservice.mcp;

import net.aouchar.billingservice.entities.Bill;
import java.util.Date;
import java.util.List;

public record BillView(Long billId, Date billingDate, Long customerId, String customerName,
                       List<Line> lines, double total, String currency) {

    public record Line(Long productId, String productName, int quantity, double unitPrice, double lineTotal) {}

    public static BillView of(Bill bill) {
        List<Line> lines = bill.getProductItems().stream()
                .map(pi -> new Line(
                        pi.getProductId(),
                        pi.getProduct() != null && pi.getProduct().getName() != null
                                ? pi.getProduct().getName() : "Produit " + pi.getProductId(),
                        pi.getQuantity(),
                        round(pi.getPrice()),
                        round(pi.getPrice() * pi.getQuantity())))
                .toList();
        double total = round(lines.stream().mapToDouble(Line::lineTotal).sum());
        String customerName = bill.getCustomer() != null ? bill.getCustomer().getName() : null;
        return new BillView(bill.getId(), bill.getBillingDate(), bill.getCustomerId(),
                customerName, lines, total, "MAD");
    }

    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }
}