package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.view.MenuCarga;
import com.NullPtr.Pontiland.view.MenuCreditos;
import com.NullPtr.Pontiland.view.MenuJugadores;
import com.NullPtr.Pontiland.view.MenuPrincipal;
import com.jme3.app.state.AppStateManager;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador central para manejar la navegación de menús y el inicio del juego.
 *
 * <p>Mantiene y adjunta/desadjunta los AppStates de UI según corresponda y delega al
 * Launcher para la inicialización de la escena 3D.
 */
public class MenuController  implements IMenuActions{

  private final Launcher app;

  private MenuPrincipal menuPrincipal;
  private MenuJugadores menuJugadores;
  private MenuCarga menuCarga;
  private MenuCreditos menuCreditos;

  private boolean gameStarted = false;
  private int selectedPlayerCount = 0;

  public MenuController(Launcher app) {
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

    menuJugadores = new MenuJugadores(this);
    if (!stateManager().hasState(menuJugadores)) {
      stateManager().attach(menuJugadores);
    }
  }

  /** Inicia el juego principal para el número de jugadores indicado. */
  @Override
  public void startMainGame(int playerCount) {
    this.selectedPlayerCount = playerCount;
    this.gameStarted = true;

    // Desconectar cualquier menú de UI
    detachIfAttached(menuPrincipal);
    detachIfAttached(menuJugadores);
    detachIfAttached(menuCarga);
    detachIfAttached(menuCreditos);

    // Delegar a la app la preparación de la escena 3D
    app.initializeGame3D();

    System.out.println("Juego iniciado con " + playerCount + " jugadores");
  }

  /** Abre el menú de carga con datos de demo. */
  @Override
  public void loadSavedGame() {
    detachIfAttached(menuPrincipal);

    List<MenuCarga.SavedGame> saves =
        Arrays.asList(
            new MenuCarga.SavedGame("save-001", "Partida #1 - 2025-10-03 18:30"),
            new MenuCarga.SavedGame("save-002", "Partida #2 - 2025-10-02 11:05"),
            new MenuCarga.SavedGame("save-003", "Partida #3 - 2025-09-28 21:10"));

    showLoadMenu(
        saves,
        (String id) -> {
          System.out.println("[Pontiland] Seleccionado guardado: " + id);
          // TODO: lógica real de carga desde persistencia
          showStartScreen();
        });
  }

  /** Muestra el menú de carga con una lista y callback de selección. */
  public void showLoadMenu(List<MenuCarga.SavedGame> saves, Consumer<String> onSelect) {
    detachIfAttached(menuCarga);
    menuCarga = new MenuCarga(saves, onSelect);
    if (!stateManager().hasState(menuCarga)) {
      stateManager().attach(menuCarga);
    }
  }

  /** Abre los créditos, usando owner/repo si están disponibles como variables de entorno. */
  public void showCredits() {
    String owner = System.getenv("GITHUB_REPO_OWNER");
    String repo = System.getenv("GITHUB_REPO_NAME");
    if (owner != null && !owner.isBlank() && repo != null && !repo.isBlank()) {
      showCredits(owner.trim(), repo.trim(), 10);
      return;
    }

    detachIfAttached(menuPrincipal);
    detachIfAttached(menuCreditos);

    menuCreditos = new MenuCreditos(this::showStartScreen);
    if (!stateManager().hasState(menuCreditos)) {
      stateManager().attach(menuCreditos);
    }
  }

    @Override
    public void showCredits(String owner, String repo, int limit) {
    detachIfAttached(menuPrincipal);
    detachIfAttached(menuCreditos);

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
