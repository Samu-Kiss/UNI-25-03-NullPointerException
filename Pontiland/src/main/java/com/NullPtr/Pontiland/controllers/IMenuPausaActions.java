package com.NullPtr.Pontiland.controllers;

/** Defines callbacks triggered from the in-game pause menu. */
public interface IMenuPausaActions {
  /** Resumes gameplay, dismissing the pause overlay. */
  void resumeGame();

  /** Persists the current game state and exits to the main menu. */
  void saveAndExit();
}
