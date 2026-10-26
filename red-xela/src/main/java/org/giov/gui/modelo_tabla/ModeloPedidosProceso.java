package org.giov.gui.modelo_tabla;

import org.giov.Main;
import org.giov.hilos.SistemaPedidos;
import org.giov.modelo.Pedido;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Modelo de tabla que muestra los pedidos actualmente siendo procesados por los workers.
 * Lee del {@link SistemaPedidos} la lista de pedidos en proceso.
 */
public class ModeloPedidosProceso extends AbstractTableModel {

    private static final String[] COLUMNAS = {
            "ID", "Cliente", "Prioridad", "Artículos", "Estado", "Tiempo transcurrido (s)"
    };

    /**
     * Devuelve el nombre de la columna indicada.
     */
    @Override
    public String getColumnName(int column) {
        return COLUMNAS[column];
    }

    @Override
    public int getRowCount() {
        SistemaPedidos sistema = Main.getSistemaPedidos();
        if (sistema == null) return 0;
        return sistema.getPedidosProcesando().size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SistemaPedidos sistema = Main.getSistemaPedidos();
        if (sistema == null) return "";
        List<Pedido> procesando = sistema.getPedidosProcesando();
        if (rowIndex >= procesando.size()) return "";
        Pedido pedido = procesando.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> pedido.getIdPedido();
            case 1 -> pedido.getNombreCliente();
            case 2 -> pedido.getNombreNivelServicio() + " (" + pedido.getNivelServicio() + ")";
            case 3 -> pedido.getCantidadTotalArticulos();
            case 4 -> pedido.getEstadoActual().name();
            case 5 -> {
                // Se calcula el tiempo transcurrido desde la llegada del pedido
                long diff = System.currentTimeMillis() - pedido.getTiempoLlegada();
                yield diff / 1000;
            }
            default -> "";
        };
    }
}
