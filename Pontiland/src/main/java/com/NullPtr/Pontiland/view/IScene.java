package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.entities.Ficha;

public interface IScene {
  void replicateFichaPosition(int jugadorId, int casillaIndex);

  void loadFichasModels(Ficha[] data);

  void update(float tpf);

  void resetCamera();
}
