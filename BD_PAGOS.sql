-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS pagos_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

-- Entrar a esa base de datos
USE pagos_db;

-- (Opcional) Mostrar en cuál estás
SELECT DATABASE();

-- Crear tabla "pagos"
CREATE TABLE pagos (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id VARCHAR(255) NOT NULL,
  payer_email VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  amount DOUBLE NOT NULL,
  currency VARCHAR(10) NOT NULL,
  fecha DATETIME NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Insertar un registro de prueba
INSERT INTO pagos (order_id, payer_email, status, amount, currency, fecha)
VALUES (
  '5O123456789012345',
  'comprador@test.com',
  'COMPLETED',
  15.0,
  'USD',
  '2025-10-21 14:30:00'
);

-- Cambiar tipo de dato 
ALTER TABLE pagos
MODIFY amount DECIMAL(10,2) NOT NULL;

-- Limitar moneda a 3 caracteres
ALTER TABLE pagos
MODIFY currency CHAR(3) NOT NULL;

-- Evitar ordenes duplicadas
ALTER TABLE pagos
ADD CONSTRAINT uk_order_id UNIQUE (order_id);

-- Controlar los valores
ALTER TABLE pagos
MODIFY status ENUM('CREATED','PENDING','COMPLETED','CANCELLED','REFUNDED') NOT NULL;

-- Hacer que la fecha se agregue automaticamente
ALTER TABLE pagos
MODIFY fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- total registros
SELECT COUNT(*) AS total_registros
FROM pagos;

-- total por estado
SELECT status, COUNT(*) AS cantidad
FROM pagos
GROUP BY status
ORDER BY cantidad DESC;

-- total dinero por moneda
SELECT currency, SUM(amount) AS total_pagado
FROM pagos
GROUP BY currency
ORDER BY total_pagado DESC;

-- total dinero por estatus
SELECT status, SUM(amount) AS total_pagado
FROM pagos
GROUP BY status;

-- pago del ultimo mes
SELECT * 
FROM pagos
WHERE fecha >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)
ORDER BY fecha DESC;

-- vista totales por estado
CREATE OR REPLACE VIEW vista_totales_por_estado AS
SELECT status, SUM(amount) AS total_pagado, COUNT(*) AS cantidad
FROM pagos
GROUP BY status;

-- vista de totales por moneda
CREATE OR REPLACE VIEW vista_totales_por_moneda AS
SELECT currency, SUM(amount) AS total_pagado, COUNT(*) AS cantidad
FROM pagos
GROUP BY currency;

-- vista de resumen general
CREATE OR REPLACE VIEW vista_resumen_general AS
SELECT 
  COUNT(*) AS total_pagos,
  SUM(amount) AS monto_total,
  COUNT(DISTINCT currency) AS monedas_usadas,
  COUNT(DISTINCT payer_email) AS clientes_unicos
FROM pagos;

-- consultas
SELECT * FROM vista_totales_por_estado;
SELECT * FROM vista_totales_por_moneda;
SELECT * FROM vista_resumen_general;


















