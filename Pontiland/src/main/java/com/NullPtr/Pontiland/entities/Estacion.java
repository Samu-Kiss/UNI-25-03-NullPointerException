package com.NullPtr.Pontiland.entities;

/** Clase que representa una estación en el juego */
public class Estacion extends Casilla {
  private short precioViaje = -1;

  /**
   * Constructor de la clase Estacion
   *
   * @param posicionTablero Posición de la estación en el tablero
   * @param nombreCasilla Nombre de la estación
   * @param precioViaje Precio del viaje en la estación
   * @throws IllegalArgumentException Si el precio del viaje es negativo
   */
  public Estacion(byte posicionTablero, String nombreCasilla, short precioViaje) {
    super(posicionTablero, nombreCasilla);
    if (precioViaje < 0) {
      throw new IllegalArgumentException("El precio del viaje no puede ser negativo");
    }
    this.precioViaje = precioViaje;
  }
}
