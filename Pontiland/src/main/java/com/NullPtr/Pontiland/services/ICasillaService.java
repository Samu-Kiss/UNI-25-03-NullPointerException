package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Jugador;

public interface ICasillaService {

  boolean getIrACarcel();

  void interaccion(Jugador jugador, Casilla casilla);
}
