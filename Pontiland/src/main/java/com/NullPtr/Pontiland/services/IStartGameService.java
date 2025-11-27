package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.SavedGame;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface IStartGameService {
  void creatingNewGame(ArrayList<Jugador> jugadores, ArrayList<Integer> iconos) throws SQLException;

  void loadingOldGame(String archivoSeleccionado) throws SQLException;

  void ensureSceneReady();

  List<SavedGame> listPastGames();
}
