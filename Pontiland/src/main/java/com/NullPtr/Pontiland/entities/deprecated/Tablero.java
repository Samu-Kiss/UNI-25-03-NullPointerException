package com.NullPtr.Pontiland.entities.deprecated;

import com.NullPtr.Pontiland.entities.ICasilla;

import java.io.File;
import java.util.List;

/**
* Representa el tablero del juego, compuesto por una lista de casillas.
* Cada casilla puede ser una propiedad, una estación, un evento, etc.
 */
@Deprecated
public class Tablero {
    //WARNING: La lista a nivel de programación está con indexación 0, pero en el juego la casilla 1 es la primera.
    private List<ICasilla> casillas = null;
    private List<List<ICasilla>> gruposPropiedades = null;
    private File modeloTablero = null;

    /** Constructor que inicializa el tablero con una lista de casillas.
     * @param casillas Lista de casillas que componen el tablero.
     */
    public Tablero(List<ICasilla> casillas, File modeloTablero) {
        if (casillas == null || casillas.isEmpty()) {
            throw new IllegalArgumentException("El tablero debe tener al menos una casilla.");
        }
        if (modeloTablero == null || !modeloTablero.exists()) {
            throw new IllegalArgumentException("El modelo del tablero no puede ser nulo y debe existir");
        }
        this.casillas = casillas;
        this.modeloTablero = modeloTablero;
    }

    public List<ICasilla> getCasillas() {
        return casillas;
    }
    public void setCasillas(List<ICasilla> casillas) {
        if (casillas == null || casillas.size() != 40) {
            throw new IllegalArgumentException("El tablero debe tener exactamente 40 casillas.");
        }
        this.casillas = casillas;
    }

    public File getModeloTablero() {
        return modeloTablero;
    }
    public void setModeloTablero(File modeloTablero) {
        if (modeloTablero == null || !modeloTablero.exists()) {
            throw new IllegalArgumentException("El modelo del tablero no puede ser nulo y debe existir");
        }
        this.modeloTablero = modeloTablero;
    }
}
