package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.view.Scene;
import java.sql.SQLException;
import java.util.ArrayList;
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

    Jugador j1 = new Jugador(1000, "Jugador1", 1);
    Jugador j2 = new Jugador(1000, "Jugador2", 2);

    jugadores.add(j1);
    jugadores.add(j2);
    iconos.add(1);
    iconos.add(2);

    when(partidaRepository.newPartida(2)).thenReturn(123L);
    when(jugadorRepository.getPlayerIdByNumJugador(1)).thenReturn(10);
    when(partidaRepository.getNumJugadores()).thenReturn(2);

    startGameService.creatingNewGame(jugadores, iconos);

    // Verifica que se creó la base de datos
    verify(dataService, times(1)).newDataBase();
    // Verifica que se creó la nueva partida con el número correcto de jugadores
    verify(partidaRepository, times(1)).newPartida(2);
    // Verifica que se asignó el ID de partida a los jugadores
    verify(jugadorRepository, times(1)).setPartidaID(123L);
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
  void testCreatingNewGameRuntimeException() {
    // Preparar datos de prueba
    ArrayList<Jugador> jugadores = new ArrayList<>();
    ArrayList<Integer> iconos = new ArrayList<>();
    jugadores.add(new Jugador(1000, "Jugador1", 1));
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

  // ------------------------------------------------------------
  // loadingOldGame()
  // ------------------------------------------------------------

  /**
   * Verifica que loadingOldGame llama al servicio de datos para cargar la base de datos con el
   * archivo especificado.
   */
  @Test
  void testLoadingOldGameCallsDataService() {
    String archivo = "partida_guardada.db";

    startGameService.loadingOldGame(archivo);

    // Verifica que se llamó al método loadDataBase del servicio de datos
    verify(dataService, times(1)).loadDataBase(archivo);
  }

  // ------------------------------------------------------------
  // ensureSceneReady()
  // ------------------------------------------------------------

  /** Verifica que ensureSceneReady lanza IllegalStateException si la escena no fue inyectada. */
  @Test
  void testEnsureSceneReadyThrowsExceptionIfSceneNull() {
    StartGameService serviceWithoutScene =
        new StartGameService(jugadorRepository, partidaRepository, dataService, null);

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

  @Test
  /** Verifica que se pueda instanciar StartGameService usando el constructor por defecto. */
  void testDefaultConstructor() {
    // no debe lanzar ninguna excepción
    assertDoesNotThrow(
        () -> new StartGameService(), "El constructor por defecto no debe lanzar excepciones");
  }
}
