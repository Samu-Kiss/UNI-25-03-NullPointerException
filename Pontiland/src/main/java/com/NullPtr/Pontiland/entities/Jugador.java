package com.NullPtr.Pontiland.entities;

import com.NullPtr.Pontiland.enums.Estado;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa a un jugador en el juego.
 * Contiene atributos como ID, nombre, icono, posicion, estado en la carcel, dinero y propiedades
 */
public class Jugador {
    private byte jugadorId = -1;
    private String nombreJugador = "";
    private byte posicion = 1;
    private Estado estado = Estado.MOVIMIENTO;
    private Double dinero = 0.0;
    private List<Propiedad> propiedades;

    /** Constructor de la clase Jugador por defecto, con posicion inicial 1 y sin propiedades
     * @param dinero Cantidad inicial de dinero del jugador
     * @param nombreJugador Nombre del jugador
     * @param jugadorId Identificador único del jugador
     * @throws IllegalArgumentException si el dinero es negativo, los IDs son negativos o el nombre está vacío
     */
    public Jugador(Double dinero, String nombreJugador, byte jugadorId) {
        if (dinero < 0) {
            throw new IllegalArgumentException("El dinero no puede ser negativo");
        }
        if (jugadorId > 4 || jugadorId < 1) {
            throw new IllegalArgumentException("Identificador del jugador es inválido");
        }
        if (nombreJugador == null || nombreJugador.isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }

        this.dinero = dinero;
        this.nombreJugador = nombreJugador;
        this.jugadorId = jugadorId;

    propiedades = new ArrayList<Propiedad>();
  }

    /** Constructor de la clase Jugador por defecto, con posicion variable y con propiedades
     * @param dinero Cantidad inicial de dinero del jugador
     * @param nombreJugador Nombre del jugador
     * @param jugadorId Identificador único del jugador
     * @param posicion Posicion del jugador en el tablero
     * @param propiedades Lista de propiedades que posee el jugador
     * @param estado Estado actual del jugador (en movimiento, en la carcel, en bancarrota)
     * @throws IllegalArgumentException si el dinero es negativo, los IDs son negativos,
     * el nombre está vacío, si la posicion es inválida
     */
    public Jugador(Double dinero, String nombreJugador, byte jugadorId, Estado estado,byte posicion, List<Propiedad> propiedades) {
        if (dinero < 0) {
            throw new IllegalArgumentException("El dinero no puede ser negativo");
        }
        if (nombreJugador == null || nombreJugador.isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }
        if (posicion < 1 || posicion > 40) {
            throw new IllegalArgumentException("Posicion invalida");
        }

        this.dinero = dinero;
        this.nombreJugador = nombreJugador;
        this.jugadorId = jugadorId;
        this.posicion = posicion;
        this.estado = estado;
        this.propiedades = propiedades;
    }
    if (iconoJugador == null || jugadorId < 0 || jugadorId > 4) {
      throw new IllegalArgumentException("El identificador o el icono del jugador es invalido");
    }
    if (nombreJugador == null || nombreJugador.isEmpty()) {
      throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
    }
    if (posicion < 1 || posicion > 40) {
      throw new IllegalArgumentException("Posicion invalida");
    }

    public byte getJugadorId() {
        return jugadorId;
    }
    public void setJugadorId(byte jugadorId) {
        if (jugadorId < 0 || jugadorId > 4) {
            throw new IllegalArgumentException("Identificador del jugador es inválido");
        }
        this.jugadorId = jugadorId;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }
    public void setNombreJugador(String nombreJugador) {
        if (nombreJugador == null || nombreJugador.isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }
        this.nombreJugador = nombreJugador;
    }

    public byte getPosicion() {
        return posicion;
    }
    public void setPosicion(byte posicion) {
        if (posicion < 1 || posicion > 40) {
            throw new IllegalArgumentException("Posicion invalida");
        }
        this.posicion = posicion;
    }

    public Estado getEstado() {
        return estado;
    }
    public void setEstado(Estado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado del jugador no puede ser nulo");
        }
        this.estado = estado;
    }

    /*
        * TODO: los getter y setter de dinero no son realmente necesarios
        *  Dinero podría ser publico
     */
    public Double getDinero() {
        return dinero;
    }
    public void setDinero(Double dinero) {
        this.dinero = dinero;
    }

    public List<Propiedad> getPropiedades() {
        return propiedades;
    }
    public void setPropiedades(List<Propiedad> propiedades) {
        if (propiedades == null) {
            throw new IllegalArgumentException("La lista de propiedades no puede ser nula");
        }
        this.propiedades = propiedades;
    }
}
