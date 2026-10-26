package org.giov.hilos;


import org.giov.modelo.Almacen;
import org.giov.modelo.EstadoPedido;
import org.giov.modelo.Pedido;
import org.giov.modelo.Producto;
import org.giov.modelo.TipoMercancia;
import org.giov.recursos.GestorRecursos;
import org.giov.recursos.TipoRecurso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representa un hilo trabajador que procesa pedidos de forma concurrente.
 * Cada worker obtiene pedidos de la cola de espera, solicita los recursos
 * necesarios, simula el procesamiento y libera los recursos al finalizar.
 */
public class WorkerProcesamiento extends Thread {
    private final SistemaPedidos sistema;
    private final GestorRecursos gestor;
    private Almacen almacen;
    private final int idWorker;

    /**
     * Construye un hilo trabajador con un identificador único.
     *
     * @param sistema  sistema de pedidos del cual se obtiene el trabajo
     * @param idWorker identificador numérico del worker para fines de registro
     */
    public WorkerProcesamiento(SistemaPedidos sistema, int idWorker) {
        this.sistema = sistema;
        this.gestor = GestorRecursos.getInstancia();
        this.almacen = Almacen.getInstancia();
        this.idWorker = idWorker;
    }

    /**
     * Ejecuta el bucle principal del worker.
     * Obtiene pedidos de la cola, solicita los recursos compartidos,
     * simula el procesamiento y libera los recursos al completar.
     * Si no hay pedidos disponibles, espera brevemente antes de volver a intentar,
     * evitando así saturar la CPU.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Pedido pedidoActual = sistema.obtenerSiguientePedido();

            if (pedidoActual != null) {
                // Verificar stock antes de procesar
                if (almacen.hayStockParaPedido(pedidoActual.getProductosSolicitados())) {

                    try {
                        pedidoActual.setEstadoActual(EstadoPedido.PROCESANDO);
                        sistema.registrarEnProceso(pedidoActual);
                        System.out.println("Worker " + idWorker + ": Iniciando " + pedidoActual.getIdPedido());

                        // Recursos necesarios según tipo de mercancía
                        List<TipoRecurso> recursosNecesarios = obtenerRecursosNecesarios(pedidoActual.getProductosSolicitados());

                        // Solicitud de recursos en orden
                        for (TipoRecurso recurso : recursosNecesarios) {
                            gestor.solicitarRecurso(recurso, pedidoActual.getIdPedido());
                        }

                        // Simulación del trabajo
                        int tiempoProcesamiento = pedidoActual.getCantidadTotalArticulos() * 100;
                        if (tiempoProcesamiento == 0) tiempoProcesamiento = 1500;

                        Thread.sleep(tiempoProcesamiento);

                        // Descontar stock del almacén
                        almacen.descontarStock(pedidoActual.getProductosSolicitados());

                        // Liberación de recursos en orden inverso
                        for (int i = recursosNecesarios.size() - 1; i >= 0; i--) {
                            gestor.liberarRecurso(recursosNecesarios.get(i), pedidoActual.getIdPedido());
                        }

                        pedidoActual.setEstadoActual(EstadoPedido.COMPLETADO);
                        sistema.finalizarProceso(pedidoActual);
                        sistema.incrementarCompletados();
                        System.out.println("Worker " + idWorker + ": Finalizó " + pedidoActual.getIdPedido());

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        sistema.finalizarProceso(pedidoActual);
                        System.out.println("Worker " + idWorker + " interrumpido.");
                    }

                } else {
                    // No hay stock, se envía a la lista de espera de inventario
                    sistema.enviarAEsperaDeInventario(pedidoActual);
                }
            } else {
                // Si no hay pedidos en cola, espera un poco para no saturar el procesador
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Determina los recursos únicos necesarios para procesar todos los productos
     * de un pedido, basándose en el tipo de mercancía de cada producto.
     *
     * <p>Si un pedido contiene productos de diferentes tipos, se unifican los recursos
     * requeridos por todos ellos, eliminando duplicados.</p>
     *
     * @param productos mapa de productos solicitados con sus cantidades
     * @return lista ordenada de recursos necesarios (sin duplicados)
     */
    private List<TipoRecurso> obtenerRecursosNecesarios(Map<Producto, Integer> productos) {
        Set<TipoRecurso> recursosSet = new HashSet<>();

        for (Producto producto : productos.keySet()) {
            TipoMercancia tipoMercancia = producto.tipoMercancia();
            if (tipoMercancia != null) {
                for (String recursoNombre : tipoMercancia.getRecursosRequeridos()) {
                    try {
                        TipoRecurso tipo = TipoRecurso.valueOf(recursoNombre);
                        recursosSet.add(tipo);
                    } catch (IllegalArgumentException e) {
                        // Se ignora el recurso si no existe en el enum
                    }
                }
            }
        }

        // Se retorna una lista ordenada para que la adquisición sea determinista
        // y reduzca la probabilidad de deadlocks por orden inverso
        List<TipoRecurso> recursos = new ArrayList<>(recursosSet);
        recursos.sort(null); // Orden alfabético por nombre del enum
        return recursos;
    }
}