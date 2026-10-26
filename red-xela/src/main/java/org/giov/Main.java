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

    private static GeneradorPedidos getGenerador(BlockingQueue<MensajeRecepcion> canal) {
        HiloInventario inventario = new HiloInventario(canal);
        HiloRecepcion recepcion = new HiloRecepcion(canal);

        inventario.start();
        recepcion.start();

        // 2. Se configura el sistema de pedidos con la estrategia de prioridad estricta
        SistemaPedidos sistema = new SistemaPedidos(new EstrategiaPrioridadEstricta());

        // 3. Se levantan los hilos trabajadores para procesar pedidos en paralelo
        // Se asignan 5 workers que simulan las capacidades paralelas del centro
        for (int i = 1; i <= 5; i++) {
            WorkerProcesamiento worker = new WorkerProcesamiento(sistema, i);
            worker.start();
        }

        // 4. Se inicia la generación automática de clientes y pedidos
        GeneradorPedidos generador = new GeneradorPedidos(sistema);
        return generador;
    }
}