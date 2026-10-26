package org.giov.hilos;


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
                try {
                    pedidoActual.setEstadoActual(EstadoPedido.PROCESANDO);
                    System.out.println("Worker " + idWorker + ": Iniciando " + pedidoActual.getIdPedido());

                    // 1. Se solicitan los recursos requeridos según el tipo de mercancía
                    // Para el ejemplo, se solicita una estación de empaque y un encargado
                    gestor.solicitarRecurso(TipoRecurso.ESTACION_EMPAQUE, pedidoActual.getIdPedido());
                    gestor.solicitarRecurso(TipoRecurso.ENCARGADO_BODEGA, pedidoActual.getIdPedido());

                    // 2. Se simula el trabajo de procesamiento
                    // La cantidad de unidades determina el tiempo de procesamiento (100 ms por unidad)
                    int tiempoProcesamiento = pedidoActual.getCantidadTotalArticulos() * 100;
                    if (tiempoProcesamiento == 0) tiempoProcesamiento = 1500; // Tiempo mínimo por defecto

                    Thread.sleep(tiempoProcesamiento);

                    // 3. Se liberan los recursos al finalizar el procesamiento
                    gestor.liberarRecurso(TipoRecurso.ENCARGADO_BODEGA, pedidoActual.getIdPedido());
                    gestor.liberarRecurso(TipoRecurso.ESTACION_EMPAQUE, pedidoActual.getIdPedido());

                    pedidoActual.setEstadoActual(EstadoPedido.COMPLETADO);
                    System.out.println("Worker " + idWorker + ": Finalizó " + pedidoActual.getIdPedido());

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // No hay pedidos disponibles; se espera brevemente para no saturar la CPU
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}