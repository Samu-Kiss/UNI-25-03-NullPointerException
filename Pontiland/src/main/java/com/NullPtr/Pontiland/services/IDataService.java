package com.NullPtr.Pontiland.services;

import java.sql.Connection;

public interface IDataService {
    Connection createConnection();
    void newDataBase();
    void loadDataBase(String archivoSeleccionado);
}
