package org.giov.hilos;

import org.giov.modelo.Almacen;
import org.giov.modelo.EstadoPedido;
import org.giov.modelo.Pedido;
import org.giov.planificacion.EstrategiaPlanificacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
}