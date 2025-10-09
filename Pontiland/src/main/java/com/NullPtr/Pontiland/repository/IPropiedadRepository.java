package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Propiedad;

public interface IPropiedadRepository {

    Propiedad getPropiedadById(int id);
    void updatePropiedad(Propiedad propiedad);

}
