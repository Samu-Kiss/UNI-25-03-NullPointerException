package com.NullPtr.Pontiland.controllers;

public interface IMenuActions {
  void startPlayerSelection();

  void loadSavedGame();

  void showCredits(String owner, String repo, int limit);

  void goToMainMenu();

  void startMainGame(int playerCount);
}
