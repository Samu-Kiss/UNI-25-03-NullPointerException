package com.NullPtr.Pontiland.controllers;

import java.util.List;

public interface IHUDcontroller {

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

  void hideAuction();
}
