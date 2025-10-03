package com.NullPtr.Pontiland.entities;

/**
 * Clase que representa una casilla en el tablero de juego
 * Cada casilla tiene por lo menos una posición y un nombre
 */
public interface ICasilla {

     void setPosicionTablero(Byte posicionTablero);
     byte getPosicionTablero();
     void setNombreCasilla(String nombreCasilla);
     String getNombreCasilla();
     void setTipoCasilla(Tipo tipoCasilla);
     Tipo getTipoCasilla();

}
