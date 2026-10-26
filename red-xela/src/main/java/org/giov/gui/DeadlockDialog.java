package org.giov.gui;

import org.giov.hilos.SimuladorDeadlock;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Diálogo para simular y resolver deadlocks manualmente.
 * Inicia un {@link SimuladorDeadlock} con detección automática del interbloqueo
 * y permite al usuario decidir qué operación continúa y cuál se interrumpe.
 *
 * <p>El diálogo muestra en tiempo real el estado de cada operación, los recursos
 * involucrados y el motivo del conflicto, cumpliendo con el requisito del proyecto
 * de visualizar y resolver la situación de conflicto de recursos.</p>
 */
public class DeadlockDialog extends JDialog {

    private final JTextArea txtEstado;
    private SimuladorDeadlock simulador;
    private JButton btnSimular;
    private JButton btnContinuarOp1;
    private JButton btnContinuarOp2;
    private JButton btnCerrar;
    private JLabel lblEstadoOp1;
    private JLabel lblEstadoOp2;
    private JLabel lblConflicto;

    /**
     * Construye el diálogo de resolución de deadlocks.
     *
     * @param parent ventana padre
     */
    public DeadlockDialog(JFrame parent) {
        super(parent, "Resolución de Conflicto de Recursos", true);
        setSize(600, 480);
        setLocationRelativeTo(parent);

        // Se cierra el diálogo al destruirlo, limpiando recursos
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (simulador != null) {
                    // Se interrumpen ambas operaciones para evitar hilos huérfanos
                    simulador.resolverConflicto(true);
                }
            }
        });

        // Se configura el panel principal
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ── Encabezado con título y descripción ──
        JPanel panelHeader = new JPanel(new BorderLayout(8, 4));
        JLabel lblTitulo = new JLabel("⚠ CONFLICTO DE RECURSOS");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(220, 53, 69));

        JTextArea lblDescripcion = new JTextArea(
                "Dos operaciones solicitan los mismos dos recursos en orden inverso:\n" +
                "  • Operación 1: MONTACARGAS → ESCANER_CODIGOS\n" +
                "  • Operación 2: ESCANER_CODIGOS → MONTACARGAS\n\n" +
                "Esto genera un ciclo de dependencias mutuas (deadlock).\n" +
                "Seleccione qué operación debe continuar y cuál debe regresar a la cola de espera."
        );
        lblDescripcion.setEditable(false);
        lblDescripcion.setWrapStyleWord(true);
        lblDescripcion.setLineWrap(true);
        lblDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblDescripcion.setBackground(new Color(255, 243, 238));
        lblDescripcion.setBorder(BorderFactory.createLineBorder(new Color(255, 204, 188), 1));

        panelHeader.add(lblTitulo, BorderLayout.NORTH);
        panelHeader.add(lblDescripcion, BorderLayout.CENTER);
        panel.add(panelHeader, BorderLayout.NORTH);

        // ── Panel central: estado de operaciones + log ──
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerSize(4);

        // Estado visual de las operaciones
        JPanel panelEstado = new JPanel(new GridLayout(2, 1, 8, 8));
        panelEstado.setBorder(BorderFactory.createTitledBorder("Estado de las operaciones"));

        // Operación 1
        JPanel rowOp1 = new JPanel(new BorderLayout(10, 0));
        rowOp1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JLabel lblOp1Titulo = new JLabel("Operación 1");
        lblOp1Titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblOp1Titulo.setForeground(new Color(59, 130, 246));
        JLabel lblOp1Recursos = new JLabel("Recurso retenido: MONTACARGAS  |  Espera: ESCANER_CODIGOS");
        lblOp1Recursos.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblOp1Recursos.setForeground(new Color(100, 100, 100));
        lblEstadoOp1 = new JLabel("● Esperando");
        lblEstadoOp1.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEstadoOp1.setForeground(new Color(255, 183, 77));
        lblEstadoOp1.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel colOp1 = new JPanel(new GridLayout(2, 1, 2, 2));
        colOp1.setOpaque(false);
        colOp1.add(lblOp1Titulo);
        colOp1.add(lblOp1Recursos);
        rowOp1.add(colOp1, BorderLayout.WEST);
        rowOp1.add(lblEstadoOp1, BorderLayout.EAST);
        panelEstado.add(rowOp1);

        // Operación 2
        JPanel rowOp2 = new JPanel(new BorderLayout(10, 0));
        rowOp2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JLabel lblOp2Titulo = new JLabel("Operación 2");
        lblOp2Titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblOp2Titulo.setForeground(new Color(168, 85, 247));
        JLabel lblOp2Recursos = new JLabel("Recurso retenido: ESCANER_CODIGOS  |  Espera: MONTACARGAS");
        lblOp2Recursos.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblOp2Recursos.setForeground(new Color(100, 100, 100));
        lblEstadoOp2 = new JLabel("● Esperando");
        lblEstadoOp2.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEstadoOp2.setForeground(new Color(255, 183, 77));
        lblEstadoOp2.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel colOp2 = new JPanel(new GridLayout(2, 1, 2, 2));
        colOp2.setOpaque(false);
        colOp2.add(lblOp2Titulo);
        colOp2.add(lblOp2Recursos);
        rowOp2.add(colOp2, BorderLayout.WEST);
        rowOp2.add(lblEstadoOp2, BorderLayout.EAST);
        panelEstado.add(rowOp2);

        // Indicador de conflicto
        lblConflicto = new JLabel("  ");
        lblConflicto.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblConflicto.setForeground(new Color(220, 53, 69));
        lblConflicto.setHorizontalAlignment(SwingConstants.CENTER);
        lblConflicto.setText("⏳ Presione 'Simular deadlock' para iniciar");

        JPanel panelEstadoWrapper = new JPanel(new BorderLayout(0, 8));
        panelEstadoWrapper.add(panelEstado, BorderLayout.CENTER);
        panelEstadoWrapper.add(lblConflicto, BorderLayout.SOUTH);

        // Log de actividad
        txtEstado = new JTextArea();
        txtEstado.setEditable(false);
        txtEstado.setWrapStyleWord(true);
        txtEstado.setLineWrap(true);
        txtEstado.setFont(new Font("SansSerif", Font.PLAIN, 11));
        JScrollPane scrollLog = new JScrollPane(txtEstado);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Registro de actividad"));

        splitPane.setTopComponent(panelEstadoWrapper);
        splitPane.setBottomComponent(scrollLog);
        panel.add(splitPane, BorderLayout.CENTER);

        // ── Botones de acción ──
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        btnSimular = new JButton("▶ Simular deadlock");
        btnContinuarOp1 = new JButton("✓ Continuar Operación 1");
        btnContinuarOp2 = new JButton("✓ Continuar Operación 2");
        btnCerrar = new JButton("Cerrar");

        btnContinuarOp1.setEnabled(false);
        btnContinuarOp2.setEnabled(false);

        panelBotones.add(btnSimular);
        panelBotones.add(btnContinuarOp1);
        panelBotones.add(btnContinuarOp2);
        panelBotones.add(btnCerrar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        add(panel);

        // ── Acción del botón simular ──
        btnSimular.addActionListener(e -> iniciarSimulacion());

        // ── Acción para continuar con la Operación 1 ──
        btnContinuarOp1.addActionListener(e -> {
            if (simulador != null) {
                simulador.resolverConflicto(true);
                txtEstado.append("→ Resolución: Operación 1 continúa, Operación 2 regresa a la cola de espera.\n");
                btnContinuarOp1.setEnabled(false);
                btnContinuarOp2.setEnabled(false);
                lblEstadoOp1.setText("✓ Completada");
                lblEstadoOp1.setForeground(new Color(72, 209, 133));
                lblEstadoOp2.setText("✗ Interrumpida");
                lblEstadoOp2.setForeground(new Color(255, 121, 121));
                lblConflicto.setText("✓ Conflicto resuelto: Operación 1 continuó con los recursos disputados.");
                lblConflicto.setForeground(new Color(72, 209, 133));
            }
        });

        // ── Acción para continuar con la Operación 2 ──
        btnContinuarOp2.addActionListener(e -> {
            if (simulador != null) {
                simulador.resolverConflicto(false);
                txtEstado.append("→ Resolución: Operación 2 continúa, Operación 1 regresa a la cola de espera.\n");
                btnContinuarOp1.setEnabled(false);
                btnContinuarOp2.setEnabled(false);
                lblEstadoOp2.setText("✓ Completada");
                lblEstadoOp2.setForeground(new Color(72, 209, 133));
                lblEstadoOp1.setText("✗ Interrumpida");
                lblEstadoOp1.setForeground(new Color(255, 121, 121));
                lblConflicto.setText("✓ Conflicto resuelto: Operación 2 continuó con los recursos disputados.");
                lblConflicto.setForeground(new Color(72, 209, 133));
            }
        });

        // ── Acción del botón cerrar ──
        btnCerrar.addActionListener(e -> dispose());
    }

    /**
     * Inicia la simulación de deadlock con detección automática.
     * Se registran los eventos en el log y se actualizan los indicadores
     * de estado de cada operación.
     */
    private void iniciarSimulacion() {
        txtEstado.setText("");
        btnSimular.setEnabled(false);
        btnContinuarOp1.setEnabled(false);
        btnContinuarOp2.setEnabled(false);

        // Se reinician los indicadores visuales
        lblEstadoOp1.setText("● Iniciando...");
        lblEstadoOp1.setForeground(new Color(255, 183, 77));
        lblEstadoOp2.setText("● Iniciando...");
        lblEstadoOp2.setForeground(new Color(255, 183, 77));
        lblConflicto.setText("⏳ Esperando conflicto...");
        lblConflicto.setForeground(new Color(220, 53, 69));

        txtEstado.append("═══ Simulación de deadlock iniciada ═══\n");

        // Se crea el simulador con listener para detección automática
        simulador = new SimuladorDeadlock();
        simulador.setDeadlockListener(() -> {
            SwingUtilities.invokeLater(() -> {
                txtEstado.append("⚠ ¡INTERBLOQUEO DETECTADO! Ambas operaciones bloqueadas mutuamente.\n");
                txtEstado.append("  Operación 1 retiene MONTACARGAS, espera ESCANER_CODIGOS\n");
                txtEstado.append("  Operación 2 retiene ESCANER_CODIGOS, espera MONTACARGAS\n");
                txtEstado.append("  Motivo: Adquisición de recursos en orden inverso (ciclo de dependencias).\n\n");
                txtEstado.append("Seleccione qué operación debe continuar:\n");
                lblEstadoOp1.setText("● Bloqueada");
                lblEstadoOp1.setForeground(new Color(255, 121, 121));
                lblEstadoOp2.setText("● Bloqueada");
                lblEstadoOp2.setForeground(new Color(255, 121, 121));
                lblConflicto.setText("⚠ INTERBLOQUEO: Seleccione una operación para continuar");
                lblConflicto.setForeground(new Color(220, 53, 69));
                btnContinuarOp1.setEnabled(true);
                btnContinuarOp2.setEnabled(true);
            });
        });

        // Se ejecuta la simulación
        simulador.provocarConflicto();

        // Se monitorean los estados de las operaciones para actualizar la UI
        new Thread(() -> {
            try {
                while (simulador.isOperacion1Activa() || simulador.isOperacion2Activa()) {
                    Thread.sleep(500);
                    SwingUtilities.invokeLater(() -> {
                        if (simulador.isOperacion1Activa() && !simulador.isDeadlockDetectado()) {
                            lblEstadoOp1.setText("● Adquirió MONTACARGAS");
                            lblEstadoOp1.setForeground(new Color(59, 130, 246));
                        }
                        if (simulador.isOperacion2Activa() && !simulador.isDeadlockDetectado()) {
                            lblEstadoOp2.setText("● Adquirió ESCANER_CODIGOS");
                            lblEstadoOp2.setForeground(new Color(168, 85, 247));
                        }
                    });
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
    }
}
