package com.alantonic.backend_paypal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alantonic.backend_paypal.model.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
}