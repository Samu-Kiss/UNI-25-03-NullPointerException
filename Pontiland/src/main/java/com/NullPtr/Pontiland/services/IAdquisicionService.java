package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;

public interface IAdquisicionService {

  boolean comprarPropiedadPorPosicion(int position, Jugador jugador);

  Propiedad prepararSubasta(int position);

  boolean comprarPropiedadEnSubasta(int position, Jugador jugador, int precioFinal);
}
