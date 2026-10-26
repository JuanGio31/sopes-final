package org.giov;

import org.giov.hilos.*;
import org.giov.modelo.ConfiguracionMercancias;
import org.giov.planificacion.EstrategiaPrioridadEstricta;

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
    /**
     * Inicia el sistema REDXela configurando todos los componentes necesarios.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        System.out.println("--- Iniciando Sistema REDXela ---");

        // 0. Se inicializa la configuración de mercancías y se imprime el catálogo
        ConfiguracionMercancias.imprimirConfiguracion();

        // 1. Se inicia la comunicación asíncrona entre Inventario y Recepción
        BlockingQueue<MensajeRecepcion> canal = new LinkedBlockingQueue<>();
        GeneradorPedidos generador = getGenerador(canal);
        generador.start();

        System.out.println("Sistema ejecutándose. Presione Ctrl+C para detener.");
    }

    /**
     * Configura y devuelve el generador de pedidos, inicializando el sistema de pedidos,
     * los hilos de inventario y recepción, y los hilos trabajadores de procesamiento.
     *
     * @param canal cola de comunicación entre recepción e inventario
     * @return el generador de pedidos configurado
     */
    private static GeneradorPedidos getGenerador(BlockingQueue<MensajeRecepcion> canal) {
        SistemaPedidos sistema = new SistemaPedidos(new EstrategiaPrioridadEstricta());

        HiloInventario inventario = new HiloInventario(canal, sistema);
        HiloRecepcion recepcion = new HiloRecepcion(canal);

        inventario.start();
        recepcion.start();


        for (int i = 1; i <= 5; i++) {
            WorkerProcesamiento worker = new WorkerProcesamiento(sistema, i);
            worker.start();
        }

        GeneradorPedidos generador = new GeneradorPedidos(sistema);
        return generador;
    }
}