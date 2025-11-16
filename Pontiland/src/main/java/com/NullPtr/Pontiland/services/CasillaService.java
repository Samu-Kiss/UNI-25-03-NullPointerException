package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import com.NullPtr.Pontiland.repository.TarjetaEventoRepository;
import java.sql.SQLException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CasillaService implements ICasillaService {

  private boolean irACarcel = false;
  private IHUDcontroller hudController;
  private DiceService diceService;
  private IPropiedadRepository propiedadRepository;
  private IAdquisicionService adquisicionService;
  private TarjetaEventoRepository tarjetaEventoRepository;

  // Logger
  private static Logger logger = LogManager.getLogger(CasillaService.class);

  public CasillaService(
      IHUDcontroller hudController,
      DiceService diceService,
      IPropiedadRepository propiedadRepository,
      IAdquisicionService adquisicionService,
      TarjetaEventoRepository tarjetaEventoRepository) {
    this.hudController = hudController;
    this.diceService = diceService;
    this.propiedadRepository = propiedadRepository;
    this.adquisicionService = adquisicionService;
    this.tarjetaEventoRepository = tarjetaEventoRepository;
  }

  @Override
  public void interaccion(Jugador jugador, Casilla casilla) {
    logger.debug(
        "{} ha caido en la casilla {}", jugador.getNombreJugador(), casilla.getTipoCasilla());

    switch (casilla.getTipoCasilla()) {
      case PARADALIBRE:
        onParadaLibre(jugador, casilla);
        break;
      case EVENTO:
        onEvento(jugador, casilla);
        break;
      case PROPIEDAD:
        onPropiedad(casilla);
        break;
      case MOVIMIENTO:
        onMovimiento(jugador, casilla);
        break;
      case IRALACARCEL:
        onCarcel(true);
        break;
    }
  }

  @Override
  public void terminarInteraccion(Jugador jugador, Casilla casilla) {
    if (hudController == null) return;
    switch (casilla.getTipoCasilla()) {
      case PARADALIBRE:
        hudController.terminarTurno();
        break;
      case EVENTO:
        hudController.terminarTurno();
        break;
      case PROPIEDAD:
        if (hudController != null && hudController.getPuedeComprar()) {
          try {
            adquisicionService.comprarPropiedadPorPosicion(casilla.getPosicionTablero(), jugador);

            hudController.hidePropertyCard();
            hudController.terminarTurno();
          } catch (Exception ex) {
            logger.error("Failed to purchase property: {}", casilla.getNombreCasilla(), ex);
          }
        }

        break;
      case MOVIMIENTO:
        hudController.terminarTurno();
        break;
      case IRALACARCEL:
        if (hudController != null) {
          hudController.terminarTurno();
        }
        onCarcel(false);
        break;
    }
  }

  private void onParadaLibre(Jugador j, Casilla c) {
    if (diceService != null) diceService.enableInteract(false);
  }

  private void onEvento(Jugador j, Casilla c) {
    if (diceService != null) diceService.enableInteract(false);
  }

  private void onPropiedad(Casilla casilla) {

    Propiedad prop = null;
    if (propiedadRepository != null) {
      try {
        prop = propiedadRepository.getPropiedadByPosition(casilla.getPosicionTablero());
      } catch (Exception ex) {
        logger.error("Failed to read Propiedad from repository: {}", ex.getMessage(), ex);
      }
    }

    String name;
    String priceText;
    String[] rentsText;
    int groupIndex;

    if (prop == null) {
      logger.warn(
          "Warning: propiedad is null for casilla at position {}", casilla.getPosicionTablero());

      return;
    }

    name = casilla.getNombreCasilla();
    priceText = String.valueOf(prop.getPrecioCompra());
    rentsText = prop.getRentasText();
    groupIndex = prop.getGrupo();
    logger.debug("Nombre Propiedad: {}", name);
    hudController.showPropertyCard(name, priceText, rentsText, groupIndex);

    if (diceService != null) diceService.enableInteract(false);
    hudController.setPuedeComprar(false);
  }

  private void onMovimiento(Jugador j, Casilla c) {
    if (diceService != null) diceService.enableInteract(false);
  }

  private void onCarcel(boolean irACarcel) {
    this.irACarcel = irACarcel;
  }

  @Override
  public boolean getIrACarcel() {
    return irACarcel;
  }

  @Override
  public void updateActivePlayerPropertyTokens(Jugador jugador) {
    if (hudController == null) return;
    if (jugador == null) {
      logger.warn("Jugador activo no encontrado");
      hudController.updatePropertyTokens(new String[0]);
      return;
    }

    List<Propiedad> propiedades = null;
    try {
      propiedades = propiedadRepository.getPropiedadesByJugador(jugador.getJugadorId());
    } catch (SQLException e) {
      logger.error("Error al obtener las propiedades del jugador {}", jugador.getJugadorId(), e);
      hudController.updatePropertyTokens(new String[0]);
      return;
    }

    if (propiedades == null || propiedades.isEmpty()) {
      logger.debug("El jugador {} no tiene propiedades", jugador.getJugadorId());
      hudController.updatePropertyTokens(new String[0]);
      return;
    }

    String[] tokens = new String[propiedades.size()];
    for (int i = 0; i < propiedades.size(); i++) {
      Propiedad p = propiedades.get(i);
      String propNum = String.valueOf(p.getIdPropiedad());
      String nivel = String.valueOf(p.getNivelPropiedad());
      int grupo = p.getGrupo();
      tokens[i] = propNum + "|" + nivel + "|" + grupo;
      logger.debug("Token creado -> propiedad={}, nivel= {}, grupo={}", propNum, nivel, grupo);
    }

    hudController.updatePropertyTokens(tokens);
  }
}
