package com.alantonic.backend_paypal.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paypal")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class PayPalController {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    private static final String API_BASE = "https://api.sandbox.paypal.com";

    private String getAccessToken() throws Exception {
        String url = API_BASE + "/v1/oauth2/token";
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + encoded)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Fallo autenticación PayPal");
        }

        String body = response.body();
        int start = body.indexOf("\"access_token\":\"") + 16;
        int end = body.indexOf("\"", start);
        return body.substring(start, end);
    }

    @PostMapping("/create-order")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            String nombre = (String) payload.get("nombre");
            Double precio = ((Number) payload.get("precio")).doubleValue();

            String token = getAccessToken();
            String json = "{"
                    + "\"intent\":\"CAPTURE\","
                    + "\"purchase_units\":[{"
                    + "\"description\":\"" + nombre + "\","
                    + "\"amount\":{\"currency_code\":\"USD\",\"value\":\"" + String.format("%.2f", precio) + "\"}"
                    + "}],"
                    + "\"application_context\":{"
                    + "\"return_url\":\"http://127.0.0.1:5500/success.html\","
                    + "\"cancel_url\":\"http://127.0.0.1:5500/elements.html\""
                    + "}"
                    + "}";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/v2/checkout/orders"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 201) {
                throw new RuntimeException("Error PayPal: " + res.body());
            }

            String body = res.body();
            int s = body.indexOf("\"id\":\"") + 6;
            int e = body.indexOf("\"", s);
            String orderId = body.substring(s, e);

            return ResponseEntity.ok(Map.of("orderId", orderId));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }
}