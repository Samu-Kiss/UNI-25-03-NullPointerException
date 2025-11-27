package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.entities.Jugador;
import java.sql.SQLException;
import java.util.ArrayList;

public interface IMenuActions {
  void startPlayerSelection();

  void loadSavedGame();

  void showCredits(String owner, String repo, int limit);

  void goToMainMenu();

  void startMainGame(int playerCount);

  // Nueva sobrecarga para iniciar el juego con datos detallados de jugadores y personajes
  void startMainGame(int playerCount, ArrayList<Jugador> jugadores, ArrayList<Integer> personajeIds)
      throws SQLException;

  /** Muestra la pantalla de fin de juego con los resultados finales. */
  void showFinDeJuego();
}
