package org.giov.hilos;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Simula un escenario de interbloqueo (deadlock) clásico entre dos operaciones
 * que solicitan dos recursos compartidos en orden inverso.
 *
 * <p>La Operación 1 adquiere MONTACARGAS y luego intenta adquirir ESCANER_CODIGOS,
 * mientras que la Operación 2 adquiere ESCANER_CODIGOS y luego intenta adquirir
 * MONTACARGAS, lo que genera un ciclo de dependencias mutuas.</p>
 *
 * <p>Se utilizan {@link ReentrantLock} dedicados (en lugar de los semáforos del
 * {@code GestorRecursos}) para garantizar que cada recurso tenga exactamente un
 * permiso, asegurando que el interbloqueo ocurra de forma determinista.</p>
 */
public class SimuladorDeadlock {
    // Locks dedicados para simular recursos con permiso único
    private final ReentrantLock lockMontacargas = new ReentrantLock();
    private final ReentrantLock lockEscaner = new ReentrantLock();

    private Thread operacion1;
    private Thread operacion2;
    private volatile boolean deadlockDetectado = false;

    /** Callback notificado cuando se detecta el interbloqueo. */
    public interface DeadlockListener {
        /** Se invoca cuando ambas operaciones quedan bloqueadas mutuamente. */
        void onDeadlockDetected();
    }

    private DeadlockListener listener;

    /**
     * Establece un observador que recibe notificación cuando se detecta
     * el interbloqueo.
     *
     * @param l el observador a registrar
     */
    public void setDeadlockListener(DeadlockListener l) {
        this.listener = l;
    }

    /**
     * Indica si se ha detectado un interbloqueo en la simulación actual.
     *
     * @return {@code true} si ambas operaciones están bloqueadas mutuamente
     */
    public boolean isDeadlockDetectado() {
        return deadlockDetectado;
    }

    /**
     * Indica si la Operación 1 aún está activa (no terminada).
     *
     * @return {@code true} si el hilo de la Operación 1 está vivo
     */
    public boolean isOperacion1Activa() {
        return operacion1 != null && operacion1.isAlive();
    }

    /**
     * Indica si la Operación 2 aún está activa (no terminada).
     *
     * @return {@code true} si el hilo de la Operación 2 está vivo
     */
    public boolean isOperacion2Activa() {
        return operacion2 != null && operacion2.isAlive();
    }

    /**
     * Inicia dos hilos que solicitan recursos en orden inverso,
     * provocando un interbloqueo mutuo determinista.
     *
     * <p>Cada hilo adquiere un lock exclusivo y luego intenta adquirir el otro,
     * creando un ciclo de dependencias. Un hilo detector espera a que ambos
     * hilos queden bloqueados y notifica al listener registrado.</p>
     */
    public void provocarConflicto() {
        deadlockDetectado = false;

        operacion1 = new Thread(() -> {
            try {
                lockMontacargas.lock();
                System.out.println("[Deadlock] Operacion 1 adquirió MONTACARGAS");
                Thread.sleep(300); // Se introduce pausa para que la Op 2 tome el escáner primero

                System.out.println("[Deadlock] Operacion 1 intenta adquirir ESCANER_CODIGOS...");
                lockEscaner.lock(); // Se bloquea aquí si la Op 2 ya tiene el escáner

                // Si llega aquí, no hubo deadlock (caso raro)
                lockEscaner.unlock();
                lockMontacargas.unlock();
                System.out.println("[Deadlock] Operacion 1 completó sin conflicto");
            } catch (InterruptedException e) {
                System.out.println("[Deadlock] Operacion 1 fue interrumpida para resolver el conflicto.");
                if (lockMontacargas.isHeldByCurrentThread()) lockMontacargas.unlock();
            }
        }, "Operacion-1");

        operacion2 = new Thread(() -> {
            try {
                lockEscaner.lock();
                System.out.println("[Deadlock] Operacion 2 adquirió ESCANER_CODIGOS");
                Thread.sleep(300);

                System.out.println("[Deadlock] Operacion 2 intenta adquirir MONTACARGAS...");
                lockMontacargas.lock(); // Se bloquea aquí si la Op 1 ya tiene el montacargas

                lockMontacargas.unlock();
                lockEscaner.unlock();
                System.out.println("[Deadlock] Operacion 2 completó sin conflicto");
            } catch (InterruptedException e) {
                System.out.println("[Deadlock] Operacion 2 fue interrumpida para resolver el conflicto.");
                if (lockEscaner.isHeldByCurrentThread()) lockEscaner.unlock();
            }
        }, "Operacion-2");

        operacion1.start();
        operacion2.start();

        // Hilo detector: verifica que ambas operaciones queden bloqueadas
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Se espera a que ambas operaciones intenten el segundo recurso
                if (operacion1.isAlive() && operacion2.isAlive() && !deadlockDetectado) {
                    deadlockDetectado = true;
                    System.out.println("[Deadlock] ¡Interbloqueo detectado! Ambas operaciones bloqueadas.");
                    if (listener != null) {
                        listener.onDeadlockDetected();
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }, "Deadlock-Detector").start();
    }

    /**
     * Resuelve el conflicto de interbloqueo liberando el lock de una operación
     * y permitiendo que la otra continúe.
     *
     * <p>Si {@code continuarOperacion1} es {@code true}, se fuerza la liberación
     * del lock de la Operación 2 (escáner), permitiendo que la Operación 1
     * lo adquiera y complete. La Operación 2 se interrumpe y regresa a la cola
     * de espera.</p>
     *
     * @param continuarOperacion1 si es {@code true}, se continúa con la Operación 1
     *                            y se resuelve a favor de ella; en caso contrario,
     *                            se resuelve a favor de la Operación 2
     */
    public void resolverConflicto(boolean continuarOperacion1) {
        if (continuarOperacion1) {
            // Se libera el escáner para que la Op 1 pueda continuar
            // La Op 2 debe liberar su lock; como está bloqueada en acquire,
            // se interrumpe para que salga con InterruptedException
            operacion2.interrupt();
            System.out.println("[Deadlock] Resolución: Operación 1 continúa, Operación 2 regresa a espera.");
        } else {
            // Se libera el montacargas para que la Op 2 pueda continuar
            operacion1.interrupt();
            System.out.println("[Deadlock] Resolución: Operación 2 continúa, Operación 1 regresa a espera.");
        }
    }
}