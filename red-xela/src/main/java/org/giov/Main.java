package org.giov;

import com.formdev.flatlaf.FlatLightLaf;
import org.giov.hilos.*;
import org.giov.modelo.ConfiguracionMercancias;
import org.giov.planificacion.EstrategiaPrioridadEstricta;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Punto de entrada principal de la aplicación REDXela.
 * Inicializa los componentes del sistema: configuración de mercancías y catálogo,
 * comunicación asíncrona entre Recepción e Inventario, el sistema de pedidos con
 * su estrategia de planificación, los hilos trabajadores y el generador automático
 * de pedidos.
 */
public class Main {

    /** Referencia al sistema de pedidos, accesible por la GUI. */
    private static SistemaPedidos sistemaPedidos;

    /** Referencia al generador de pedidos, accesible por la GUI. */
    private static GeneradorPedidos generadorPedidos;

    /** Lista de hilos trabajadores, accesible por la GUI. */
    private static List<WorkerProcesamiento> workers;

    /**
     * Inicia el sistema REDXela configurando todos los componentes necesarios.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        System.out.println("--- Iniciando Sistema REDXela ---");

        // Inicialización de mercancías y catálogo
        ConfiguracionMercancias.imprimirConfiguracion();

        // Se carga el stock inicial en el almacén para que los pedidos automáticos no queden bloqueados
        ConfiguracionMercancias.cargarStockInicial();

        // Comunicación asíncrona entre Inventario y Recepción
        BlockingQueue<MensajeRecepcion> canal = new LinkedBlockingQueue<>();
        generadorPedidos = getGenerador(canal);

        // Generador automático de pedidos
        generadorPedidos.start();

        // Se marca el sistema como inicializado para que la GUI pueda acceder a los datos
        sistemaPedidos.marcarInicializado();

        System.out.println("Sistema ejecutándose. Presione Ctrl+C para detener.");

        // Se inicia la interfaz gráfica en el hilo EDT de Swing con FlatLaf
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            new org.giov.gui.REDXelaMainFrame().setVisible(true);
        });
    }

    /**
     * Devuelve la referencia al sistema de pedidos.
     *
     * @return el sistema de pedidos en ejecución, o {@code null} si no se ha inicializado
     */
    public static SistemaPedidos getSistemaPedidos() {
        return sistemaPedidos;
    }

    /**
     * Devuelve la referencia al generador de pedidos.
     *
     * @return el generador de pedidos en ejecución, o {@code null} si no se ha inicializado
     */
    public static GeneradorPedidos getGeneradorPedidos() {
        return generadorPedidos;
    }

    /**
     * Devuelve la lista de hilos trabajadores activos.
     *
     * @return lista de workers en ejecución, o {@code null} si no se han inicializado
     */
    public static List<WorkerProcesamiento> getWorkers() {
        return workers;
    }

    /**
     * Configura y devuelve el generador de pedidos, inicializando el sistema de pedidos,
     * los hilos de inventario y recepción, y los hilos trabajadores de procesamiento.
     *
     * @param canal cola de comunicación entre recepción e inventario
     * @return el generador de pedidos configurado
     */
    private static GeneradorPedidos getGenerador(BlockingQueue<MensajeRecepcion> canal) {
        sistemaPedidos = new SistemaPedidos(new EstrategiaPrioridadEstricta());

        HiloInventario inventario = new HiloInventario(canal, sistemaPedidos);
        HiloRecepcion recepcion = new HiloRecepcion(canal);

        inventario.start();
        recepcion.start();

        // Se almacena la referencia a los workers para que la GUI pueda acceder
        workers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            WorkerProcesamiento worker = new WorkerProcesamiento(sistemaPedidos, i);
            workers.add(worker);
            worker.start();
        }

        GeneradorPedidos generador = new GeneradorPedidos(sistemaPedidos);
        return generador;
    }
}