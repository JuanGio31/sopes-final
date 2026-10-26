package org.giov.gui.controlador;

import org.giov.Main;
import org.giov.modelo.*;

import java.util.HashMap;
import java.util.Map;

/** Controlador para la creación y envío de pedidos manuales. */
public class PedidoController {

    /** Resultado de intentar enviar un pedido. */
    public static class Resultado {
        public boolean exito;
        public String mensaje;
        public String tipo; // "confirmacion" o "error"

        Resultado(boolean exito, String mensaje, String tipo) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.tipo = tipo;
        }
    }

    /** Valida y envía un pedido al sistema. */
    public Resultado enviarPedido(String clienteId, Map<String, Integer> productosSeleccionados) {
        if (Main.getSistemaPedidos() == null) {
            return new Resultado(false, "El sistema no está inicializado.", "error");
        }

        Cliente cliente = GestorClientes.getInstancia().buscarPorId(clienteId);
        if (cliente == null) {
            return new Resultado(false, "Cliente no encontrado. Seleccione un cliente válido.", "error");
        }

        Map<Producto, Integer> productos = new HashMap<>();
        for (Map.Entry<String, Integer> entry : productosSeleccionados.entrySet()) {
            Producto producto = buscarPorNombre(entry.getKey());
            if (producto != null && entry.getValue() > 0) {
                productos.put(producto, entry.getValue());
            }
        }

        if (productos.isEmpty()) {
            return new Resultado(false, "No se pudo identificar ningún producto. Intente de nuevo.", "error");
        }

        String idPedido = Main.getSistemaPedidos().generarIdUnico(true);
        Pedido pedido = new Pedido(idPedido, cliente, productos);
        Main.getSistemaPedidos().agregarPedido(pedido);

        return new Resultado(true, "Pedido " + idPedido + " agregado para " + cliente.getNombre() + ".", "confirmacion");
    }

    private Producto buscarPorNombre(String nombre) {
        for (Producto p : ConfiguracionMercancias.getCatalogo().values()) {
            if (p.nombre().equals(nombre)) {
                return p;
            }
        }
        return null;
    }
}
