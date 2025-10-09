package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.SavedGame;
import com.NullPtr.Pontiland.services.IDataService;
import com.NullPtr.Pontiland.services.IStartGameService;
import com.NullPtr.Pontiland.view.MenuCarga;
import com.NullPtr.Pontiland.view.MenuCreditos;
import com.NullPtr.Pontiland.view.MenuJugadores;
import com.NullPtr.Pontiland.view.MenuPrincipal;
import com.NullPtr.Pontiland.view.MenuSeleccion;
import com.jme3.app.state.AppStateManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador central para manejar la navegación de menús y el inicio del juego.
 *
 * <p>Mantiene y adjunta/desadjunta los AppStates de UI según corresponda y delega al Launcher para
 * la inicialización de la escena 3D.
 */
public class MenuController implements IMenuActions {

  private final Launcher app;

  private MenuPrincipal menuPrincipal;
  private MenuJugadores menuJugadores;
  private MenuCarga menuCarga;
  private MenuCreditos menuCreditos;
  private MenuSeleccion menuSeleccion; // nueva pantalla de selección de personajes

  private boolean gameStarted = false;
  private int selectedPlayerCount = 0;

  private final IStartGameService startGameService;
  private final IDataService dataService;

  public MenuController(
      Launcher app, IStartGameService startGameService, IDataService dataService) {
    this.dataService = dataService;
    if (app == null) {
      throw new IllegalArgumentException("app no puede ser null");
    }
    if (startGameService == null) {
      throw new IllegalArgumentException("startGameService no puede ser null");
    }
    this.startGameService = startGameService;
    this.app = app;
  }

  private AppStateManager stateManager() {
    return app.getStateManager();
  }

  // ============ Entradas desde Launcher o la UI ============

  /** Muestra la pantalla de inicio. */
  public void showStartScreen() {
    // Cerrar cualquier otro menú activo
    detachIfAttached(menuJugadores);
    detachIfAttached(menuCarga);
    detachIfAttached(menuCreditos);
    detachIfAttached(menuSeleccion);

    if (menuPrincipal == null) {
      menuPrincipal = new MenuPrincipal(this);
    }

    if (!stateManager().hasState(menuPrincipal)) {
      stateManager().attach(menuPrincipal);
    }
  }

  /** Abre el menú de selección de jugadores. */
  @Override
  public void startPlayerSelection() {
    detachIfAttached(menuPrincipal);
    detachIfAttached(menuCarga);
    detachIfAttached(menuCreditos);
    detachIfAttached(menuSeleccion);

    menuJugadores = new MenuJugadores(this);
    if (!stateManager().hasState(menuJugadores)) {
      stateManager().attach(menuJugadores);
    }
  }

  /**
   * Paso 1: recibido el número de jugadores desde MenuJugadores. Ahora se muestra la pantalla de
   * selección de personajes (MenuSeleccion). El juego real aún no inicia hasta recibir los datos
   * completos.
   */
  @Override
  public void startMainGame(int playerCount) {
    this.selectedPlayerCount = playerCount;
    this.gameStarted = false; // todavía no inicia

    // Cerrar menús previos
    detachIfAttached(menuPrincipal);
    detachIfAttached(menuJugadores);
    detachIfAttached(menuCarga);
    detachIfAttached(menuCreditos);
    detachIfAttached(menuSeleccion);

    menuSeleccion = new MenuSeleccion(this, playerCount);
    if (!stateManager().hasState(menuSeleccion)) {
      stateManager().attach(menuSeleccion);
    }
  }

  /**
   * Paso 2: Se reciben los datos completos de jugadores y personajes. Inicia la escena 3D y se
   * cierra la UI.
   */
  @Override
  public void startMainGame(
      int playerCount, ArrayList<Jugador> jugadores, ArrayList<Integer> personajeIds)
      throws SQLException {
    this.selectedPlayerCount = playerCount;
    this.gameStarted = true;

    // Aquí podría persistirse/prepararse la información (jugadores/personajes) si se requiere.
    // Por ahora solo se imprime para debug.
    System.out.println("[Pontiland] Jugadores seleccionados: " + jugadores.size());
    for (int i = 0; i < jugadores.size(); i++) {
      Jugador j = jugadores.get(i);
      Integer pid = personajeIds.size() > i ? personajeIds.get(i) : -1;
      System.out.println(
          "  - J" + j.getJugadorId() + " nombre='" + j.getNombreJugador() + "' personajeId=" + pid);
    }

    startGameService.creatingNewGame(jugadores, personajeIds);

    // Desconectar cualquier menú de UI
    detachIfAttached(menuPrincipal);
    detachIfAttached(menuJugadores);
    detachIfAttached(menuCarga);
    detachIfAttached(menuCreditos);
    detachIfAttached(menuSeleccion);

    // Delegar a la app la preparación de la escena 3D
    app.initializeGame3D();

    System.out.println("Juego iniciado con " + playerCount + " jugadores");
  }

  /** Abre el menú de carga con datos de demo. */
  @Override
  public void loadSavedGame() {
    detachIfAttached(menuPrincipal);
    detachIfAttached(menuSeleccion);

    List<SavedGame> saves = dataService.listarPartidasPasadas();

    showLoadMenu(
        saves,
        (String id) -> {
          System.out.println("[Pontiland] Seleccionado guardado: " + id);
          dataService.loadDataBase(id);

          // Desconectar cualquier menú de UI
          detachIfAttached(menuPrincipal);
          detachIfAttached(menuJugadores);
          detachIfAttached(menuCarga);
          detachIfAttached(menuCreditos);
          detachIfAttached(menuSeleccion);

          app.initializeGame3D();
        });
  }

  /** Muestra el menú de carga con una lista y callback de selección. */
  public void showLoadMenu(List<SavedGame> saves, java.util.function.Consumer<String> onSelect) {
    detachIfAttached(menuCarga);
    menuCarga = new MenuCarga(this,saves, onSelect);
    if (!stateManager().hasState(menuCarga)) {
      stateManager().attach(menuCarga);
    }
  }

  @Override
  public void showCredits(String owner, String repo, int limit) {
    detachIfAttached(menuPrincipal);
    detachIfAttached(menuCreditos);
    detachIfAttached(menuSeleccion);

    menuCreditos = new MenuCreditos(this::showStartScreen, owner, repo, limit);
    if (!stateManager().hasState(menuCreditos)) {
      stateManager().attach(menuCreditos);
    }
  }

  /** Vuelve al menú principal desde cualquier menú. */
  public void goToMainMenu() {
    detachIfAttached(menuJugadores);
    detachIfAttached(menuCarga);
    detachIfAttached(menuCreditos);
    detachIfAttached(menuPrincipal);
    detachIfAttached(menuSeleccion);

    showStartScreen();
  }

  // ============ Consultas de estado ============
  public int getSelectedPlayerCount() {
    return selectedPlayerCount;
  }

  public boolean isGameStarted() {
    return gameStarted;
  }

  // ============ Utilidades internas ============
  private void detachIfAttached(com.jme3.app.state.AppState state) {
    if (state != null && stateManager().hasState(state)) {
      stateManager().detach(state);
    }
  }
}
