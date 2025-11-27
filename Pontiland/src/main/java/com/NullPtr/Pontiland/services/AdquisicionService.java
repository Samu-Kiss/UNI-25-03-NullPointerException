package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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

    Jugador jugadorPago;
    try {
      jugadorPago = propiedadRepository.propiedadHasOwner(propiedadId);
    } catch (SQLException e) {
      logger.error("Error al comprobar si la propiedad con Id={} tiene dueño", propiedadId, e);
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
        precioNivel =
            propiedad.getRentaPorNivel()[propiedadRepository.getNivelPropiedad(propiedadId) - 1];
      } catch (SQLException e) {
        logger.error("Error al obtener el nivel de la propiedad con ID={}", propiedadId, e);
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

        if (!jugadorRepository.getJugadorEstadoByID(jugadorPago.getJugadorId())) {
          if (jugador.getJugadorId() != jugadorPago.getJugadorId()) {
            int saldoDeudor = jugador.getDinero() - precioNivel;
            int saldoAcreedor = jugadorPago.getDinero() + precioNivel;
            jugadorRepository.updateDinero(jugador.getJugadorId(), saldoDeudor);
            jugadorRepository.updateDinero(jugadorPago.getJugadorId(), saldoAcreedor);

            Jugador deudor = jugadorRepository.getJugadorByID(jugador.getJugadorId());
            Jugador acreedor = jugadorRepository.getJugadorByID(jugadorPago.getJugadorId());
            if (deudor.getDinero() < 0) {
              liquidarDeudaEntreJugadores(deudor, acreedor);
            }
          }
        } else {
          logger.debug(
              "Pago bloqueado porque el jugador receptor está en cárcel (ID={})",
              jugadorPago.getJugadorId());
        }
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
      logger.error(
          "Error al obtener el dueño de la propiedad con Id={}", propiedad.getIdPropiedad(), e);
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
      logger.error("Error al obtener la propiedad en la posición {}", position, e);
      return false;
    }

    if (propiedad == null) return false;
    Integer ownerId = null;
    try {
      ownerId = propiedadRepository.getOwnerIdByPropiedadId(propiedad.getIdPropiedad());
    } catch (SQLException e) {
      logger.error(
          "Error al obtener el dueño de la propiedad con Id={}", propiedad.getIdPropiedad(), e);
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

  @Override
  public void venderPropiedad(Propiedad propiedad, Jugador jugador) {
    try {
      int precioVenta = propiedad.getPrecioCompra();
      int nuevoDinero = jugador.getDinero() + precioVenta;
      jugador.setDinero(nuevoDinero);
      jugadorRepository.updateDinero(jugador.getJugadorId(), nuevoDinero);
      propiedadRepository.venderAdquisicion(propiedad.getIdPropiedad(), jugador.getJugadorId());
    } catch (SQLException e) {
      logger.error(
          "Error al vender la propiedad con Id={} del jugador con Id={}",
          propiedad.getIdPropiedad(),
          jugador.getJugadorId(),
          e);
    }
  }

  @Override
  public void liquidarDeudaConBanco(Jugador jugador) {
    try {
      List<Propiedad> props = propiedadRepository.getPropiedadesByJugador(jugador.getJugadorId());
      int valorTotalPropiedades =
          propiedadRepository.getPatrimonioTotalJugador(jugador.getJugadorId());

      if (jugador.getDinero() + valorTotalPropiedades < 0) {
        logger.debug(
            "{} no cuenta con el patrimonio suficiente para continuar el juego y termina el juego",
            jugador.getNombreJugador());
        return;
      }

      logger.debug(
          "{} cuenta con patrimonio suficiente, se venderán propiedades hasta saldar la deuda",
          jugador.getNombreJugador());

      props.sort(Comparator.comparingInt(Propiedad::getPrecioCompra).reversed());

      while (jugador.getDinero() < 0 && !props.isEmpty()) {
        jugador = jugadorRepository.getJugadorByID(jugador.getJugadorId());
        Propiedad propiedadMasCara = props.remove(0);
        venderPropiedad(propiedadMasCara, jugador);
        logger.debug(
            "{} ha vendido la propiedad {} por {} monedas.",
            jugador.getNombreJugador(),
            propiedadMasCara.getIdPropiedad(),
            propiedadMasCara.getPrecioCompra());
      }
    } catch (SQLException e) {
      logger.error(
          "Error al liquidar deuda con el banco del jugador {}", jugador.getJugadorId(), e);
    }
  }

  @Override
  public List<Jugador> obtenerRankingJugadoresDesc() {
    try {
      int count = jugadorRepository.getPlayerCount();
      List<Jugador> jugadores = new java.util.ArrayList<>();
      for (int i = 1; i <= count; i++) {
        Jugador j = jugadorRepository.getJugadorByID(jugadorRepository.getPlayerIdByNumJugador(i));
        int patrimonio = propiedadRepository.getPatrimonioTotalJugador(j.getJugadorId());
        // Capital total: dinero + valor propiedades
        j =
            new Jugador(
                j.getJugadorId(),
                j.getNombreJugador(),
                j.getPosicion(),
                j.getEstado(),
                j.getDinero() + patrimonio,
                j.getPropiedades());
        jugadores.add(j);
      }
      return jugadores.stream()
          .sorted(Comparator.comparingInt(Jugador::getDinero).reversed())
          .collect(Collectors.toList());
    } catch (SQLException e) {
      logger.error("Error al calcular ranking de jugadores por capital", e);
      return java.util.Collections.emptyList();
    }
  }

  @Override
  public void liquidarDeudaEntreJugadores(Jugador deudor, Jugador acreedor) {
    try {
      List<Propiedad> props = propiedadRepository.getPropiedadesByJugador(deudor.getJugadorId());
      int valorTotalPropiedades =
          propiedadRepository.getPatrimonioTotalJugador(deudor.getJugadorId());

      if (deudor.getDinero() + valorTotalPropiedades < 0) {
        logger.debug(
            "{} no puede saldar la deuda con {} ni con todo su patrimonio",
            deudor.getNombreJugador(),
            acreedor.getNombreJugador());
        // TODO: lógica de finalizarPartida
        return;
      }

      // Ordenar de mayor a menor valor
      props.sort(Comparator.comparingInt(Propiedad::getPrecioCompra).reversed());

      while (deudor.getDinero() < 0 && !props.isEmpty()) {
        // Releer saldos actuales
        deudor = jugadorRepository.getJugadorByID(deudor.getJugadorId());
        Propiedad propiedad = props.remove(0);

        // Transferir propiedad: quitar al deudor y asignar al acreedor manteniendo nivel
        try {
          propiedadRepository.venderAdquisicion(propiedad.getIdPropiedad(), deudor.getJugadorId());
          propiedadRepository.addAdquisicion(
              acreedor.getJugadorId(), propiedad.getIdPropiedad(), propiedad.getNivelPropiedad());
        } catch (SQLException e) {
          logger.error(
              "Error al transferir la propiedad {} de {} a {}",
              propiedad.getIdPropiedad(),
              deudor.getJugadorId(),
              acreedor.getJugadorId(),
              e);
          continue;
        }

        // El valor de la propiedad cubre deuda del deudor; no modifica saldo del acreedor
        int nuevoSaldoDeudor = deudor.getDinero() + propiedad.getPrecioCompra();
        jugadorRepository.updateDinero(deudor.getJugadorId(), nuevoSaldoDeudor);
        logger.debug(
            "Transferida propiedad {} (valor={}) de {} a {}. Saldo deudor: {}",
            propiedad.getIdPropiedad(),
            propiedad.getPrecioCompra(),
            deudor.getJugadorId(),
            acreedor.getJugadorId(),
            nuevoSaldoDeudor);
      }
    } catch (SQLException e) {
      logger.error(
          "Error al liquidar deuda entre jugadores de {} a {}",
          deudor.getJugadorId(),
          acreedor.getJugadorId(),
          e);
    }
  }
}
