package org.giov.gui;

import org.giov.modelo.GestorClientes;
import org.giov.modelo.Pedido;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para registrar un nuevo cliente en el sistema.
 *
 * <p>Permite ingresar el nombre del cliente y seleccionar el nivel de servicio
 * que desea contratar. Al confirmar, se registra en el GestorClientes.</p>
 */
public class NuevoClienteDialog extends JDialog {

    private final JTextField txtNombre;
    private final JComboBox<String> comboNivel;

    /**
     * Construye el diálogo con un formulario simple: nombre y nivel de servicio.
     *
     * @param parent ventana padre
     */
    public NuevoClienteDialog(Component parent) {
        super(
                parent instanceof JFrame ? (JFrame) parent :
                parent instanceof Frame ? (Frame) parent : null,
                "Registrar Nuevo Cliente",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        setSize(420, 220);
        setLocationRelativeTo(parent);

        // Se configura el panel principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Campos del formulario ──
        JPanel panelForm = new JPanel(new GridLayout(2, 2, 10, 10));

        txtNombre = new JTextField();
        panelForm.add(new JLabel("Nombre del cliente:"));
        panelForm.add(txtNombre);

        String[] niveles = {"1 - Exprés", "2 - Prioritario", "3 - Estándar", "4 - Programado", "5 - Económico"};
        comboNivel = new JComboBox<>(niveles);
        panelForm.add(new JLabel("Nivel de servicio:"));
        panelForm.add(comboNivel);

        panel.add(panelForm, BorderLayout.CENTER);

        // ── Botones ──
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnCancelar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        add(panel);

        // Acción del botón registrar
        btnRegistrar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el nombre del cliente.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int nivel = comboNivel.getSelectedIndex() + 1; // 1-5
            GestorClientes.getInstancia().registrarCliente(nombre, nivel);

            JOptionPane.showMessageDialog(this,
                    "Cliente \"" + nombre + "\" registrado con nivel " + Pedido.nombreDeNivel(nivel) + ".",
                    "Confirmación", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        // Acción del botón cancelar
        btnCancelar.addActionListener(e -> dispose());
    }
}
