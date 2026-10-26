package org.giov.modelo;

/**
 * Define los estados por los que transita un pedido dentro del centro de distribución.
 */
public enum EstadoPedido {
    CREADO, EN_ESPERA, PROCESANDO, BLOQUEADO, COMPLETADO
}
