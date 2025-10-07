package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;

import java.util.ArrayList;

/**
 * Servicio encargado de iniciar y cargar partidas en el sistema Pontiland.
 * Permite la creación de nuevas partidas y la carga de partidas existentes,
 * gestionando la interacción con los repositorios de jugadores y partidas,
 * así como con el servicio de datos.
 */
public class StartGameService implements IStartGameService {
    /**
     * Repositorio para la gestión de jugadores.
     */
    IJugadorRepository jugadorRepository;
    /**
     * Repositorio para la gestión de partidas.
     */
    IPartidaRepository partidaRepository;
    /**
     * Servicio para la gestión de la base de datos y persistencia.
     */
    IDataService dataService;

    /**
     * Constructor por defecto.
     */
    public StartGameService() {}

    /**
     * Constructor que permite la inyección de dependencias.
     *
     * @param jugadorRepository Repositorio de jugadores.
     * @param partidaRepository Repositorio de partidas.
     * @param dataService Servicio de datos.
     */
    public StartGameService(IJugadorRepository jugadorRepository, IPartidaRepository partidaRepository, IDataService dataService) {
        this.jugadorRepository = jugadorRepository;
        this.partidaRepository = partidaRepository;
        this.dataService = dataService;
    }

    /**
     * Crea una nueva partida en la base de datos, registrando los jugadores y sus iconos.
     *
     * @param jugadores Lista de jugadores que participarán en la partida.
     * @param iconos Lista de identificadores de iconos asociados a cada jugador.
     */
    public void creatingNewGame(ArrayList<Jugador> jugadores, ArrayList<Integer> iconos) {
        dataService.newDataBase();
        long partidaID = partidaRepository.newPartida(jugadores.size());
        for(int i=1; i<=jugadores.size(); i++) {
            jugadorRepository.newPlayer(jugadores.get(i-1),partidaID, iconos.get(i-1));
        }
    }

    /**
     * Carga una partida existente desde un archivo seleccionado.
     *
     * @param archivoSeleccionado Nombre o ruta del archivo de la partida a cargar.
     */
    public void loadingOldGame(String archivoSeleccionado){
        dataService.loadDataBase(archivoSeleccionado);
    }
}