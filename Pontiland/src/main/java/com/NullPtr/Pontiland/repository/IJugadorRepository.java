package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Jugador;
import java.sql.SQLException;
import java.util.List;

public interface IJugadorRepository {
  void newPlayer(Jugador newPlayer, int icono) throws SQLException;

  int getPlayerIdByNumJugador(int numJugador) throws SQLException;

  void changeActivePlayer(int numJugadores) throws SQLException;

  void insertActivePlayer(int jugadorID) throws SQLException;

  Jugador getJugadorByID(int jugadorID) throws SQLException;

  void setPartidaID(long partidaID);
}
