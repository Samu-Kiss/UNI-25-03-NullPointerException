package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import java.sql.SQLException;

public class AdquisicionService implements IAdquisicionService {
  private final IPropiedadRepository propiedadRepository;
  private IJugadorRepository jugadorRepository;

  public AdquisicionService(
      IPropiedadRepository propiedadRepository, IJugadorRepository jugadorRepository) {
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
        jugadorRepository.updateDinero(jugador.getJugadorId(), nuevoDinero);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

    } else {

      System.out.println("Jugador pago: " + jugadorPago.getDinero());
      int precioNivel =
          propiedad.getRentaPorNivel()[propiedadRepository.getNivelPropiedad(propiedadId) - 1];

      System.out.println("Precio nivel: " + precioNivel);
      try {
        System.out.println(
            "Dinero actual jugador a pagar: "
                + jugadorRepository.getJugadorByID(jugador.getJugadorId()).getDinero());
        System.out.println(
            "Dinero actual jugador pagado: "
                + jugadorRepository.getJugadorByID(jugadorPago.getJugadorId()).getDinero());
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      try {
        jugadorRepository.updateDinero(jugador.getJugadorId(), jugador.getDinero() - precioNivel);
        jugadorRepository.updateDinero(
            jugadorPago.getJugadorId(), jugadorPago.getDinero() + precioNivel);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      try {
        System.out.println(
            "Dinero jugador a pagar: "
                + jugadorRepository.getJugadorByID(jugador.getJugadorId()).getDinero());
        System.out.println(
            "Dinero jugador pagado: "
                + jugadorRepository.getJugadorByID(jugadorPago.getJugadorId()).getDinero());
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      propiedadRepository.incrementarNivelPropiedad(propiedadId);
    }

    return true;
  }
}
