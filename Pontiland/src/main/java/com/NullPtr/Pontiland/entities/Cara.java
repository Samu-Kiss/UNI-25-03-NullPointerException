package com.NullPtr.Pontiland.entities;

import com.jme3.math.Vector3f;

public class Cara {
    private byte valor;
    private Vector3f normal;

    public Cara(byte valor, Vector3f normal) {
        this.valor = valor;
        this.normal = normal;
    }

    public byte getValor() {
        return valor;
    }

    public Vector3f getNormal() {
        return normal;
    }

}