package org.giov.gui;

import org.giov.Main;
import org.giov.gui.controlador.PedidoController;
import org.giov.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Diálogo para ingresar pedidos manualmente. */
public class PedidoManualDialog extends JDialog {

    private JComboBox<String> comboCliente;
    private final DefaultComboBoxModel<String> modeloComboCliente;
    private final JComboBox<String> comboProducto;
    private final JSpinner spinnerCantidad;
    private final JTable tablaSeleccion;
    private final DefaultTableModel modeloTablaSeleccion;

    private final PedidoController controller;

    public PedidoManualDialog(JFrame parent) {
        super(parent, "Ingresar Pedido Manual", true);
        setSize(560, 500);
        setLocationRelativeTo(parent);

        // Se configura el panel principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Selección del cliente ──
        JPanel panelCliente = new JPanel(new BorderLayout(10, 8));
        panelCliente.setBorder(BorderFactory.createTitledBorder("Cliente"));

        JPanel panelFormCliente = new JPanel(new GridLayout(2, 2, 10, 10));

        modeloComboCliente = new DefaultComboBoxModel<>();
        comboCliente = new JComboBox<>(modeloComboCliente);
        actualizarComboClientes();
        controller = new PedidoController();
        panelFormCliente.add(new JLabel("Cliente existente:"));
        panelFormCliente.add(comboCliente);

        JButton btnNuevoCliente = new JButton("+ Registrar nuevo cliente");
        panelFormCliente.add(new JLabel());
        panelFormCliente.add(btnNuevoCliente);

        panelCliente.add(panelFormCliente, BorderLayout.NORTH);

        btnNuevoCliente.addActionListener(e -> {
            NuevoClienteDialog dialog = new NuevoClienteDialog(this);
            dialog.setVisible(true);
            actualizarComboClientes();
        });

        panel.add(panelCliente, BorderLayout.NORTH);

        // ── Selector de productos ──
        JPanel panelProducto = new JPanel(new BorderLayout(10, 8));
        panelProducto.setBorder(BorderFactory.createTitledBorder("Agregar productos"));

        JPanel panelFormProducto = new JPanel(new GridLayout(2, 2, 10, 10));

        String[] productos = new String[ConfiguracionMercancias.getCatalogo().size()];
        int idx = 0;
        for (Map.Entry<String, Producto> entry : ConfiguracionMercancias.getCatalogo().entrySet()) {
            Producto p = entry.getValue();
            productos[idx] = p.nombre() + " (" + entry.getKey() + ")";
            idx++;
        }
        comboProducto = new JComboBox<>(productos);
        spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));

        panelFormProducto.add(new JLabel("Producto:"));
        panelFormProducto.add(comboProducto);
        panelFormProducto.add(new JLabel("Cantidad:"));
        panelFormProducto.add(spinnerCantidad);

        JButton btnAgregar = new JButton("+ Agregar");
        panelFormProducto.add(btnAgregar);

        panelProducto.add(panelFormProducto, BorderLayout.NORTH);

        // Tabla de productos seleccionados
        modeloTablaSeleccion = new DefaultTableModel(new Object[]{"Producto", "Cantidad"}, 0);
        tablaSeleccion = new JTable(modeloTablaSeleccion);
        tablaSeleccion.setAutoCreateRowSorter(true);
        JScrollPane scrollSeleccion = new JScrollPane(tablaSeleccion);
        scrollSeleccion.setPreferredSize(new Dimension(0, 120));
        panelProducto.add(scrollSeleccion, BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> {
            String seleccion = (String) comboProducto.getSelectedItem();
            int cantidad = (int) spinnerCantidad.getValue();
            int parentesisInicio = seleccion.lastIndexOf('(');
            String nombre = seleccion.substring(0, parentesisInicio).trim();
            modeloTablaSeleccion.addRow(new Object[]{nombre, cantidad});
        });

        panel.add(panelProducto, BorderLayout.CENTER);

        // ── Botones ──
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEnviar = new JButton("Enviar pedido");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnEnviar);
        panelBotones.add(btnCancelar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        add(panel);

        btnEnviar.addActionListener(e -> {
            String seleccion = (String) comboCliente.getSelectedItem();
            if (seleccion == null || seleccion.startsWith(">")) {
                JOptionPane.showMessageDialog(this, "Seleccione o registre un cliente.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (modeloTablaSeleccion.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Agregue al menos un producto al pedido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int parentesisInicio = seleccion.indexOf('(');
            int parentesisFin = seleccion.indexOf(')');
            String clienteId = seleccion.substring(parentesisInicio + 1, parentesisFin);

            java.util.Map<String, Integer> productosMap = new java.util.HashMap<>();
            for (int i = 0; i < modeloTablaSeleccion.getRowCount(); i++) {
                Object cantidadObj = modeloTablaSeleccion.getValueAt(i, 1);
                if (cantidadObj instanceof Number) {
                    String nombre = (String) modeloTablaSeleccion.getValueAt(i, 0);
                    productosMap.put(nombre, ((Number) cantidadObj).intValue());
                }
            }

            PedidoController.Resultado resultado = controller.enviarPedido(clienteId, productosMap);
            int msgType = resultado.tipo.equals("confirmacion") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
            String title = resultado.tipo.equals("confirmacion") ? "Confirmación" : "Error";
            JOptionPane.showMessageDialog(this, resultado.mensaje, title, msgType);

            if (resultado.exito) {
                dispose();
            }
        });

        btnCancelar.addActionListener(e -> dispose());
    }

    private void actualizarComboClientes() {
        GestorClientes gestor = GestorClientes.getInstancia();
        List<Cliente> clientes = gestor.obtenerTodos();

        modeloComboCliente.removeAllElements();
        modeloComboCliente.addElement("> Registrar nuevo cliente...");
        for (Cliente c : clientes) {
            modeloComboCliente.addElement(c.getNombre() + " (" + c.getId() + ") — " + c.getNombreNivelServicio());
        }
        comboCliente.setSelectedIndex(0);
    }

}
