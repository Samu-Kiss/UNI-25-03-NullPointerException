package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;

public class CasillaService implements ICasillaService {

  private boolean irACarcel = false;
  private IHUDcontroller hudController;
  private DiceService diceService;
  private IPropiedadRepository propiedadRepository;
  private IAdquisicionService adquisicionService;

  public CasillaService(
      IHUDcontroller hudController,
      DiceService diceService,
      IPropiedadRepository propiedadRepository,
      IAdquisicionService adquisicionService) {
    this.hudController = hudController;
    this.diceService = diceService;
    this.propiedadRepository = propiedadRepository;
    this.adquisicionService = adquisicionService;
  }

  @Override
  public void interaccion(Jugador jugador, Casilla casilla) {
    System.out.println(
        "\n" + jugador.getNombreJugador() + " ha caido en la casilla " + casilla.getTipoCasilla());
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
        onCarcel();
        break;
    }
  }

  @Override
  public void terminarInteraccion(Jugador jugador, Casilla casilla) {
    switch (casilla.getTipoCasilla()) {
      case PARADALIBRE:
        if (diceService != null) diceService.enableInteract(true);
        hudController.terminarTurno();
        break;
      case EVENTO:
        if (diceService != null) diceService.enableInteract(true);
        hudController.terminarTurno();
        break;
      case PROPIEDAD:
        if (hudController != null) {
          if (hudController.getPuedeComprar()) {
            try {
              adquisicionService.comprarPropiedadPorPosicion(casilla.getPosicionTablero(), jugador);
              if (diceService != null) diceService.enableInteract(true);
              hudController.hidePropertyCard();
              hudController.terminarTurno();
            } catch (Exception ex) {
              System.err.println("Warning: failed to purchase property: " + ex.getMessage());
            }
          }
        }

        break;
      case MOVIMIENTO:
        if (diceService != null) diceService.enableInteract(true);
        hudController.terminarTurno();
        break;
      case IRALACARCEL:
        if (diceService != null) diceService.enableInteract(true);
        hudController.terminarTurno();
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
        System.err.println("Warning: failed to read Propiedad from repository: " + ex.getMessage());
      }
    }

    String name;
    String priceText;
    String[] rentsText;
    int groupIndex;
    name = casilla.getNombreCasilla();
    priceText = String.valueOf(prop.getPrecioCompra());
    rentsText = prop.getRentasText();
    groupIndex = prop.getGrupo();
    System.out.println("Nombre Propiedad: " + name);
    hudController.showPropertyCard(name, priceText, rentsText, groupIndex);

    if (diceService != null) diceService.enableInteract(false);
    hudController.setPuedeComprar(false);
  }

  private void onMovimiento(Jugador j, Casilla c) {
    if (diceService != null) diceService.enableInteract(false);
  }

  private void onCarcel() {
    irACarcel = true;
  }

  @Override
  public boolean getIrACarcel() {
    return irACarcel;
  }
}
