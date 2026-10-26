package org.giov.modelo;

/**
 * Representa un artículo del catálogo, vinculando la mercancía
 * con sus requerimientos operativos.
 */
public record Producto(String codigo, String nombre, TipoMercancia tipoMercancia) {
}