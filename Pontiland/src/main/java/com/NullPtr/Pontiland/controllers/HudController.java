package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.entities.Jugador;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del HUD que actúa como capa intermedia entre las entidades del juego
 * (como Jugador) y la vista (Hud). Se encarga de exponer información ya procesada
 * para que el HUD solo tenga que mostrarla.
 */
public class HudController {

    public static class PlayerHudData {
        public final String nombre;
        public final String personaje;
        public final int dinero;
        public final boolean enCarcel;

        public PlayerHudData(String nombre, String personaje, int dinero, boolean enCarcel) {
            this.nombre = nombre;
            this.personaje = personaje;
            this.dinero = dinero;
            this.enCarcel = enCarcel;
        }
    }

    private final List<PlayerHudData> playersData = new ArrayList<>();

    // Constructor: traduce Jugadores y personajes a datos de HUD
    public HudController(List<Jugador> jugadores, List<Integer> personajeIds) {
        String[] nombresPersonajes = {
                "Kiwi", "Balon", "Maleta", "Pescadito", "Carnet", "Ignacito", "Nave"
        };

        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            int personajeId = (personajeIds != null && i < personajeIds.size()) ? personajeIds.get(i) : 1;
            String personaje = (personajeId > 0 && personajeId <= nombresPersonajes.length)
                    ? nombresPersonajes[personajeId - 1]
                    : "???";

            playersData.add(new PlayerHudData(
                    j.getNombreJugador(),
                    personaje,
                    j.getDinero(),
                    j.getEstado()
            ));
        }
    }

    public List<PlayerHudData> getPlayersData() {
        return playersData;
    }

    /**
     * Ejemplo de actualización de dinero o estado, usado para refrescar el HUD.
     */
    public void updatePlayerData(int index, int nuevoDinero, boolean enCarcel) {
        if (index < 0 || index >= playersData.size()) return;

        PlayerHudData old = playersData.get(index);
        PlayerHudData updated = new PlayerHudData(
                old.nombre, old.personaje, nuevoDinero, enCarcel
        );
        playersData.set(index, updated);
    }
}
