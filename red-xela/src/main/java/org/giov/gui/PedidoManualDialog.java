package org.giov.gui;

import org.giov.Main;
import org.giov.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Diálogo para ingresar pedidos manualmente.
 *
 * <p>Permite seleccionar un cliente existente o registrar uno nuevo
 * con su nivel de servicio, luego agregar productos del catálogo
 * y enviar el pedido al sistema.</p>
 */
public class PedidoManualDialog extends JDialog {

    private JComboBox<String> comboCliente;
    private final DefaultComboBoxModel<String> modeloComboCliente;
    private final JComboBox<String> comboProducto;
    private final JSpinner spinnerCantidad;
    private final JTable tablaSeleccion;
    private final DefaultTableModel modeloTablaSeleccion;

    /**
     * Construye el diálogo de pedido manual con un formulario que permite
     * seleccionar o registrar un cliente, agregar productos y enviar el pedido.
     *
     * @param parent ventana padre
     */
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

        // Combo con clientes existentes + opción de nuevo
        modeloComboCliente = new DefaultComboBoxModel<>();
        comboCliente = new JComboBox<>(modeloComboCliente);
        actualizarComboClientes();
        panelFormCliente.add(new JLabel("Cliente existente:"));
        panelFormCliente.add(comboCliente);

        JButton btnNuevoCliente = new JButton("+ Registrar nuevo cliente");
        panelFormCliente.add(new JLabel());
        panelFormCliente.add(btnNuevoCliente);

        panelCliente.add(panelFormCliente, BorderLayout.NORTH);

        btnNuevoCliente.addActionListener(e -> {
            NuevoClienteDialog dialog = new NuevoClienteDialog(this);
            dialog.setVisible(true);
            // Si se registró un cliente nuevo, se actualiza el modelo del combo
            actualizarComboClientes();
        });

        panel.add(panelCliente, BorderLayout.NORTH);

        // ── Selector de productos ──
        JPanel panelProducto = new JPanel(new BorderLayout(10, 8));
        panelProducto.setBorder(BorderFactory.createTitledBorder("Agregar productos"));

        JPanel panelFormProducto = new JPanel(new GridLayout(2, 2, 10, 10));

        // Combo con nombre y código del producto
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

            // Se extrae el nombre sin el código para mostrar en la tabla
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

        // Acción del botón enviar
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
            if (enviarPedido(seleccion)) {
                dispose();
            }
        });

        // Acción del botón cancelar
        btnCancelar.addActionListener(e -> dispose());
    }

    /**
     * Actualiza el modelo del combo de clientes con los clientes registrados
     * y una opción para registrar uno nuevo.
     */
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

    /**
     * Recopila los datos del formulario y envía el pedido al sistema.
     *
     * @param clienteStr la selección del combo de cliente
     * @return {@code true} si el pedido se envió correctamente
     */
    private boolean enviarPedido(String clienteStr) {
        if (Main.getSistemaPedidos() == null) {
            JOptionPane.showMessageDialog(this, "El sistema no está inicializado.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Se extrae el ID del cliente del texto seleccionado: "Nombre (CLT-0001) — Exprés"
        int parentesisInicio = clienteStr.indexOf('(');
        int parentesisFin = clienteStr.indexOf(')');
        String clienteId = clienteStr.substring(parentesisInicio + 1, parentesisFin);
        Cliente cliente = GestorClientes.getInstancia().buscarPorId(clienteId);

        if (cliente == null) {
            JOptionPane.showMessageDialog(this, "Cliente no encontrado. Seleccione un cliente válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String idPedido = Main.getSistemaPedidos().generarIdUnico(true);

        Map<Producto, Integer> productos = new HashMap<>();
        for (int i = 0; i < modeloTablaSeleccion.getRowCount(); i++) {
            Object cantidadObj = modeloTablaSeleccion.getValueAt(i, 1);
            if (cantidadObj instanceof Number) {
                int cantidad = ((Number) cantidadObj).intValue();
                if (cantidad > 0) {
                    String nombre = (String) modeloTablaSeleccion.getValueAt(i, 0);
                    Producto producto = buscarPorNombre(nombre);
                    if (producto != null) {
                        productos.put(producto, cantidad);
                    }
                }
            }
        }

        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se pudo identificar ningún producto. Intente de nuevo.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Pedido pedido = new Pedido(idPedido, cliente, productos);
        Main.getSistemaPedidos().agregarPedido(pedido);
        JOptionPane.showMessageDialog(this,
                "Pedido " + idPedido + " agregado para " + cliente.getNombre() + ".",
                "Confirmación", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /**
     * Busca un producto en el catálogo por su nombre.
     *
     * @param nombre nombre del producto
     * @return el producto encontrado o {@code null}
     */
    private Producto buscarPorNombre(String nombre) {
        for (Producto p : ConfiguracionMercancias.getCatalogo().values()) {
            if (p.nombre().equals(nombre)) {
                return p;
            }
        }
        return null;
    }
}
