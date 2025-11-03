package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Propiedad;
import java.sql.SQLException;

public interface IAdquisicionService {

  boolean comprarPropiedadPorPosicion(int position) throws SQLException;

}

