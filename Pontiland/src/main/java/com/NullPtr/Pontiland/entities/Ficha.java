package com.NullPtr.Pontiland.entities;

import com.NullPtr.Pontiland.utils.PropertiesReader;
import com.jme3.scene.Spatial;

import java.io.File;
import java.nio.file.Path;

public class Ficha {
    private Spatial spatial = null;
    private int idFicha = -1;
    private String nombreFicha = "";
    private File rutaFicha = null;

    public Ficha(int idFicha, String nombreFicha) {
        this.idFicha = idFicha;
        this.nombreFicha = nombreFicha;
        this.rutaFicha = new File(getClass().getResource(PropertiesReader.getProperty("Modelo"+nombreFicha)).getFile());
    }

    public int getIdFicha() {
        return idFicha;
    }

    public String getNombreFicha() {
        return nombreFicha;
    }

    public File getRutaFicha() {
        return rutaFicha;
    }

    public Spatial getSpatial() {
        return spatial;
    }

  @Override
  public String toString() {
    return "Ficha{" +
            "nombreFicha='" + nombreFicha + '\'' +
            ", rutaFicha=" + rutaFicha +
            '}';
  }
}
