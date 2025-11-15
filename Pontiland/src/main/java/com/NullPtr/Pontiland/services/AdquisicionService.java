package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class AdquisicionService implements IAdquisicionService {
  private final IPropiedadRepository propiedadRepository;
  private IJugadorRepository jugadorRepository;

  // Logger
  private static Logger logger = LogManager.getLogger(AdquisicionService.class);

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
        logger.error("Error al actualizar el dinero del jugador con Id={}", jugador.getJugadorId(), e);
        /*
        TODO: Revisar si quitarlo del todo o dejar solo el loger de arriba
        throw new RuntimeException(e);
         */
      }

    } else {
      logger.info("Jugador pago: {}", jugadorPago.getDinero());

      int precioNivel =
          propiedad.getRentaPorNivel()[propiedadRepository.getNivelPropiedad(propiedadId) - 1];

      logger.info("Precio nivel: {}", precioNivel);

      try {
        logger.debug("Dinero actual jugador a pagar: {}", jugadorRepository.getJugadorByID(jugador.getJugadorId()).getDinero());
        logger.debug("Dinero actual jugador pagado: {}", jugadorRepository.getJugadorByID(jugadorPago.getJugadorId()).getDinero());
      } catch (SQLException e) {
        logger.error("Error al recuperar los datos de los jugadores con id={} e id={}", jugador.getJugadorId(), jugadorPago.getJugadorId());
        /*
        TODO: Revisar si quitarlo del todo o dejar solo el loger de arriba
        throw new RuntimeException(e);
         */
      }

      try {
        jugadorRepository.updateDinero(jugador.getJugadorId(), jugador.getDinero() - precioNivel);
        jugadorRepository.updateDinero(
            jugadorPago.getJugadorId(), jugadorPago.getDinero() + precioNivel);
      } catch (SQLException e) {
        logger.error("Hubo un problema al actualizar el dinero del jugador con ID={}",jugador.getJugadorId(), e);
      }

      try {
        logger.info("Dinero jugador a pagar: {}", jugadorRepository.getJugadorByID(jugador.getJugadorId()).getDinero());
        logger.info("Dinero jugador pagado: {}", jugadorRepository.getJugadorByID(jugadorPago.getJugadorId()).getDinero());

      } catch (SQLException e) {
        logger.error("Error al actualizar de alguno de los jugadores con ID= {} - {}", jugador.getJugadorId(), jugadorPago.getNombreJugador(), e);
        /*
        TODO: Revisar si quitarlo del todo o dejar solo el loger de arriba
        throw new RuntimeException(e);
         */
      }

      propiedadRepository.incrementarNivelPropiedad(propiedadId);
    }

    return true;
  }

  @Override
  public Propiedad prepararSubasta(int position) {
    Propiedad propiedad = propiedadRepository.getPropiedadByPosition(position);
    if (propiedad == null) return null;
    Integer ownerId = propiedadRepository.getOwnerIdByPropiedadId(propiedad.getIdPropiedad());
    if (ownerId != null) return null;
    return propiedad;
  }

  @Override
  public boolean comprarPropiedadEnSubasta(int position, Jugador jugador, int precioFinal) {
    if (precioFinal < 0) return false;
    Propiedad propiedad = propiedadRepository.getPropiedadByPosition(position);
    if (propiedad == null) return false;
    Integer ownerId = propiedadRepository.getOwnerIdByPropiedadId(propiedad.getIdPropiedad());
    if (ownerId != null) return false;
    try {
      int saldoDb = jugadorRepository.getJugadorByID(jugador.getJugadorId()).getDinero();
      if (saldoDb < precioFinal) return false;
      int nuevo = saldoDb - precioFinal;
      jugadorRepository.updateDinero(jugador.getJugadorId(), nuevo);
      jugador.setDinero(nuevo);
      propiedadRepository.addAdquisicion(
          jugador.getJugadorId(), propiedad.getIdPropiedad(), propiedad.getNivelPropiedad());
      return true;
    } catch (SQLException e) {
      logger.error("No se pudo obtener el jugador con ID={} o no se pudo actualizar el jugador{}", jugador.getJugadorId(), jugador.getNombreJugador(), e);
      /*
        TODO: Revisar si quitarlo del todo o dejar solo el loger de arriba
        throw new RuntimeException(e);
      */
    }
    return false;
  }
}
