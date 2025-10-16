package com.NullPtr.Pontiland.entities;

import com.NullPtr.Pontiland.utils.PropertiesReader;
import com.jme3.scene.Spatial;

public class Ficha {
  private Spatial spatial = null;
  private int idFicha = -1;
  private int jugadorId = -1;
  private String nombreFicha = "";
  private String rutaFicha = null;

  public Ficha(int idFicha, int jugadorId, String nombreFicha) {
    this.idFicha = idFicha;
    this.jugadorId = jugadorId;
    this.nombreFicha = nombreFicha;
    this.rutaFicha = PropertiesReader.getProperty("Modelo" + nombreFicha);
  }

  public int getJugadorId() {
    return jugadorId;
  }

  public void setJugadorId(int jugadorId) {
    this.jugadorId = jugadorId;
  }

  public int getIdFicha() {
    return idFicha;
  }

  public String getNombreFicha() {
    return nombreFicha;
  }

  public String getRutaFicha() {
    return rutaFicha;
  }

  public Spatial getSpatial() {
    return spatial;
  }

  @Override
  public String toString() {
    return "Ficha{" + "nombreFicha='" + nombreFicha + '\'' + ", rutaFicha=" + rutaFicha + '}';
  }
}
