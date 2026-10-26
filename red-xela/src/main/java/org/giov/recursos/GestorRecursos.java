package org.giov.recursos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Gestiona los recursos compartidos del centro de distribución mediante semáforos.
 * Implementa el patrón Singleton para centralizar el control, la exclusión mutua
 * y la sincronización entre hilos.
 *
 * <p>Cada tipo de recurso dispone de un semáforo con política de equidad (fairness)
 * para evitar la inanición (starvation) de los procesos.</p>
 */
public class GestorRecursos {
    private static GestorRecursos instancia;

    /** Almacena los semáforos asociados a cada tipo de recurso. */
    private Map<TipoRecurso, Semaphore> semaforos;

    /** Rastrea qué operaciones tienen asignado cada recurso en un momento dado. */
    private Map<TipoRecurso, List<String>> registroUso;

    private GestorRecursos() {
        semaforos = new ConcurrentHashMap<>();
        registroUso = new ConcurrentHashMap<>();
        // Se inicializa cada tipo de recurso con una lista vacía de usuarios
        for (TipoRecurso tipo : TipoRecurso.values()) {
            registroUso.put(tipo, Collections.synchronizedList(new ArrayList<>()));
        }

        // Se activa la política de equidad (fairness) tipo FIFO para evitar inanición (starvation)
        semaforos.put(TipoRecurso.ESTACION_EMPAQUE, new Semaphore(6, true));
        semaforos.put(TipoRecurso.AREA_CARGA, new Semaphore(2, true));
        semaforos.put(TipoRecurso.ENCARGADO_BODEGA, new Semaphore(10, true));
        semaforos.put(TipoRecurso.MONTACARGAS, new Semaphore(3, true));
        semaforos.put(TipoRecurso.CONTROL_CALIDAD, new Semaphore(4, true));
        semaforos.put(TipoRecurso.ESCANER_CODIGOS, new Semaphore(1, true));
    }

    /**
     * Devuelve la única instancia de {@link GestorRecursos}.
     * El acceso es sincronizado para garantizar la seguridad entre hilos.
     *
     * @return la instancia singleton del gestor de recursos
     */
    public static synchronized GestorRecursos getInstancia() {
        if (instancia == null) {
            instancia = new GestorRecursos();
        }
        return instancia;
    }

    /**
     * Solicita la adquisición de un recurso compartido.
     * Si el recurso no está disponible, el hilo que invoca este método
     * se bloquea hasta que otro hilo lo libere.
     *
     * @param tipo         tipo de recurso a solicitar
     * @param idOperacion  identificador de la operación que solicita el recurso
     * @throws InterruptedException si el hilo es interrumpido mientras espera
     */
    public void solicitarRecurso(TipoRecurso tipo, String idOperacion) throws InterruptedException {
        Semaphore semaforo = semaforos.get(tipo);
        if (semaforo != null) {
            // El hilo se bloquea aquí si no hay recursos disponibles
            semaforo.acquire();
            // Se registra la operación que utiliza el recurso
            List<String> usuarios = registroUso.get(tipo);
            if (usuarios != null) {
                usuarios.add(idOperacion);
            }
            System.out.println("Operación " + idOperacion + " adquirió: " + tipo);
        }
    }

    /**
     * Libera un recurso previamente adquirido, permitiendo que otro hilo
     * en espera lo obtenga.
     *
     * @param tipo         tipo de recurso a liberar
     * @param idOperacion  identificador de la operación que libera el recurso
     */
    public void liberarRecurso(TipoRecurso tipo, String idOperacion) {
        Semaphore semaforo = semaforos.get(tipo);
        if (semaforo != null) {
            List<String> usuarios = registroUso.get(tipo);
            if (usuarios != null) {
                usuarios.remove(idOperacion);
            }
            semaforo.release();
            System.out.println("Operación " + idOperacion + " liberó: " + tipo);
        }
    }

    /**
     * Consulta la cantidad de permisos disponibles para un tipo de recurso.
     *
     * @param tipo tipo de recurso a consultar
     * @return número de permisos disponibles
     */
    public int getRecursosDisponibles(TipoRecurso tipo) {
        return semaforos.get(tipo).availablePermits();
    }

    /**
     * Devuelve el identificador de la operación que actualmente utiliza un recurso.
     * Si hay múltiples operaciones, retorna los IDs separados por coma.
     *
     * @param tipo tipo de recurso a consultar
     * @return identificador(es) de la(s) operación(es) actual(es), o "Ninguno" si el recurso está libre
     */
    public String getUsuarioActual(TipoRecurso tipo) {
        List<String> usuarios = registroUso.get(tipo);
        if (usuarios == null || usuarios.isEmpty()) {
            return "Ninguno";
        }
        return String.join(", ", usuarios);
    }

    /**
     * Devuelve la lista de todas las operaciones que actualmente utilizan un recurso.
     *
     * @param tipo tipo de recurso a consultar
     * @return lista de identificadores de las operaciones activas, vacía si el recurso está libre
     */
    public List<String> getUsuariosActivos(TipoRecurso tipo) {
        List<String> usuarios = registroUso.get(tipo);
        if (usuarios == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(usuarios);
    }

    /**
     * Devuelve la cantidad total de permisos para un tipo de recurso.
     * Se calcula sumando los permisos disponibles con la cantidad de usuarios activos.
     *
     * @param tipo tipo de recurso a consultar
     * @return número total de permisos del recurso
     */
    public int getRecursosTotales(TipoRecurso tipo) {
        Semaphore semaforo = semaforos.get(tipo);
        if (semaforo == null) {
            return 0;
        }
        // Los permisos totales son los disponibles más los en uso
        List<String> usuarios = registroUso.get(tipo);
        int enUso = (usuarios != null) ? usuarios.size() : 0;
        return semaforo.availablePermits() + enUso;
    }
}
