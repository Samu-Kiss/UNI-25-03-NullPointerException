package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.view.Scene;
import java.sql.*;
import java.util.ArrayList;

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
   * @param scene escena principal usada para cargar los modelos al iniciar partida.
   */
  public StartGameService(
      IJugadorRepository jugadorRepository,
      IPartidaRepository partidaRepository,
      IDataService dataService,
      Scene scene) {
    this.jugadorRepository = jugadorRepository;
    this.partidaRepository = partidaRepository;
    this.dataService = dataService;
    this.scene = scene;
  }

  /** Constructs a new object. */
  public StartGameService() {}

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
    for (int i = 1; i <= jugadores.size(); i++) {
      jugadorRepository.newPlayer(jugadores.get(i - 1), iconos.get(i - 1));
    }
    int playerID = jugadorRepository.getPlayerIdByNumJugador(1);
    jugadorRepository.newActivePlayer(playerID);

    scene.loadFichasModels(jugadorRepository.getFichas(partidaRepository.getNumJugadores()));
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
          "Scene no inyectada en StartGameService. "
              + "Inyéctala en el constructor o llama startGameService.setScene(scene) antes de continuar.");
    }
  }
}
