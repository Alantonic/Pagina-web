package com.alantonic.backend_paypal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayPalConfig {

    @Value("${paypal.client.id}")
    public String clientId;

    @Value("${paypal.client.secret}")
    public String clientSecret;

    @Value("${paypal.mode:sandbox}")
    public String mode;
}