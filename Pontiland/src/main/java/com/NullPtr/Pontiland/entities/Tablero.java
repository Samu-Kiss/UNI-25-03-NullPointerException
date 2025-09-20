package com.NullPtr.Pontiland.entities;

import java.util.List;

/**
* Representa el tablero del juego, compuesto por una lista de casillas.
* Cada casilla puede ser una propiedad, una estación, un evento, etc.
 */
public class Tablero {
    //WARNING: La lista a nivel de programación está con indexación 0, pero en el juego la casilla 1 es la primera.
    private List<Casilla> casillas = null;

    /** Constructor que inicializa el tablero con una lista de casillas.
     * @param casillas Lista de casillas que componen el tablero.
     */
    public Tablero(List<Casilla> casillas) {
        if (casillas == null || casillas.isEmpty()) {
            throw new IllegalArgumentException("El tablero debe tener al menos una casilla.");
        }
        this.casillas = casillas;
    }
}
