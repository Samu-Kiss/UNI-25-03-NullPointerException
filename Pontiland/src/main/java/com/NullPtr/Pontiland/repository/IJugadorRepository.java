package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Jugador;
import java.sql.SQLException;

public interface IJugadorRepository {
  void newPlayer(Jugador newPlayer, long partidaID, int icono) throws SQLException;
}
