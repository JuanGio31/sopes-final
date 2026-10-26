package org.giov.hilos;


import org.giov.modelo.Almacen;
import org.giov.modelo.EstadoPedido;
import org.giov.modelo.Pedido;
import org.giov.recursos.GestorRecursos;
import org.giov.recursos.TipoRecurso;

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
                // 1. Verificar si hay stock suficiente antes de intentar procesar
                if (almacen.hayStockParaPedido(pedidoActual.getProductosSolicitados())) {

                    try {
                        pedidoActual.setEstadoActual(EstadoPedido.PROCESANDO);
                        System.out.println("Worker " + idWorker + ": Iniciando " + pedidoActual.getIdPedido());

                        // 2. Solicitud de recursos (AQUÍ DEBE USAR LOS RECURSOS QUE DICTE EL TIPO DE MERCANCÍA)
                        gestor.solicitarRecurso(TipoRecurso.ESTACION_EMPAQUE, pedidoActual.getIdPedido());
                        gestor.solicitarRecurso(TipoRecurso.ENCARGADO_BODEGA, pedidoActual.getIdPedido());

                        // 3. Simulación del trabajo basado en la cantidad de unidades[cite: 4]
                        int tiempoProcesamiento = pedidoActual.getCantidadTotalArticulos() * 100;
                        if (tiempoProcesamiento == 0) tiempoProcesamiento = 1500;

                        Thread.sleep(tiempoProcesamiento);

                        // 4. Descontar el stock real del almacén ahora que se empacó
                        almacen.descontarStock(pedidoActual.getProductosSolicitados());

                        // 5. Liberación de recursos
                        gestor.liberarRecurso(TipoRecurso.ENCARGADO_BODEGA, pedidoActual.getIdPedido());
                        gestor.liberarRecurso(TipoRecurso.ESTACION_EMPAQUE, pedidoActual.getIdPedido());

                        pedidoActual.setEstadoActual(EstadoPedido.COMPLETADO);
                        System.out.println("Worker " + idWorker + ": Finalizó " + pedidoActual.getIdPedido());

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
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
}