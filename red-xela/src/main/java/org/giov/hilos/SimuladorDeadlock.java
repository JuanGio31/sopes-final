package org.giov.hilos;

import org.giov.recursos.GestorRecursos;
import org.giov.recursos.TipoRecurso;

/**
 * Simula un escenario de interbloqueo (deadlock) clásico entre dos operaciones
 * que solicitan dos recursos compartidos en orden inverso.
 *
 * <p>La Operación 1 adquiere MONTACARGAS y luego intenta adquirir ESCANER_CODIGOS,
 * mientras que la Operación 2 adquiere ESCANER_CODIGOS y luego intenta adquirir
 * MONTACARGAS, lo que genera un ciclo de dependencias mutuas.</p>
 */
public class SimuladorDeadlock {
    private final GestorRecursos gestor;
    private Thread operacion1;
    private Thread operacion2;

    /**
     * Construye el simulador de interbloqueos utilizando la instancia
     * singleton del gestor de recursos.
     */
    public SimuladorDeadlock() {
        this.gestor = GestorRecursos.getInstancia();
    }

    /**
     * Inicia dos hilos que solicitan recursos en orden inverso,
 * provocando un interbloqueo mutuo.
     *
     * <p>La Operación 1 adquiere MONTACARGAS y luego intenta adquirir ESCANER_CODIGOS,
     * mientras que la Operación 2 adquiere ESCANER_CODIGOS y luego intenta adquirir
     * MONTACARGAS, lo que genera un ciclo de dependencias mutuas.</p>
     */
    public void provocarConflicto() {
        operacion1 = new Thread(() -> {
            try {
                gestor.solicitarRecurso(TipoRecurso.MONTACARGAS, "Operacion 1");
                Thread.sleep(500); // Se introduce una pausa para asegurar que el otro hilo tome el escáner

                System.out.println("Operacion 1 intentando solicitar ESCANER_CODIGOS...");
                gestor.solicitarRecurso(TipoRecurso.ESCANER_CODIGOS, "Operacion 1");

                // Si llega a este punto, no se produjo deadlock
                gestor.liberarRecurso(TipoRecurso.ESCANER_CODIGOS, "Operacion 1");
                gestor.liberarRecurso(TipoRecurso.MONTACARGAS, "Operacion 1");
            } catch (InterruptedException e) {
                System.out.println("Operacion 1 fue interrumpida para resolver el conflicto.");
                gestor.liberarRecurso(TipoRecurso.MONTACARGAS, "Operacion 1"); // Se libera el recurso retenido
            }
        });

        operacion2 = new Thread(() -> {
            try {
                gestor.solicitarRecurso(TipoRecurso.ESCANER_CODIGOS, "Operacion 2");
                Thread.sleep(500);

                System.out.println("Operacion 2 intentando solicitar MONTACARGAS...");
                gestor.solicitarRecurso(TipoRecurso.MONTACARGAS, "Operacion 2");

                gestor.liberarRecurso(TipoRecurso.MONTACARGAS, "Operacion 2");
                gestor.liberarRecurso(TipoRecurso.ESCANER_CODIGOS, "Operacion 2");
            } catch (InterruptedException e) {
                System.out.println("Operacion 2 fue interrumpida para resolver el conflicto.");
                gestor.liberarRecurso(TipoRecurso.ESCANER_CODIGOS, "Operacion 2"); // Se libera el recurso retenido
            }
        });

        operacion1.start();
        operacion2.start();
    }

    /**
     * Resuelve el conflicto de interbloqueo interrumpiendo una de las dos operaciones.
     * La operación interrumpida libera el recurso retenido y regresa a la cola de espera.
     *
     * @param continuarOperacion1 si es {@code true}, se continúa con la Operación 1
     *                            y se interrumpe la Operación 2; en caso contrario,
     *                            se interrumpe la Operación 1
     */
    public void resolverConflicto(boolean continuarOperacion1) {
        if (continuarOperacion1) {
            operacion2.interrupt(); // Se cancela la Operación 2, obligando a liberar el escáner y devolverla a espera
        } else {
            operacion1.interrupt(); // Se cancela la Operación 1, liberando el montacargas
        }
    }
}