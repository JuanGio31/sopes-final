package org.giov.modelo;

import java.util.List;

/**
 * Clasifica los productos y determina qué recursos del sistema se
 * necesitan para procesarlos.
 */
public class TipoMercancia {
    private final String nombre;
    private String descripcion;
    private final List<String> recursosRequeridos;

    public TipoMercancia(String nombre, String descripcion,
                         List<String> recursosRequeridos) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.recursosRequeridos = recursosRequeridos;
    }

    public String getNombre() {
        return nombre;
    }

    public List<String> getRecursosRequeridos() {
        return recursosRequeridos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}