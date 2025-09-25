package com.NullPtr.Pontiland.entities;

import java.io.File;

/**
 * Clase que representa un icono en el juego
 * Un icono tiene un id y un nombre
 */
public class Icono {
    private String nombreIcono = null;
    File modeloIcono = null;
    //Warning: Posible uso futuro
    //private String spriteIcono = null;

    /**
     * Constructor de la clase Icono
     * @param nombreIcono
     */
    public Icono(String nombreIcono, File modeloIcono) {
        if (nombreIcono == null || nombreIcono.isEmpty()) {
            throw new IllegalArgumentException("El nombre del icono no puede ser nulo o vacío");
        }
        if (modeloIcono == null || !modeloIcono.exists()) {
            throw new IllegalArgumentException("El modelo del icono no puede ser nulo y debe existir");
        }

        this.nombreIcono = nombreIcono;
        this.modeloIcono = modeloIcono;
    }

    public String getNombreIcono() {
        return nombreIcono;
    }

    public void setNombreIcono(String nombreIcono) {
        if (nombreIcono == null || nombreIcono.isEmpty()) {
            throw new IllegalArgumentException("El nombre del icono no puede ser nulo o vacío");
        }
        this.nombreIcono = nombreIcono;
    }

    public File getModeloIcono() {
        return modeloIcono;
    }

    public void setModeloIcono(File modeloIcono) {
        if (modeloIcono == null || !modeloIcono.exists()) {
            throw new IllegalArgumentException("El modelo del icono no puede ser nulo y debe existir");
        }
        this.modeloIcono = modeloIcono;
    }
}
