package com.NullPtr.Pontiland.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa a un jugador en el juego. Contiene atributos como ID, nombre, icono,
 * posicion, estado en la carcel, dinero y propiedades
 */
public class Jugador {
  private int jugadorId = -1;
  private int numJugador = -1;
  private String nombreJugador = "";
  private int iconoId = -1;
  private int posicion = 1;
  private boolean enCarcel = false;
  private int dinero = 0;
  private long partidaId = -1;
  private List<Propiedad> propiedades;

  public Jugador(
      int jugadorId,
      String nombreJugador,
      int posicion,
      boolean enCarcel,
      int dinero,
      List<Propiedad> propiedades) {
    this.jugadorId = jugadorId;
    this.nombreJugador = nombreJugador;
    this.posicion = posicion;
    this.enCarcel = enCarcel;
    this.dinero = dinero;
    this.propiedades = propiedades;
  }

  /**
   * Constructor de la clase Jugador por defecto, con posicion inicial 1 y sin propiedades
   *
   * @param dinero Cantidad inicial de dinero del jugador
   * @param nombreJugador Nombre del jugador
   * @param jugadorId Identificador único del jugador
   * @throws IllegalArgumentException si el dinero es negativo, los IDs son negativos o el nombre
   *     está vacío
   */
  public Jugador(int dinero, String nombreJugador, int jugadorId) {
    if (dinero < 0) {
      throw new IllegalArgumentException("El dinero no puede ser negativo");
    }
    if (jugadorId > 4 || jugadorId < 1) {
      throw new IllegalArgumentException("Identificador del jugador es inválido");
    }
    if (nombreJugador == null || nombreJugador.isEmpty()) {
      throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
    }

    this.dinero = dinero;
    this.nombreJugador = nombreJugador;
    this.jugadorId = jugadorId;

    propiedades = new ArrayList<Propiedad>();
  }

  /**
   * Constructor completo de la clase Jugador
   *
   * @param jugadorId Identificador único del jugador
   * @param numJugador Número del jugador en la partida
   * @param nombreJugador Nombre del jugador
   * @param iconoId Identificador del icono del jugador
   * @param posicion Posición actual del jugador en el tablero
   * @param enCarcel Estado del jugador (si está en la cárcel o no)
   * @param dinero Cantidad de dinero que posee el jugador
   * @param partidaId Identificador de la partida a la que pertenece el jugador
   */
  public Jugador(
      int jugadorId,
      int numJugador,
      String nombreJugador,
      int iconoId,
      int posicion,
      boolean enCarcel,
      int dinero,
      long partidaId) {

    if (dinero < 0) {
      throw new IllegalArgumentException("El dinero no puede ser negativo");
    }
    if (nombreJugador == null || nombreJugador.isEmpty()) {
      throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
    }
    if (posicion < 0 || posicion > 40) {
      throw new IllegalArgumentException("Posicion invalida");
    }
    if (partidaId < 0) {
      throw new IllegalArgumentException("Identificador de la partida es inválido");
    }
    if (iconoId < 1 || iconoId > 7) {
      throw new IllegalArgumentException("Identificador del icono es inválido");
    }
    if (numJugador < 1 || numJugador > 4) {
      throw new IllegalArgumentException("Número de jugador es inválido");
    }

    this.jugadorId = jugadorId;
    this.numJugador = numJugador;
    this.nombreJugador = nombreJugador;
    this.iconoId = iconoId;
    this.posicion = posicion;
    this.enCarcel = enCarcel;
    this.dinero = dinero;
    this.partidaId = partidaId;
  }

  public int getJugadorId() {
    return jugadorId;
  }

  public void setJugadorId(byte jugadorId) {
    if (jugadorId < 0 || jugadorId > 4) {
      throw new IllegalArgumentException("Identificador del jugador es inválido");
    }
    this.jugadorId = jugadorId;
  }

  public int getNumJugador() {
    return numJugador;
  }

  public String getNombreJugador() {
    return nombreJugador;
  }

  public void setNombreJugador(String nombreJugador) {
    if (nombreJugador == null || nombreJugador.isEmpty()) {
      throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
    }
    this.nombreJugador = nombreJugador;
  }

  public int getPosicion() {
    return posicion;
  }

  public void setPosicion(int posicion) {
    if (posicion < 1 || posicion > 40) {
      throw new IllegalArgumentException("Posicion invalida");
    }
    this.posicion = posicion;
  }

  public boolean getEstado() {
    return enCarcel;
  }

  public void setEstado(boolean enCarcel) {
    this.enCarcel = enCarcel;
  }

  /*
   * TODO: los getter y setter de dinero no son realmente necesarios
   *  Dinero podría ser publico
   */
  public int getDinero() {
    return dinero;
  }

  public void setDinero(int dinero) {
    this.dinero = dinero;
  }

  public List<Propiedad> getPropiedades() {
    return propiedades;
  }

  public void setPropiedades(List<Propiedad> propiedades) {
    if (propiedades == null) {
      throw new IllegalArgumentException("La lista de propiedades no puede ser nula");
    }
    this.propiedades = propiedades;
  }
  public long getPartida(){ return this.partidaId;}
}
