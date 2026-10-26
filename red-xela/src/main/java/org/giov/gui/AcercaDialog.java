package org.giov.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo "Acerca de" que muestra información general del proyecto REDXela.
 * Se abre desde el menú Ayuda > Acerca de REDXela.
 */
public class AcercaDialog extends JDialog {

    /**
     * Construye el diálogo con la información del proyecto.
     *
     * @param parent ventana padre
     */
    public AcercaDialog(JFrame parent) {
        super(parent, "Acerca de REDXela", false);
        setSize(450, 320);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Encabezado con título ──
        JPanel panelHeader = new JPanel(new BorderLayout(8, 4));
        JLabel lblTitulo = new JLabel("REDXela - Centro de Distribución");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(30, 30, 46));
        JLabel lblVersion = new JLabel("Versión 1.0 — Simulador de concurrencia y sincronización");
        lblVersion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblVersion.setForeground(new Color(100, 100, 100));

        panelHeader.add(lblTitulo, BorderLayout.NORTH);
        panelHeader.add(lblVersion, BorderLayout.SOUTH);
        panel.add(panelHeader, BorderLayout.NORTH);

        // ── Descripción ──
        JTextArea txtDescripcion = new JTextArea(
                "Sistema de simulación de un centro de distribución que demuestra\n" +
                "conceptos de programación concurrente en Java:\n\n" +
                "  • Planificación y priorización de pedidos (5 niveles de servicio)\n" +
                "  • Gestión de recursos compartidos con semáforos\n" +
                "  • Comunicación entre procesos (productor-consumidor)\n" +
                "  • Detección y resolución de interbloqueos (deadlocks)\n" +
                "  • Monitoreo en tiempo real mediante interfaz gráfica\n\n" +
                "Desarrollado como proyecto de Sistemas Operativos.\n" +
                "Autor: Giovanic"
        );
        txtDescripcion.setEditable(false);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtDescripcion.setBackground(new Color(247, 247, 247));
        txtDescripcion.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        txtDescripcion.setOpaque(true);
        panel.add(txtDescripcion, BorderLayout.CENTER);

        // ── Botón cerrar ──
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        panelBoton.add(btnCerrar);
        panel.add(panelBoton, BorderLayout.SOUTH);

        add(panel);
    }
}
