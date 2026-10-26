package org.giov.hilos;

import org.giov.modelo.Almacen;
import org.giov.modelo.EstadoPedido;
import org.giov.modelo.Pedido;
import org.giov.planificacion.EstrategiaPlanificacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestiona la cola de pedidos pendientes y aplica la estrategia de planificación
 * seleccionada para determinar el orden de procesamiento.
 *
 * <p>Proporciona acceso sincronizado a la cola de espera para garantizar
 * la seguridad entre hilos al agregar y remover pedidos.</p>
 */
public class SistemaPedidos {
    /**
     * Cola de espera sincronizada para evitar errores de concurrencia al añadir o remover elementos.
     */
    private final List<Pedido> colaEspera;
    private EstrategiaPlanificacion estrategiaActual;

    // Nueva lista para separar los que esperan stock de los que esperan ser atendidos
    private List<Pedido> pendientesInventario = Collections.synchronizedList(new ArrayList<>());

    /**
     * Contador atómico centralizado para garantizar IDs únicos en toda la aplicación.
     */
    private final AtomicInteger contadorIdGeneral = new AtomicInteger(1);

    /** Contador de pedidos que han completado su procesamiento exitosamente. */
    private final AtomicInteger pedidosCompletados = new AtomicInteger(0);

    /** Lista de pedidos actualmente siendo procesados por los workers, segura para concurrencia. */
    private final List<Pedido> pedidosProcesando = new CopyOnWriteArrayList<>();

    /** Marca que indica si el sistema ha sido inicializado. */
    private volatile boolean inicializado = false;

    /**
     * Construye el sistema de pedidos con una estrategia de planificación inicial.
     *
     * @param estrategiaInicial política de selección de pedidos a aplicar
     */
    public SistemaPedidos(EstrategiaPlanificacion estrategiaInicial) {
        this.colaEspera = Collections.synchronizedList(new ArrayList<>());
        this.estrategiaActual = estrategiaInicial;
    }

    /**
     * Genera un identificador único para un nuevo pedido.
     * El prefijo indica si el pedido es manual ("PED-M-") o automático ("PED-A-").
     *
     * @param esManual {@code true} si el pedido fue ingresado manualmente,
     *                 {@code false} si fue generado automáticamente
     * @return identificador único del pedido
     */
    public String generarIdUnico(boolean esManual) {
        int id = contadorIdGeneral.getAndIncrement();
        if (esManual) {
            return "PED-M-" + id;
        }
        return "PED-A-" + id;
    }

    /**
     * Cambia la política de planificación en tiempo de ejecución.
     * El acceso es sincronizado para evitar condiciones de carrera.
     *
     * @param nuevaEstrategia nueva política de selección de pedidos
     */
    public synchronized void setEstrategia(EstrategiaPlanificacion nuevaEstrategia) {
        this.estrategiaActual = nuevaEstrategia;
        System.out.println("Se ha cambiado la política de planificación.");
    }

    /**
     * Agrega un pedido a la cola de espera.
     * Este método se utiliza tanto para el ingreso automático como para el manual.
     *
     * @param pedido pedido a agregar a la cola
     */
    public void agregarPedido(Pedido pedido) {
        colaEspera.add(pedido);
    }

    /**
     * Obtiene el siguiente pedido de la cola de espera según la estrategia
     * de planificación actualmente activa. Los hilos trabajadores invocan
     * este método para obtener trabajo.
     *
     * @return el siguiente pedido a procesar, o {@code null} si la cola está vacía
     */
    public synchronized Pedido obtenerSiguientePedido() {
        if (colaEspera.isEmpty()) {
            return null;
        }

        // Se aplica la estrategia para seleccionar el pedido con mayor prioridad
        Pedido siguiente = estrategiaActual.seleccionarSiguiente(colaEspera);

        if (siguiente != null) {
            colaEspera.remove(siguiente); // Se elimina el pedido de la cola de espera
        }
        return siguiente;
    }

    /**
     * Devuelve una copia de la cola de espera actual.
     * La copia permite que la interfaz gráfica lea el estado sin afectar
     * la cola original.
     *
     * @return lista de pedidos actualmente en espera
     */
    public List<Pedido> getColaEspera() {
        return new ArrayList<>(colaEspera); // Se retorna una copia para la interfaz gráfica
    }

    // Método que llamará el Worker si descubre que no hay stock
    public void enviarAEsperaDeInventario(Pedido pedido) {
        pedido.setEstadoActual(EstadoPedido.BLOQUEADO);
        pendientesInventario.add(pedido);
        System.out.println("Pedido " + pedido.getIdPedido() + " bloqueado por falta de inventario.");
    }

    // Método para reevaluar (Este es el que reemplaza el comentario)
    public synchronized void reevaluarPendientesInventario() {
        List<Pedido> pedidosLiberados = new ArrayList<>();

        for (Pedido p : pendientesInventario) {
            // Verifica si ahora, con la nueva mercancía, ya hay stock
            if (Almacen.getInstancia().hayStockParaPedido(p.getProductosSolicitados())) {
                pedidosLiberados.add(p);
            }
        }

        // Los saca de la espera de inventario y los regresa a la cola normal de atención
        if (!pedidosLiberados.isEmpty()) {
            pendientesInventario.removeAll(pedidosLiberados);
            colaEspera.addAll(pedidosLiberados);
            for(Pedido p : pedidosLiberados){
                p.setEstadoActual(EstadoPedido.EN_ESPERA);
                System.out.println("Pedido " + p.getIdPedido() + " reevaluado y devuelto a la cola principal.");
            }
        }
    }

    /**
     * Incrementa el contador de pedidos completados.
     * Se invoca cuando un worker finaliza el procesamiento de un pedido.
     */
    public void incrementarCompletados() {
        pedidosCompletados.incrementAndGet();
    }

    /**
     * Registra un pedido como actualmente en proceso por un worker.
     * Se invoca al inicio del procesamiento de cada pedido.
     *
     * @param pedido pedido que comienza a procesarse
     */
    public void registrarEnProceso(Pedido pedido) {
        pedidosProcesando.add(pedido);
    }

    /**
     * Elimina un pedido de la lista de pedidos en proceso.
     * Se invoca cuando un worker finaliza o cancela el procesamiento.
     *
     * @param pedido pedido que deja de procesarse
     */
    public void finalizarProceso(Pedido pedido) {
        pedidosProcesando.remove(pedido);
    }

    /**
     * Devuelve el número total de pedidos completados exitosamente.
     *
     * @return número de pedidos completados
     */
    public int getPedidosCompletados() {
        return pedidosCompletados.get();
    }

    /**
     * Devuelve una copia de la lista de pedidos actualmente en proceso.
     *
     * @return lista de pedidos siendo procesados por los workers
     */
    public List<Pedido> getPedidosProcesando() {
        return new ArrayList<>(pedidosProcesando);
    }

    /**
     * Devuelve una copia de la lista de pedidos bloqueados por falta de inventario.
     *
     * @return lista de pedidos esperando stock
     */
    public List<Pedido> getPedidosBloqueados() {
        return new ArrayList<>(pendientesInventario);
    }

    /**
     * Devuelve la estrategia de planificación actualmente activa.
     *
     * @return la estrategia de planificación en uso
     */
    public synchronized EstrategiaPlanificacion getEstrategiaActual() {
        return estrategiaActual;
    }

    /**
     * Marca el sistema como inicializado, permitiendo que la GUI acceda a los datos.
     */
    public void marcarInicializado() {
        this.inicializado = true;
    }

    /**
     * Verifica si el sistema ha sido inicializado.
     *
     * @return {@code true} si el sistema está listo para ser consultado por la GUI
     */
    public boolean isInicializado() {
        return inicializado;
    }
}