package com.NullPtr.Pontiland.entities;

import com.NullPtr.Pontiland.enums.Tipo;
import java.util.Queue;

/**
 * Clase que representa una casilla de evento en el juego
 * Contiene una lista de tarjetas de evento disponibles de la cual
 * se sacarán las tarjetas de manera aleatoria
 */

/*
 * REVIEW: Se usa la misma clase para el uso de otras casillas como:
 *   - Carcel
 *   - Parking Gratis
 *   - Ir a la carcel
 *   - Salida
 *   - Movimiento/Estacion
 */
public class Evento extends Casilla{
    private Tipo tipoCasilla = null;
    private Queue<TarjetaEvento> tarjetasDisponibles;

    /**
     * Constructor de la clase Evento
     * @param posicionTablero Posición de la casilla en el tablero
     * @param nombreCasilla Nombre de la casilla
     * @param tipoCasilla Tipo de la casilla
     */
    public Evento(byte posicionTablero, String nombreCasilla, Tipo tipoCasilla) {
        super(posicionTablero, nombreCasilla);
        this.tipoCasilla = tipoCasilla;
    }

    public Tipo getTipoCasilla() {
        return tipoCasilla;
    }
    public void setTipoCasilla(Tipo tipoCasilla) {
        this.tipoCasilla = tipoCasilla;
    }

    public Queue<TarjetaEvento> getTarjetasDisponibles() {
        // Asegurarse de que la casilla es de tipo Evento antes de devolver las tarjetas
        if (this.tipoCasilla != Tipo.Evento)
            throw new IllegalStateException("La casilla no es de tipo Evento, no tiene tarjetas disponibles");
        return tarjetasDisponibles;
    }
    public void setTarjetasDisponibles(Queue<TarjetaEvento> tarjetasDisponibles) {
        if (this.tipoCasilla != Tipo.Evento)
            throw new IllegalStateException("La casilla no es de tipo Evento, no puede tener tarjetas disponibles");
        this.tarjetasDisponibles = tarjetasDisponibles;
    }
}
