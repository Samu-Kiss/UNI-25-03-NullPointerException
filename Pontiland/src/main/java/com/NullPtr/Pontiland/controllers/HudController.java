package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.view.Hud;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del HUD (Head-Up Display) que actúa como puente entre el modelo de datos del juego
 * (entidades como Jugador) y la vista gráfica {@link com.NullPtr.Pontiland.view.Hud}.
 *
 * <p>Su función principal es recopilar información procesada y lista para mostrar,
 * evitando que la vista tenga que acceder directamente a las entidades del juego.
 */
public class HudController {

    /** Clase interna que representa los datos que se mostrarán en pantalla para cada jugador. */
    public static class PlayerHudData {
        public final String nombre;
        public final String personaje;
        public final int dinero;
        public boolean enCarcel;

        public PlayerHudData(String nombre, String personaje, int dinero, boolean enCarcel) {
            this.nombre = nombre;
            this.personaje = personaje;
            this.dinero = dinero;
            this.enCarcel = enCarcel;
        }
    }

    private final List<PlayerHudData> playersData = new ArrayList<>();
    private Hud hud;

    public void setHud(Hud hud) {
        this.hud = hud;
    }


    /** Constructor vacío usado cuando aún no se ha cargado una partida o lista de jugadores. */

    /**
     * Construye el controlador del HUD a partir de los datos de jugadores y sus personajes.
     *
     * @param jugadores Lista de jugadores activos en la partida
     * @param personajeIds Identificadores numéricos de los personajes seleccionados
     */
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

    /** Devuelve los datos de todos los jugadores que deben mostrarse en el HUD. */
    public List<PlayerHudData> getPlayersData() {
        return playersData;
    }

    /**
     * Actualiza la información de un jugador específico (por ejemplo, al cambiar su dinero o estado).
     *
     * @param index Índice del jugador en la lista
     * @param nuevoDinero Nuevo valor de dinero
     * @param enCarcel Nuevo estado de encarcelamiento
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

