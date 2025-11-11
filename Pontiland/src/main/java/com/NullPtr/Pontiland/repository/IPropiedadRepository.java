package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import java.util.List;

public interface IPropiedadRepository {

  void setPartidaID(long partidaID);

  Propiedad getPropiedadByPosition(int position);

  Jugador propiedadHasOwner(int propiedadID);

  int getNivelPropiedad(int propiedadID);

  void incrementarNivelPropiedad(int propiedadID);

  Integer getOwnerIdByPropiedadId(int propiedadId);

  List<Propiedad> getPropiedadesByJugador(int jugadorID);

  void addAdquisicion(int jugadorId, int propiedadId, int nivel);
}
