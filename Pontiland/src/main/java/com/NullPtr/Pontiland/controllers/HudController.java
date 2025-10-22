package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HudController {

    private final IJugadorRepository jugadorRepository;
    private final IPartidaRepository partidaRepository;
    private final List<Jugador> jugadores = new ArrayList<>();

    public HudController(IJugadorRepository jugadorRepository, IPartidaRepository partidaRepository) throws SQLException {
        this.jugadorRepository = jugadorRepository;
        this.partidaRepository = partidaRepository;
        cargarJugadores();
    }

    private void cargarJugadores() throws SQLException {
        // Obtener el número real de jugadores de la partida
        int numJugadores = partidaRepository.getNumJugadores();

        for (int i = 1; i <= numJugadores; i++) {
            try {
                Jugador j = jugadorRepository.getJugadorByID(i);
                jugadores.add(j);
            } catch (SQLException ignored) {
                // Ignoramos si no existe ese jugador (por seguridad)
            }
        }
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public void actualizarJugador(int jugadorId, int nuevoDinero, boolean enCarcel) throws SQLException {
        for (Jugador j : jugadores) {
            if (j.getJugadorId() == jugadorId) {
                j.setDinero(nuevoDinero);
                j.setEstado(enCarcel);
                jugadorRepository.updateJugador(j);
                break;
            }
        }
    }
}



