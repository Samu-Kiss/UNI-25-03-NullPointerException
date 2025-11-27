package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Tipo;
import com.NullPtr.Pontiland.repository.ICasillaRepository;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.view.IScene;
import java.sql.SQLException;
import org.junit.jupiter.api.*;
import org.mockito.*;

/** Clase de pruebas unitarias para TurnService con cobertura completa. */
class TurnServiceTest {

  @Mock IJugadorRepository jugadorRepo;
  @Mock IPartidaRepository partidaRepo;
  @Mock DiceService diceService;
  @Mock ICasillaRepository casillaRepo;
  @Mock ICasillaService casillaService;
  @Mock IHUDcontroller hudController;
  @Mock ISubastaService subastaService;
  @Mock IScene scene;
  @Mock IAdquisicionService adquisicionService;

  TurnService turnService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    turnService =
        new TurnService(
            jugadorRepo,
            partidaRepo,
            diceService,
            casillaRepo,
            casillaService,
            hudController,
            subastaService,
                adquisicionService);
    turnService.setScene(scene);
  }

  // ------------------------------------------------------------
  // nextTurn()
  // ------------------------------------------------------------

  @Test
  void testNextTurnSuccess() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(partidaRepo.getNumJugadores()).thenReturn(4);
    when(jugadorRepo.getPlayerIdByNumJugador(2)).thenReturn(20);

    turnService.nextTurn();

    verify(jugadorRepo, times(1)).changeActivePlayer(20);
  }

  @Test
  void testNextTurnWithModuloWrap() throws SQLException {
    // Caso donde el jugador activo es el último y debe volver al primero
    when(jugadorRepo.getActivePlayer()).thenReturn(4);
    when(partidaRepo.getNumJugadores()).thenReturn(4);
    when(jugadorRepo.getPlayerIdByNumJugador(1)).thenReturn(10);

    turnService.nextTurn();

    verify(jugadorRepo, times(1)).changeActivePlayer(10);
  }

  @Test
  void testNextTurnSQLException() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenThrow(new SQLException("error"));

    assertDoesNotThrow(() -> turnService.nextTurn());
    verify(jugadorRepo, never()).changeActivePlayer(anyInt());
  }

  // ------------------------------------------------------------
  // movePlayer()
  // ------------------------------------------------------------

  @Test
  void testMovePlayerSuccess() throws SQLException {
    Jugador j = new Jugador("Test", 1);
    j.setDinero(1000);
    j.setPosicion(10);

    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);

    Casilla mockCasilla = mock(Casilla.class);
    when(mockCasilla.getNombreCasilla()).thenReturn("Terreno");
    when(mockCasilla.getTipoCasilla()).thenReturn(Tipo.PROPIEDAD);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(mockCasilla);

    turnService.movePlayer(2);

    assertEquals(12, j.getPosicion());
    verify(jugadorRepo, times(1)).updatePosition(j.getJugadorId(), 12);
    verify(scene, times(1)).replicateFichaPosition(j.getJugadorId(), 11);
  }

  @Test
  void testMovePlayerPasaPorSalida() throws SQLException {
    Jugador j = new Jugador("Test", 1);
    j.setDinero(1000);
    j.setPosicion(38); // Cerca del final

    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);

    Casilla mockCasilla = mock(Casilla.class);
    when(mockCasilla.getNombreCasilla()).thenReturn("ParadaLibre");
    when(mockCasilla.getTipoCasilla()).thenReturn(Tipo.PARADALIBRE);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(mockCasilla);

    turnService.movePlayer(5); // 38 + 5 = 43 -> pasa por salida

    // Verifica que cobró 200 por pasar por salida
    assertEquals(1200, j.getDinero());
    verify(jugadorRepo, times(1)).updateDinero(j.getJugadorId(), 1200);
  }

  @Test
  void testMovePlayerCaeEnSalida() throws SQLException {
    Jugador j = new Jugador("Test", 1);
    j.setDinero(1000);
    j.setPosicion(39);

    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);

    Casilla mockCasilla = mock(Casilla.class);
    when(mockCasilla.getNombreCasilla()).thenReturn("ParadaLibre");
    when(mockCasilla.getTipoCasilla()).thenReturn(Tipo.PARADALIBRE);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(mockCasilla);

    turnService.movePlayer(2); // Cae exactamente en posición 1 (salida)

    assertEquals(1, j.getPosicion());
    assertEquals(1200, j.getDinero()); // Cobra 200
    verify(jugadorRepo, times(1)).updateDinero(j.getJugadorId(), 1200);
  }

  @Test
  void testMovePlayerSQLExceptionGetJugador() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenThrow(new SQLException("error"));

    assertDoesNotThrow(() -> turnService.movePlayer(3));
    verify(scene, never()).replicateFichaPosition(anyInt(), anyInt());
  }

  // ------------------------------------------------------------
  // setEnabled() / isEnabled()
  // ------------------------------------------------------------

  @Test
  void testSetEnabledTrue() throws SQLException {
    Jugador j = new Jugador("Test", 1);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);

    turnService.setEnabled(true);

    assertTrue(turnService.isEnabled());
    verify(casillaService, times(1)).updateActivePlayerPropertyTokens(j);
  }

  @Test
  void testSetEnabledFalse() {
    turnService.setEnabled(false);

    assertFalse(turnService.isEnabled());
    verifyNoInteractions(casillaService);
  }

  @Test
  void testSetEnabledSQLException() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenThrow(new SQLException("error"));

    // setEnabled no lanza excepción, solo la captura y loguea
    turnService.setEnabled(true);

    // Verifica que no se llamó a updateActivePlayerPropertyTokens debido al error
    verify(casillaService, never()).updateActivePlayerPropertyTokens(any());
  }

  // ------------------------------------------------------------
  // terminarTurno()
  // ------------------------------------------------------------

  @Test
  void testTerminarTurno() {
    turnService.terminarTurno();

    verify(diceService, times(1)).enableInteract(true);
  }

  @Test
  void testTerminarTurnoConDiceServiceNulo() {
    TurnService serviceConDiceNulo =
        new TurnService(
            jugadorRepo,
            partidaRepo,
            null,
            casillaRepo,
            casillaService,
            hudController,
            subastaService,
                adquisicionService);

    assertDoesNotThrow(() -> serviceConDiceNulo.terminarTurno());
  }

  // ------------------------------------------------------------
  // update() - FSM completa
  // ------------------------------------------------------------

  @Test
  void testUpdateDisabled() {
    turnService.setEnabled(false);

    turnService.update();

    verifyNoInteractions(diceService, jugadorRepo, casillaService);
  }

  @Test
  void testUpdateMovingState() throws SQLException {
    turnService.setEnabled(true);

    // Estado AWAIT_ROLL -> MOVING
    Byte[] dados = {3, 4};
    when(diceService.getResultados()).thenReturn(dados);

    Jugador j = new Jugador("Test", 1);
    j.setPosicion(5);
    j.setDinero(1000);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);
    when(jugadorRepo.getPlayerCount()).thenReturn(2);
    when(jugadorRepo.getPlayerIdByNumJugador(anyInt())).thenReturn(1);

    Casilla casilla = mock(Casilla.class);
    when(casilla.getNombreCasilla()).thenReturn("Terreno");
    when(casilla.getTipoCasilla()).thenReturn(Tipo.PROPIEDAD);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(casilla);

    when(diceService.getCanInteract()).thenReturn(false); // Bloquea en MOVING

    // Primera actualización: lanza dados
    turnService.update();
    // Segunda actualización: mueve jugador
    turnService.update();

    verify(scene, times(1)).resetCamera();
    verify(scene, times(1)).replicateFichaPosition(anyInt(), anyInt());
  }

  @Test
  void testUpdateInteractNoDoble() throws SQLException {
    turnService.setEnabled(true);

    Byte[] dados = {3, 4}; // No dobles
    when(diceService.getResultados()).thenReturn(dados);

    Jugador j = new Jugador("Test", 1);
    j.setPosicion(5);
    j.setDinero(1000);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);
    when(jugadorRepo.getPlayerCount()).thenReturn(2);
    when(jugadorRepo.getPlayerIdByNumJugador(anyInt())).thenReturn(1);

    Casilla casilla = mock(Casilla.class);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(casilla);
    when(diceService.getCanInteract()).thenReturn(true);
    when(casillaService.getIrACarcel()).thenReturn(false);

    // Ejecutar FSM completa
    for (int i = 0; i < 10; i++) {
      turnService.update();
    }

    verify(casillaService, atLeastOnce()).interaccion(eq(j), any(Casilla.class));
  }

  @Test
  void testUpdateInteractDobleUnaVez() throws SQLException {
    turnService.setEnabled(true);

    Byte[] dados = {3, 3}; // Dobles
    when(diceService.getResultados()).thenReturn(dados);

    Jugador j = new Jugador("Test", 1);
    j.setPosicion(5);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);
    when(jugadorRepo.getPlayerCount()).thenReturn(2);
    when(jugadorRepo.getPlayerIdByNumJugador(anyInt())).thenReturn(1);

    Casilla casilla = mock(Casilla.class);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(casilla);
    when(diceService.getCanInteract()).thenReturn(true);
    when(casillaService.getIrACarcel()).thenReturn(false);

    // Ejecutar múltiples updates
    for (int i = 0; i < 10; i++) {
      turnService.update();
    }

    // Con dobles, no debe cambiar de jugador
    verify(jugadorRepo, never()).changeActivePlayer(anyInt());
  }

  @Test
  void testUpdateEndTurnConIrACarcel() throws SQLException {
    turnService.setEnabled(true);

    Byte[] dados = {3, 4};
    when(diceService.getResultados()).thenReturn(dados);

    Jugador j = new Jugador("Test", 1);
    j.setPosicion(5);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);
    when(jugadorRepo.getPlayerCount()).thenReturn(2);
    when(jugadorRepo.getPlayerIdByNumJugador(anyInt())).thenReturn(1);

    Casilla casilla = mock(Casilla.class);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(casilla);
    when(diceService.getCanInteract()).thenReturn(true);
    when(casillaService.getIrACarcel()).thenReturn(true); // Casilla envía a cárcel

    for (int i = 0; i < 10; i++) {
      turnService.update();
    }

    verify(jugadorRepo, atLeastOnce()).goToJail(anyInt());
  }

  @Test
  void testUpdateTerminarInteraccion() throws SQLException {
    turnService.setEnabled(true);

    Byte[] dados = {3, 4};
    when(diceService.getResultados()).thenReturn(dados);

    Jugador j = new Jugador("Test", 1);
    j.setPosicion(5);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);
    when(jugadorRepo.getPlayerCount()).thenReturn(2);
    when(jugadorRepo.getPlayerIdByNumJugador(anyInt())).thenReturn(1);

    Casilla casilla = mock(Casilla.class);
    when(casillaRepo.casillaFromPosition(anyInt())).thenReturn(casilla);
    when(diceService.getCanInteract()).thenReturn(true);
    when(casillaService.getIrACarcel()).thenReturn(false);

    for (int i = 0; i < 15; i++) {
      turnService.update();
    }

    verify(casillaService, atLeastOnce()).terminarInteraccion(eq(j), any(Casilla.class));
  }

  @Test
  void testUpdateFSMException() {
    turnService.setEnabled(true);

    when(diceService.getResultados()).thenThrow(new RuntimeException("error"));

    // No debe propagar la excepción
    assertDoesNotThrow(() -> turnService.update());
  }

  // ------------------------------------------------------------
  // moveToJail()
  // ------------------------------------------------------------

  @Test
  void testMoveToJailSuccess() throws SQLException {
    Jugador j = new Jugador("Test", 1);

    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);
    when(jugadorRepo.getNumJugadorByPlayerId(1)).thenReturn(1);

    turnService.moveToJail();

    verify(jugadorRepo, times(1)).goToJail(1);
    verify(scene, times(1)).replicateFichaPosition(1, 10);
  }

  @Test
  void testMoveToJailSQLException() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenThrow(new SQLException("error"));

    assertDoesNotThrow(() -> turnService.moveToJail());
    verify(scene, never()).replicateFichaPosition(anyInt(), anyInt());
  }

  // ------------------------------------------------------------
  // Métodos de subasta
  // ------------------------------------------------------------

  @Test
  void testIniciarSubastaExitoso() {
    when(subastaService.iniciarSubasta()).thenReturn(true);

    boolean resultado = turnService.iniciarSubasta();

    assertTrue(resultado);
    verify(subastaService, times(1)).iniciarSubasta();
  }

  @Test
  void testIniciarSubastaFallido() {
    when(subastaService.iniciarSubasta()).thenReturn(false);

    boolean resultado = turnService.iniciarSubasta();

    assertFalse(resultado);
  }

  @Test
  void testIniciarSubastaConSubastaServiceNulo() {
    TurnService serviceConSubastaNula =
        new TurnService(
            jugadorRepo,
            partidaRepo,
            diceService,
            casillaRepo,
            casillaService,
            hudController,
            null,
                adquisicionService);

    boolean resultado = serviceConSubastaNula.iniciarSubasta();

    assertFalse(resultado);
  }

  @Test
  void testIncreaseAuctionExitoso() {
    when(subastaService.aumentarPrecio(50)).thenReturn(true);

    boolean resultado = turnService.increaseAuction(50);

    assertTrue(resultado);
    verify(subastaService, times(1)).aumentarPrecio(50);
  }

  @Test
  void testIncreaseAuctionDeltaNegativo() {
    boolean resultado = turnService.increaseAuction(-10);

    assertFalse(resultado);
    verifyNoInteractions(subastaService);
  }

  @Test
  void testIncreaseAuctionDeltaCero() {
    boolean resultado = turnService.increaseAuction(0);

    assertFalse(resultado);
    verifyNoInteractions(subastaService);
  }

  @Test
  void testIncreaseAuctionConSubastaServiceNulo() {
    TurnService serviceConSubastaNula =
        new TurnService(
            jugadorRepo,
            partidaRepo,
            diceService,
            casillaRepo,
            casillaService,
            hudController,
            null,
                adquisicionService);

    boolean resultado = serviceConSubastaNula.increaseAuction(50);

    assertFalse(resultado);
  }

  @Test
  void testExitAuction() {
    turnService.exitAuction();

    verify(subastaService, times(1)).salirSubasta();
  }

  // ------------------------------------------------------------
  // Métodos stub (buyProperty, payRent)
  // ------------------------------------------------------------

  @Test
  void testBuyProperty() {
    assertDoesNotThrow(() -> turnService.buyProperty());
  }

  @Test
  void testPayRent() {
    assertDoesNotThrow(() -> turnService.payRent());
  }

  // ------------------------------------------------------------
  // setScene()
  // ------------------------------------------------------------

  @Test
  void testSetScene() {
    IScene newScene = mock(IScene.class);
    turnService.setScene(newScene);

    // No hay getter para verificar, pero no debe lanzar excepción
    assertDoesNotThrow(() -> turnService.setScene(newScene));
  }
}
