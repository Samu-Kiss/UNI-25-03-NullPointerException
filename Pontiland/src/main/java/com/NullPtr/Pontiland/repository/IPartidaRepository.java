package com.NullPtr.Pontiland.repository;

import java.sql.SQLException;

public interface IPartidaRepository {
  long newPartida(int numJugadores) throws SQLException;

  int getNumJugadores() throws SQLException;

  long getPartidaID();
}
