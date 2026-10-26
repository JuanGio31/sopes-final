package org.giov.planificacion;

import org.giov.modelo.Pedido;

import java.util.Comparator;
import java.util.List;

/**
 * Da la oportunidad a los pedidos que llevan más tiempo esperando en la cola.
 */
public class EstrategiaTiempoEspera implements EstrategiaPlanificacion {
    @Override
    public Pedido seleccionarSiguiente(List<Pedido> colaEspera) {
        if (colaEspera == null || colaEspera.isEmpty()) return null;

        // El menor tiempo de llegada significa que llegó antes (lleva más tiempo esperando).
        return colaEspera.stream()
                .min(Comparator.comparingLong(Pedido::getTiempoLlegada))
                .orElse(null);
    }
}