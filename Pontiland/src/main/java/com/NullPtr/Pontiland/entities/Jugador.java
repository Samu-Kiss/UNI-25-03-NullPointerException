package com.NullPtr.Pontiland.entities;

import java.util.ArrayList;
import java.util.List;

enum Estado{
    MOVIMIENTO, CARCEL, BANCARROTA
}

/**
    * Clase que representa a un jugador en el juego.
    * Contiene atributos como ID, nombre, icono, posicion, estado en la carcel, dinero y propiedades
 */
public class Jugador {
    private byte jugadorId = -1;
    private String nombreJugador = "";
    private Icono iconoJugador = null;
    private byte posicion = 1;
    private Estado estado = Estado.MOVIMIENTO;
    private Double dinero = 0.0;
    private List<Propiedad> propiedades;

    /** Constructor de la clase Jugador por defecto, con posicion inicial 1 y sin propiedades
     * @param dinero Cantidad inicial de dinero del jugador
     * @param iconoJugador Identificador del icono del jugador
     * @param nombreJugador Nombre del jugador
     * @param jugadorId Identificador único del jugador
     * @throws IllegalArgumentException si el dinero es negativo, los IDs son negativos o el nombre está vacío
     */
    public Jugador(Double dinero, Icono iconoJugador, String nombreJugador, byte jugadorId) {
        if (dinero < 0) {
            throw new IllegalArgumentException("El dinero no puede ser negativo");
        }
        if (jugadorId > 4 || jugadorId < 0) {
            throw new IllegalArgumentException("Identificador del jugador es inválido");
        }
        if (iconoJugador == null) {
            throw new IllegalArgumentException("El icono del jugador no puede ser nulo");
        }
        if (nombreJugador == null || nombreJugador.isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }

        this.dinero = dinero;
        this.iconoJugador = iconoJugador;
        this.nombreJugador = nombreJugador;
        this.jugadorId = jugadorId;

        propiedades = new ArrayList<Propiedad>();
    }

    /** Constructor de la clase Jugador por defecto, con posicion variable y con propiedades
     * @param dinero Cantidad inicial de dinero del jugador
     * @param iconoJugador Identificador del icono del jugador
     * @param nombreJugador Nombre del jugador
     * @param jugadorId Identificador único del jugador
     * @param posicion Posicion del jugador en el tablero
     * @param propiedades Lista de propiedades que posee el jugador
     * @param estado Estado actual del jugador (en movimiento, en la carcel, en bancarrota)
     * @throws IllegalArgumentException si el dinero es negativo, los IDs son negativos,
     * el nombre está vacío, si la posicion es inválida
     */
    public Jugador(Double dinero, Icono iconoJugador, String nombreJugador, byte jugadorId, Estado estado,byte posicion, List<Propiedad> propiedades) {
        if (dinero < 0) {
            throw new IllegalArgumentException("El dinero no puede ser negativo");
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

        this.dinero = dinero;
        this.iconoJugador = iconoJugador;
        this.nombreJugador = nombreJugador;
        this.jugadorId = jugadorId;
        this.posicion = posicion;
        this.estado = estado;
        this.propiedades = propiedades;
    }


}
