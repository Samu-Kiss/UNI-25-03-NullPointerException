package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.SavedGame;
import java.sql.Connection;
import java.util.List;

public interface IDataService {
  Connection createConnection();

  void newDataBase();

  void loadDataBase(String archivoSeleccionado);

  List<SavedGame> listarPartidasPasadas();
}
