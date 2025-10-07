package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.entities.Jugador;
import java.util.List;

public interface IMenuActions {
  void startPlayerSelection();

  void loadSavedGame();

  void showCredits(String owner, String repo, int limit);

  void goToMainMenu();

  void startMainGame(int playerCount);

  // Nueva sobrecarga para iniciar el juego con datos detallados de jugadores y personajes
  void startMainGame(int playerCount, List<Jugador> jugadores, List<Integer> personajeIds);
}
