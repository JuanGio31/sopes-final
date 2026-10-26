package org.giov.gui.controlador;

import org.giov.modelo.Almacen;

/** Controlador para registrar mercadería en el almacén. */
public class MercaderiaController {

    /** Resultado del registro de mercadería. */
    public static class Resultado {
        public boolean exito;
        public String mensaje;

        Resultado(boolean exito, String mensaje) {
            this.exito = exito;
            this.mensaje = mensaje;
        }
    }

    /** Registra la llegada de mercadería al almacén. */
    public Resultado registrar(String codigo, int cantidad) {
        boolean exito = Almacen.getInstancia().registrarLlegadaMercaderia(codigo, cantidad);
        if (exito) {
            return new Resultado(true, "Se registraron " + cantidad + " unidades de " + codigo + ".");
        } else {
            return new Resultado(false, "No hay espacio suficiente en el almacén.");
        }
    }

    /** Extrae el código del formato "Nombre (CODIGO)". */
    public static String extraerCodigo(String seleccion) {
        int parentesisInicio = seleccion.lastIndexOf('(');
        int parentesisFin = seleccion.lastIndexOf(')');
        if (parentesisInicio >= 0 && parentesisFin > parentesisInicio) {
            return seleccion.substring(parentesisInicio + 1, parentesisFin);
        }
        return seleccion;
    }
}
