package com.NullPtr.Pontiland.entities;

import com.jme3.scene.Spatial;

import java.nio.file.Path;

public class Ficha {
    private Spatial spatial = null;
    private int idFicha = -1;
    private String nombreFicha = "";
    private Path rutaFicha = null;

    public Ficha(int idFicha, String nombreFicha, Path rutaFicha) {
        this.idFicha = idFicha;
        this.nombreFicha = nombreFicha;
        this.rutaFicha = rutaFicha;
    }

    public int getIdFicha() {
        return idFicha;
    }

    public String getNombreFicha() {
        return nombreFicha;
    }

    public Path getRutaFicha() {
        return rutaFicha;
    }
}
