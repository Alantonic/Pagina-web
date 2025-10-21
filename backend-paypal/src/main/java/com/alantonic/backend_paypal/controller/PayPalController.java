package com.alantonic.backend_paypal.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alantonic.backend_paypal.service.PayPalService;

@RestController
@RequestMapping("/api/paypal")
@CrossOrigin(origins = {
    "http://127.0.0.1:5500",         // Live Server en VS Code
    "https://alantonic.github.io"    // Tu sitio en GitHub Pages (producción)
})
public class PayPalController {

    @Autowired
    private PayPalService payPalService;

    /**
     * Crea una orden de pago en PayPal.
     * Espera un JSON como:
     * {
     *   "amount": 15.00,
     *   "currency": "USD",
     *   "description": "Donación"
     * }
     */
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            String currency = (String) payload.get("currency");
            Double amount = ((Number) payload.get("amount")).doubleValue();
            String description = (String) payload.getOrDefault("description", "Compra en mi sitio");

            // Llama al servicio que hace la llamada HTTP a PayPal
            Map<String, Object> order = payPalService.createOrder(currency, description, amount);

            // Extrae el ID de la orden (viene en la respuesta de PayPal)
            String orderId = (String) order.get("id");
            return ResponseEntity.ok(Map.of("id", orderId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Captura (confirma) una orden de pago después de que el usuario la aprueba.
     */
    @PostMapping("/capture-order/{orderId}")
    public ResponseEntity<Map<String, Object>> captureOrder(@PathVariable String orderId) {
        try {
            // Llama al servicio para capturar el pago
            Map<String, Object> captureResult = payPalService.captureOrder(orderId);
            return ResponseEntity.ok(captureResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}