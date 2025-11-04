package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.services.DiceService;
import com.NullPtr.Pontiland.services.ITurnService;
import com.NullPtr.Pontiland.view.HUD;

import java.util.List;

public interface IHUDcontroller {
  void setTurnService(ITurnService turnService);
  void setDiceService(DiceService diceService);
  void setHud(HUD hud);

  void showHUD();

  void hideHUD();

  void detachHUD();

  void setPlayerNames(List<String> names);

  void updatePlayerCard(String playerName, String moneyText, boolean inJail, int playerIndex);

  void updatePropertyCard(String name, String priceText, String[] rentsText);

  void showPropertyCard(String name, String priceText, String[] rentsText, int groupIndex);

  void hidePropertyCard();

  void updatePropertyTokens(String[] tokens);

  void showAuction(String propertyName, String currentPriceText);

  void comprarPropiedad();

  void hideAuction();

  void terminarTurno();

  boolean getPuedeComprar();

  void setPuedeComprar(boolean puedeComprar);

  void setAdquisicionService(com.NullPtr.Pontiland.services.IAdquisicionService adquisicionService);
}
