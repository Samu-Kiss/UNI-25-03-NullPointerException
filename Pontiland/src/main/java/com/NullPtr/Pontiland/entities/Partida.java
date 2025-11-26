package com.NullPtr.Pontiland.entities;

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

  public String getNombrePartida() {
    return nombrePartida;
  }

  public void setNombrePartida(String nombrePartida) {
    this.nombrePartida = nombrePartida;
  }

  public int getCantidadJugadores() {
    return cantidadJugadores;
  }

  public void setCantidadJugadores(int cantidadJugadores) {
    this.cantidadJugadores = cantidadJugadores;
  }

  public boolean isActiva() {
    return activa;
  }

  public void setActiva(boolean activa) {
    this.activa = activa;
  }
}
