package net.aouchar.billingservice.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.aouchar.billingservice.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="inventory-service")
public interface InventoryServiceRestClient {
    @GetMapping("/api/products/{id}")
    @CircuitBreaker(name="inv-service", fallbackMethod = "getDefaultProduct")
    Product getProduct(@PathVariable Long id);
    default Product getDefaultProduct(Long id, Exception e){
        return Product.builder().id(id).build();
    }
}
