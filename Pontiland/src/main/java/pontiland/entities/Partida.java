package pontiland.entities;

import java.util.Queue;

/**
 * Clase que representa una partida del juego.
 * Contiene el tablero y la cola de jugadores (Facilita el manejo de los turnos,
 * usando una cola para ir jugador a jugador en un orden determinado de manera
 * constante
 */
public class Partida {
    private Tablero tablero;
    private Queue<Jugador> jugadores;

    /**
     * Constructor de la clase Partida
     * @param tablero Tablero del juego
     * @param jugadores Cola de jugadores que participan en la partida
     * @throws IllegalArgumentException si el tablero es nulo o la cola de jugadores es nula o vacía
     */
    public Partida(Tablero tablero, Queue<Jugador> jugadores) {
        if (tablero == null) {
            throw new IllegalArgumentException("El tablero no puede ser nulo");
        }
        if (jugadores == null || jugadores.isEmpty()) {
            throw new IllegalArgumentException("La cola de jugadores no puede ser nula o vacía");
        }
        this.tablero = tablero;
        this.jugadores = jugadores;
    }

}
