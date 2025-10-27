package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Ficha;
import com.NullPtr.Pontiland.entities.Jugador;
import java.sql.SQLException;

public interface IJugadorRepository {
  void newPlayer(Jugador newPlayer, int icono) throws SQLException;

  void changeActivePlayer(int nuevoID) throws SQLException;

  int getActivePlayer() throws SQLException;

    void goToJail(int jugadorID) throws SQLException;

    int getPlayerIdByNumJugador(int numJugador) throws SQLException;

  int getNumJugadorByPlayerId(int playerId) throws SQLException;

  void newActivePlayer(int jugadorID) throws SQLException;

  Jugador getJugadorByID(int jugadorID) throws SQLException;

  void setPartidaID(long partidaID);

  Ficha[] getFichas(int numJugadores) throws SQLException;

  void updateJugador(Jugador jugador) throws SQLException;

  void rentPaymentTransaction(Jugador j1, Jugador j2) throws SQLException;
}
