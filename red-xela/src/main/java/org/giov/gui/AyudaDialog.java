package org.giov.gui;

import javax.swing.*;
import java.awt.*;

/** Diálogo con instrucciones de uso del sistema. */
public class AyudaDialog extends JDialog {

    public AyudaDialog(JFrame parent) {
        super(parent, "Modo de uso", false);
        setSize(550, 500);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JEditorPane txtAyuda = new JEditorPane("text/html",
                "<html>" +
                "<style>" +
                "body { font-family: SansSerif; font-size: 13px; margin: 8px; }" +
                "h3 { color: #1e1e2e; margin-top: 14px; margin-bottom: 4px; }" +
                "ul { margin: 4px 0; padding-left: 18px; }" +
                "li { margin-bottom: 2px; }" +
                "code { background: #f0f0f0; padding: 1px 4px; border-radius: 3px; font-size: 12px; }" +
                "</style>" +
                "<h3>Dashboard</h3>" +
                "<p>La vista principal muestra el estado del sistema en tiempo real:</p>" +
                "<ul>" +
                "  <li><b>KPIs superiores</b>: Indicadores clave como pedidos en espera, en proceso, completados, tiempo promedio y ocupación de recursos.</li>" +
                "  <li><b>Pedidos en espera</b>: Cola de pedidos pendientes, ordenados según la estrategia de planificación activa.</li>" +
                "  <li><b>Pedidos en proceso</b>: Pedidos que los workers están procesando actualmente con su tiempo transcurrido.</li>" +
                "  <li><b>Recursos compartidos</b>: Tabla con los 6 tipos de recurso, cantidad disponible y quién los usa.</li>" +
                "  <li><b>Actividad</b>: Registro en tiempo real de eventos del sistema con marca de hora.</li>" +
                "</ul>" +
                "<h3>Pestaña Almacén</h3>" +
                "<ul>" +
                "  <li><b>Capacidad</b>: Barra de progreso que muestra el espacio ocupado vs. el total (5000 unidades).</li>" +
                "  <li><b>Existencias</b>: Lista de productos disponibles con sus cantidades actuales.</li>" +
                "  <li><b>Ubicaciones físicas</b>: Grid de 100 espacios donde verde = libre y rojo = ocupado.</li>" +
                "</ul>" +
                "<h3>Menú Archivo</h3>" +
                "<ul>" +
                "  <li><b>Nuevo pedido</b> (<code>Ctrl+N</code>): Abre el formulario para crear un pedido manualmente.</li>" +
                "  <li><b>Registrar mercadería</b> (<code>Ctrl+M</code>): Aumenta el stock del almacén con nuevos productos.</li>" +
                "  <li><b>Salir</b> (<code>Ctrl+Q</code>): Cierra la aplicación.</li>" +
                "</ul>" +
                "<h3>Menú Sistema</h3>" +
                "<ul>" +
                "  <li><b>Detener/Reanudar generador</b>: Pausa o reanuda la creación automática de pedidos.</li>" +
                "  <li><b>Estrategia</b>: Cambia el criterio de ordenamiento de la cola de espera:</li>" +
                "  <ul>" +
                "    <li><b>Prioridad estricta</b>: Primero los niveles Exprés y Prioritario.</li>" +
                "    <li><b>Tiempo de espera</b>: El que llegó primero se procesa primero (FIFO).</li>" +
                "    <li><b>Menor cantidad</b>: Primero los pedidos con menos artículos.</li>" +
                "  </ul>" +
                "  <li><b>Simular deadlock</b>: Crea un conflicto de recursos deliberado para practicar la resolución manual.</li>" +
                "</ul>" +
                "<h3>Resolución de Deadlocks</h3>" +
                "<p>Cuando se detecta un interbloqueo, el diálogo muestra:</p>" +
                "<ul>" +
                "  <li>Los recursos involucrados en el conflicto.</li>" +
                "  <li>Las operaciones bloqueadas y su estado actual.</li>" +
                "  <li>Botones para elegir qué operación continúa y cuál regresa a la cola de espera.</li>" +
                "</ul>" +
                "<p>Seleccione una operación para continuar, liberando los recursos para que el sistema pueda seguir funcionando.</p>" +
                "</html>"
        );
        txtAyuda.setEditable(false);
        txtAyuda.setBackground(new Color(250, 250, 250));
        txtAyuda.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        JScrollPane scrollAyuda = new JScrollPane(txtAyuda);
        scrollAyuda.getViewport().setOpaque(false);
        panel.add(scrollAyuda, BorderLayout.CENTER);

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        panelBoton.add(btnCerrar);
        panel.add(panelBoton, BorderLayout.SOUTH);

        add(panel);
    }
}
