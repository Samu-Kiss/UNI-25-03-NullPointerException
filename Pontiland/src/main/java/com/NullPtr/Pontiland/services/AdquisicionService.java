package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import java.sql.SQLException;

public class AdquisicionService implements IAdquisicionService {
  private final IPropiedadRepository propiedadRepository;
  private IJugadorRepository jugadorRepository;
  private IHUDcontroller hudController;

  public AdquisicionService(
      IPropiedadRepository propiedadRepository,
      IJugadorRepository jugadorRepository,
      IHUDcontroller hudController) {
    this.hudController = hudController;
    this.propiedadRepository = propiedadRepository;
    this.jugadorRepository = jugadorRepository;
  }

  @Override
  public boolean comprarPropiedadPorPosicion(int position) {

    Propiedad propiedad = propiedadRepository.getPropiedadByPosition(position);

    int propiedadId = propiedad.getIdPropiedad();
    int precio = propiedad.getPrecioCompra();

    int jugadorActivoId = 0;
    Jugador jugador;
    try {
      jugadorActivoId = jugadorRepository.getActivePlayer();

      jugador = jugadorRepository.getJugadorByID(jugadorActivoId);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    if (jugador.getDinero() < precio) {
      return false;
    }

    propiedadRepository.addAdquisicion(
        jugador.getJugadorId(), propiedadId, propiedad.getNivelPropiedad());

    int nuevoDinero = jugador.getDinero() - precio;

    try {
      jugadorRepository.updateJugadorDineroById(jugador.getJugadorId(), nuevoDinero);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    hudController.updatePlayerCard(
        jugador.getNombreJugador(),
        String.valueOf(nuevoDinero),
        jugador.getEstado(),
        jugador.getNumJugador());

    return true;
  }
}
