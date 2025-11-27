package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.view.IScene;

public interface ITurnService {

  void setScene(IScene scene);

  void nextTurn();

  void update();

  void buyProperty();

  void payRent();

  void setEnabled(boolean enabled);

  boolean isEnabled();

  void terminarTurno();

  boolean iniciarSubasta();

  boolean increaseAuction(int delta);

  void exitAuction();

  void rollDice();
}
