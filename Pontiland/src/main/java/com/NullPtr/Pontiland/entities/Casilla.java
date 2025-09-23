package com.NullPtr.Pontiland.entities;

/**
 * Clase que representa una casilla en el tablero de juego Cada casilla tiene por lo menos una
 * posición y un nombre
 */
public class Casilla {
  private byte posicionTablero = -1;
  private String nombreCasilla = null;

  /**
   * Constructor de la clase casilla con posición y nombre
   *
   * @param posicionTablero
   * @param nombreCasilla
   * @throws IllegalArgumentException si la posición es menor que 1 o mayor que 40 o si el nombre es
   *     nulo o vacío
   */
  public Casilla(byte posicionTablero, String nombreCasilla) {
    if (posicionTablero < 1 || posicionTablero > 40) {
      throw new IllegalArgumentException("La posición es invalida");
    }
    if (nombreCasilla == null || nombreCasilla.isEmpty()) {
      throw new IllegalArgumentException("El nombre de la casilla no puede ser nulo o vacío");
    }

    this.posicionTablero = posicionTablero;
    this.nombreCasilla = nombreCasilla;
  }

  public byte getPosicionTablero() {
    return posicionTablero;
  }

  public void setPosicionTablero(byte posicionTablero) {
    this.posicionTablero = posicionTablero;
  }

  public String getNombreCasilla() {
    return nombreCasilla;
  }

  public void setNombreCasilla(String nombreCasilla) {
    this.nombreCasilla = nombreCasilla;
  }
}
