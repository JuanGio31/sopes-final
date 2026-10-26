package org.giov.modelo;

/**
 * Representa un cliente registrado en el sistema con un nivel de servicio
 * contratado que determina la prioridad de sus pedidos.
 *
 * <p>Los niveles de servicio van del 1 (Exprés) al 5 (Económico).
 * El nivel 1 es la máxima prioridad y el nivel 5 la mínima.</p>
 */
public class Cliente {

    /** Identificador único del cliente, generado secuencialmente. */
    private final String id;

    /** Nombre completo del cliente. */
    private final String nombre;

    /** Nivel de servicio contratado (1-5). */
    private final int nivelServicio;

    /**
     * Construye un cliente con un ID, nombre y nivel de servicio.
     *
     * @param id identificador único del cliente
     * @param nombre nombre completo del cliente
     * @param nivelServicio nivel de servicio contratado (1-5)
     */
    public Cliente(String id, String nombre, int nivelServicio) {
        this.id = id;
        this.nombre = nombre;
        this.nivelServicio = nivelServicio;
    }

    /**
     * Devuelve el identificador único del cliente.
     *
     * @return ID del cliente
     */
    public String getId() {
        return id;
    }

    /**
     * Devuelve el nombre completo del cliente.
     *
     * @return nombre del cliente
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve el nivel de servicio contratado por el cliente.
     *
     * @return nivel de servicio (1-5)
     */
    public int getNivelServicio() {
        return nivelServicio;
    }

    /**
     * Devuelve el nombre descriptivo del nivel de servicio.
     *
     * @return nombre como "Exprés", "Prioritario", etc.
     */
    public String getNombreNivelServicio() {
        return Pedido.nombreDeNivel(nivelServicio);
    }

    @Override
    public String toString() {
        return nombre + " (" + id + ")";
    }
}
