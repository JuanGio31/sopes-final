package org.giov.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementa el patrón Singleton para garantizar un único punto de control
 * del inventario y de las ubicaciones físicas, gestionando una capacidad definida.
 *
 * <p>El almacén cuenta con 100 ubicaciones físicas que pueden ocuparse de forma
 * no contigua, simulando la distribución real de un centro logístico.</p>
 */
public class Almacen {
    private static final int CAPACIDAD_MAXIMA = 5000;
    private static final int NUMERO_UBICACIONES = 100;
    private static Almacen instancia;
    private int espacioOcupado;
    private final boolean[] ubicaciones;

    private final Map<String, Integer> existenciasPorProducto;

    private Almacen() {
        this.existenciasPorProducto = new ConcurrentHashMap<>();
        this.espacioOcupado = 0;
        this.ubicaciones = new boolean[NUMERO_UBICACIONES];
    }

    /**
     * Devuelve la única instancia de {@link Almacen}.
     * El acceso es sincronizado para garantizar la seguridad entre hilos.
     *
     * @return la instancia singleton del almacén
     */
    public static synchronized Almacen getInstancia() {
        if (instancia == null) {
            instancia = new Almacen();
        }
        return instancia;
    }

    /**
     * Registra la llegada de mercadería al almacén.
     * Verifica que exista espacio suficiente antes de actualizar las existencias
     * y asigna las ubicaciones físicas correspondientes.
     *
     * @param codigoProducto código identificador del producto
     * @param cantidad       número de unidades que llegan
     * @return {@code true} si la mercadería se registró correctamente,
     * {@code false} si no hay espacio suficiente
     */
    public synchronized boolean registrarLlegadaMercaderia(String codigoProducto, int cantidad) {
        if (espacioOcupado + cantidad <= CAPACIDAD_MAXIMA) {
            existenciasPorProducto.put(codigoProducto,
                    existenciasPorProducto.getOrDefault(codigoProducto, 0) + cantidad);
            espacioOcupado += cantidad;

            // Se asignan las ubicaciones físicas para este lote
            int ubicacionesNecesarias = calcularUbicacionesNecesarias(cantidad);
            asignarUbicaciones(ubicacionesNecesarias);

            return true;
        }
        return false; // No hay espacio suficiente
    }

    /**
     * Calcula cuántas ubicaciones físicas se necesitan para un lote dado.
     * Se utiliza una regla de negocio donde cada 50 unidades ocupan una ubicación,
     * con un mínimo de 1 ubicación por lote.
     *
     * @param cantidad número de unidades del lote
     * @return número de ubicaciones físicas necesarias
     */
    private int calcularUbicacionesNecesarias(int cantidad) {
        int ubicaciones = (cantidad + 49) / 50; // Redondeo hacia arriba
        return Math.max(1, Math.min(ubicaciones, NUMERO_UBICACIONES));
    }

    /**
     * Asigna las ubicaciones físicas libres para un lote de mercadería.
     * Busca espacios disponibles de forma no contigua, marcándolos como ocupados.
     *
     * @param cantidad número de ubicaciones a asignar
     * @return {@code true} si se asignaron todas las ubicaciones solicitadas,
     * {@code false} si no hay suficientes ubicaciones libres
     */
    private boolean asignarUbicaciones(int cantidad) {
        int asignadas = 0;
        for (int i = 0; i < NUMERO_UBICACIONES && asignadas < cantidad; i++) {
            if (!ubicaciones[i]) {
                ubicaciones[i] = true;
                asignadas++;
            }
        }
        return asignadas == cantidad;
    }

    /**
     * Devuelve la lista de índices de las ubicaciones físicas actualmente ocupadas.
     *
     * @return lista de enteros representando los índices de ubicaciones ocupadas
     */
    public synchronized List<Integer> getUbicacionesOcupadas() {
        List<Integer> ocupadas = new ArrayList<>();
        for (int i = 0; i < NUMERO_UBICACIONES; i++) {
            if (ubicaciones[i]) {
                ocupadas.add(i);
            }
        }
        return ocupadas;
    }

    /**
     * Devuelve el número total de ubicaciones físicas disponibles en el almacén.
     *
     * @return número total de ubicaciones (100)
     */
    public int getNumeroTotalUbicaciones() {
        return NUMERO_UBICACIONES;
    }

    /**
     * Devuelve el número de ubicaciones físicas actualmente libres.
     *
     * @return número de ubicaciones libres
     */
    public synchronized int getUbicacionesLibres() {
        int libres = 0;
        for (boolean ocupada : ubicaciones) {
            if (!ocupada) {
                libres++;
            }
        }
        return libres;
    }

    /**
     * Devuelve el estado de una ubicación física específica.
     *
     * @param indice índice de la ubicación (0 a 99)
     * @return {@code true} si la ubicación está ocupada, {@code false} si está libre
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public synchronized boolean getEstadoUbicacion(int indice) {
        if (indice < 0 || indice >= NUMERO_UBICACIONES) {
            throw new IndexOutOfBoundsException("Indice de ubicacion fuera de rango: " + indice);
        }
        return ubicaciones[indice];
    }

    /**
     * Libera las ubicaciones físicas asociadas a un lote de mercadería que sale del almacén.
     * Se liberan las primeras ubicaciones ocupadas encontradas, hasta la cantidad solicitada.
     *
     * @param cantidad número de ubicaciones a liberar
     */
    public synchronized void liberarUbicaciones(int cantidad) {
        int liberadas = 0;
        for (int i = 0; i < NUMERO_UBICACIONES && liberadas < cantidad; i++) {
            if (ubicaciones[i]) {
                ubicaciones[i] = false;
                liberadas++;
            }
        }
    }

    /**
     * Verifica si existe stock suficiente para todos los productos de un pedido.
     *
     * @param productosSolicitados mapa de productos con sus cantidades requeridas
     * @return {@code true} si hay stock suficiente para todos los productos,
     *         {@code false} si falta stock de al menos uno
     */
    public synchronized boolean hayStockParaPedido(Map<Producto, Integer> productosSolicitados) {
        for (Map.Entry<Producto, Integer> entry : productosSolicitados.entrySet()) {
            String codProd = entry.getKey().codigo();
            int cantidadNecesaria = entry.getValue();
            if (existenciasPorProducto.getOrDefault(codProd, 0) < cantidadNecesaria) {
                return false; // Falta stock de al menos un producto
            }
        }
        return true;
    }

    /**
     * Descuenta el stock de los productos cuando un pedido se procesa.
     * Se reduce la cantidad de existencias y el espacio ocupado en el almacén.
     *
     * @param productosSolicitados mapa de productos con sus cantidades a descontar
     */
    public synchronized void descontarStock(Map<Producto, Integer> productosSolicitados) {
        for (Map.Entry<Producto, Integer> entry : productosSolicitados.entrySet()) {
            String codProd = entry.getKey().codigo();
            int cantidadNecesaria = entry.getValue();
            existenciasPorProducto.put(codProd, existenciasPorProducto.get(codProd) - cantidadNecesaria);
            espacioOcupado -= cantidadNecesaria; // Se libera espacio físico
        }
    }

    /**
     * Devuelve el espacio actualmente ocupado en el almacén.
     *
     * @return número de unidades ocupadas
     */
    public synchronized int getEspacioOcupado() {
        return espacioOcupado;
    }

    /**
     * Devuelve la capacidad máxima del almacén.
     *
     * @return capacidad máxima en unidades
     */
    public int getCapacidadMaxima() {
        return CAPACIDAD_MAXIMA;
    }

    /**
     * Devuelve un mapa con las existencias actuales por producto.
     *
     * @return mapa inmutable de código de producto a cantidad disponible
     */
    public synchronized Map<String, Integer> getExistenciasPorProducto() {
        return new ConcurrentHashMap<>(existenciasPorProducto);
    }
}
