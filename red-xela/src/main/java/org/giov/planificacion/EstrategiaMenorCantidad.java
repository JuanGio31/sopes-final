package org.giov.planificacion;

import org.giov.modelo.Pedido;

import java.util.Comparator;
import java.util.List;

/**
 * Procesa primero los pedidos con menor cantidad de artículos
 * para liberar las estaciones de trabajo más rápido.
 */
public class EstrategiaMenorCantidad implements EstrategiaPlanificacion {
    @Override
    public Pedido seleccionarSiguiente(List<Pedido> colaEspera) {
        if (colaEspera == null || colaEspera.isEmpty()) return null;

        // Ordena por la cantidad total de artículos, de menor a mayor
        return colaEspera.stream()
                .min(Comparator.comparingInt(Pedido::getCantidadTotalArticulos))
                .orElse(null);
    }
}
