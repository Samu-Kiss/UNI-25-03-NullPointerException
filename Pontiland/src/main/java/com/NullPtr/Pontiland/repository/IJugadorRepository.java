package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Jugador;

public interface IJugadorRepository {
  void newPlayer(Jugador newPlayer, long partidaID, int icono);
}
