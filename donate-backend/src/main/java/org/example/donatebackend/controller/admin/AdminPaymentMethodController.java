package org.example.donatebackend.controller.admin;

import org.example.donatebackend.dto.request.PaymentMethodRequest;
import org.example.donatebackend.dto.response.AdminPaymentMethodResponse;
import org.example.donatebackend.service.SystemPaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payment-methods")
public class AdminPaymentMethodController {

    @Autowired
    private SystemPaymentMethodService systemPaymentMethodService;

    @GetMapping
    public List<AdminPaymentMethodResponse> getAll() {
        return systemPaymentMethodService.adminFindAll();
    }

    @PostMapping
    public AdminPaymentMethodResponse create(@RequestBody PaymentMethodRequest req) {
        return systemPaymentMethodService.adminCreate(req);
    }

    @PutMapping("/{id}")
    public AdminPaymentMethodResponse update(@PathVariable Long id, @RequestBody PaymentMethodRequest req) {
        return systemPaymentMethodService.adminUpdate(id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        systemPaymentMethodService.adminDelete(id);
        return Map.of("message", "Delete payment method successfully");
    }
}
