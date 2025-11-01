package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.view.HUD;
import com.jme3.app.state.AppStateManager;
import java.util.List;
import java.util.Objects;

/**
 * Controlador del HUD que orquesta la vista {@link HUD} como AppState de Lemur. Se encarga de
 * adjuntarla/desadjuntarla y expone un API para actualizar sus componentes.
 */
public class HUDController {

  private final Launcher app;
  private HUD hud;

  public HUDController(Launcher app) {
    this.app = Objects.requireNonNull(app, "app no puede ser null");
  }

  private AppStateManager stateManager() {
    return app.getStateManager();
  }

  /** Crea (si es necesario) y adjunta el HUD al gestor de estados. */
  public void showHUD() {
    if (hud == null) {
      hud = new HUD();
    }
    if (!stateManager().hasState(hud)) {
      stateManager().attach(hud);
    }
    hud.setVisible(true);
  }

  /** Oculta el HUD sin desadjuntarlo. */
  public void hideHUD() {
    if (hud != null) {
      hud.setVisible(false);
    }
  }

  /** Elimina el HUD del gestor de estados. */
  public void detachHUD() {
    if (hud != null && stateManager().hasState(hud)) {
      stateManager().detach(hud);
    }
  }

  public void setPlayerNames(List<String> names) {
    if (hud != null) {
      hud.setPlayerNames(names);
    }
  }

  // ==== Wrappers de actualización ====

  public void updatePlayerCard(
      String playerName, String moneyText, boolean inJail, int playerIndex) {
    if (hud != null) {
      hud.updatePlayerCard(playerName, moneyText, inJail, playerIndex);
    }
  }

  public void updatePropertyCard(String name, String priceText, String[] rentsText) {
    if (hud != null) {
      hud.updatePropertyCard(name, priceText, rentsText);
    }
  }

  public void showPropertyCard(String name, String priceText, String[] rentsText, int groupIndex) {
    if (hud != null) {
      hud.showPropertyCard(name, priceText, rentsText, groupIndex);
    }
  }

  public void hidePropertyCard() {
    if (hud != null) {
      hud.hidePropertyCard();
    }
  }

  public void updatePropertyTokens(String[] tokens) {
    if (hud != null) {
      hud.updatePropertyTokens(tokens);
    }
  }

  public void showAuction(String propertyName, String currentPriceText) {
    if (hud != null) {
      hud.showAuction(propertyName, currentPriceText);
    }
  }

  public void hideAuction() {
    if (hud != null) {
      hud.hideAuction();
    }
  }
}
