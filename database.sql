-- =====================================================================
-- Sistema de Gestión de Estudiantes
-- Base de datos: gestion_estudiantes
-- SGBD: MariaDB 10.4+ (XAMPP)
-- Ejecutar desde phpMyAdmin (pestaña SQL) o por línea de comandos:
--   cmd /c "mysql -u root -p < database.sql"
-- =====================================================================

CREATE DATABASE IF NOT EXISTS gestion_estudiantes
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gestion_estudiantes;

CREATE TABLE IF NOT EXISTS estudiantes (
    id             INT             NOT NULL AUTO_INCREMENT,
    nombres        VARCHAR(100)    NOT NULL,
    apellidos      VARCHAR(100)    NOT NULL,
    dni            VARCHAR(8)      NOT NULL,
    correo         VARCHAR(120)    NOT NULL,
    telefono       VARCHAR(15)     NOT NULL,
    fecha_registro TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_estudiantes_dni UNIQUE (dni),
    CONSTRAINT uk_estudiantes_correo UNIQUE (correo),
    CONSTRAINT chk_estudiantes_dni_formato CHECK (dni REGEXP '^[0-9]{8}$'),
    INDEX idx_estudiantes_apellidos_nombres (apellidos, nombres),
    INDEX idx_estudiantes_fecha_registro (fecha_registro)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- Usuario de aplicación (ejecutar por separado y cambiar la contraseña)
-- No se incluye una contraseña real en este archivo.
--
-- CREATE USER 'gestion_user'@'localhost' IDENTIFIED BY '<TU_CLAVE_SEGURA>';
-- CREATE USER 'gestion_user'@'127.0.0.1' IDENTIFIED BY '<TU_CLAVE_SEGURA>';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON gestion_estudiantes.* TO 'gestion_user'@'localhost';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON gestion_estudiantes.* TO 'gestion_user'@'127.0.0.1';
-- FLUSH PRIVILEGES;
-- =====================================================================

-- Verificación opcional:
-- SHOW TABLES;
-- DESCRIBE estudiantes;
