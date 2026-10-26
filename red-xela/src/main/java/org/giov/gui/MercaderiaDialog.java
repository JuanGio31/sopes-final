package org.giov.gui;

import org.giov.gui.controlador.MercaderiaController;
import org.giov.modelo.Almacen;
import org.giov.modelo.ConfiguracionMercancias;
import org.giov.modelo.Producto;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/** Diálogo para registrar la llegada de mercadería al almacén. */
public class MercaderiaDialog extends JDialog {

    private final JComboBox<String> comboProducto;
    private final JSpinner spinnerCantidad;

    public MercaderiaDialog(JFrame parent) {
        super(parent, "Registrar Mercadería", true);
        setSize(400, 250);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(2, 2, 10, 10));
        panelFormulario.add(new JLabel("Producto:"));

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

        btnRegistrar.addActionListener(e -> {
            String seleccion = (String) comboProducto.getSelectedItem();
            int cantidad = (int) spinnerCantidad.getValue();

            String codigo = MercaderiaController.extraerCodigo(seleccion);
            MercaderiaController controller = new MercaderiaController();
            MercaderiaController.Resultado resultado = controller.registrar(codigo, cantidad);

            if (resultado.exito) {
                int parentesisInicio = seleccion.lastIndexOf('(');
                String nombre = seleccion.substring(0, parentesisInicio).trim();
                JOptionPane.showMessageDialog(this,
                        "Se registraron " + cantidad + " unidades de " + nombre + ".",
                        "Confirmación", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        resultado.mensaje,
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dispose());
    }
}
