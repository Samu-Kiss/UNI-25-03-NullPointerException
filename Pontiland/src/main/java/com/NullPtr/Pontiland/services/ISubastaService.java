package com.NullPtr.Pontiland.services;

import java.sql.SQLException;

public interface ISubastaService {
  boolean iniciarSubasta();

  void avanzarAlSiguienteJugador() throws SQLException;

  boolean aumentarPrecio(int delta);

  boolean comprarActual();

  void salirSubasta();
}
