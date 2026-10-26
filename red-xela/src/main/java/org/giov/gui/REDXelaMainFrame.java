package org.giov.gui;

import org.giov.Main;
import org.giov.gui.modelo_tabla.ModeloPedidosEspera;
import org.giov.gui.modelo_tabla.ModeloPedidosProceso;
import org.giov.gui.modelo_tabla.ModeloRecursos;
import org.giov.hilos.GeneradorPedidos;
import org.giov.hilos.SistemaPedidos;
import org.giov.modelo.Almacen;
import org.giov.planificacion.EstrategiaMenorCantidad;
import org.giov.planificacion.EstrategiaPrioridadEstricta;
import org.giov.planificacion.EstrategiaPlanificacion;
import org.giov.planificacion.EstrategiaTiempoEspera;
import org.giov.recursos.GestorRecursos;
import org.giov.recursos.TipoRecurso;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Ventana principal de la aplicación REDXela con diseño de dashboard
 * organizado en pestañas para separar la información por relevancia.
 *
 * <p>La pestaña principal (Dashboard) muestra los indicadores clave,
 * pedidos en espera y en proceso, recursos compartidos y actividad reciente.
 * La pestaña Almacén muestra el estado del inventario. Las acciones de
 * control se acceden desde el menú global (Archivo, Sistema, Ayuda).</p>
 */
public class REDXelaMainFrame extends JFrame {

    // ── Constantes de tipografía global ──
    private static final String FUENTE_PRINCIPAL = "SansSerif";
    private static final Font FUENTE_TITULO = new Font(FUENTE_PRINCIPAL, Font.BOLD, 18);
    private static final Font FUENTE_SUBTITULO = new Font(FUENTE_PRINCIPAL, Font.BOLD, 13);
    private static final Font FUENTE_TABLA_HEADER = new Font(FUENTE_PRINCIPAL, Font.BOLD, 12);
    private static final Font FUENTE_TABLA = new Font(FUENTE_PRINCIPAL, Font.PLAIN, 13);
    private static final Font FUENTE_LOG = new Font(FUENTE_PRINCIPAL, Font.PLAIN, 12);
    private static final Font FUENTE_LABEL = new Font(FUENTE_PRINCIPAL, Font.PLAIN, 13);
    private static final Font FUENTE_LABEL_BOLD = new Font(FUENTE_PRINCIPAL, Font.BOLD, 13);
    private static final Font FUENTE_LABEL_SMALL = new Font(FUENTE_PRINCIPAL, Font.PLAIN, 12);
    private static final Font FUENTE_LABEL_ITALIC = new Font(FUENTE_PRINCIPAL, Font.ITALIC, 12);
    private static final Font FUENTE_KPI_TITULO = new Font(FUENTE_PRINCIPAL, Font.BOLD, 10);
    private static final Font FUENTE_KPI_VALOR = new Font(FUENTE_PRINCIPAL, Font.BOLD, 22);

    private final ModeloRecursos modeloRecursos;
    private final ModeloPedidosEspera modeloPedidosEspera;
    private final ModeloPedidosProceso modeloPedidosProceso;
    private final Timer timerActualizacion;

    // Referencias a componentes que se actualizan periódicamente
    private JLabel lblSimulando;
    private KPIPanel kpiEnEspera;
    private KPIPanel kpiEnProceso;
    private KPIPanel kpiCompletados;
    private KPIPanel kpiTiempoProm;
    private KPIPanel kpiRecursosOcupacion;
    private JTable tablaEspera;
    private JTable tablaRecursos;
    private JTable tablaProceso;
    private JTextArea txtActividad;
    private JProgressBar progressBarAlmacen;
    private JPanel gridUbicaciones;
    private DefaultListModel<String> modeloExistencias;
    private JList<String> listaExistencias;
    private JLabel lblEstrategiaActiva;

    /** Historial de mensajes de actividad para detectar cambios. */
    private int ultimoCompletados = 0;
    private int ultimoEnProceso = 0;
    private int ultimoEnEspera = 0;

    /**
     * Construye la ventana principal con diseño de pestañas y menú global.
     */
    public REDXelaMainFrame() {
        super("REDXela - Centro de Distribución");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);

        // Se inicializan los modelos de tabla
        modeloRecursos = new ModeloRecursos();
        modeloPedidosEspera = new ModeloPedidosEspera();
        modeloPedidosProceso = new ModeloPedidosProceso();

        setLayout(new BorderLayout(0, 0));
        setJMenuBar(crearMenuGlobal());
        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(crearPestañas(), BorderLayout.CENTER);

        // Se configura el temporizador de actualización cada 1 segundo
        timerActualizacion = new Timer(1000, e -> actualizarPaneles());
        timerActualizacion.start();

        // Mensaje inicial de actividad
        agregarActividad("Sistema REDXela iniciado correctamente.");
    }

    /**
     * Crea la barra superior con el título y el indicador de estado.
     */
    private JPanel crearBarraSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(8, 16, 8, 16));
        panel.setBackground(new Color(30, 30, 46));
        panel.setForeground(Color.WHITE);

        JLabel lblTitulo = new JLabel("REDXela - Centro de Distribución");
        lblTitulo.setFont(FUENTE_TITULO);
        lblTitulo.setForeground(Color.WHITE);

        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        panelDerecha.setOpaque(false);

        lblSimulando = new JLabel("● SIMULANDO");
        lblSimulando.setFont(FUENTE_SUBTITULO);
        lblSimulando.setForeground(new Color(72, 209, 133));

        panelDerecha.add(lblSimulando);

        panel.add(lblTitulo, BorderLayout.WEST);
        panel.add(panelDerecha, BorderLayout.EAST);

        return panel;
    }

    /**
     * Crea el panel de pestañas con las dos secciones principales:
     * Dashboard (indicadores y monitoreo) y Almacén.
     */
    private JTabbedPane crearPestañas() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FUENTE_TABLA_HEADER);

        // Pestaña 1: Dashboard principal
        tabs.addTab("Dashboard", crearPanelDashboard());

        // Pestaña 2: Almacén
        tabs.addTab("Almacén", crearPanelAlmacen());

        return tabs;
    }

    /**
     * Crea el panel del dashboard con KPIs, pedidos y recursos.
     * Es la vista principal que muestra lo más importante del sistema.
     */
    private JPanel crearPanelDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(12, 12));
        dashboard.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panel de estadísticas rápidas (fila superior)
        dashboard.add(crearPanelEstadisticasRapidas(), BorderLayout.NORTH);

        // Contenido principal dividido en dos columnas
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(420);
        splitPane.setDividerSize(4);

        // Columna izquierda: Pedidos en espera + Pedidos en proceso
        JPanel colIzquierda = crearColumnaPedidos();
        splitPane.setLeftComponent(colIzquierda);

        // Columna derecha: Recursos + Actividad
        JPanel colDerecha = crearColumnaDerecha();
        splitPane.setRightComponent(colDerecha);

        dashboard.add(splitPane, BorderLayout.CENTER);

        return dashboard;
    }

    /**
     * Crea la fila de estadísticas rápidas con los KPIs principales.
     * Usa GridBagLayout para distribuir los KPIs de forma uniforme y responsiva.
     */
    private JPanel crearPanelEstadisticasRapidas() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 12, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 8, 0, 8);

        kpiEnEspera = new KPIPanel("EN ESPERA", "0", new Color(255, 183, 77));
        gbc.gridx = 0; gbc.weightx = 1.0;
        panel.add(kpiEnEspera, gbc);

        kpiEnProceso = new KPIPanel("EN PROCESO", "0", new Color(100, 181, 246));
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(kpiEnProceso, gbc);

        kpiCompletados = new KPIPanel("COMPLETADOS", "0", new Color(72, 209, 133));
        gbc.gridx = 2; gbc.weightx = 1.0;
        panel.add(kpiCompletados, gbc);

        kpiTiempoProm = new KPIPanel("TIEMPO PROM.", "0.0 s", new Color(168, 130, 255));
        gbc.gridx = 3; gbc.weightx = 1.0;
        panel.add(kpiTiempoProm, gbc);

        kpiRecursosOcupacion = new KPIPanel("RECURSOS", "0%", new Color(255, 121, 121));
        gbc.gridx = 4; gbc.weightx = 1.0;
        panel.add(kpiRecursosOcupacion, gbc);

        return panel;
    }

    /**
     * Panel personalizado para mostrar un KPI con título, valor y color de acento.
     * Mantiene un tamaño preferido fijo para evitar que crezca en cada actualización.
     */
    private static class KPIPanel extends JPanel {
        private final JLabel lblValor;
        private final Color acento;

        /**
         * Construye un panel KPI con el título, valor inicial y color de acento.
         */
        KPIPanel(String titulo, String valorInicial, Color acento) {
            this.acento = acento;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setPreferredSize(new Dimension(0, 70));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            setMinimumSize(new Dimension(80, 70));

            JLabel lblTitulo = new JLabel(titulo);
            lblTitulo.setFont(FUENTE_KPI_TITULO);
            lblTitulo.setForeground(new Color(136, 136, 136));
            lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

            lblValor = new JLabel(valorInicial);
            lblValor.setFont(FUENTE_KPI_VALOR);
            lblValor.setForeground(new Color(51, 51, 51));
            lblValor.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel contenido = new JPanel(new GridBagLayout());
            contenido.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(0, 0, 4, 0);
            c.gridx = 0; c.gridy = 0;
            contenido.add(lblTitulo, c);
            c.gridy = 1; c.insets = new Insets(0, 0, 0, 0);
            contenido.add(lblValor, c);
            add(contenido, BorderLayout.CENTER);

            // Se agrega un borde de color como acento visual
            javax.swing.border.CompoundBorder borde = new javax.swing.border.CompoundBorder(
                    new javax.swing.border.LineBorder(acento, 3, false),
                    new EmptyBorder(10, 12, 10, 12)
            );
            setBorder(borde);
        }

        /**
         * Actualiza solo el valor del KPI sin reconstruir el panel.
         */
        void setValor(String valor) {
            lblValor.setText(valor);
        }
    }

    /**
     * Crea la columna de pedidos con las tablas de espera y proceso.
     */
    private JPanel crearColumnaPedidos() {
        // Pedidos en espera con indicador de estrategia activa
        JPanel panelEspera = new JPanel(new BorderLayout());
        panelEspera.setBorder(BorderFactory.createTitledBorder("PEDIDOS EN ESPERA"));

        // Barra superior que muestra la estrategia de planificación activa
        JPanel barraEstrategia = new JPanel(new BorderLayout());
        barraEstrategia.setOpaque(false);
        lblEstrategiaActiva = new JLabel();
        lblEstrategiaActiva.setFont(FUENTE_TABLA_HEADER);
        lblEstrategiaActiva.setForeground(new Color(100, 100, 100));
        barraEstrategia.add(lblEstrategiaActiva, BorderLayout.WEST);
        panelEspera.add(barraEstrategia, BorderLayout.NORTH);

        tablaEspera = new JTable(modeloPedidosEspera);
        configurarTablaModerna(tablaEspera);
        JScrollPane scrollEspera = new JScrollPane(tablaEspera);
        panelEspera.add(scrollEspera, BorderLayout.CENTER);

        // Pedidos en proceso
        JPanel panelProceso = new JPanel(new BorderLayout());
        panelProceso.setBorder(BorderFactory.createTitledBorder("PEDIDOS EN PROCESO"));
        tablaProceso = new JTable(modeloPedidosProceso);
        configurarTablaModerna(tablaProceso);
        JScrollPane scrollProceso = new JScrollPane(tablaProceso);
        panelProceso.add(scrollProceso, BorderLayout.CENTER);

        // Se divide verticalmente de forma responsiva
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.5);
        split.setDividerSize(4);
        split.setTopComponent(panelEspera);
        split.setBottomComponent(panelProceso);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Crea la columna derecha con recursos compartidos y actividad.
     * Usa JSplitPane vertical para que ambos paneles se redimensionen de forma responsiva.
     */
    private JPanel crearColumnaDerecha() {
        // Recursos compartidos
        JPanel panelRecursos = new JPanel(new BorderLayout());
        panelRecursos.setBorder(BorderFactory.createTitledBorder("RECURSOS COMPARTIDOS"));
        tablaRecursos = new JTable(modeloRecursos);
        configurarTablaModerna(tablaRecursos);
        JScrollPane scrollRecursos = new JScrollPane(tablaRecursos);
        panelRecursos.add(scrollRecursos, BorderLayout.CENTER);

        // Actividad en tiempo real
        JPanel panelActividad = new JPanel(new BorderLayout());
        panelActividad.setBorder(BorderFactory.createTitledBorder("ACTIVIDAD"));
        txtActividad = new JTextArea();
        txtActividad.setEditable(false);
        txtActividad.setFont(FUENTE_LOG);
        txtActividad.setLineWrap(true);
        txtActividad.setWrapStyleWord(true);
        txtActividad.setOpaque(false);
        JScrollPane scrollActividad = new JScrollPane(txtActividad);
        scrollActividad.getViewport().setOpaque(false);
        panelActividad.add(scrollActividad, BorderLayout.CENTER);

        // Se divide verticalmente de forma responsiva
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.4);
        split.setDividerSize(4);
        split.setTopComponent(panelRecursos);
        split.setBottomComponent(panelActividad);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Configura una tabla con estilo moderno, fuente global y anchos de columna definidos.
     */
    private void configurarTablaModerna(JTable tabla) {
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tabla.setRowHeight(28);
        tabla.setFont(FUENTE_TABLA);
        JTableHeader header = tabla.getTableHeader();
        header.setFont(FUENTE_TABLA_HEADER);
        header.setReorderingAllowed(false);

        // Se definen los anchos de columna según el modelo de la tabla
        if (tabla.getModel() instanceof ModeloRecursos) {
            tabla.getColumnModel().getColumn(0).setPreferredWidth(150); // Tipo
            tabla.getColumnModel().getColumn(1).setPreferredWidth(60);  // Total
            tabla.getColumnModel().getColumn(2).setPreferredWidth(80);  // Disponibles
            tabla.getColumnModel().getColumn(3).setPreferredWidth(60);  // En uso
            // Columna 4 (Usuario(s)) ocupa el resto
        } else if (tabla.getModel() instanceof ModeloPedidosEspera) {
            tabla.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
            tabla.getColumnModel().getColumn(1).setPreferredWidth(120); // Cliente
            tabla.getColumnModel().getColumn(2).setPreferredWidth(130); // Prioridad
            tabla.getColumnModel().getColumn(3).setPreferredWidth(70);  // Artículos
            tabla.getColumnModel().getColumn(4).setPreferredWidth(90);  // Estado
            // Columna 5 (Tiempo espera) ocupa el resto
        } else if (tabla.getModel() instanceof ModeloPedidosProceso) {
            tabla.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
            tabla.getColumnModel().getColumn(1).setPreferredWidth(120); // Cliente
            tabla.getColumnModel().getColumn(2).setPreferredWidth(130); // Prioridad
            tabla.getColumnModel().getColumn(3).setPreferredWidth(70);  // Artículos
            tabla.getColumnModel().getColumn(4).setPreferredWidth(90);  // Estado
            // Columna 5 (Tiempo transcurrido) ocupa el resto
        }
    }

    /**
     * Crea el panel de la pestaña Almacén con capacidad, existencias y ubicaciones.
     */
    private JPanel crearPanelAlmacen() {
        JPanel panel = new JPanel(new BorderLayout(12, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Sección de capacidad
        JPanel panelCapacidad = new JPanel(new BorderLayout(10, 0));
        panelCapacidad.setBorder(BorderFactory.createTitledBorder("Capacidad del almacén"));
        progressBarAlmacen = new JProgressBar(0, Almacen.getInstancia().getCapacidadMaxima());
        progressBarAlmacen.setStringPainted(true);
        progressBarAlmacen.setString("0 / 5000");
        progressBarAlmacen.setMinimumSize(new Dimension(100, 24));
        progressBarAlmacen.setPreferredSize(new Dimension(0, 30));
        panelCapacidad.add(progressBarAlmacen, BorderLayout.CENTER);

        // Sección de existencias
        modeloExistencias = new DefaultListModel<>();
        listaExistencias = new JList<>(modeloExistencias);
        listaExistencias.setFont(FUENTE_TABLA);
        JScrollPane scrollExistencias = new JScrollPane(listaExistencias);
        JPanel panelExistencias = new JPanel(new BorderLayout());
        panelExistencias.setBorder(BorderFactory.createTitledBorder("Existencias por producto"));
        panelExistencias.add(scrollExistencias, BorderLayout.CENTER);

        // Grid de ubicaciones físicas (10x10)
        gridUbicaciones = new JPanel(new GridLayout(10, 10, 2, 2));
        for (int i = 0; i < 100; i++) {
            JLabel celda = new JLabel();
            celda.setOpaque(true);
            celda.setBackground(new Color(72, 209, 133));
            celda.setPreferredSize(new Dimension(20, 20));
            gridUbicaciones.add(celda);
        }
        JPanel panelUbicaciones = new JPanel(new BorderLayout());
        panelUbicaciones.setBorder(BorderFactory.createTitledBorder("Ubicaciones físicas (verde = libre, rojo = ocupado)"));
        panelUbicaciones.add(gridUbicaciones, BorderLayout.CENTER);

        // Layout: capacidad arriba, existencias y ubicaciones abajo
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(panelExistencias);
        split.setRightComponent(panelUbicaciones);
        split.setDividerLocation(350);
        split.setDividerSize(4);

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.add(panelCapacidad, BorderLayout.NORTH);
        wrapper.add(split, BorderLayout.CENTER);

        return wrapper;
    }

    // Referencia al JMenuItem del generador para actualizar su texto
    private JMenuItem btnMenuGenerador;

    /**
     * Crea el menú global con las opciones de Archivo, Sistema y Ayuda.
     * Reemplaza la pestaña Controles para liberar espacio en el dashboard.
     */
    private JMenuBar crearMenuGlobal() {
        JMenuBar menuBar = new JMenuBar();

        // ── Menú Archivo ──
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem btnNuevoPedido = new JMenuItem("Nuevo pedido");
        btnNuevoPedido.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        btnNuevoPedido.addActionListener(e -> new PedidoManualDialog(this).setVisible(true));
        menuArchivo.add(btnNuevoPedido);

        JMenuItem btnMercaderia = new JMenuItem("Registrar mercadería");
        btnMercaderia.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        btnMercaderia.addActionListener(e -> new MercaderiaDialog(this).setVisible(true));
        menuArchivo.add(btnMercaderia);

        menuArchivo.addSeparator();

        JMenuItem btnSalir = new JMenuItem("Salir");
        btnSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        btnSalir.addActionListener(e -> System.exit(0));
        menuArchivo.add(btnSalir);

        menuBar.add(menuArchivo);

        // ── Menú Sistema ──
        JMenu menuSistema = new JMenu("Sistema");

        // Toggle generador
        btnMenuGenerador = new JMenuItem("⏸ Detener generador");
        btnMenuGenerador.addActionListener(e -> {
            GeneradorPedidos generador = Main.getGeneradorPedidos();
            if (generador != null) {
                if (generador.estaActivo()) {
                    generador.detener();
                    btnMenuGenerador.setText("▶ Reanudar generador");
                    agregarActividad("Generador de pedidos detenido.");
                } else {
                    generador.reanudar();
                    btnMenuGenerador.setText("⏸ Detener generador");
                    agregarActividad("Generador de pedidos reanudado.");
                }
            }
        });
        menuSistema.add(btnMenuGenerador);

        // Submenú Estrategia con radio buttons
        JMenu menuEstrategia = new JMenu("Estrategia");
        ButtonGroup grupoEstrategia = new ButtonGroup();

        JRadioButtonMenuItem rbPrioridad = new JRadioButtonMenuItem("Prioridad estricta");
        rbPrioridad.setSelected(true);
        rbPrioridad.addActionListener(e -> cambiarEstrategia(new EstrategiaPrioridadEstricta(), "Prioridad estricta"));
        grupoEstrategia.add(rbPrioridad);
        menuEstrategia.add(rbPrioridad);

        JRadioButtonMenuItem rbTiempo = new JRadioButtonMenuItem("Tiempo de espera");
        rbTiempo.addActionListener(e -> cambiarEstrategia(new EstrategiaTiempoEspera(), "Tiempo de espera"));
        grupoEstrategia.add(rbTiempo);
        menuEstrategia.add(rbTiempo);

        JRadioButtonMenuItem rbMenor = new JRadioButtonMenuItem("Menor cantidad");
        rbMenor.addActionListener(e -> cambiarEstrategia(new EstrategiaMenorCantidad(), "Menor cantidad"));
        grupoEstrategia.add(rbMenor);
        menuEstrategia.add(rbMenor);

        menuSistema.add(menuEstrategia);
        menuSistema.addSeparator();

        JMenuItem btnDeadlock = new JMenuItem("⚠ Simular deadlock");
        btnDeadlock.addActionListener(e -> new DeadlockDialog(this).setVisible(true));
        menuSistema.add(btnDeadlock);

        menuBar.add(menuSistema);

        // ── Menú Ayuda ──
        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem btnAyuda = new JMenuItem("Modo de uso");
        btnAyuda.addActionListener(e -> new AyudaDialog(this).setVisible(true));
        menuAyuda.add(btnAyuda);

        JMenuItem btnAcerca = new JMenuItem("Acerca de REDXela");
        btnAcerca.addActionListener(e -> new AcercaDialog(this).setVisible(true));
        menuAyuda.add(btnAcerca);

        menuBar.add(menuAyuda);

        return menuBar;
    }

    /**
     * Cambia la estrategia de planificación y actualiza el radio button correspondiente.
     */
    private void cambiarEstrategia(EstrategiaPlanificacion estrategia, String nombre) {
        SistemaPedidos sistema = Main.getSistemaPedidos();
        if (sistema != null) {
            sistema.setEstrategia(estrategia);
            agregarActividad("Estrategia cambiada a: " + nombre);
        }
    }

    /**
     * Agrega un mensaje con timestamp al panel de actividad.
     * Se limita a 50 líneas para no saturar la vista.
     */
    private void agregarActividad(String mensaje) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String timestamp = sdf.format(new Date());
        SwingUtilities.invokeLater(() -> {
            if (txtActividad != null) {
                txtActividad.append(timestamp + " " + mensaje + "\n");
                // Se eliminan las líneas más antiguas si hay más de 50
                while (txtActividad.getLineCount() > 50) {
                    int primeraLineaFin = txtActividad.getText().indexOf('\n');
                    if (primeraLineaFin >= 0) {
                        txtActividad.replaceRange("", 0, primeraLineaFin + 1);
                    } else {
                        break;
                    }
                }
                txtActividad.setCaretPosition(txtActividad.getDocument().getLength());
            }
        });
    }

    /**
     * Actualiza todos los paneles leyendo el estado actual del sistema.
     * Se ejecuta en el EDT gracias al Timer de Swing.
     */
    private void actualizarPaneles() {
        SistemaPedidos sistema = Main.getSistemaPedidos();
        if (sistema == null || !sistema.isInicializado()) return;

        // Se leen los contadores actuales
        int enEspera = sistema.getColaEspera().size();
        int enProceso = sistema.getPedidosProcesando().size();
        int completados = sistema.getPedidosCompletados();

        // Detección de nuevos pedidos en la cola
        if (enEspera > ultimoEnEspera) {
            agregarActividad(enEspera - ultimoEnEspera + " nuevo(s) pedido(s) en cola de espera.");
        }
        ultimoEnEspera = enEspera;

        // Detección de pedidos completados
        if (completados > ultimoCompletados) {
            agregarActividad(completados - ultimoCompletados + " pedido(s) completado(s). Total: " + completados);
        }
        ultimoCompletados = completados;

        // Detección de pedidos iniciados en proceso
        if (enProceso > ultimoEnProceso) {
            agregarActividad(enProceso - ultimoEnProceso + " pedido(s) iniciado(s) en proceso.");
        }
        ultimoEnProceso = enProceso;

        // Detección de conflictos de recursos (pedidos bloqueados)
        int bloqueados = sistema.getPedidosBloqueados().size();
        if (bloqueados > 0) {
            agregarActividad("⚠ " + bloqueados + " pedido(s) bloqueado(s) por falta de stock.");
        }

        // Se actualizan los KPIs visuales
        kpiEnEspera.setValor(String.valueOf(enEspera));
        kpiEnProceso.setValor(String.valueOf(enProceso));
        kpiCompletados.setValor(String.valueOf(completados));

        // Ocupación de recursos
        GestorRecursos gestor = GestorRecursos.getInstancia();
        int totalRecursos = 0;
        int recursosEnUso = 0;
        for (TipoRecurso tr : TipoRecurso.values()) {
            totalRecursos += gestor.getRecursosTotales(tr);
            recursosEnUso += gestor.getUsuariosActivos(tr).size();
        }
        double ocupacion = totalRecursos > 0 ? (recursosEnUso * 100.0 / totalRecursos) : 0;
        kpiRecursosOcupacion.setValor(String.format("%.0f%%", ocupacion));

        // Tiempo promedio por pedido completado
        if (completados > 0) {
            double tiempoProm = (System.currentTimeMillis() - obtenerTiempoInicioEstimado()) / completados / 1000.0;
            kpiTiempoProm.setValor(String.format("%.1f s", tiempoProm));
        }

        // Se actualiza la etiqueta de estrategia activa
        EstrategiaPlanificacion estrategia = sistema.getEstrategiaActual();
        if (estrategia != null && lblEstrategiaActiva != null) {
            String nombreEstrategia = estrategia.getClass().getSimpleName()
                    .replace("Estrategia", "")
                    .replace("PrioridadEstricta", "Prioridad estricta")
                    .replace("TiempoEspera", "Tiempo de espera")
                    .replace("MenorCantidad", "Menor cantidad");
            lblEstrategiaActiva.setText("Orden: " + nombreEstrategia);
        }

        // Se actualizan las tablas
        modeloRecursos.fireTableDataChanged();
        modeloPedidosEspera.fireTableDataChanged();
        modeloPedidosProceso.fireTableDataChanged();

        // Se actualiza el almacén
        actualizarAlmacen();
    }

    /**
     * Estimación del tiempo de inicio del sistema para calcular promedios.
     */
    private long obtenerTiempoInicioEstimado() {
        SistemaPedidos sistema = Main.getSistemaPedidos();
        if (sistema != null && !sistema.getPedidosProcesando().isEmpty()) {
            return sistema.getPedidosProcesando().get(0).getTiempoLlegada();
        }
        return System.currentTimeMillis() - 60000;
    }


    /**
     * Actualiza el panel del almacén con los datos actuales.
     */
    private void actualizarAlmacen() {
        Almacen almacen = Almacen.getInstancia();

        // Barra de progreso
        int ocupado = almacen.getEspacioOcupado();
        int maximo = almacen.getCapacidadMaxima();
        progressBarAlmacen.setValue(ocupado);
        progressBarAlmacen.setString(ocupado + " / " + maximo);

        // Existencias por producto (se muestran con nombre legible)
        modeloExistencias.clear();
        almacen.getExistenciasPorProducto().forEach((codigo, cantidad) -> {
            if (cantidad > 0) {
                // Se busca el nombre del producto en el catálogo
                org.giov.modelo.Producto producto = org.giov.modelo.ConfiguracionMercancias.buscarProducto(codigo);
                String nombre = (producto != null) ? producto.nombre() : codigo;
                modeloExistencias.addElement(nombre + " (" + codigo + "): " + cantidad);
            }
        });
        if (modeloExistencias.isEmpty()) {
            modeloExistencias.addElement("Sin existencias");
        }

        // Grid de ubicaciones
        for (int i = 0; i < gridUbicaciones.getComponentCount(); i++) {
            Component celda = gridUbicaciones.getComponent(i);
            if (almacen.getEstadoUbicacion(i)) {
                celda.setBackground(new Color(255, 121, 121));
            } else {
                celda.setBackground(new Color(72, 209, 133));
            }
        }
    }
}
