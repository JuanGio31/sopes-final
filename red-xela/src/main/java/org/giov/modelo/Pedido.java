package org.giov.modelo;

import java.util.Map;

/**
 * Estructura central que contiene la información mínima requerida
 * para procesar las solicitudes de los clientes
 */
public class Pedido {
    private String idPedido;
    private String cliente;
    private int nivelServicio;
    private Map<Producto, Integer> productosSolicitados;
    private int cantidadTotalArticulos;
    private EstadoPedido estadoActual;
    private final long tiempoLlegada = System.currentTimeMillis();

    public Pedido(String idPedido, String cliente, int nivelServicio,
                  Map<Producto, Integer> productosSolicitados) {
        this.idPedido = idPedido;
        this.cliente = cliente;
        this.nivelServicio = nivelServicio;
        this.productosSolicitados = productosSolicitados;
        this.estadoActual = EstadoPedido.CREADO;
        this.cantidadTotalArticulos = calcularTotalArticulos();
    }

    private int calcularTotalArticulos() {
        return productosSolicitados.values().stream().mapToInt(Integer::intValue).sum();
    }

    public String getIdPedido() {
        return idPedido;
    }

    public int getNivelServicio() {
        return nivelServicio;
    }

    public int getCantidadTotalArticulos() {
        return cantidadTotalArticulos;
    }

    public EstadoPedido getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(EstadoPedido estadoActual) {
        this.estadoActual = estadoActual;
    }

    public long getTiempoLlegada() {
        return this.tiempoLlegada;
    }
}
