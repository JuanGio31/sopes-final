package org.giov.recursos;

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

    /** Rastrea qué operación tiene asignado cada recurso en un momento dado. */
    private Map<TipoRecurso, String> registroUso;

    private GestorRecursos() {
        semaforos = new ConcurrentHashMap<>();
        registroUso = new ConcurrentHashMap<>();

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
            registroUso.put(tipo, idOperacion);
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
            registroUso.remove(tipo);
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
     *
     * @param tipo tipo de recurso a consultar
     * @return identificador de la operación actual, o "Ninguno" si el recurso está libre
     */
    public String getUsuarioActual(TipoRecurso tipo) {
        return registroUso.getOrDefault(tipo, "Ninguno");
    }
}
