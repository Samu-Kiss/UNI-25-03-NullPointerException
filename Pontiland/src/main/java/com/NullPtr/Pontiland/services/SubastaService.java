package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubastaService implements ISubastaService {
  private final IAdquisicionService adquisicionService;
  private final IJugadorRepository jugadorRepository;
  private final IHUDcontroller hudController;
  private final IPropiedadRepository propiedadRepository;

  private boolean subastaActiva = false;
  private int precioActual = 0;
  private int posicion = -1;
  private int indiceActual = 1;
  private final Set<Integer> excluidos = new HashSet<>();

  // Logger
  private static Logger logger = LogManager.getLogger(SubastaService.class);

  public SubastaService(
      IAdquisicionService adquisicionService,
      IJugadorRepository jugadorRepository,
      IHUDcontroller hudController,
      IPropiedadRepository propiedadRepository) {
    this.adquisicionService = adquisicionService;
    this.jugadorRepository = jugadorRepository;
    this.hudController = hudController;
    this.propiedadRepository = propiedadRepository;
  }

  @Override
  public boolean iniciarSubasta() {
    if (subastaActiva) return false;
    try {
      int jugadorActivoId = jugadorRepository.getActivePlayer();
      Jugador jugadorActivo = jugadorRepository.getJugadorByID(jugadorActivoId);
      int position = jugadorActivo.getPosicion();
      Propiedad propiedad = adquisicionService.prepararSubasta(position);
      if (propiedad == null) return false;
      subastaActiva = true;
      posicion = position;
      precioActual = Math.max(1, propiedad.getPrecioCompra() / 2);
      indiceActual = jugadorRepository.getNumJugadorByPlayerId(jugadorActivoId);
      excluidos.clear();
      excluidos.add(indiceActual);
      avanzarAlSiguienteJugador();
      if (hudController != null) {
        hudController.showAuction("Subasta", String.valueOf(precioActual));
        Jugador turno =
            jugadorRepository.getJugadorByID(
                jugadorRepository.getPlayerIdByNumJugador(indiceActual));
        hudController.setAuctionPlayerName(turno.getNombreJugador());
      }
      return true;
    } catch (SQLException e) {
      logger.error("Error al iniciar la subasta", e);
    }
    return false;
  }

  @Override
  public void avanzarAlSiguienteJugador() throws SQLException {
    int total = jugadorRepository.getPlayerCount();
    if (excluidos.size() >= total - 1) {
      comprarActual();
      return;
    }
    int intentos = 0;
    do {
      indiceActual = (indiceActual % total) + 1;
      intentos++;
      if (intentos > total) break;
    } while (excluidos.contains(indiceActual));

    int jugadorId = jugadorRepository.getPlayerIdByNumJugador(indiceActual);
    Jugador jugador = jugadorRepository.getJugadorByID(jugadorId);
    if (hudController != null) hudController.setAuctionPlayerName(jugador.getNombreJugador());
  }

  @Override
  public boolean aumentarPrecio(int delta) {
    if (!subastaActiva || delta <= 0) return false;
    try {
      int jugadorId = jugadorRepository.getPlayerIdByNumJugador(indiceActual);
      Jugador j = jugadorRepository.getJugadorByID(jugadorId);
      int nuevoPrecio = precioActual + delta;
      if (j.getDinero() < nuevoPrecio) return false;
      precioActual = nuevoPrecio;
      if (hudController != null) hudController.showAuction("Subasta", String.valueOf(precioActual));
      avanzarAlSiguienteJugador();
      return true;
    } catch (SQLException e) {
      logger.error("Error al aumentar el precio de la subasta", e);
    }
    return false;
  }

  @Override
  public boolean comprarActual() {
    if (!subastaActiva) return false;
    try {
      int compradorId = jugadorRepository.getPlayerIdByNumJugador(indiceActual);
      Jugador comprador = jugadorRepository.getJugadorByID(compradorId);
      boolean comprar =
          adquisicionService.comprarPropiedadEnSubasta(posicion, comprador, precioActual);
      if (comprar && hudController != null) hudController.hideAuction();
      reset();
      if (hudController != null) {
        hudController.terminarTurno();
      }
      return comprar;
    } catch (SQLException e) {
      logger.error("Error al comprar la propiedad en la subasta", e);
      return false;
    }
  }

  @Override
  public void salirSubasta() {
    if (!subastaActiva) return;
    try {
      excluidos.add(indiceActual);
      int total = jugadorRepository.getPlayerCount();
      if (excluidos.size() >= total - 1) {
        for (int i = 1; i <= total; i++) {
          if (!excluidos.contains(i)) {
            indiceActual = i;
            break;
          }
        }
        comprarActual();
        return;
      }
      avanzarAlSiguienteJugador();
      if (subastaActiva) {
        int jugadorId = jugadorRepository.getPlayerIdByNumJugador(indiceActual);
        Jugador jugador = jugadorRepository.getJugadorByID(jugadorId);
        if (hudController != null) hudController.setAuctionPlayerName(jugador.getNombreJugador());
      }
    } catch (SQLException e) {
      logger.error("Error al salir de la subasta", e);
    }
  }

  private void reset() {
    subastaActiva = false;
    precioActual = 0;
    posicion = -1;
    indiceActual = 1;
    excluidos.clear();
  }

  // Para q sonarquba no se queje
  public IPropiedadRepository getPropiedadRepository() {
    return propiedadRepository;
  }
}
