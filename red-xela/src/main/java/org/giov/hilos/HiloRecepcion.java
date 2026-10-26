package org.giov.hilos;


import java.util.concurrent.BlockingQueue;

/**
 * Representa el área de Recepción de Mercadería como un hilo productor.
 * Envía mensajes de llegada de mercadería a través de una cola bloqueante
 * hacia el área de Control de Inventario.
 */
public class HiloRecepcion extends Thread {
    private final BlockingQueue<MensajeRecepcion> canalComunicacion;

    /**
     * Construye el hilo de recepción vinculado a un canal de comunicación compartido.
     *
     * @param canal cola bloqueante utilizada para enviar mensajes al hilo de inventario
     */
    public HiloRecepcion(BlockingQueue<MensajeRecepcion> canal) {
        this.canalComunicacion = canal;
    }

    /**
     * Registra la llegada de mercadería enviando un mensaje al canal de comunicación.
     * Si la cola está llena, el hilo se bloquea hasta que haya espacio disponible,
     * evitando así la sobrecarga del sistema.
     *
     * @param codigo   código identificador del producto
     * @param cantidad número de unidades que llegan
     */
    public void registrarLlegada(String codigo, int cantidad) {
        try {
            // El método put bloquea si la cola está llena, evitando sobrecarga
            canalComunicacion.put(new MensajeRecepcion(codigo, cantidad));
            System.out.println("Recepción: Registró llegada de " + cantidad + " unidades del prod: " + codigo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error en Recepción: " + e.getMessage());
        }
    }
}