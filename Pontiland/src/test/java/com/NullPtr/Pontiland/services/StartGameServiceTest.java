package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.SavedGame;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import com.NullPtr.Pontiland.view.Scene;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

/**
 * Clase de pruebas unitarias para StartGameService. Se prueban los métodos de creación de nuevas
 * partidas, carga de partidas existentes y la validación de la inyección de Scene.
 */
class StartGameServiceTest {

  @Mock IJugadorRepository jugadorRepository;
  @Mock IPartidaRepository partidaRepository;
  @Mock IPropiedadRepository propiedadRepository;
  @Mock IDataService dataService;
  @Mock Scene scene;

  @InjectMocks StartGameService startGameService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // ------------------------------------------------------------
  // creatingNewGame()
  // ------------------------------------------------------------

  /**
   * Verifica que creatingNewGame crea una partida correctamente, registra jugadores y carga los
   * modelos en la escena.
   */
  @Test
  void testCreatingNewGameSuccess() throws SQLException {
    ArrayList<Jugador> jugadores = new ArrayList<>();
    ArrayList<Integer> iconos = new ArrayList<>();

    Jugador j1 = new Jugador("Jugador1", 1);
    Jugador j2 = new Jugador("Jugador2", 2);

    jugadores.add(j1);
    jugadores.add(j2);
    iconos.add(1);
    iconos.add(2);

    when(partidaRepository.newPartida(2)).thenReturn(123L);
    when(jugadorRepository.getPlayerIdByNumJugador(1)).thenReturn(10);
    when(partidaRepository.getNumJugadores()).thenReturn(2);

    // Mock para propiedadRepository
    doNothing().when(propiedadRepository).setPartidaID(anyLong());

    startGameService.creatingNewGame(jugadores, iconos);

    // Verifica que se creó la base de datos
    verify(dataService, times(1)).newDataBase();
    // Verifica que se creó la nueva partida con el número correcto de jugadores
    verify(partidaRepository, times(1)).newPartida(2);
    // Verifica que se asignó el ID de partida a los jugadores
    verify(jugadorRepository, times(1)).setPartidaID(123L);
    // Verifica que se asignó el ID de partida a las propiedades
    verify(propiedadRepository, times(1)).setPartidaID(123L);
    // Verifica que se registraron todos los jugadores con sus iconos
    verify(jugadorRepository, times(1)).newPlayer(j1, 1);
    verify(jugadorRepository, times(1)).newPlayer(j2, 2);
    // Verifica que se estableció el jugador activo
    verify(jugadorRepository, times(1)).newActivePlayer(10);
    // Verifica que se cargaron los modelos de las fichas en la escena
    verify(scene, times(1)).loadFichasModels(any());
  }

  /** Test que verifica que se lance una excepción si falla la creación de la partida. */
  @Test
  void testCreatingNewGameRuntimeException() throws SQLException {
    // Preparar datos de prueba
    ArrayList<Jugador> jugadores = new ArrayList<>();
    ArrayList<Integer> iconos = new ArrayList<>();
    jugadores.add(new Jugador("Jugador1", 1));
    iconos.add(1);

    // Simula que al crear la partida ocurre un error
    when(partidaRepository.newPartida(1)).thenThrow(new RuntimeException("Error al crear partida"));

    // Ejecutar y verificar que se lanza RuntimeException
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> startGameService.creatingNewGame(jugadores, iconos),
            "Debe lanzar RuntimeException si falla la creación de la partida");

    // Verifica que el mensaje contenga la palabra "Error"
    assertTrue(exception.getMessage().contains("Error"), "El mensaje debe contener 'Error'");
  }

  /** Verifica que se lance excepción cuando newPlayer falla. */
  @Test
  void testCreatingNewGameFailsOnNewPlayer() throws SQLException {
    ArrayList<Jugador> jugadores = new ArrayList<>();
    ArrayList<Integer> iconos = new ArrayList<>();

    Jugador j1 = new Jugador("Jugador1", 1);
    jugadores.add(j1);
    iconos.add(1);

    when(partidaRepository.newPartida(1)).thenReturn(123L);
    doThrow(new SQLException("Error al insertar jugador"))
        .when(jugadorRepository)
        .newPlayer(any(Jugador.class), anyInt());

    assertThrows(
        SQLException.class,
        () -> startGameService.creatingNewGame(jugadores, iconos),
        "Debe lanzar SQLException cuando falla newPlayer");
  }

  /** Verifica que se manejen correctamente múltiples jugadores. */
  @Test
  void testCreatingNewGameMultiplePlayers() throws SQLException {
    ArrayList<Jugador> jugadores = new ArrayList<>();
    ArrayList<Integer> iconos = new ArrayList<>();

    for (int i = 1; i <= 4; i++) {
      jugadores.add(new Jugador("Jugador" + i, i));
      iconos.add(i);
    }

    when(partidaRepository.newPartida(4)).thenReturn(456L);
    when(jugadorRepository.getPlayerIdByNumJugador(1)).thenReturn(1);
    when(partidaRepository.getNumJugadores()).thenReturn(4);
    doNothing().when(propiedadRepository).setPartidaID(anyLong());

    startGameService.creatingNewGame(jugadores, iconos);

    verify(partidaRepository, times(1)).newPartida(4);
    verify(jugadorRepository, times(4)).newPlayer(any(Jugador.class), anyInt());
    verify(scene, times(1)).loadFichasModels(any());
  }

  // ------------------------------------------------------------
  // loadingOldGame()
  // ------------------------------------------------------------

  /** Verifica que loadingOldGame configura correctamente los IDs de partida en los repositorios. */
  @Test
  void testLoadingOldGameSuccess() throws SQLException {
    String partidaId = "123";

    startGameService.loadingOldGame(partidaId);

    // Verifica que se configuró el ID de partida en todos los repositorios
    verify(partidaRepository, times(1)).setPartidaID(123L);
    verify(jugadorRepository, times(1)).setPartidaID(123L);
    verify(propiedadRepository, times(1)).setPartidaID(123L);
  }

  /** Verifica que loadingOldGame funciona con diferentes IDs de partida. */
  @Test
  void testLoadingOldGameWithDifferentIds() throws SQLException {
    String[] partidaIds = {"456", "789", "1000"};

    for (String partidaId : partidaIds) {
      reset(partidaRepository, jugadorRepository, propiedadRepository);

      startGameService.loadingOldGame(partidaId);

      long expectedId = Long.parseLong(partidaId);
      verify(partidaRepository, times(1)).setPartidaID(expectedId);
      verify(jugadorRepository, times(1)).setPartidaID(expectedId);
      verify(propiedadRepository, times(1)).setPartidaID(expectedId);
    }
  }

  /** Verifica que loadingOldGame lanza NumberFormatException con ID inválido. */
  @Test
  void testLoadingOldGameWithInvalidId() {
    String invalidId = "not_a_number";

    assertThrows(
        NumberFormatException.class,
        () -> startGameService.loadingOldGame(invalidId),
        "Debe lanzar NumberFormatException con ID inválido");
  }

  /** Verifica que loadingOldGame lanza NumberFormatException con ID null. */
  @Test
  void testLoadingOldGameWithNullId() {
    // Long.parseLong(null) lanza NumberFormatException, no NullPointerException
    assertThrows(
        NumberFormatException.class,
        () -> startGameService.loadingOldGame(null),
        "Debe lanzar NumberFormatException con ID null");
  }

  /**
   * Verifica que loadingOldGame completa exitosamente incluso si setPartidaID no lanza excepción.
   */
  @Test
  void testLoadingOldGameCompletesSuccessfully() throws SQLException {
    String partidaId = "123";

    // Configurar mocks para que no lancen excepciones
    doNothing().when(partidaRepository).setPartidaID(123L);
    doNothing().when(jugadorRepository).setPartidaID(123L);
    doNothing().when(propiedadRepository).setPartidaID(123L);

    // Ejecutar sin lanzar excepción
    assertDoesNotThrow(
        () -> startGameService.loadingOldGame(partidaId),
        "No debe lanzar excepción en operación normal");

    // Verificar que se llamaron todos los setPartidaID
    verify(partidaRepository, times(1)).setPartidaID(123L);
    verify(jugadorRepository, times(1)).setPartidaID(123L);
    verify(propiedadRepository, times(1)).setPartidaID(123L);
  }

  // ------------------------------------------------------------
  // listPastGames()
  // ------------------------------------------------------------

  /** Verifica que listPastGames devuelve la lista de partidas guardadas. */
  @Test
  void testListPastGamesSuccess() throws SQLException {
    List<SavedGame> expectedGames = new ArrayList<>();
    expectedGames.add(new SavedGame("1", "2025-01-01"));
    expectedGames.add(new SavedGame("2", "2025-01-02"));

    when(partidaRepository.getAllPartidaIDs()).thenReturn(expectedGames);

    List<SavedGame> result = startGameService.listPastGames();

    assertEquals(expectedGames, result, "Debe devolver la lista de partidas guardadas");
    verify(partidaRepository, times(1)).getAllPartidaIDs();
  }

  /** Verifica que listPastGames devuelve lista vacía en caso de error. */
  @Test
  void testListPastGamesReturnsEmptyOnException() throws SQLException {
    when(partidaRepository.getAllPartidaIDs()).thenThrow(new SQLException("Error de BD"));

    List<SavedGame> result = startGameService.listPastGames();

    assertNotNull(result, "No debe devolver null");
    assertTrue(result.isEmpty(), "Debe devolver lista vacía en caso de error");
  }

  /** Verifica que listPastGames maneja correctamente lista vacía. */
  @Test
  void testListPastGamesEmptyList() throws SQLException {
    when(partidaRepository.getAllPartidaIDs()).thenReturn(new ArrayList<>());

    List<SavedGame> result = startGameService.listPastGames();

    assertNotNull(result, "No debe devolver null");
    assertTrue(result.isEmpty(), "Debe devolver lista vacía cuando no hay partidas");
  }

  // ------------------------------------------------------------
  // ensureSceneReady()
  // ------------------------------------------------------------

  /** Verifica que ensureSceneReady lanza IllegalStateException si la escena no fue inyectada. */
  @Test
  void testEnsureSceneReadyThrowsExceptionIfSceneNull() {
    // Crear servicio con scene null
    StartGameService serviceWithoutScene =
        new StartGameService(
            jugadorRepository, partidaRepository, dataService, null, propiedadRepository);

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, serviceWithoutScene::ensureSceneReady);

    // Verifica que el mensaje contenga la palabra "Scene"
    assertTrue(ex.getMessage().contains("Scene"), "El mensaje debe contener 'Scene'");
  }

  /** Verifica que ensureSceneReady no lanza excepción si la escena fue correctamente inyectada. */
  @Test
  void testEnsureSceneReadyDoesNotThrowIfSceneInjected() {
    assertDoesNotThrow(() -> startGameService.ensureSceneReady());
  }

  /** Verifica que ensureSceneReady puede llamarse múltiples veces sin problemas. */
  @Test
  void testEnsureSceneReadyMultipleCalls() {
    assertDoesNotThrow(
        () -> {
          startGameService.ensureSceneReady();
          startGameService.ensureSceneReady();
          startGameService.ensureSceneReady();
        });
  }

  // ------------------------------------------------------------
  // Constructor tests
  // ------------------------------------------------------------

  /** Verifica que se pueda instanciar StartGameService usando el constructor por defecto. */
  @Test
  void testDefaultConstructor() {
    assertDoesNotThrow(
        () -> new StartGameService(), "El constructor por defecto no debe lanzar excepciones");
  }

  /** Verifica que el constructor con parámetros funcione correctamente. */
  @Test
  void testParameterizedConstructor() {
    assertDoesNotThrow(
        () ->
            new StartGameService(
                jugadorRepository, partidaRepository, dataService, scene, propiedadRepository),
        "El constructor con parámetros no debe lanzar excepciones");
  }

  /** Verifica que el constructor maneje correctamente parámetros null. */
  @Test
  void testConstructorWithNullParameters() {
    assertDoesNotThrow(
        () -> new StartGameService(null, null, null, null, null),
        "El constructor debe manejar parámetros null sin lanzar excepciones");
  }
}
