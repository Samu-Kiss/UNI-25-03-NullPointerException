package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import java.util.List;

public interface IAdquisicionService {

  boolean comprarPropiedadPorPosicion(int position, Jugador jugador);

  Propiedad prepararSubasta(int position);

  boolean comprarPropiedadEnSubasta(int position, Jugador jugador, int precioFinal);

  void venderPropiedad(Propiedad propiedad, Jugador jugador);

  void liquidarDeudaConBanco(Jugador jugador);

  void liquidarDeudaEntreJugadores(Jugador deudor, Jugador acreedor);

  List<Jugador> obtenerRankingJugadoresDesc();
}
