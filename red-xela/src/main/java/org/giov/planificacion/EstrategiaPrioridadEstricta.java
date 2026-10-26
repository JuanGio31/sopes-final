package org.giov.planificacion;

import org.giov.modelo.Pedido;

import java.util.Comparator;
import java.util.List;

/**
 * Esta política da prioridad siempre al nivel de servicio (Nivel 1: Exprés va primero).
 */
public class EstrategiaPrioridadEstricta implements EstrategiaPlanificacion {
    @Override
    public Pedido seleccionarSiguiente(List<Pedido> colaEspera) {
        if (colaEspera == null || colaEspera.isEmpty()) return null;

        // Ordena buscando el menor número de nivel (1 es mayor prioridad)
        return colaEspera.stream()
                .min(Comparator.comparingInt(Pedido::getNivelServicio))
                .orElse(null);
    }
}
