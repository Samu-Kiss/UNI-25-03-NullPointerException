package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Jugador;
import java.sql.SQLException;

public interface IJugadorRepository {
  void newPlayer(Jugador newPlayer, int icono) throws SQLException;

  int getPlayerIdByNumJugador(int numJugador) throws SQLException;

  void changeActivePlayer(int numJugadores) throws SQLException;

  void insertActivePlayer(int jugadorID) throws SQLException;
}
