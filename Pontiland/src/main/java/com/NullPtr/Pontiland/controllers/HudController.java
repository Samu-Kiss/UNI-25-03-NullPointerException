package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.view.HUD.HudCompra;
import com.NullPtr.Pontiland.view.HUD.HudPropiedades;
import com.NullPtr.Pontiland.view.HUD.HudSubasta;

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
        int numJugadores = partidaRepository.getNumJugadores();
        for (int i = 1; i <= numJugadores; i++) {
            try {
                Jugador j = jugadorRepository.getJugadorByID(i);
                jugadores.add(j);
            } catch (SQLException ignored) {}
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

    // coordinador visual entre los HUDs
    public void registrarListeners(HudCompra hudCompra, HudSubasta hudSubasta, HudPropiedades hudPropiedades) {

        // === Listener de compra ===
        hudCompra.setListener(new HudCompra.HudCompraListener() {
            @Override
            public void onSubastaSolicitada() {
                hudCompra.ocultar();
                hudSubasta.iniciarSubasta(getJugadores(), hudCompra.getCurrentCasilla());
            }

            @Override
            public void onComprarPropiedad(int numCasilla) {
                hudPropiedades.agregarPropiedadJugadorActivo(numCasilla);
            }

            @Override
            public void onFinalizarTurno() {
                System.out.println("Turno finalizado");
            }
        });

        // === Listener de subasta ===
        hudSubasta.setListener(new HudSubasta.HudSubastaListener() {
            @Override
            public void onJugadorPuja(Jugador jugador, int nuevaOferta) {
                System.out.println(" " + jugador.getNombreJugador() + " puja: $" + nuevaOferta);
            }

            @Override
            public void onJugadorSeRetira(Jugador jugador) {
                System.out.println(" " + jugador.getNombreJugador() + " se retira.");
            }

            @Override
            public void onSubastaTerminada(Jugador ganador, int ofertaFinal) {
                System.out.println(" " + ganador.getNombreJugador() + " gana con $" + ofertaFinal);
                hudPropiedades.highlightActivePlayer(ganador.getJugadorId() - 1);
                hudPropiedades.agregarPropiedadAJugador(ganador.getJugadorId(), hudSubasta.getPropiedadSubastada());
            }
        });
    }
}




