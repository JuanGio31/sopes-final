package org.giov.modelo;

import java.util.Map;

/**
 * Estructura central que contiene la información mínima requerida
 * para procesar las solicitudes de los clientes.
 *
 * <p>Cada pedido está asociado a un cliente registrado, cuyo nivel de servicio
 * contratado determina la prioridad de procesamiento. Los niveles van del
 * 1 (Exprés) al 5 (Económico).</p>
 *
 * <p>Los tiempos máximos permitidos por nivel son:
 * <ul>
 *   <li>Nivel 1 — Exprés: casi instantáneo (60s como referencia)</li>
 *   <li>Nivel 2 — Prioritario: máximo 10 minutos</li>
 *   <li>Nivel 3 — Estándar: máximo 30 minutos</li>
 *   <li>Nivel 4 — Programado: máximo 60 minutos</li>
 *   <li>Nivel 5 — Económico: máximo 120 minutos</li>
 * </ul>
 * </p>
 */
public class Pedido {

    /** Tiempo máximo de procesamiento por nivel de servicio en milisegundos. */
    private static final long[] TIEMPOS_MAXIMOS = {
            0,              // índice 0 no se usa
            60_000,         // Nivel 1 — Exprés: casi instantáneo (60s como referencia)
            10 * 60_000,    // Nivel 2 — Prioritario: 10 minutos
            30 * 60_000,    // Nivel 3 — Estándar: 30 minutos
            60 * 60_000,    // Nivel 4 — Programado: 60 minutos
            120 * 60_000    // Nivel 5 — Económico: 120 minutos
    };

    /** Nombres descriptivos de cada nivel de servicio. */
    private static final String[] NOMBRES_NIVELES = {
            "", "Exprés", "Prioritario", "Estándar", "Programado", "Económico"
    };

    private String idPedido;
    private Cliente cliente;
    private Map<Producto, Integer> productosSolicitados;
    private int cantidadTotalArticulos;
    private EstadoPedido estadoActual;
    private final long tiempoLlegada = System.currentTimeMillis();

    /**
     * Construye un pedido asociado a un cliente registrado.
     * El nivel de servicio se hereda del cliente.
     *
     * @param idPedido identificador único del pedido
     * @param cliente cliente que realiza el pedido
     * @param productosSolicitados mapa de productos y cantidades
     */
    public Pedido(String idPedido, Cliente cliente,
                  Map<Producto, Integer> productosSolicitados) {
        this.idPedido = idPedido;
        this.cliente = cliente;
        this.productosSolicitados = productosSolicitados;
        this.estadoActual = EstadoPedido.CREADO;
        this.cantidadTotalArticulos = calcularTotalArticulos();
    }

    /**
     * Constructor legacy para compatibilidad con código existente.
     * Registra un cliente nuevo si no existe.
     *
     * @param idPedido identificador único del pedido
     * @param nombreCliente nombre del cliente
     * @param nivelServicio nivel de servicio (1-5)
     * @param productosSolicitados mapa de productos y cantidades
     */
    public Pedido(String idPedido, String nombreCliente, int nivelServicio,
                  Map<Producto, Integer> productosSolicitados) {
        this.idPedido = idPedido;
        this.cliente = GestorClientes.getInstancia().registrarCliente(nombreCliente, nivelServicio);
        this.productosSolicitados = productosSolicitados;
        this.estadoActual = EstadoPedido.CREADO;
        this.cantidadTotalArticulos = calcularTotalArticulos();
    }

    private int calcularTotalArticulos() {
        return productosSolicitados.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Devuelve el identificador único del pedido.
     *
     * @return ID del pedido
     */
    public String getIdPedido() {
        return idPedido;
    }

    /**
     * Devuelve el cliente asociado al pedido.
     *
     * @return objeto Cliente
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * Devuelve el nombre del cliente asociado al pedido.
     *
     * @return nombre del cliente
     */
    public String getNombreCliente() {
        return cliente != null ? cliente.getNombre() : "Desconocido";
    }

    /**
     * Devuelve el nivel de servicio del cliente asociado al pedido.
     *
     * @return nivel de servicio (1-5)
     */
    public int getNivelServicio() {
        return cliente != null ? cliente.getNivelServicio() : 3;
    }

    /**
     * Devuelve la cantidad total de artículos en el pedido.
     *
     * @return número total de unidades
     */
    public int getCantidadTotalArticulos() {
        return cantidadTotalArticulos;
    }

    /**
     * Devuelve el estado actual del pedido.
     *
     * @return estado del pedido
     */
    public EstadoPedido getEstadoActual() {
        return estadoActual;
    }

    /**
     * Establece el estado actual del pedido.
     *
     * @param estadoActual nuevo estado
     */
    public void setEstadoActual(EstadoPedido estadoActual) {
        this.estadoActual = estadoActual;
    }

    /**
     * Devuelve el timestamp de llegada del pedido.
     *
     * @return tiempo de llegada en milisegundos
     */
    public long getTiempoLlegada() {
        return this.tiempoLlegada;
    }

    /**
     * Devuelve el mapa de productos solicitados y sus cantidades.
     *
     * @return mapa Producto -> cantidad
     */
    public Map<Producto, Integer> getProductosSolicitados() {
        return productosSolicitados;
    }

    /**
     * Devuelve el nombre descriptivo del nivel de servicio del cliente.
     *
     * @return nombre como "Exprés", "Prioritario", etc.
     */
    public String getNombreNivelServicio() {
        return cliente != null ? cliente.getNombreNivelServicio() : "Desconocido";
    }

    /**
     * Devuelve el tiempo máximo permitido de procesamiento según el nivel
     * de servicio del cliente.
     *
     * @return tiempo máximo en milisegundos
     */
    public long getTiempoMaximoPermitido() {
        int nivel = getNivelServicio();
        if (nivel >= 1 && nivel <= 5) {
            return TIEMPOS_MAXIMOS[nivel];
        }
        return Long.MAX_VALUE;
    }

    /**
     * Devuelve el nombre del nivel de servicio para un número dado.
     *
     * @param nivel número de nivel (1-5)
     * @return nombre descriptivo o "Desconocido"
     */
    public static String nombreDeNivel(int nivel) {
        if (nivel >= 1 && nivel <= 5) {
            return NOMBRES_NIVELES[nivel];
        }
        return "Desconocido";
    }
}
