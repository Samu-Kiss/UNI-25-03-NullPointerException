package com.NullPtr.Pontiland.entities;

/** Enum que representa los diferentes tipos de casillas en el juego */
public enum Tipo {
  PARADALIBRE("ParadaLibre"),
  EVENTO("Evento"),
  PROPIEDAD("Propiedad"),
  MOVIMIENTO("Movimiento");

  private final String action;

  Tipo(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }
}
