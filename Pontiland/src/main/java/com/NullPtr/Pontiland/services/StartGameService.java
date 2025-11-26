package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.SavedGame;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import com.NullPtr.Pontiland.view.IScene;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

  private IPropiedadRepository propiedadRepository;

  private IScene scene;

  private static Logger logger = LogManager.getLogger(StartGameService.class);

  /**
   * Constructor que permite la inyección de dependencias.
   *
   * @param jugadorRepository Repositorio de jugadores.
   * @param partidaRepository Repositorio de partidas.
   * @param dataService Servicio de datos.
   * @param scene escena principal usada para cargar los modelos al iniciar partida.
   * @param propiedadRepository Repositorio de propiedades.
   */
  public StartGameService(
      IJugadorRepository jugadorRepository,
      IPartidaRepository partidaRepository,
      IDataService dataService,
      IScene scene,
      IPropiedadRepository propiedadRepository) {
    this.propiedadRepository = propiedadRepository;
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
    propiedadRepository.setPartidaID(partidaID);
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
   * @param partidaSeleccionada Nombre o ruta del archivo de la partida a cargar.
   */
  @Override
  public void loadingOldGame(String partidaSeleccionada) throws SQLException {
    Long partidaID = Long.parseLong(partidaSeleccionada);
    partidaRepository.setPartidaID(partidaID);
    jugadorRepository.setPartidaID(partidaID);
    propiedadRepository.setPartidaID(partidaID);

    // scene.loadFichasModels(jugadorRepository.getFichas(partidaRepository.getNumJugadores()));
  }

  /**
   * Devuelve la lista de partidas guardadas disponibles.
   *
   * <p>Este método delega en {@code partidaRepository.getAllPartidaIDs()} para obtener el historial
   * de partidas guardadas (objetos {@link SavedGame}).
   *
   * <p>Contratos y comportamientos: - Retorna una lista de {@link SavedGame} que representa las
   * partidas guardadas. - Si no hay partidas, lo habitual es que se devuelva una lista vacía (según
   * la implementación concreta del repositorio). - Si {@code partidaRepository} no ha sido
   * inyectado, este método lanzará una {@link NullPointerException} al intentar delegar la llamada.
   *
   * @return lista de partidas guardadas ({@link SavedGame}), potencialmente vacía.
   */
  @Override
  public List<SavedGame> listPastGames() {
    try {
      return partidaRepository.getAllPartidaIDs();
    } catch (SQLException e) {
      logger.error("Error buscando partidas pasadas", e);
      return new ArrayList<>();
    }
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
