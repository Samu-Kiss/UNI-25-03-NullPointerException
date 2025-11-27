package com.NullPtr.Pontiland.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.SavedGame;
import com.NullPtr.Pontiland.services.IDataService;
import com.NullPtr.Pontiland.services.IStartGameService;
import com.NullPtr.Pontiland.services.ITurnService;
import com.NullPtr.Pontiland.view.*;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para la clase {@link MenuController}.
 *
 * <p>Estas pruebas verifican que {@code MenuController} gestione correctamente la navegación entre
 * los menús del juego, la carga de partidas guardadas y el inicio del juego principal.
 *
 * <p>Se utiliza Mockito para simular las dependencias externas ({@link Launcher}, {@link
 * AppStateManager}, {@link IStartGameService}, {@link IDataService}) y verificar las interacciones
 * con los {@link AppState}.
 */
class MenuControllerTest {

  private Launcher app;
  private AppStateManager stateManager;
  private IStartGameService startGameService;
  private IDataService dataService;
  private MenuController controller;
  private IHUDcontroller hudController;
  private ITurnService turnService;

  /**
   * Antes de cada prueba, se crean los mocks y se inicializa el controlador. Esto garantiza que
   * cada test comience con un estado limpio y controlado.
   */
  @BeforeEach
  void setUp() {
    app = mock(Launcher.class);
    stateManager = mock(AppStateManager.class);
    startGameService = mock(IStartGameService.class);
    dataService = mock(IDataService.class);
    hudController = mock(IHUDcontroller.class);
    turnService = mock(ITurnService.class);

    when(app.getStateManager()).thenReturn(stateManager);

    controller = new MenuController(app, startGameService, dataService, hudController, turnService);
  }

  // --- Pruebas de constructor ---

  /**
   * Verifica que el constructor lance una excepción si el Launcher (app) es null. Esto garantiza
   * que la dependencia crítica no pueda ser nula.
   */
  @Test
  void testConstructorThrowsIfAppIsNull() {
    IHUDcontroller hudController = mock(IHUDcontroller.class);
    ITurnService turnService = mock(ITurnService.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> new MenuController(null, startGameService, dataService, hudController, turnService));
  }

  // --- Pruebas de mostrar pantalla principal ---

  /**
   * Verifica que showStartScreen() adjunte el MenuPrincipal al AppStateManager si no está ya
   * adjunto.
   */
  @Test
  void testShowStartScreenAttachesMenuPrincipal() {
    when(stateManager.hasState(any(AppState.class))).thenReturn(false);

    controller.showStartScreen();

    verify(stateManager).attach(any(MenuPrincipal.class));
  }

  /** Verifica que showStartScreen() no re-adjunte el MenuPrincipal si ya está adjunto. */
  @Test
  void testShowStartScreenDoesNotReattachIfAlreadyAttached() {
    when(stateManager.hasState(any(AppState.class))).thenReturn(true);

    controller.showStartScreen();

    verify(stateManager, never()).attach(any(MenuPrincipal.class));
  }

  /**
   * Asegura que showStartScreen() solo adjunte MenuPrincipal una vez, incluso si se llama
   * repetidamente.
   */
  @Test
  void testShowStartScreenAttachesMenuPrincipalOnlyOnce() {
    when(stateManager.hasState(any(MenuPrincipal.class))).thenReturn(false);

    // Primera llamada: adjunta
    controller.showStartScreen();
    verify(stateManager, times(1)).attach(any(MenuPrincipal.class));

    // Segunda llamada: simula que ya está adjunto
    when(stateManager.hasState(any(MenuPrincipal.class))).thenReturn(true);
    controller.showStartScreen();

    // No se adjunta de nuevo
    verify(stateManager, times(1)).attach(any(MenuPrincipal.class));
  }

  // --- Pruebas de selección de jugadores ---

  /** Verifica que startPlayerSelection() adjunte MenuJugadores si no está adjunto. */
  @Test
  void testStartPlayerSelectionAttachesMenuJugadores() {
    when(stateManager.hasState(any(AppState.class))).thenReturn(false);

    controller.startPlayerSelection();

    verify(stateManager).attach(any(MenuJugadores.class));
  }

  /**
   * Verifica que startPlayerSelection() adjunte MenuJugadores solo si no estaba previamente
   * adjunto.
   */
  @Test
  void testStartPlayerSelectionAttachesMenuJugadoresOnlyIfNotAttached() {
    when(stateManager.hasState(any(MenuJugadores.class))).thenReturn(false);

    controller.startPlayerSelection();

    verify(stateManager, times(1)).attach(any(MenuJugadores.class));
  }

  /** Verifica que startPlayerSelection() no vuelva a adjuntar MenuJugadores si ya está adjunto. */
  @Test
  void testStartPlayerSelectionDoesNotReattachIfAlreadyAttached() {
    when(stateManager.hasState(any(MenuJugadores.class))).thenReturn(true);

    controller.startPlayerSelection();

    verify(stateManager, never()).attach(any(MenuJugadores.class));
  }

  /**
   * Verifica que startPlayerSelection() des-adjunte todos los menús previos antes de adjuntar
   * MenuJugadores. Utiliza mocks concretos de cada menú y reflexión para inyectarlos en el
   * controlador.
   */
  @Test
  void testStartPlayerSelectionDetachesOtherMenus() throws Exception {
    MenuPrincipal mockMenuPrincipal = mock(MenuPrincipal.class);
    MenuCarga mockMenuCarga = mock(MenuCarga.class);
    MenuCreditos mockMenuCreditos = mock(MenuCreditos.class);
    MenuSeleccion mockMenuSeleccion = mock(MenuSeleccion.class);

    when(stateManager.hasState(mockMenuPrincipal)).thenReturn(true);
    when(stateManager.hasState(mockMenuCarga)).thenReturn(true);
    when(stateManager.hasState(mockMenuCreditos)).thenReturn(true);
    when(stateManager.hasState(mockMenuSeleccion)).thenReturn(true);

    setPrivateField(controller, "menuPrincipal", mockMenuPrincipal);
    setPrivateField(controller, "menuCarga", mockMenuCarga);
    setPrivateField(controller, "menuCreditos", mockMenuCreditos);
    setPrivateField(controller, "menuSeleccion", mockMenuSeleccion);

    controller.startPlayerSelection();

    verify(stateManager).detach(mockMenuPrincipal);
    verify(stateManager).detach(mockMenuCarga);
    verify(stateManager).detach(mockMenuCreditos);
    verify(stateManager).detach(mockMenuSeleccion);

    verify(stateManager).attach(any(MenuJugadores.class));
  }

  /** Método de ayuda para establecer campos privados mediante reflexión. */
  private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    var field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  // --- Pruebas de inicio del juego ---

  /**
   * Verifica que startMainGame(int) adjunte MenuSeleccion y configure correctamente el número de
   * jugadores seleccionados.
   */
  @Test
  void testStartMainGameWithPlayerCount() {
    when(stateManager.hasState(any(AppState.class))).thenReturn(false);

    controller.startMainGame(3);

    verify(stateManager).attach(any(MenuSeleccion.class));
    assertFalse(controller.isGameStarted());
    assertEquals(3, controller.getSelectedPlayerCount());
  }

  /**
   * Verifica que startMainGame(int, List, List) llame a startGameService con listas de jugadores y
   * personajes y marque el juego como iniciado.
   */
  @Test
  void testStartMainGameWithPlayersAndCharacters() throws SQLException {
    when(stateManager.hasState(any(AppState.class))).thenReturn(true);

    ArrayList<Jugador> jugadores = new ArrayList<>();
    ArrayList<Integer> personajes = new ArrayList<>();
    jugadores.add(mock(Jugador.class));
    personajes.add(10);

    controller.startMainGame(1, jugadores, personajes);

    verify(startGameService).creatingNewGame(jugadores, personajes);
    verify(startGameService).ensureSceneReady();

    assertTrue(controller.isGameStarted());
    assertEquals(1, controller.getSelectedPlayerCount());
  }

  /**
   * Verifica que pasar listas vacías a startMainGame() funcione correctamente y no modifique las
   * listas.
   */
  @Test
  void testStartMainGameWithEmptyPlayersAndCharacters() throws SQLException {
    when(stateManager.hasState(any(AppState.class))).thenReturn(false);

    ArrayList<Jugador> jugadores = new ArrayList<>();
    ArrayList<Integer> personajes = new ArrayList<>();

    controller.startMainGame(0, jugadores, personajes);

    verify(startGameService).creatingNewGame(jugadores, personajes);
    verify(startGameService).ensureSceneReady();
    assertTrue(controller.isGameStarted());
    assertEquals(0, controller.getSelectedPlayerCount());
    assertTrue(jugadores.isEmpty() && personajes.isEmpty(), "Listas deben permanecer vacías");
  }

  /**
   * Verifica que startMainGame() con más jugadores que personajes no altera automáticamente la
   * lista de personajes.
   */
  @Test
  void testStartMainGameWithMorePlayersThanCharacters() throws SQLException {
    when(stateManager.hasState(any(AppState.class))).thenReturn(false);

    ArrayList<Jugador> jugadores = new ArrayList<>();
    ArrayList<Integer> personajes = new ArrayList<>();

    Jugador j1 = mock(Jugador.class);
    Jugador j2 = mock(Jugador.class);
    when(j1.getJugadorId()).thenReturn(1);
    when(j2.getJugadorId()).thenReturn(2);
    jugadores.add(j1);
    jugadores.add(j2);

    personajes.add(10);

    controller.startMainGame(2, jugadores, personajes);

    verify(startGameService).creatingNewGame(jugadores, personajes);
    verify(startGameService).ensureSceneReady();

    assertTrue(controller.isGameStarted());
    assertEquals(2, controller.getSelectedPlayerCount());
    assertEquals(1, personajes.size(), "La lista de personajes no se modifica automáticamente");
    assertEquals(10, personajes.get(0), "El primer jugador conserva su personaje");
  }

  /** Verifica que startMainGame() lance NullPointerException si se pasan listas null. */
  @Test
  void testStartMainGameWithNullListsThrows() {
    assertThrows(NullPointerException.class, () -> controller.startMainGame(1, null, null));
  }

  // --- Pruebas de carga de partidas ---

  /** Verifica que loadSavedGame() llame a showLoadMenu() con la lista de partidas guardadas. */
  @Test
  void testLoadSavedGameShowsMenuCargaAndLoadsGame() {
    SavedGame savedGame = new SavedGame("save1", "Partida de prueba");
    List<SavedGame> saves = List.of(savedGame);

    when(dataService.listarPartidasPasadas()).thenReturn(saves);

    MenuController spyController = spy(controller);
    doNothing().when(spyController).showLoadMenu(anyList(), any());

    spyController.loadSavedGame();

    verify(spyController).showLoadMenu(eq(saves), any());
  }

  /**
   * Verifica que la función de callback de loadSavedGame() cargue la partida y prepare la escena.
   */
  @Test
  void testLoadSavedGameCallbackExecution() {
    SavedGame save = new SavedGame("s1", "Test save");
    when(dataService.listarPartidasPasadas()).thenReturn(List.of(save));

    MenuController spyController = spy(controller);

    doAnswer(
            inv -> {
              List<SavedGame> saves = inv.getArgument(0);
              Consumer<String> callback = inv.getArgument(1);
              callback.accept("s1"); // simula selección de la partida
              return null;
            })
        .when(spyController)
        .showLoadMenu(anyList(), any());

    spyController.loadSavedGame();

    verify(dataService).loadDataBase("s1");
    verify(startGameService).ensureSceneReady();
  }

  /** Verifica que showLoadMenu() adjunte MenuCarga al AppStateManager. */
  @Test
  void testShowLoadMenuAttachesMenuCarga() {
    when(stateManager.hasState(any(AppState.class))).thenReturn(false);

    List<SavedGame> saves = List.of(new SavedGame("1", "Partida de prueba"));
    Consumer<String> consumer = s -> {};

    controller.showLoadMenu(saves, consumer);

    verify(stateManager).attach(any(MenuCarga.class));
  }

  /** Verifica que showLoadMenu() funcione incluso si la lista de partidas está vacía. */
  @Test
  void testShowLoadMenuWithEmptyList() {
    when(stateManager.hasState(any(AppState.class))).thenReturn(false);

    List<SavedGame> saves = new ArrayList<>();
    controller.showLoadMenu(saves, s -> {});

    verify(stateManager).attach(any(MenuCarga.class));
  }

  // --- Pruebas de créditos ---

  /** Verifica que showCredits() adjunte MenuCreditos si no está adjunto. */
  @Test
  void testShowCreditsAttachesMenuCreditos() {
    when(stateManager.hasState(any(AppState.class))).thenReturn(false);

    controller.showCredits("owner", "repo", 5);

    verify(stateManager).attach(any(MenuCreditos.class));
  }

  /** Verifica que showCredits() no re-adjunte MenuCreditos si ya está adjunto. */
  @Test
  void testShowCreditsDoesNotReattachIfAlreadyAttached() {
    when(stateManager.hasState(any(AppState.class))).thenReturn(true);

    controller.showCredits("owner", "repo", 3);

    verify(stateManager, never()).attach(any(MenuCreditos.class));
  }

  // --- Pruebas de navegación ---

  /** Verifica que goToMainMenu() llame a showStartScreen(). */
  @Test
  void testGoToMainMenuCallsShowStartScreen() {
    MenuController spyController = spy(controller);
    doNothing().when(spyController).showStartScreen();

    spyController.goToMainMenu();

    verify(spyController).showStartScreen();
  }

  // --- Pruebas de detachIfAttached() ---

  /** Verifica que detachIfAttached() des-adjunte un AppState si está adjunto. */
  @Test
  void testDetachIfAttachedDetachesWhenAttached() throws Exception {
    AppState mockState = mock(AppState.class);
    when(stateManager.hasState(mockState)).thenReturn(true);

    var method = MenuController.class.getDeclaredMethod("detachIfAttached", AppState.class);
    method.setAccessible(true);
    method.invoke(controller, mockState);

    verify(stateManager).detach(mockState);
  }

  /** Verifica que detachIfAttached() no haga nada si el AppState no está adjunto. */
  @Test
  void testDetachIfAttachedDoesNothingWhenNotAttached() throws Exception {
    AppState mockState = mock(AppState.class);
    when(stateManager.hasState(mockState)).thenReturn(false);

    var method = MenuController.class.getDeclaredMethod("detachIfAttached", AppState.class);
    method.setAccessible(true);
    method.invoke(controller, mockState);

    verify(stateManager, never()).detach(any());
  }

  /** Verifica que detachIfAttached() no falle si se le pasa null. */
  @Test
  void testDetachIfAttachedWithNullDoesNothing() throws Exception {
    var method = MenuController.class.getDeclaredMethod("detachIfAttached", AppState.class);
    method.setAccessible(true);
    method.invoke(controller, (AppState) null);

    verify(stateManager, never()).detach(any());
  }
}
