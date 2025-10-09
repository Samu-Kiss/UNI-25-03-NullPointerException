package com.NullPtr.Pontiland.entities;

/** Enum que representa los diferentes tipos de casillas en el juego */
public enum Tipo {
  SALIDA("Salida"),
  CARCEL("Carcel"),
  PARADA_LIBRE("ParadaLibre"),
  MOVIMIENTO("Movimiento"),
  IR_CARCEL("IrALaCarcel"),
  EVENTO("Evento"),
  PROPIEDAD("Propiedad");

  private final String action;

    Tipo(String action) {
        this.action = action;
    }

    public String getAction(){
        return action;
    }
}
