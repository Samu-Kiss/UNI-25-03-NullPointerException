package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.SavedGame;

import java.sql.SQLException;
import java.util.List;

public interface IPartidaRepository {
  long newPartida(int numJugadores) throws SQLException;

  int getNumJugadores() throws SQLException;

  long getPartidaID();

  List<SavedGame> getAllPartidaIDs() throws SQLException;
}
