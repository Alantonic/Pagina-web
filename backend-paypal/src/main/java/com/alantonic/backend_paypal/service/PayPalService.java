package com.alantonic.backend_paypal.service;

import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alantonic.backend_paypal.config.PayPalConfig;
import com.alantonic.backend_paypal.model.Pago;
import com.alantonic.backend_paypal.repository.PagoRepository;

@Service
public class PayPalService {

    @Autowired
    private PayPalConfig payPalConfig;

    @Autowired
    private PagoRepository pagoRepository;

    private String getBaseUrl() {
        if ("live".equalsIgnoreCase(payPalConfig.mode)) {
            return "https://api.paypal.com";
        } else {
            return "https://api.sandbox.paypal.com";
        }
    }

    private String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();

        String auth = payPalConfig.clientId + ":" + payPalConfig.clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);
        String url = getBaseUrl() + "/v1/oauth2/token";

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return (String) response.getBody().get("access_token");
    }

    public Map<String, Object> createOrder(String currency, String description, double amount) {
        RestTemplate restTemplate = new RestTemplate();
        String accessToken = getAccessToken();
        String url = getBaseUrl() + "/v2/checkout/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
            {
              "intent": "CAPTURE",
              "purchase_units": [
                {
                  "description": "%s",
                  "amount": {
                    "currency_code": "%s",
                    "value": "%.2f"
                  }
                }
              ]
            }
            """.formatted(description, currency, amount);

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return response.getBody();
    }

    public Map<String, Object> captureOrder(String orderId) {
        RestTemplate restTemplate = new RestTemplate();
        String accessToken = getAccessToken();
        String url = getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("", headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> result = response.getBody();

        // Guardar en base de datos si el pago fue completado
        if ("COMPLETED".equals(result.get("status"))) {
            try {
                // Extraer datos del resultado
                String payerEmail = "N/A";
                Double amount = 0.0;
                String currency = "USD";

                // Payer email
                Map<String, Object> payer = (Map<String, Object>) result.get("payer");
                if (payer != null && payer.containsKey("email_address")) {
                    payerEmail = (String) payer.get("email_address");
                }

                // Monto y moneda
                java.util.List<?> purchaseUnits = (java.util.List<?>) result.get("purchase_units");
                if (!purchaseUnits.isEmpty()) {
                    Map<String, Object> unit = (Map<String, Object>) purchaseUnits.get(0);
                    Map<String, Object> amountObj = (Map<String, Object>) unit.get("amount");
                    amount = Double.valueOf((String) amountObj.get("value"));
                    currency = (String) amountObj.get("currency_code");
                }

                // Guardar en BD
                Pago pago = new Pago();
                pago.setOrderId(orderId);
                pago.setPayerEmail(payerEmail);
                pago.setStatus((String) result.get("status"));
                pago.setAmount(amount);
                pago.setCurrency(currency);
                pagoRepository.save(pago);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}