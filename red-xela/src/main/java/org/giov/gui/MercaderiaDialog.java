package org.giov.gui;

import org.giov.modelo.Almacen;
import org.giov.modelo.ConfiguracionMercancias;
import org.giov.modelo.Producto;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Diálogo para registrar la llegada de mercadería al almacén.
 * Permite seleccionar un producto del catálogo y especificar la cantidad.
 */
public class MercaderiaDialog extends JDialog {

    private final JComboBox<String> comboProducto;
    private final JSpinner spinnerCantidad;

    /**
     * Construye el diálogo de registro de mercadería.
     *
     * @param parent ventana padre
     */
    public MercaderiaDialog(JFrame parent) {
        super(parent, "Registrar Mercadería", true);
        setSize(400, 250);
        setLocationRelativeTo(parent);

        // Se configura el panel principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Campos del formulario
        JPanel panelFormulario = new JPanel(new GridLayout(2, 2, 10, 10));
        panelFormulario.add(new JLabel("Producto:"));

        // Se pobla el combo mostrando "Nombre (Código)" para que el usuario identifique cada producto
        String[] productos = new String[ConfiguracionMercancias.getCatalogo().size()];
        int idx = 0;
        for (Map.Entry<String, Producto> entry : ConfiguracionMercancias.getCatalogo().entrySet()) {
            Producto p = entry.getValue();
            productos[idx] = p.nombre() + " (" + entry.getKey() + ")";
            idx++;
        }
        comboProducto = new JComboBox<>(productos);
        panelFormulario.add(comboProducto);

        panelFormulario.add(new JLabel("Cantidad:"));
        spinnerCantidad = new JSpinner(new SpinnerNumberModel(50, 1, 5000, 1));
        panelFormulario.add(spinnerCantidad);

        panel.add(panelFormulario, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnCancelar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        add(panel);

        // Acción del botón registrar
        btnRegistrar.addActionListener(e -> {
            String seleccion = (String) comboProducto.getSelectedItem();
            int cantidad = (int) spinnerCantidad.getValue();

            // Se extrae el código del formato "Nombre (CODIGO)"
            int parentesisInicio = seleccion.lastIndexOf('(');
            int parentesisFin = seleccion.lastIndexOf(')');
            String codigo = (parentesisInicio >= 0 && parentesisFin > parentesisInicio)
                    ? seleccion.substring(parentesisInicio + 1, parentesisFin)
                    : seleccion;

            boolean exito = Almacen.getInstancia().registrarLlegadaMercaderia(codigo, cantidad);
            if (exito) {
                // Se muestra el nombre sin el código en el mensaje
                String nombre = seleccion.substring(0, parentesisInicio).trim();
                JOptionPane.showMessageDialog(this,
                        "Se registraron " + cantidad + " unidades de " + nombre + ".",
                        "Confirmación", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No hay espacio suficiente en el almacén.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Acción del botón cancelar
        btnCancelar.addActionListener(e -> dispose());
    }
}
