package com.NullPtr.Pontiland.entities;

import java.util.Queue;

/**
 * Clase que representa una casilla de evento en el juego Contiene una lista de tarjetas de evento
 * disponibles de la cual se sacarán las tarjetas de manera aleatoria
 */

/*
 * TODO: Se podria usar la misma clase para el uso de otras casillas como:
 *   - Carcel
 *   - Parking Gratis
 *   - Ir a la carcel
 *   - Salida
 *   - Movimiento/Estacion
 */
public class Evento extends Casilla {
  private Queue<TarjetaEvento> tarjetasDisponibles;

  /**
   * Constructor de la clase Evento
   *
   * @param posicionTablero Posición de la casilla en el tablero
   * @param nombreCasilla Nombre de la casilla
   * @param tarjetasDisponibles Cola de tarjetas de evento disponibles
   */
  public Evento(
      byte posicionTablero, String nombreCasilla, Queue<TarjetaEvento> tarjetasDisponibles) {
    super(posicionTablero, nombreCasilla);
    this.tarjetasDisponibles = tarjetasDisponibles;
  }
}
