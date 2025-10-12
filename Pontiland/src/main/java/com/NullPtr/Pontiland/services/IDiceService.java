package com.NullPtr.Pontiland.services;

import com.jme3.scene.Spatial;

public interface IDiceService {
  void setDados(Spatial dado1, Spatial dado2);

  void lanzarDados();

  Byte[] leerDados();

  Byte[] getResultados();

  void lanzamientoDados();

  void update();

  void setTurnService(ITurnService turnService);
}
