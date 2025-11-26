package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Tipo;
import com.NullPtr.Pontiland.repository.ICasillaRepository;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import java.sql.SQLException;
import org.junit.jupiter.api.*;
import org.mockito.*;

/**
 * Clase de pruebas unitarias para TurnService. Contiene tests para los métodos nextTurn,
 * movePlayer, update, consumeLastMove y moveToJail. Se usan mocks para repositorios y servicios
 * auxiliares.
 */
class TurnServiceTest {

  @Mock IJugadorRepository jugadorRepo;
  @Mock IPartidaRepository partidaRepo;
  @Mock DiceService diceService;
  @Mock ICasillaRepository casillaRepo;
  @Mock ICasillaService casillaService;

  @InjectMocks TurnService turnService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // ------------------------------------------------------------
  // nextTurn()
  // ------------------------------------------------------------

  /** Verifica que nextTurn cambia correctamente el jugador activo cuando no ocurre ningún error. */
  @Test
  void testNextTurnSuccess() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(partidaRepo.getNumJugadores()).thenReturn(4);
    when(jugadorRepo.getPlayerIdByNumJugador(2)).thenReturn(20);

    turnService.nextTurn();

    // Verifica que se haya cambiado el jugador activo al siguiente
    verify(jugadorRepo, times(1)).changeActivePlayer(20);
  }

  /** Verifica que nextTurn lanza RuntimeException cuando ocurre un SQLException. */
  @Test
  void testNextTurnThrowsSQLException() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenThrow(new SQLException("err"));

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> turnService.nextTurn(),
            "Debe lanzar RuntimeException al fallar SQL");
    // Comprueba que el mensaje de la excepción contenga "err"
    assertTrue(ex.getMessage().contains("err"), "Mensaje de excepción debe contener 'err'");
  }

  // ------------------------------------------------------------
  // movePlayer()
  // ------------------------------------------------------------

  /**
   * Verifica que movePlayer mueve correctamente al jugador activo y deja el movimiento pendiente
   * para ser consumido.
   */
  @Test
  void testMovePlayerSuccess() throws SQLException {
    Jugador j = new Jugador(1000, "Test", 1);
    j.setPosicion(10);

    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);

    Casilla mockCasilla = mock(Casilla.class);
    when(mockCasilla.getNombreCasilla()).thenReturn("Terreno");
    when(mockCasilla.getTipoCasilla()).thenReturn(Tipo.PROPIEDAD);
    when(casillaRepo.casillaFromPosition(12)).thenReturn(mockCasilla);
    when(casillaRepo.casillaFromPosition(10)).thenReturn(mockCasilla);

    turnService.movePlayer(2);

    // Verifica que hay movimiento pendiente
    assertTrue(turnService.hasMovePending(), "Debe haber movimiento pendiente");
    int[] move = turnService.consumeLastMove();
    // Verifica que el movimiento no sea null
    assertNotNull(move, "El movimiento consumido no debe ser null");
    // Verifica que el movimiento sea desde la posición 1 hasta la 12
    assertArrayEquals(new int[] {1, 12}, move, "El movimiento debe ser [1,12]");

    // Verifica que se actualizó al jugador
    verify(jugadorRepo).updateJugador(j);
  }

  /**
   * Verifica que movePlayer lanza RuntimeException si ocurre un SQLException al obtener el jugador
   * activo.
   */
  @Test
  void testMovePlayerSQLException() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenThrow(new SQLException("boom"));

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> turnService.movePlayer(3),
            "Debe lanzar RuntimeException si falla SQL al mover jugador");
    // Verifica que el mensaje contenga "boom"
    assertTrue(ex.getMessage().contains("boom"), "Mensaje de excepción debe contener 'boom'");
  }

  // ------------------------------------------------------------
  // update()
  // ------------------------------------------------------------

  /** Verifica que update no hace nada si los dados son null. */
  @Test
  void testUpdateDadosNull_NoAction() {
    when(diceService.getResultados()).thenReturn(null);

    turnService.update();

    // Verifica que no hubo interacción con repositorios ni servicios
    verifyNoInteractions(jugadorRepo, casillaRepo, casillaService);
  }

  /** Verifica que update cambia el jugador activo si no se sacan dobles. */
  @Test
  void testUpdateNonDoublesChangesTurn() throws SQLException {
    Byte[] dados = {3, 4};
    when(diceService.getResultados()).thenReturn(dados);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(partidaRepo.getNumJugadores()).thenReturn(4);

    Jugador j = new Jugador(1500, "TestJugador", 1);
    j.setPosicion(5);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);

    Casilla mockCasilla = mock(Casilla.class);
    when(mockCasilla.getNombreCasilla()).thenReturn("Terreno");
    when(mockCasilla.getTipoCasilla()).thenReturn(Tipo.PROPIEDAD);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(mockCasilla);

    turnService.update();

    // Verifica que se cambió el jugador activo al no haber dobles
    verify(jugadorRepo, atLeastOnce()).changeActivePlayer(anyInt());
  }

  /** Verifica que update no cambia el jugador activo si se sacan dobles. */
  @Test
  void testUpdateDoublesExtraTurn() throws SQLException {
    Byte[] dados = {2, 2};
    when(diceService.getResultados()).thenReturn(dados);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);

    Jugador j = new Jugador(1500, "TestJugador", 1);
    j.setPosicion(5);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);

    Casilla casilla = mock(Casilla.class);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(casilla);

    turnService.update();

    // Verifica que no se cambió el jugador activo
    verify(jugadorRepo, never()).changeActivePlayer(anyInt());
  }

  /** Verifica que update llama a la interacción de la casilla si el jugador puede interactuar. */
  @Test
  void testUpdateInteractWhenAllowed() throws SQLException {
    Byte[] dados = {1, 2};
    when(diceService.getResultados()).thenReturn(dados);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(partidaRepo.getNumJugadores()).thenReturn(4);

    Jugador j = new Jugador(1500, "TestJugador", 1);
    j.setPosicion(3);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);

    Casilla c = mock(Casilla.class);
    when(c.getNombreCasilla()).thenReturn("Terreno");
    when(c.getTipoCasilla()).thenReturn(Tipo.PROPIEDAD);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(c);

    when(diceService.getCanInteract()).thenReturn(true);

    turnService.update();

    // Verifica que se llamó a la interacción de la casilla exactamente una vez
    verify(casillaService, times(1)).interaccion(j, c);
  }

  /** Verifica que update manda al jugador a la cárcel si la bandera getIrACarcel es verdadera. */
  @Test
  void testUpdateGoToJailFlag() throws SQLException {
    Byte[] dados = {3, 3};
    when(diceService.getResultados()).thenReturn(dados);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);

    Jugador j = new Jugador(1500, "TestJugador", 1);
    j.setPosicion(10);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);
    when(jugadorRepo.getNumJugadorByPlayerId(1)).thenReturn(1);

    Casilla c = mock(Casilla.class);
    when(c.getNombreCasilla()).thenReturn("Terreno");
    when(c.getTipoCasilla()).thenReturn(Tipo.PROPIEDAD);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(c);

    when(casillaService.getIrACarcel()).thenReturn(true);

    turnService.update();

    // Verifica que se llamó a goToJail del jugador activo
    verify(jugadorRepo, times(1)).goToJail(1);
  }

  // ------------------------------------------------------------
  // consumeLastMove()
  // ------------------------------------------------------------

  /** Verifica que consumeLastMove devuelve null si no hay movimientos pendientes. */
  @Test
  void testConsumeLastMove_NoPending() {
    assertNull(turnService.consumeLastMove(), "No debe haber movimiento pendiente");
  }

  // ------------------------------------------------------------
  // moveToJail()
  // ------------------------------------------------------------

  /** Verifica que moveToJail manda al jugador activo a la cárcel y deja un movimiento pendiente. */
  @Test
  void testMoveToJailSuccess() throws SQLException {
    Jugador j = new Jugador(1500, "TestJugador", 1);

    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);
    when(jugadorRepo.getNumJugadorByPlayerId(1)).thenReturn(1);

    turnService.moveToJail();

    verify(jugadorRepo, times(1)).goToJail(1);
    // Verifica que haya movimiento pendiente
    assertTrue(
        turnService.hasMovePending(), "Debe quedar movimiento pendiente tras ir a la cárcel");
  }

  /**
   * Verifica que moveToJail lanza RuntimeException si ocurre un SQLException al obtener el jugador
   * activo.
   */
  @Test
  void testMoveToJailSQLException() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenThrow(new SQLException("fail"));

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> turnService.moveToJail(),
            "Debe lanzar RuntimeException si falla SQL al ir a la cárcel");
    // Verifica que el mensaje contenga "fail"
    assertTrue(ex.getMessage().contains("fail"), "Mensaje de excepción debe contener 'fail'");
  }

  /** Verifica que update no lanza excepción si ocurre SQLException al obtener el jugador activo. */
  @Test
  void testUpdateSQLExceptionInGetActivePlayer() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenThrow(new SQLException("boom"));

    // Verifica que update no lance ninguna excepción
    assertDoesNotThrow(() -> turnService.update());

    // Verifica que no se haya interactuado con los servicios de casilla
    verifyNoInteractions(casillaRepo, casillaService);
  }
}
