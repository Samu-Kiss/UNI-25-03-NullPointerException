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

  void aplicarCostoSistema(int costo);

  /** Finaliza la partida: deshabilita turnos/inputs y muestra pantalla de fin de juego. */
  void finalizarPartida();

  /**
   * Establece la referencia al controlador de menú para navegación al finalizar partida.
   *
   * @param menuActions referencia a las acciones del menú
   */
  void setMenuActions(com.NullPtr.Pontiland.controllers.IMenuActions menuActions);
}
