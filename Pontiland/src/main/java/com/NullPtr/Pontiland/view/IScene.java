package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.controllers.LanzamientoDadosController;
import com.NullPtr.Pontiland.entities.Ficha;
import com.jme3.app.LegacyApplication;
import com.jme3.bullet.BulletAppState;

public interface IScene {
    void replicateFichaPosition(int jugadorId, int casillaIndex);
    void loadFichasModels(Ficha[] data);
    void update(float tpf);
}
