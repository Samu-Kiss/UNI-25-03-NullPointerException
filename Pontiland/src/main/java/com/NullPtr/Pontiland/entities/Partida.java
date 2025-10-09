package com.NullPtr.Pontiland.entities;

import java.util.List;
import java.util.Queue;

/**
 * Clase que representa una partida del juego. Contiene el tablero y la cola de jugadores (Facilita
 * el manejo de los turnos, usando una cola para ir jugador a jugador en un orden determinado de
 * manera constante
 */
public class Partida {
  private String nombrePartida = null;
  private int cantidadJugadores = 0;
  private boolean activa;

    public Partida(String nombrePartida, int cantidadJugadores) {
        this.nombrePartida = nombrePartida;
        this.cantidadJugadores = cantidadJugadores;
    }

}
