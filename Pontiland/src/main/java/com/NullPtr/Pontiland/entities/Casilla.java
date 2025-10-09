package com.NullPtr.Pontiland.entities;

/**
 * Clase que representa una casilla de evento en el juego Contiene una lista de tarjetas de evento
 * disponibles de la cual se sacarán las tarjetas de manera aleatoria
 */

/*
 * WARNING: Se usa la misma clase para el uso de otras casillas como:
 *   - Carcel
 *   - Parking Gratis
 *   - Ir a la carcel
 *   - Salida
 *   - Movimiento/Estacion
 *   - Evento/Suerte
 *   - Propiedad (Solo para indicar que es una propiedad y hacer la acción
 *     correspondiente)
 */
public class Casilla implements ICasilla {
  private Tipo tipoCasilla = null;
  private int posicionTablero = -1;
  private String nombreCasilla = null;

  /**
   * Constructor de la clase Evento
   *
   * @param posicionTablero Posición de la casilla en el tablero
   * @param nombreCasilla Nombre de la casilla
   * @param tipoCasilla Tipo de la casilla
   */
  public Casilla(int posicionTablero, String nombreCasilla, Tipo tipoCasilla) {
    if (posicionTablero < 1 || posicionTablero > 40) {
      throw new IllegalArgumentException("Posición de casilla inválida");
    }
    if (nombreCasilla == null || nombreCasilla.isEmpty()) {
      throw new IllegalArgumentException("Nombre de casilla inválido");
    }
    if (tipoCasilla == null) {
      throw new IllegalArgumentException("Tipo de casilla inválido");
    }

    this.posicionTablero = posicionTablero;
    this.nombreCasilla = nombreCasilla;
    this.tipoCasilla = tipoCasilla;
  }

  @Override
  public Tipo getTipoCasilla() {
    return tipoCasilla;
  }

    @Override
    public void setPosicionTablero(Byte posicionTablero) {
        return;
    }

    @Override
  public int getPosicionTablero() {
    return posicionTablero;
  }

  public void setPosicionTablero(int posicionTablero) {
    this.posicionTablero = posicionTablero;
  }

  @Override
  public String getNombreCasilla() {
    return nombreCasilla;
  }

  @Override
  public void setTipoCasilla(Tipo tipoCasilla) {
    this.tipoCasilla = tipoCasilla;
  }

  @Override
  public void setNombreCasilla(String nombreCasilla) {
    this.nombreCasilla = nombreCasilla;
  }
}
