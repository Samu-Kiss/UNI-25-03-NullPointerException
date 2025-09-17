package pontiland.entities;

import java.util.ArrayList;
import java.util.List;

/**
    * Clase que representa a un jugador en el juego.
    * Contiene atributos como ID, nombre, icono, posicion, estado en la carcel, dinero y propiedades
 */
public class Jugador {
    private byte jugadorId;
    private String nombreJugador = "";
    private byte iconoId = 0;
    private byte posicion = 1;
    private boolean enCarcel = false;
    private Double dinero = 0.0;
    private List<Propiedad> propiedades;

    /** Constructor de la clase Jugador por defecto, con posicion inicial 1 y sin propiedades
     * @param dinero Cantidad inicial de dinero del jugador
     * @param iconoId Identificador del icono del jugador
     * @param nombreJugador Nombre del jugador
     * @param jugadorId Identificador único del jugador
     * @throws IllegalArgumentException si el dinero es negativo, los IDs son negativos o el nombre está vacío
     */
    public Jugador(Double dinero, byte iconoId, String nombreJugador, byte jugadorId) {
        if (dinero < 0) {
            throw new IllegalArgumentException("El dinero no puede ser negativo");
        }
        if (iconoId < 0 || jugadorId < 0) {
            throw new IllegalArgumentException("Identificadores no pueden ser negativos");
        }
        if (nombreJugador == null || nombreJugador.isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }

        this.dinero = dinero;
        this.iconoId = iconoId;
        this.nombreJugador = nombreJugador;
        this.jugadorId = jugadorId;

        propiedades = new ArrayList<Propiedad>();
    }

    /** Constructor de la clase Jugador por defecto, con posicion variable y con propiedades
     * @param dinero Cantidad inicial de dinero del jugador
     * @param iconoId Identificador del icono del jugador
     * @param nombreJugador Nombre del jugador
     * @param jugadorId Identificador único del jugador
     * @param posicion Posicion del jugador en el tablero
     * @param propiedades Lista de propiedades que posee el jugador
     * @param enCarcel Estado del jugador (si está en la carcel o no)
     * @throws IllegalArgumentException si el dinero es negativo, los IDs son negativos,
     * el nombre está vacío, si la posicion es inválida
     */
    public Jugador(Double dinero, byte iconoId, String nombreJugador, byte jugadorId, byte posicion, boolean enCarcel, List<Propiedad> propiedades) {
        if (dinero < 0) {
            throw new IllegalArgumentException("El dinero no puede ser negativo");
        }
        if (iconoId < 0 || jugadorId < 0) {
            throw new IllegalArgumentException("Identificadores no pueden ser negativos");
        }
        if (nombreJugador == null || nombreJugador.isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }
        if (posicion < 1 || posicion > 40) {
            throw new IllegalArgumentException("Posicion invalida");
        }

        this.dinero = dinero;
        this.iconoId = iconoId;
        this.nombreJugador = nombreJugador;
        this.jugadorId = jugadorId;
        this.posicion = posicion;
        this.enCarcel = enCarcel;
        this.propiedades = propiedades;
    }


}
