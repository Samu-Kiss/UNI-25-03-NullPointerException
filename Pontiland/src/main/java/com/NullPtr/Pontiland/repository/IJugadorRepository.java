package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Jugador;
import java.sql.SQLException;

public interface IJugadorRepository {
  void newPlayer(Jugador newPlayer, long partidaID, int icono) throws SQLException;
  int getPlayerIdByNumJugador(int numJugador, long partidaID) throws SQLException;
  void changeActivePlayer(int id, long partidaID) throws SQLException;
  void insertActivePlayer(int jugadorID, long partidaID) throws SQLException;
}
