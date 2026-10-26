package org.giov.gui.modelo_tabla;

import org.giov.recursos.GestorRecursos;
import org.giov.recursos.TipoRecurso;

import javax.swing.table.AbstractTableModel;

/** Modelo de tabla para el estado de recursos compartidos. */
public class ModeloRecursos extends AbstractTableModel {

    private static final String[] COLUMNAS = {
            "Tipo", "Total", "Disponibles", "En uso", "Usuario(s)"
    };

    private final GestorRecursos gestor;

    public ModeloRecursos() {
        this.gestor = GestorRecursos.getInstancia();
    }

    @Override
    public int getRowCount() {
        return TipoRecurso.values().length;
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNAS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TipoRecurso tipo = TipoRecurso.values()[rowIndex];
        return switch (columnIndex) {
            case 0 -> tipo.name();
            case 1 -> gestor.getRecursosTotales(tipo);
            case 2 -> gestor.getRecursosDisponibles(tipo);
            case 3 -> {
                int totales = gestor.getRecursosTotales(tipo);
                int disponibles = gestor.getRecursosDisponibles(tipo);
                yield totales - disponibles;
            }
            case 4 -> gestor.getUsuarioActual(tipo);
            default -> "";
        };
    }
}
