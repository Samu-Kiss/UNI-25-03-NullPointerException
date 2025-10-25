package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.view.Scene;

import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Servicio encargado de iniciar y cargar partidas en el sistema Pontiland. Permite la creación de
 * nuevas partidas y la carga de partidas existentes, gestionando la interacción con los
 * repositorios de jugadores y partidas, así como con el servicio de datos.
 */
public class StartGameService implements IStartGameService {
  /** Repositorio para la gestión de jugadores. */
  private IJugadorRepository jugadorRepository;

  /** Repositorio para la gestión de partidas. */
  private IPartidaRepository partidaRepository;

  /** Servicio para la gestión de la base de datos y persistencia. */
  private IDataService dataService;

  private Scene scene;


    /**
   * Constructor que permite la inyección de dependencias.
   *
   * @param jugadorRepository Repositorio de jugadores.
   * @param partidaRepository Repositorio de partidas.
   * @param dataService Servicio de datos.
   */
  public StartGameService( IJugadorRepository jugadorRepository, IPartidaRepository partidaRepository, IDataService dataService, Scene scene) {
    this.jugadorRepository = jugadorRepository;
    this.partidaRepository = partidaRepository;
    this.dataService = dataService;
    this.scene = scene;
  }

    /**
     * Constructs a new object.
     */
    public StartGameService() {
    }

    /**
   * Crea una nueva partida en la base de datos, registrando los jugadores y sus iconos.
   *
   * @param jugadores Lista de jugadores que participarán en la partida.
   * @param iconos Lista de identificadores de iconos asociados a cada jugador.
   */
    @Override
  public void creatingNewGame(ArrayList<Jugador> jugadores, ArrayList<Integer> iconos)
      throws SQLException {
    dataService.newDataBase();
    long partidaID = partidaRepository.newPartida(jugadores.size());
    jugadorRepository.setPartidaID(partidaID);
      for (int i = 0; i < jugadores.size(); i++) {
        Jugador j = jugadores.get(i);
        // Inicialización lógica TEMPORAL (sin tocar el repositorio)
        //TODO: conectar logica de back para inicializar el HUD
        j.setJugadorId((byte) (i + 1));
        j.setPosicion(1);
        j.setEstado(false);
        j.setDinero(1500);
        jugadorRepository.newPlayer(j, iconos.get(i));
      }
    int playerID = jugadorRepository.getPlayerIdByNumJugador(1);
    jugadorRepository.newActivePlayer(playerID);

    scene.loadFichasModels(jugadorRepository.getFichas(partidaRepository.getNumJugadores()));

  }

  public IJugadorRepository getJugadorRepository() {
    return jugadorRepository;
  }

  public IPartidaRepository getPartidaRepository() {
    return partidaRepository;
  }



  /**
   * Carga una partida existente desde un archivo seleccionado.
   *
   * @param archivoSeleccionado Nombre o ruta del archivo de la partida a cargar.
   */
  @Override
  public void loadingOldGame(String archivoSeleccionado) {
    dataService.loadDataBase(archivoSeleccionado);
  }
    @Override
    public void ensureSceneReady() {
        if (this.scene == null) {
            throw new IllegalStateException(
                    "Scene no inyectada en StartGameService. " +
                            "Inyéctala en el constructor o llama startGameService.setScene(scene) antes de continuar."
            );
        }
    }
}
