package org.giov.modelo;

import org.giov.recursos.TipoRecurso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configura las mercancías y el catálogo de productos del sistema REDXela.
 *
 * <p>Define los 5 tipos de mercancía con sus recursos requeridos y crea
 * un catálogo de productos de ejemplo para pruebas y demostración.</p>
 */
public class ConfiguracionMercancias {

    /** Tipos de mercancía predefinidos del sistema. */
    public static final TipoMercancia FRAGIL;
    public static final TipoMercancia PESADO;
    public static final TipoMercancia PELIGROSO;
    public static final TipoMercancia PERECEDERO;
    public static final TipoMercancia ESTANDAR;

    /** Catálogo de productos indexado por código. */
    private static final Map<String, Producto> CATALOGO = new ConcurrentHashMap<>();

    /**
     * Inicializa estáticamente los tipos de mercancía con sus recursos requeridos.
     * Cada tipo especifica qué recursos del sistema necesita para su procesamiento.
     */
    static {
        // Mercancía frágil: requiere control de calidad y empaque especial
        FRAGIL = new TipoMercancia("Frágil",
                "Productos que requieren manipulación delicada y control de calidad obligatorio",
                Arrays.asList(
                        TipoRecurso.ESTACION_EMPAQUE.name(),
                        TipoRecurso.ENCARGADO_BODEGA.name(),
                        TipoRecurso.CONTROL_CALIDAD.name()
                ));

        // Mercancía pesada: requiere montacargas y área de carga
        PESADO = new TipoMercancia("Pesado",
                "Productos de gran peso que requieren maquinaria especializada para mover",
                Arrays.asList(
                        TipoRecurso.MONTACARGAS.name(),
                        TipoRecurso.AREA_CARGA.name(),
                        TipoRecurso.ENCARGADO_BODEGA.name()
                ));

        // Mercancía peligrosa: requiere escáner y encargado especializado
        PELIGROSO = new TipoMercancia("Peligroso",
                "Materiales inflamables, corrosivos o tóxicos que requieren verificación y manejo especial",
                Arrays.asList(
                        TipoRecurso.ESCANER_CODIGOS.name(),
                        TipoRecurso.ENCARGADO_BODEGA.name(),
                        TipoRecurso.AREA_CARGA.name()
                ));

        // Mercancía perecedera: requiere control de calidad y escáner para trazabilidad
        PERECEDERO = new TipoMercancia("Perecedero",
                "Alimentos o productos con fecha de caducidad que requieren verificación de frescura",
                Arrays.asList(
                        TipoRecurso.CONTROL_CALIDAD.name(),
                        TipoRecurso.ESCANER_CODIGOS.name(),
                        TipoRecurso.ENCARGADO_BODEGA.name()
                ));

        // Mercancía estándar: requiere solo empaque y encargado
        ESTANDAR = new TipoMercancia("Estándar",
                "Productos sin restricciones especiales de manipulación o almacenamiento",
                Arrays.asList(
                        TipoRecurso.ESTACION_EMPAQUE.name(),
                        TipoRecurso.ENCARGADO_BODEGA.name()
                ));

        // Se inicializa el catálogo de productos de ejemplo
        inicializarCatalogo();
    }

    /**
     * Pobla el catálogo con productos de ejemplo para cada tipo de mercancía.
     */
    private static void inicializarCatalogo() {
        // Productos frágiles
        CATALOGO.put("PROD-F001", new Producto("PROD-F001", "Vaso de cristal", FRAGIL));
        CATALOGO.put("PROD-F002", new Producto("PROD-F002", "Espejo decorativo", FRAGIL));
        CATALOGO.put("PROD-F003", new Producto("PROD-F003", "Lámpara de vidrio", FRAGIL));

        // Productos pesados
        CATALOGO.put("PROD-P001", new Producto("PROD-P001", "Motor industrial", PESADO));
        CATALOGO.put("PROD-P002", new Producto("PROD-P002", "Tanque de acero", PESADO));
        CATALOGO.put("PROD-P003", new Producto("PROD-P003", "Compresor de aire", PESADO));

        // Productos peligrosos
        CATALOGO.put("PROD-L001", new Producto("PROD-L001", "Ácido sulfúrico", PELIGROSO));
        CATALOGO.put("PROD-L002", new Producto("PROD-L002", "Pintura inflamable", PELIGROSO));
        CATALOGO.put("PROD-L003", new Producto("PROD-L003", "Gas propano", PELIGROSO));

        // Productos perecederos
        CATALOGO.put("PROD-E001", new Producto("PROD-E001", "Leche fresca", PERECEDERO));
        CATALOGO.put("PROD-E002", new Producto("PROD-E002", "Frutas importadas", PERECEDERO));
        CATALOGO.put("PROD-E003", new Producto("PROD-E003", "Carnes congeladas", PERECEDERO));

        // Productos estándar
        CATALOGO.put("PROD-S001", new Producto("PROD-S001", "Ropa de vestir", ESTANDAR));
        CATALOGO.put("PROD-S002", new Producto("PROD-S002", "Libros", ESTANDAR));
        CATALOGO.put("PROD-S003", new Producto("PROD-S003", "Juguetes", ESTANDAR));
    }

    /**
     * Devuelve el catálogo completo de productos.
     *
     * @return mapa inmutable de productos indexados por código
     */
    public static Map<String, Producto> getCatalogo() {
        return CATALOGO;
    }

    /**
     * Busca un producto por su código en el catálogo.
     *
     * @param codigo código identificador del producto
     * @return el producto encontrado, o {@code null} si no existe
     */
    public static Producto buscarProducto(String codigo) {
        return CATALOGO.get(codigo);
    }

    /**
     * Devuelve la lista de todos los tipos de mercancía definidos.
     *
     * @return lista con los 5 tipos de mercancía del sistema
     */
    public static List<TipoMercancia> obtenerTiposMercancia() {
        return Arrays.asList(FRAGIL, PESADO, PELIGROSO, PERECEDERO, ESTANDAR);
    }

    /**
     * Imprime en consola la información de configuración de mercancías y catálogo.
     * Se utiliza al inicio del sistema para verificar la configuración.
     */
    public static void imprimirConfiguracion() {
        System.out.println("========================================");
        System.out.println("  CONFIGURACION DE MERCANCIAS - REDXela");
        System.out.println("========================================");

        List<TipoMercancia> tipos = obtenerTiposMercancia();
        for (TipoMercancia tipo : tipos) {
            System.out.println("\nTipo: " + tipo.getNombre());
            System.out.println("  Descripcion: " + tipo.getDescripcion());
            System.out.print("  Recursos requeridos: ");
            System.out.println(String.join(", ", tipo.getRecursosRequeridos()));
        }

        System.out.println("\n========================================");
        System.out.println("  CATALOGO DE PRODUCTOS");
        System.out.println("========================================");

        CATALOGO.forEach((codigo, producto) -> {
            System.out.println("  [" + codigo + "] " + producto.nombre() +
                    " (Tipo: " + producto.tipoMercancia().getNombre() + ")");
        });

        System.out.println("\nTotal de productos en catálogo: " + CATALOGO.size());
        System.out.println("========================================");
    }
}
