package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Casilla;

public interface ICasillaRepository {
    Casilla casillaFromPosition(int posicion);
}
