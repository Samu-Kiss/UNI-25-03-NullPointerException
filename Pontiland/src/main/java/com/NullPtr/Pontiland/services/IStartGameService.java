package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import java.sql.SQLException;
import java.util.ArrayList;

public interface IStartGameService {
  void creatingNewGame(ArrayList<Jugador> jugadores, ArrayList<Integer> iconos) throws SQLException;

  void loadingOldGame(String archivoSeleccionado);

  void ensureSceneReady();
}
