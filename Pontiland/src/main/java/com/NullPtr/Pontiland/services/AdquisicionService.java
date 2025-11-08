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
  public boolean comprarPropiedadPorPosicion(int position, Jugador jugador) {

    Propiedad propiedad = propiedadRepository.getPropiedadByPosition(position);

    int propiedadId = propiedad.getIdPropiedad();
    int precio = propiedad.getPrecioCompra();

    if (jugador.getDinero() < precio) {
      return false;
    }

    Jugador jugadorPago = propiedadRepository.propiedadHasOwner(propiedadId);

    if (jugadorPago == null) {
      propiedadRepository.addAdquisicion(
          jugador.getJugadorId(), propiedadId, propiedad.getNivelPropiedad());
      int nuevoDinero = jugador.getDinero() - precio;
      jugador.setDinero(nuevoDinero);

      try {
        jugadorRepository.updateJugador(jugador);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      hudController.updatePlayerCard(
          jugador.getNombreJugador(),
          String.valueOf(nuevoDinero),
          jugador.getEstado(),
          jugador.getNumJugador());

    } else {
      int precioNivel =
          propiedad.getRentaPorNivel()[propiedadRepository.getNivelPropiedad(propiedadId) - 1];

      System.out.println("Precio nivel: " + precioNivel);
      try {
        jugadorRepository.rentPaymentTransaction(jugador, jugadorPago, precioNivel);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      hudController.updatePlayerCard(
          jugadorPago.getNombreJugador(),
          String.valueOf(jugadorPago.getDinero() + precioNivel),
          jugadorPago.getEstado(),
          jugadorPago.getNumJugador());

      hudController.updatePlayerCard(
          jugador.getNombreJugador(),
          String.valueOf(jugadorPago.getDinero() - precioNivel),
          jugador.getEstado(),
          jugador.getNumJugador());

      propiedadRepository.incrementarNivelPropiedad(propiedadId);
    }

    return true;
  }
}
