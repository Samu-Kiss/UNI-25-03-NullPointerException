package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Ficha;
import com.NullPtr.Pontiland.entities.Jugador;
import java.sql.SQLException;

public interface IJugadorRepository {
  void newPlayer(Jugador newPlayer, int icono) throws SQLException;

  int getActivePlayerID() throws SQLException;

  int getPlayerIdByNumJugador(int numJugador) throws SQLException;

  void changeActivePlayer(int numJugadores) throws SQLException;

  void insertActivePlayer(int jugadorID) throws SQLException;

  Jugador getJugadorByID(int jugadorID) throws SQLException;

  void setPartidaID(long partidaID);

  int getNumJugadorByPlayerId(int playerId) throws SQLException;

  Ficha[] getFichas() throws SQLException;

  int numJugadores() throws SQLException;

  void updateJugadorByID(Jugador jugador) throws SQLException;
}
