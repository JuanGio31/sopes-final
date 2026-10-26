package org.giov.planificacion;

import org.giov.modelo.Pedido;

import java.util.List;

/**
 * Define el contrato que todas las políticas de atención deben cumplir.
 */
public interface EstrategiaPlanificacion {
    /**
     * Selecciona el siguiente pedido de la cola de espera según la política
     * de planificación implementada.
     *
     * @param colaEspera lista de pedidos pendientes de procesamiento
     * @return el pedido que debe ser atendido a continuación
     */
    Pedido seleccionarSiguiente(List<Pedido> colaEspera);
}
