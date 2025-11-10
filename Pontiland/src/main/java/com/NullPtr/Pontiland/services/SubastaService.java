package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import java.sql.SQLException;


public class SubastaService implements ISubastaService {
  private final IAdquisicionService adquisicionService;
  private final IJugadorRepository jugadorRepository;
  private final IHUDcontroller hudController;
  private final IPropiedadRepository propiedadRepository;

  private boolean subastaActiva = false;
  private int precioActual = 0;
  private int posicion = -1;
  private int indiceActual = 1;

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
      if (hudController != null) {
        hudController.showAuction("Subasta", String.valueOf(precioActual));
        hudController.setAuctionPlayerName(jugadorActivo.getNombreJugador());
      }
      return true;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean pujar() {
    if (!subastaActiva) return false;
    try {
      int total = jugadorRepository.getPlayerCount();
      indiceActual = (indiceActual % total) + 1;
      int jugadorId = jugadorRepository.getPlayerIdByNumJugador(indiceActual);
      Jugador jugador = jugadorRepository.getJugadorByID(jugadorId);
      if (hudController != null) hudController.setAuctionPlayerName(jugador.getNombreJugador());
      return true;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
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
      pujar();
      return true;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean comprarActual() {
    if (!subastaActiva) return false;
    try {
      int compradorId = jugadorRepository.getPlayerIdByNumJugador(indiceActual);
      Jugador comprador = jugadorRepository.getJugadorByID(compradorId);
      boolean comprar = adquisicionService.comprarPropiedadEnSubasta(posicion, comprador, precioActual);
      if (comprar && hudController != null) hudController.hideAuction();
      reset();
      return comprar;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void reset() {
    subastaActiva = false;
    precioActual = 0;
    posicion = -1;
    indiceActual = 1;
  }
}
