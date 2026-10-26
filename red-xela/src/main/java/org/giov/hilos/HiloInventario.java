package org.giov.hilos;

import org.giov.modelo.Almacen;

import java.util.concurrent.BlockingQueue;

/**
 * Representa el área de Control de Inventario como un hilo consumidor.
 * Extrae mensajes de llegada de mercadería del canal de comunicación
 * y actualiza las existencias en el almacén.
 */
public class HiloInventario extends Thread {
    private final BlockingQueue<MensajeRecepcion> canalComunicacion;
    private final Almacen almacen;

    /**
     * Construye el hilo de inventario vinculado a un canal de comunicación compartido.
     *
     * @param canal cola bloqueante desde la cual se consumen los mensajes de recepción
     */
    public HiloInventario(BlockingQueue<MensajeRecepcion> canal) {
        this.canalComunicacion = canal;
        this.almacen = Almacen.getInstancia();
    }

    /**
     * Ejecuta el bucle principal del hilo consumidor.
     * Extrae mensajes de la cola de forma bloqueante y registra la mercadería
     * en el almacén. El hilo continúa hasta que se interrumpe.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // El método take bloquea el hilo si no hay mensajes, evitando consumo de CPU
                MensajeRecepcion mensaje = canalComunicacion.take();

                // Se actualizan las existencias y se asignan ubicaciones automáticamente
                boolean exito = almacen.registrarLlegadaMercaderia(mensaje.codigoProducto(), mensaje.cantidad());

                if (exito) {
                    System.out.println("Inventario: Almacenó " + mensaje.cantidad() + " unidades de " + mensaje.codigoProducto());
                    // Aquí iría la lógica para volver a evaluar pedidos en espera
                } else {
                    System.out.println("Inventario: Almacén lleno, no se pudo guardar " + mensaje.codigoProducto());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}