package com.NullPtr.Pontiland.services;
import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Jugador;

import static com.NullPtr.Pontiland.entities.Tipo.*;

public class CasillaService implements ICasillaService{

    private boolean irACarcel = false;

    @Override
    public void interaccion(Jugador jugador, Casilla casilla) {
        System.out.println("\n" + jugador.getNombreJugador() + " ha caido en la casilla " + casilla.getTipoCasilla());
        switch (casilla.getTipoCasilla()) {
            case PARADALIBRE:
                onParadaLibre(jugador, casilla);
                break;
            case EVENTO:
                onEvento(jugador, casilla);
                break;
            case PROPIEDAD:
                onPropiedad(jugador, casilla);
                break;
            case MOVIMIENTO:
                onMovimiento(jugador, casilla);
                break;
            case IRALACARCEL:
                onCarcel();
                break;
        }
    }


    private void onParadaLibre(Jugador j, Casilla c) {
    }

    private void onEvento(Jugador j, Casilla c) {
    }

    private void onPropiedad(Jugador j, Casilla c) {
    }

    private void onMovimiento(Jugador j, Casilla c) {
    }

    private void onCarcel() {
        irACarcel = true;
    }

    @Override
    public boolean getIrACarcel() {
        return irACarcel;
    }
}
