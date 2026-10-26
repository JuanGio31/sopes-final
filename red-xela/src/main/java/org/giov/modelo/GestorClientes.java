package org.giov.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestor centralizado de clientes registrados en el sistema.
 *
 * <p>Se encarga de registrar nuevos clientes con un nivel de servicio
 * contratado, buscar clientes existentes por nombre y proporcionar
 * la lista completa de clientes para selección en la interfaz.</p>
 *
 * <p>Implementa el patrón Singleton para garantizar un único registro
 * de clientes en todo el sistema.</p>
 */
public class GestorClientes {

    /** Instancia única del gestor. */
    private static volatile GestorClientes instancia;

    /** Mapa de clientes indexados por su ID. */
    private final Map<String, Cliente> clientes = new ConcurrentHashMap<>();

    /** Contador atómico para generar IDs únicos de cliente. */
    private final AtomicInteger contadorId = new AtomicInteger(1);

    /** Constructor privado para forzar el uso del Singleton. */
    private GestorClientes() {
    }

    /**
     * Devuelve la instancia única del gestor de clientes.
     *
     * @return instancia de GestorClientes
     */
    public static GestorClientes getInstancia() {
        if (instancia == null) {
            synchronized (GestorClientes.class) {
                if (instancia == null) {
                    instancia = new GestorClientes();
                }
            }
        }
        return instancia;
    }

    /**
     * Registra un nuevo cliente en el sistema con un nivel de servicio.
     * Si ya existe un cliente con el mismo nombre, se retorna el existente
     * en lugar de crear un duplicado.
     *
     * @param nombre nombre completo del cliente
     * @param nivelServicio nivel de servicio contratado (1-5)
     * @return el cliente registrado (nuevo o existente)
     */
    public Cliente registrarCliente(String nombre, int nivelServicio) {
        // Se verifica si ya existe un cliente con ese nombre
        Cliente existente = buscarPorNombre(nombre);
        if (existente != null) {
            return existente;
        }

        // Se genera un ID único y se crea el cliente
        String id = "CLT-" + String.format("%04d", contadorId.getAndIncrement());
        Cliente nuevo = new Cliente(id, nombre, nivelServicio);
        clientes.put(id, nuevo);
        return nuevo;
    }

    /**
     * Busca un cliente por su nombre, ignorando mayúsculas y minúsculas.
     *
     * @param nombre nombre del cliente a buscar
     * @return el cliente encontrado o {@code null} si no existe
     */
    public Cliente buscarPorNombre(String nombre) {
        String nombreLower = nombre.toLowerCase().trim();
        for (Cliente cliente : clientes.values()) {
            if (cliente.getNombre().toLowerCase().equals(nombreLower)) {
                return cliente;
            }
        }
        return null;
    }

    /**
     * Busca un cliente por su ID único.
     *
     * @param id identificador del cliente
     * @return el cliente encontrado o {@code null} si no existe
     */
    public Cliente buscarPorId(String id) {
        return clientes.get(id);
    }

    /**
     * Devuelve una lista inmutable con todos los clientes registrados.
     *
     * @return lista de clientes ordenada por ID
     */
    public List<Cliente> obtenerTodos() {
        List<Cliente> lista = new ArrayList<>(clientes.values());
        lista.sort((a, b) -> a.getId().compareTo(b.getId()));
        return Collections.unmodifiableList(lista);
    }

    /**
     * Devuelve la cantidad total de clientes registrados.
     *
     * @return número de clientes
     */
    public int getCantidadClientes() {
        return clientes.size();
    }
}
