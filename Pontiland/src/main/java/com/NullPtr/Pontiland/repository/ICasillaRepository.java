package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Casilla;
import java.sql.SQLException;

public interface ICasillaRepository {
  Casilla casillaFromPosition(int posicion) throws SQLException;
}
