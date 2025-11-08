package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;

public interface IAdquisicionService {

  boolean comprarPropiedadPorPosicion(int position, Jugador jugador);
}
