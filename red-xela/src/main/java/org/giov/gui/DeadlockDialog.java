package org.giov.gui;

import org.giov.gui.controlador.DeadlockController;
import org.giov.hilos.SimuladorDeadlock;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Diálogo para simular y resolver deadlocks manualmente. */
public class DeadlockDialog extends JDialog {

    private final JTextArea txtEstado;
    private DeadlockController controller;
    private JButton btnSimular;
    private JButton btnContinuarOp1;
    private JButton btnContinuarOp2;
    private JButton btnCerrar;
    private JLabel lblEstadoOp1;
    private JLabel lblEstadoOp2;
    private JLabel lblConflicto;

    public DeadlockDialog(JFrame parent) {
        super(parent, "Resolución de Conflicto de Recursos", true);
        setSize(600, 480);
        setLocationRelativeTo(parent);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (controller != null) {
                    controller.detener();
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel panelHeader = new JPanel(new BorderLayout(8, 4));
        JLabel lblTitulo = new JLabel(" CONFLICTO DE RECURSOS");
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

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerSize(4);

        JPanel panelEstado = new JPanel(new GridLayout(2, 1, 8, 8));
        panelEstado.setBorder(BorderFactory.createTitledBorder("Estado de las operaciones"));

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

        lblConflicto = new JLabel("  ");
        lblConflicto.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblConflicto.setForeground(new Color(220, 53, 69));
        lblConflicto.setHorizontalAlignment(SwingConstants.CENTER);
        lblConflicto.setText("⏳ Presione 'Simular deadlock' para iniciar");

        JPanel panelEstadoWrapper = new JPanel(new BorderLayout(0, 8));
        panelEstadoWrapper.add(panelEstado, BorderLayout.CENTER);
        panelEstadoWrapper.add(lblConflicto, BorderLayout.SOUTH);

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

        btnSimular.addActionListener(e -> iniciarSimulacion());

        btnContinuarOp1.addActionListener(e -> {
            if (controller != null) {
                controller.resolver(true);
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

        btnContinuarOp2.addActionListener(e -> {
            if (controller != null) {
                controller.resolver(false);
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

        btnCerrar.addActionListener(e -> dispose());
    }

    private void iniciarSimulacion() {
        txtEstado.setText("");
        btnSimular.setEnabled(false);
        btnContinuarOp1.setEnabled(false);
        btnContinuarOp2.setEnabled(false);

        controller = new DeadlockController(new DeadlockController.EstadoListener() {
            @Override
            public void onOperacion1Estado(String estado, int color) {
                lblEstadoOp1.setText(estado);
                lblEstadoOp1.setForeground(new Color(color));
            }

            @Override
            public void onOperacion2Estado(String estado, int color) {
                lblEstadoOp2.setText(estado);
                lblEstadoOp2.setForeground(new Color(color));
            }

            @Override
            public void onConflicto(String mensaje, int color) {
                lblConflicto.setText(mensaje);
                lblConflicto.setForeground(new Color(color));
            }

            @Override
            public void onLog(String mensaje) {
                txtEstado.append(mensaje + "\n");
            }

            @Override
            public void onBotonesResolucion(boolean habilitado) {
                btnContinuarOp1.setEnabled(habilitado);
                btnContinuarOp2.setEnabled(habilitado);
            }
        });
        controller.iniciarSimulacion();
    }
}
