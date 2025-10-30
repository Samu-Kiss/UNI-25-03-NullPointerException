package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.view.IScene;

public interface ITurnService {

  void setScene(IScene scene);

  void nextTurn();

  void update();

  void buyProperty();

  void payRent();
}
