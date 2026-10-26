package org.giov.hilos;

import org.giov.modelo.ConfiguracionMercancias;
import org.giov.modelo.Pedido;
import org.giov.modelo.Producto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Simula la llegada continua de pedidos de clientes con niveles de servicio
 * aleatorios y productos reales del catálogo, sin límite en su cantidad,
 * para demostrar el funcionamiento del sistema en tiempo de ejecución.
 */
public class GeneradorPedidos extends Thread {
    private final SistemaPedidos sistema;
    private boolean activo;
    private final Random random;
    private final List<Producto> productosLista;

    /**
     * Construye el generador de pedidos vinculado al sistema de pedidos.
     * Se inicializa con la lista de productos del catálogo para generar pedidos realistas.
     *
     * @param sistema sistema de pedidos al que se agregan los pedidos generados
     */
    public GeneradorPedidos(SistemaPedidos sistema) {
        this.sistema = sistema;
        this.activo = true;
        this.random = new Random();
        this.productosLista = List.copyOf(ConfiguracionMercancias.getCatalogo().values());
    }

    /**
     * Ejecuta el bucle principal del generador.
     * Genera pedidos con intervalos aleatorios entre 2 y 5 segundos,
     * asignando un nivel de servicio aleatorio (1 a 5) y productos reales
     * del catálogo a cada uno.
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

                // Se seleccionan 1 a 3 productos aleatorios del catálogo con cantidades realistas
                Map<Producto, Integer> productos = generarProductosAleatorios();

                // Se genera un nombre de cliente variado
                String cliente = generarNombreCliente();

                Pedido nuevoPedido = new Pedido(idPedido, cliente, nivelServicio, productos);

                // Se ingresa el pedido al sistema
                sistema.agregarPedido(nuevoPedido);
                System.out.println("Generador: Nuevo pedido ingresado -> " + idPedido
                        + " (Nivel " + Pedido.nombreDeNivel(nivelServicio) + ", "
                        + nuevoPedido.getCantidadTotalArticulos() + " artículos)");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Genera un mapa de productos aleatorios del catálogo con cantidades realistas.
     * Selecciona entre 1 y 3 productos distintos, cada uno con 1 a 20 unidades.
     *
     * @return mapa con productos y sus cantidades
     */
    private Map<Producto, Integer> generarProductosAleatorios() {
        Map<Producto, Integer> productos = new HashMap<>();
        int cantidadProductos = 1 + random.nextInt(3); // 1 a 3 productos distintos

        for (int i = 0; i < cantidadProductos; i++) {
            Producto producto = productosLista.get(random.nextInt(productosLista.size()));
            int cantidad = 1 + random.nextInt(20); // 1 a 20 unidades
            productos.merge(producto, cantidad, Integer::sum);
        }
        return productos;
    }

    /**
     * Genera un nombre de cliente aleatorio para simular variedad de clientes.
     *
     * @return nombre del cliente generado
     */
    private String generarNombreCliente() {
        String[] nombres = {"Juan Pérez", "María López", "Carlos García", "Ana Martínez",
                "Pedro Sánchez", "Laura Torres", "Diego Ramírez", "Sofia Díaz",
                "Miguel Flores", "Elena Ruiz"};
        return nombres[random.nextInt(nombres.length)];
    }

    /**
     * Detiene la generación automática de pedidos.
     * El hilo pausa su bucle en la siguiente iteración sin terminar.
     */
    public synchronized void detener() {
        this.activo = false;
    }

    /**
     * Reanuda la generación automática de pedidos si estaba detenida.
     * Si el hilo ya terminó (se interrumpió), se crea y ejecuta un nuevo hilo.
     */
    public synchronized void reanudar() {
        if (this.activo) {
            return; // Ya está activo, no se hace nada
        }
        this.activo = true;
        // Si el hilo original terminó, se crea uno nuevo
        if (getState() == Thread.State.TERMINATED) {
            new Thread(this::run).start();
        }
    }

    /**
     * Indica si el generador está actualmente activo generando pedidos.
     *
     * @return true si el generador está activo, false si está detenido
     */
    public synchronized boolean estaActivo() {
        return this.activo;
    }
}