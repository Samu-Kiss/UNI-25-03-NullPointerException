package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.services.ITurnService;
import com.NullPtr.Pontiland.view.HUD;
import java.util.List;

public interface IHUDcontroller {
  void setTurnService(ITurnService turnService);

  void setHud(HUD hud);

  void showHUD();

  void hideHUD();

  void detachHUD();

  void setPlayers(List<Jugador> jugadores);

  void updatePlayerCard(Jugador jugador, int playerIndex);

  void setActivePlayerIndex(int playerIndex);

  void updatePropertyCard(String name, String priceText, String[] rentsText);

  void showPropertyCard(String name, String priceText, String[] rentsText, int groupIndex);

  void hidePropertyCard();

  void updatePropertyTokens(String[] tokens);

  void showAuction(String propertyName, String currentPriceText);

  void comprarPropiedad();

  void iniciarSubasta();

  void hideAuction();

  void terminarTurno();

  boolean getPuedeComprar();

  void setPuedeComprar(boolean puedeComprar);

  void increaseAuction(int delta);

  void exitAuction();

  void setAuctionPlayerName(String playerName);

  // Eventos: mostrar carta buena, carta mala y ocultar cartas de evento
  void showGoodEvent(String title, String descriptionText);

  void showBadEvent(String title, String descriptionText);

  void hideEventCards();

  // Jail decision overlay (Pay/Roll)
  void showJailDecision();

  void hideJailDecision();

  // Set decision flags when JailDecision buttons are pressed
  void chooseJailPay();

  void chooseJailRoll();

  // Poll decision via two booleans
  boolean getJailPay();

  boolean getJailRoll();
}
