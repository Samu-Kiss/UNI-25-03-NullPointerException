package com.NullPtr.Pontiland.services;
import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Jugador;

import static com.NullPtr.Pontiland.entities.Tipo.*;

public class CasillaService implements ICasillaService{

    @Override
    public void interaccion(Jugador jugador, Casilla casilla) {
        switch (casilla.getTipoCasilla()) {
            case SALIDA:
                onSalida(jugador, casilla);
                break;
            case CARCEL:
                onCarcel(jugador, casilla);
                break;
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
                onIrALaCarcel(jugador, casilla);
                break;
        }
    }

    private void onSalida(Jugador j, Casilla c) {
    }

    private void onCarcel(Jugador j, Casilla c) {
    }

    private void onParadaLibre(Jugador j, Casilla c) {
    }

    private void onEvento(Jugador j, Casilla c) {
    }

    private void onPropiedad(Jugador j, Casilla c) {
    }

    private void onMovimiento(Jugador j, Casilla c) {
    }

    private void onIrALaCarcel(Jugador j, Casilla c) {
    }
}
