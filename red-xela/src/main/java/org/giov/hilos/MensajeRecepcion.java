package org.giov.hilos;

/**
 * Objeto simple para transferir datos de forma segura entre
 * los hilos de Recepción e Inventario.
 */
public record MensajeRecepcion(String codigoProducto, int cantidad) {
}