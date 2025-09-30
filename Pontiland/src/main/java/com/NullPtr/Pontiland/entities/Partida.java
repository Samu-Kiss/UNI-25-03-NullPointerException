package com.NullPtr.Pontiland.entities;

import java.util.List;
import java.util.Queue;

/**
 * Clase que representa una partida del juego.
 * Contiene el tablero y la cola de jugadores (Facilita el manejo de los turnos,
 * usando una cola para ir jugador a jugador en un orden determinado de manera
 * constante
 */
public class Partida {
    private String nombrePartida = null;
    private List<ICasilla> tablero;
    private Dado[] dados;
    private Queue<Jugador> jugadores;

    /**
     * Constructor de la clase Partida
     * @param tablero Tablero del juego
     * @param jugadores Cola de jugadores que participan en la partida
     * @throws IllegalArgumentException si el tablero es nulo o la cola de jugadores es nula o vacía
     */
    public Partida(List<ICasilla> tablero, Queue<Jugador> jugadores, Dado[] dados, String nombrePartida) {
        if (nombrePartida == null || nombrePartida.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la partida no puede ser nulo o vacío");
        }
        if (tablero == null) {
            throw new IllegalArgumentException("El tablero no puede ser nulo");
        }
        if (jugadores == null || jugadores.isEmpty()) {
            throw new IllegalArgumentException("La cola de jugadores no puede ser nula o vacía");
        }
        if (dados == null || dados.length != 2) {
            throw new IllegalArgumentException("Debe haber exactamente dos dados");
        }
        this.nombrePartida = nombrePartida;
        this.tablero = tablero;
        this.jugadores = jugadores;
        this.dados = dados;
    }

    public List<ICasilla> getTablero() {
        return tablero;
    }
    public void setTablero(List<ICasilla> tablero) {
        if (tablero == null) {
            throw new IllegalArgumentException("El tablero no puede ser nulo");
        }
        this.tablero = tablero;
    }

    public Queue<Jugador> getJugadores() {
        return jugadores;
    }
    public void setJugadores(Queue<Jugador> jugadores) {
        if (jugadores == null || jugadores.isEmpty()) {
            throw new IllegalArgumentException("La cola de jugadores no puede ser nula o vacía");
        }
        if (jugadores.size() > 4 || jugadores.size() < 2) {
            throw new IllegalArgumentException("Numero de jugadores inválido (mínimo 2, máximo 4)");
        }
        this.jugadores = jugadores;
    }
}
