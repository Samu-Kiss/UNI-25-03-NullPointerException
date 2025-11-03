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
  private IHUDcontroller hudControler;

  public AdquisicionService(
      IPropiedadRepository propiedadRepository,
      IJugadorRepository jugadorRepository, IHUDcontroller hudControler) {
    this.hudControler = hudControler;
    this.propiedadRepository = propiedadRepository;
    this.jugadorRepository = jugadorRepository;
  }

  @Override
  public boolean comprarPropiedadPorPosicion(int position) throws SQLException {

    Propiedad propiedad = propiedadRepository.getPropiedadByPosition(position);
    if (propiedad == null) {
      return false;
    }

    int propiedadId = propiedad.getIdPropiedad();
    int precio = propiedad.getPrecioCompra();

    int jugadorActivoId = jugadorRepository.getActivePlayer();
    if (jugadorActivoId < 0) return false;

    Jugador jugador = jugadorRepository.getJugadorByID(jugadorActivoId);
    if (jugador == null) return false;

    if (jugador.getDinero() < precio) {
      return false;
    }

    propiedadRepository.addAdquisicion(jugador.getJugadorId(), propiedadId, propiedad.getNivelPropiedad());

    int nuevoDinero = jugador.getDinero() - precio;
    jugadorRepository.updateJugadorDineroById(jugador.getJugadorId(), nuevoDinero);

    hudControler.updatePlayerCard(jugador.getNombreJugador(), String.valueOf(nuevoDinero), jugador.getEstado(), jugador.getNumJugador());

    return true;
  }


}
