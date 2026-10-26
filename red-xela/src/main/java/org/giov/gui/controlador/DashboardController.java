package org.giov.gui.controlador;

import org.giov.Main;
import org.giov.hilos.SistemaPedidos;
import org.giov.planificacion.EstrategiaPlanificacion;
import org.giov.recursos.GestorRecursos;
import org.giov.recursos.TipoRecurso;

/** Controlador que lee el estado del sistema para el dashboard. */
public class DashboardController {

    private int ultimoCompletados = 0;
    private int ultimoEnProceso = 0;
    private int ultimoEnEspera = 0;

    /** Retorna los datos actuales del sistema. */
    public EstadoDashboard obtenerEstado() {
        EstadoDashboard estado = new EstadoDashboard();
        SistemaPedidos sistema = Main.getSistemaPedidos();
        if (sistema == null || !sistema.isInicializado()) return estado;

        estado.enEspera = sistema.getColaEspera().size();
        estado.enProceso = sistema.getPedidosProcesando().size();
        estado.completados = sistema.getPedidosCompletados();
        estado.bloqueados = sistema.getPedidosBloqueados().size();

        GestorRecursos gestor = GestorRecursos.getInstancia();
        int totalRecursos = 0;
        int recursosEnUso = 0;
        for (TipoRecurso tr : TipoRecurso.values()) {
            totalRecursos += gestor.getRecursosTotales(tr);
            recursosEnUso += gestor.getUsuariosActivos(tr).size();
        }
        estado.ocupacionRecursos = totalRecursos > 0 ? (recursosEnUso * 100.0 / totalRecursos) : 0;

        EstrategiaPlanificacion estrategia = sistema.getEstrategiaActual();
        if (estrategia != null) {
            estado.nombreEstrategia = estrategia.getClass().getSimpleName()
                    .replace("Estrategia", "")
                    .replace("PrioridadEstricta", "Prioridad estricta")
                    .replace("TiempoEspera", "Tiempo de espera")
                    .replace("MenorCantidad", "Menor cantidad");
        }

        if (estado.completados > 0) {
            estado.tiempoPromedio = (double) (System.currentTimeMillis() - obtenerTiempoInicioEstimado(sistema))
                    / estado.completados / 1000.0;
        }

        return estado;
    }

    /** Detecta cambios y retorna mensajes de actividad, o null si no hay cambios. */
    public String[] detectarCambios(EstadoDashboard estado) {
        java.util.ArrayList<String> mensajes = new java.util.ArrayList<>();

        if (estado.enEspera > ultimoEnEspera) {
            mensajes.add(estado.enEspera - ultimoEnEspera + " nuevo(s) pedido(s) en cola de espera.");
        }
        ultimoEnEspera = estado.enEspera;

        if (estado.completados > ultimoCompletados) {
            mensajes.add(estado.completados - ultimoCompletados + " pedido(s) completado(s). Total: " + estado.completados);
        }
        ultimoCompletados = estado.completados;

        if (estado.enProceso > ultimoEnProceso) {
            mensajes.add(estado.enProceso - ultimoEnProceso + " pedido(s) iniciado(s) en proceso.");
        }
        ultimoEnProceso = estado.enProceso;

        if (estado.bloqueados > 0) {
            mensajes.add("⚠ " + estado.bloqueados + " pedido(s) bloqueado(s) por falta de stock.");
        }

        return mensajes.toArray(new String[0]);
    }

    /** Datos actuales del sistema para actualizar la UI. */
    public static class EstadoDashboard {
        public int enEspera = 0;
        public int enProceso = 0;
        public int completados = 0;
        public int bloqueados = 0;
        public double ocupacionRecursos = 0;
        public double tiempoPromedio = 0;
        public String nombreEstrategia = "";
    }

    private long obtenerTiempoInicioEstimado(SistemaPedidos sistema) {
        if (sistema != null && !sistema.getPedidosProcesando().isEmpty()) {
            return sistema.getPedidosProcesando().getFirst().getTiempoLlegada();
        }
        return System.currentTimeMillis() - 60000;
    }
}
