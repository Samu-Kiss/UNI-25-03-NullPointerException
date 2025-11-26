package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import java.sql.SQLException;
import java.util.List;

public interface IPropiedadRepository {

  void setPartidaID(long partidaID);

  Propiedad getPropiedadByPosition(int position) throws SQLException;

  Jugador propiedadHasOwner(int propiedadID) throws SQLException;

  int getNivelPropiedad(int propiedadID) throws SQLException;

  void incrementarNivelPropiedad(int propiedadID) throws SQLException;

  Integer getOwnerIdByPropiedadId(int propiedadId) throws SQLException;

  List<Propiedad> getPropiedadesByJugador(int jugadorID) throws SQLException;

  void addAdquisicion(int jugadorId, int propiedadId, int nivel) throws SQLException;

  void updateAdquisicionNivel(int propiedadId, int jugadorId, int nuevoNivel) throws SQLException;
}
