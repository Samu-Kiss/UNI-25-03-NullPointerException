package pontiland.entities;

import java.util.Queue;

public class Partida {
    private Tablero tablero;
    private Queue<Jugador> jugadores;

    public Partida(Tablero tablero, Queue<Jugador> jugadores) {
        this.tablero = tablero;
        this.jugadores = jugadores;
    }

}
