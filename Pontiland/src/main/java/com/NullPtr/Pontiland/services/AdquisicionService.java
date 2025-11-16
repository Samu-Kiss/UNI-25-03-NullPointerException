package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    Propiedad propiedad = null;
    try {
      propiedad = propiedadRepository.getPropiedadByPosition(position);
    } catch (SQLException e) {
      logger.error("Error al obtener la propiedad en la posición {}", position, e);
      return false;
    }

    int propiedadId = propiedad.getIdPropiedad();
    int precio = propiedad.getPrecioCompra();

    if (jugador.getDinero() < precio) {
      return false;
    }

    Jugador jugadorPago = null;
    try {
      jugadorPago = propiedadRepository.propiedadHasOwner(propiedadId);
    } catch (SQLException e) {
      logger.error(
          "Error al comprobar si la propiedad con Id={} tiene dueño", propiedadId, e);
      return false;
    }

    if (jugadorPago == null) {
      try {
        propiedadRepository.addAdquisicion(
            jugador.getJugadorId(), propiedadId, propiedad.getNivelPropiedad());
      } catch (SQLException e) {
        logger.error("Error al añadir la adquisición de la propiedad con Id={}", propiedadId, e);
        return false;
      }
      int nuevoDinero = jugador.getDinero() - precio;
      jugador.setDinero(nuevoDinero);

      try {
        jugadorRepository.updateDinero(jugador.getJugadorId(), nuevoDinero);
      } catch (SQLException e) {
        logger.error(
            "Error al actualizar el dinero del jugador con Id={}", jugador.getJugadorId(), e);
      }

    } else {
      logger.info("Jugador pago: {}", jugadorPago.getDinero());

      int precioNivel = 0;
      try {
        precioNivel = propiedad.getRentaPorNivel()[propiedadRepository.getNivelPropiedad(propiedadId) - 1];
      } catch (SQLException e) {
        logger.error("Error al obtener el nivel de la propiedad con ID={}",
          propiedadId,
          e);
      }

      logger.info("Precio nivel: {}", precioNivel);

      try {
        logger.debug(
            "Dinero actual jugador a pagar: {}",
            jugadorRepository.getJugadorByID(jugador.getJugadorId()).getDinero());
        logger.debug(
            "Dinero actual jugador pagado: {}",
            jugadorRepository.getJugadorByID(jugadorPago.getJugadorId()).getDinero());
      } catch (SQLException e) {
        logger.error(
            "Error al recuperar los datos de los jugadores con id={} e id={}",
            jugador.getJugadorId(),
            jugadorPago.getJugadorId());
      }

      try {
        jugadorRepository.updateDinero(jugador.getJugadorId(), jugador.getDinero() - precioNivel);
        jugadorRepository.updateDinero(
            jugadorPago.getJugadorId(), jugadorPago.getDinero() + precioNivel);
      } catch (SQLException e) {
        logger.error(
            "Hubo un problema al actualizar el dinero del jugador con ID={}",
            jugador.getJugadorId(),
            e);
      }

      try {
        logger.info(
            "Dinero jugador a pagar: {}",
            jugadorRepository.getJugadorByID(jugador.getJugadorId()).getDinero());
        logger.info(
            "Dinero jugador pagado: {}",
            jugadorRepository.getJugadorByID(jugadorPago.getJugadorId()).getDinero());
      } catch (SQLException e) {
        logger.error(
            "Error al actualizar de alguno de los jugadores con ID= {} - {}",
            jugador.getJugadorId(),
            jugadorPago.getNombreJugador(),
            e);
      }

      try {
        propiedadRepository.incrementarNivelPropiedad(propiedadId);
      } catch (SQLException e) {
        logger.error("No se pudo incrementar el nivel de la propiedad con ID={}", propiedadId, e);
      }
    }

    return true;
  }

  @Override
  public Propiedad prepararSubasta(int position) {
    Propiedad propiedad = null;
    try {
      propiedad = propiedadRepository.getPropiedadByPosition(position);
    } catch (SQLException e) {
      logger.error("Error al obtener la propiedad en la posición {}", position, e);
      return null;
    }
    if (propiedad == null) return null;
    Integer ownerId = null;
    try {
      ownerId = propiedadRepository.getOwnerIdByPropiedadId(propiedad.getIdPropiedad());
    } catch (SQLException e) {
      logger.error("Error al obtener el dueño de la propiedad con Id={}",
        propiedad.getIdPropiedad(),
        e);
    }
    if (ownerId != null) return null;
    return propiedad;
  }

  @Override
  public boolean comprarPropiedadEnSubasta(int position, Jugador jugador, int precioFinal) {
    if (precioFinal < 0) return false;
    Propiedad propiedad = null;
    try {
      propiedad = propiedadRepository.getPropiedadByPosition(position);
    } catch (SQLException e) {
      logger.error("Error al obtener la propiedad en la posición {}",
        position,
        e);
      return false;
    }

    if (propiedad == null) return false;
    Integer ownerId = null;
    try {
      ownerId = propiedadRepository.getOwnerIdByPropiedadId(propiedad.getIdPropiedad());
    } catch (SQLException e) {
      logger.error("Error al obtener el dueño de la propiedad con Id={}",
        propiedad.getIdPropiedad(),
        e);
      return false;
    }
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
      logger.error(
          "No se pudo obtener el jugador con ID={} o no se pudo actualizar el jugador{}",
          jugador.getJugadorId(),
          jugador.getNombreJugador(),
          e);
    }
    return false;
  }
}
