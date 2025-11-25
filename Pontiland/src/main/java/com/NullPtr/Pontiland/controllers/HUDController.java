package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.services.ITurnService;
import com.NullPtr.Pontiland.view.HUD;
import com.jme3.app.state.AppStateManager;
import java.util.List;
import java.util.Objects;

/**
 * Controlador del HUD que orquesta la vista {@link HUD} como AppState de Lemur. Se encarga de
 * adjuntarla/desadjuntarla y expone un API para actualizar sus componentes.
 */
public class HUDController implements IHUDcontroller {

  private final Launcher app;
  private HUD hud;

  private ITurnService turnService;

  private boolean puedeComprar = false;

  // Nuevo: servicio de adquisiciones y posición de la propiedad mostrada actualmente
  // TODO: implementar lo q se tenga q implementar para adquisiciones pq sonar molestaba si se
  // dejaba asi
  // private com.NullPtr.Pontiland.services.IAdquisicionService adquisicionService;

  public HUDController(Launcher app) {
    this.app = Objects.requireNonNull(app, "app no puede ser null");
  }

  @Override
  public void setHud(HUD hud) {
    this.hud = hud;
  }

  @Override
  public void comprarPropiedad() {
    if (hud != null) hud.hidePropertyCard();
    puedeComprar = true;
  }

  @Override
  public boolean getPuedeComprar() {
    return puedeComprar;
  }

  @Override
  public void setPuedeComprar(boolean puedeComprar) {
    this.puedeComprar = puedeComprar;
  }

  @Override
  public void setTurnService(ITurnService turnService) {
    this.turnService = turnService;
  }

  private AppStateManager stateManager() {
    return app.getStateManager();
  }

  @Override
  public void showHUD() {
    if (!stateManager().hasState(hud)) {
      stateManager().attach(hud);
    }
    hud.setVisible(true);
  }

  /** Oculta el HUD sin desadjuntarlo. */
  @Override
  public void hideHUD() {
    if (hud != null) {
      hud.setVisible(false);
    }
  }

  /** Elimina el HUD del gestor de estados. */
  @Override
  public void detachHUD() {
    if (hud != null && stateManager().hasState(hud)) {
      stateManager().detach(hud);
    }
  }

  @Override
  public void setPlayers(List<Jugador> jugadores) {
    if (hud != null) {
      hud.setPlayers(jugadores);
    }
  }

  // ==== Wrappers de actualización ====

  @Override
  public void updatePlayerCard(Jugador jugador, int playerIndex) {
    if (hud != null) {
      hud.updatePlayerCard(jugador, playerIndex);
    }
  }

  @Override
  public void updatePropertyCard(String name, String priceText, String[] rentsText) {
    if (hud != null) {
      hud.updatePropertyCard(name, priceText, rentsText);
    }
  }

  @Override
  public void showPropertyCard(String name, String priceText, String[] rentsText, int groupIndex) {
    if (hud != null) {
      hud.showPropertyCard(name, priceText, rentsText, groupIndex);
    }
  }

  @Override
  public void hidePropertyCard() {
    if (hud != null) {
      hud.hidePropertyCard();
    }
  }

  @Override
  public void updatePropertyTokens(String[] tokens) {
    if (hud != null) {
      hud.updatePropertyTokens(tokens);
    }
  }

  @Override
  public void showAuction(String propertyName, String currentPriceText) {
    if (hud != null) {
      hud.showAuction(propertyName, currentPriceText);

      hud.hidePropertyCard();
    }
  }

  @Override
  public void hideAuction() {
    if (hud != null) {
      hud.hideAuction();
    }
  }

  @Override
  public void terminarTurno() {
    turnService.terminarTurno();
  }

  @Override
  public void iniciarSubasta() {
    if (turnService != null) {
      turnService.iniciarSubasta();
    }
  }

  @Override
  public void increaseAuction(int delta) {
    if (turnService != null) {
      turnService.increaseAuction(delta);
    }
  }

  @Override
  public void exitAuction() {
    if (turnService != null) {
      turnService.exitAuction();
    }
  }

  @Override
  public void setAuctionPlayerName(String playerName) {
    if (hud != null) hud.setAuctionPlayerName(playerName);
  }
}
