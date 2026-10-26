package org.giov.gui.controlador;

import org.giov.hilos.SimuladorDeadlock;

import javax.swing.*;

/** Controlador para la simulación y resolución de deadlocks. */
public class DeadlockController {

    /** Callback para notificar cambios de estado a la UI. */
    public interface EstadoListener {
        void onOperacion1Estado(String estado, int color);
        void onOperacion2Estado(String estado, int color);
        void onConflicto(String mensaje, int color);
        void onLog(String mensaje);
        void onBotonesResolucion(boolean habilitado);
    }

    private final SimuladorDeadlock simulador;
    private final EstadoListener listener;
    private Thread hiloMonitoreo;

    public DeadlockController(EstadoListener listener) {
        this.listener = listener;
        this.simulador = new SimuladorDeadlock();
        configurarListener();
    }

    public void iniciarSimulacion() {
        listener.onOperacion1Estado("● Iniciando...", 0xFFB74B);
        listener.onOperacion2Estado("● Iniciando...", 0xFFB74B);
        listener.onConflicto("⏳ Esperando conflicto...", 0xDC3545);
        listener.onLog("═══ Simulación de deadlock iniciada ═══");

        simulador.provocarConflicto();
        iniciarMonitoreo();
    }

    public void resolver(boolean continuarOp1) {
        simulador.resolverConflicto(continuarOp1);
    }

    public void detener() {
        if (simulador != null) {
            simulador.resolverConflicto(true);
        }
    }

    private void configurarListener() {
        simulador.setDeadlockListener(() -> {
            SwingUtilities.invokeLater(() -> {
                listener.onLog("⚠ ¡INTERBLOQUEO DETECTADO! Ambas operaciones bloqueadas mutuamente.");
                listener.onLog("  Operación 1 retiene MONTACARGAS, espera ESCANER_CODIGOS");
                listener.onLog("  Operación 2 retiene ESCANER_CODIGOS, espera MONTACARGAS");
                listener.onLog("  Motivo: Adquisición de recursos en orden inverso (ciclo de dependencias).");
                listener.onLog("Seleccione qué operación debe continuar:");
                listener.onOperacion1Estado("● Bloqueada", 0xFF7979);
                listener.onOperacion2Estado("● Bloqueada", 0xFF7979);
                listener.onConflicto("⚠ INTERBLOQUEO: Seleccione una operación para continuar", 0xDC3545);
                listener.onBotonesResolucion(true);
            });
        });
    }

    private void iniciarMonitoreo() {
        hiloMonitoreo = new Thread(() -> {
            try {
                while (simulador.isOperacion1Activa() || simulador.isOperacion2Activa()) {
                    Thread.sleep(500);
                    SwingUtilities.invokeLater(() -> {
                        if (simulador.isOperacion1Activa() && !simulador.isDeadlockDetectado()) {
                            listener.onOperacion1Estado("● Adquirió MONTACARGAS", 0x3B82F6);
                        }
                        if (simulador.isOperacion2Activa() && !simulador.isDeadlockDetectado()) {
                            listener.onOperacion2Estado("● Adquirió ESCANER_CODIGOS", 0xA855F7);
                        }
                    });
                }
            } catch (InterruptedException ignored) {
            }
        });
        hiloMonitoreo.start();
    }
}
