package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;

import java.sql.SQLException;
import java.util.ArrayList;

public interface IStartGameService {
  void creatingNewGame(ArrayList<Jugador> jugadores, ArrayList<Integer> iconos) throws SQLException;

  void loadingOldGame(String archivoSeleccionado);

  void ensureSceneReady();
  IJugadorRepository getJugadorRepository();
  IPartidaRepository getPartidaRepository();
}
