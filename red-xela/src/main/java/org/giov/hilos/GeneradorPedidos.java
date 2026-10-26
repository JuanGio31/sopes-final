package org.giov.hilos;

import org.giov.modelo.Pedido;

import java.util.HashMap;
import java.util.Random;

/**
 * Simula la llegada continua de pedidos de clientes con niveles de servicio
 * aleatorios, sin límite en su cantidad, para demostrar el funcionamiento
 * del sistema en tiempo de ejecución.
 */
public class GeneradorPedidos extends Thread {
    private final SistemaPedidos sistema;
    private boolean activo;
    private final Random random;

    /**
     * Construye el generador de pedidos vinculado al sistema de pedidos.
     *
     * @param sistema sistema de pedidos al que se agregan los pedidos generados
     */
    public GeneradorPedidos(SistemaPedidos sistema) {
        this.sistema = sistema;
        this.activo = true;
        this.random = new Random();
    }

    /**
     * Ejecuta el bucle principal del generador.
     * Genera pedidos con intervalos aleatorios entre 2 y 5 segundos,
     * asignando un nivel de servicio aleatorio (1 a 5) a cada uno.
     * El bucle continúa mientras el generador esté activo y no se interrumpa el hilo.
     */
    @Override
    public void run() {
        while (activo && !Thread.currentThread().isInterrupted()) {
            try {
                // Se simula una pausa aleatoria entre la llegada de nuevos clientes (2 a 5 segundos)
                Thread.sleep(2000 + random.nextInt(3000));

                // Se generan datos aleatorios para el pedido
                int nivelServicio = 1 + random.nextInt(5); // Niveles de servicio del 1 al 5
                String idPedido = sistema.generarIdUnico(false); // Se marca como pedido automático

                Pedido nuevoPedido = new Pedido(idPedido, "Cliente-Auto", nivelServicio, new HashMap<>());
                // En una versión final, se agregarían productos reales del catálogo al mapa del pedido

                // Se ingresa el pedido al sistema
                sistema.agregarPedido(nuevoPedido);
                System.out.println("Generador: Nuevo pedido ingresado -> " + idPedido + " (Nivel " + nivelServicio + ")");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Detiene la generación automática de pedidos.
     * El hilo finaliza su bucle en la siguiente iteración.
     */
    public void detener() {
        this.activo = false;
    }
}