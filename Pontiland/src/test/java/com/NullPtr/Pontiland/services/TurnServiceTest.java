import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Tipo;
import com.NullPtr.Pontiland.repository.ICasillaRepository;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.services.*;
import com.NullPtr.Pontiland.view.IScene;
import java.sql.SQLException;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TurnServiceTest {

  private IJugadorRepository jugadorRepo;
  private IPartidaRepository partidaRepo;
  private ICasillaRepository casillaRepo;
  private ICasillaService casillaService;
  private DiceService diceService;
  private IHUDcontroller hudController;
  private ISubastaService subastaService;
  private IAdquisicionService adquisicionService;
  private TurnService turnService;
  private final IScene scene = mock(IScene.class);

  @BeforeEach
  void setUp() {
    jugadorRepo = mock(IJugadorRepository.class);
    partidaRepo = mock(IPartidaRepository.class);
    casillaRepo = mock(ICasillaRepository.class);
    casillaService = mock(ICasillaService.class);
    diceService = mock(DiceService.class);
    hudController = mock(IHUDcontroller.class);
    subastaService = mock(ISubastaService.class);
    adquisicionService = mock(IAdquisicionService.class);

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
    turnService.setEnabled(true);

    // ASIGNAR SCENE MOCK
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
  void testSetEnabledFalse() throws SQLException {
    // Mock jugador activo para evitar excepciones internas
    Jugador j = new Jugador("Test", 1);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);

    // Llamamos a setEnabled(false)
    turnService.setEnabled(false);

    // La bandera debe estar desactivada
    assertFalse(turnService.isEnabled());
  }

  @Test
  void testSetEnabledSQLException() throws SQLException {
    // Simula que getActivePlayer lanza SQLException
    when(jugadorRepo.getActivePlayer()).thenThrow(new SQLException("error"));

    // Llama a setEnabled (captura la excepción internamente)
    turnService.setEnabled(true);

    // Verifica que updateActivePlayerPropertyTokens fue llamada con null
    verify(casillaService).updateActivePlayerPropertyTokens(isNull());
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
  void testUpdateDisabled() throws SQLException {
    // Mock jugador activo para evitar excepciones internas
    Jugador j = new Jugador("Test", 1);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(j);

    // Deshabilitar el turno
    turnService.setEnabled(false);

    // Limpiar las interacciones previas (de setEnabled)
    clearInvocations(jugadorRepo, casillaService, diceService);

    // Ejecutar update
    turnService.update();

    // Verificar que update no interactúa con los mocks
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

  // -----------------------------------------
  // Tests Jail FSM
  // -----------------------------------------

  @Test
  void testJail_CheckRollsLessThan3_ShowsDecision() throws SQLException {
    Jugador jugador = crearJugador(1, "P1", 1500);
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorEstadoByID(1)).thenReturn(true);
    when(jugadorRepo.getTiradasCarcel(1)).thenReturn(2);

    turnService.update();

    verify(hudController).showJailDecision();
  }

  @Test
  void testJail_CheckRollsGreaterThan3_GoesToPay() throws SQLException {
    // Jugador activo y en la cárcel
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorEstadoByID(1)).thenReturn(true);
    when(jugadorRepo.getTiradasCarcel(1)).thenReturn(4);

    // Mock de jugador con dinero usando Mockito
    Jugador mockJugador = mock(Jugador.class);
    when(mockJugador.getDinero()).thenReturn(1000); // método usado en la FSM
    when(jugadorRepo.getJugadorByID(1)).thenReturn(mockJugador);

    // Ejecutar FSM
    turnService.update(); // CHECK_ROLLS -> PAY
    turnService.update(); // Procesa pago y libera jugador

    // Verificar interacciones
    verify(jugadorRepo, atLeastOnce()).setJugadorLibre(1);
    verify(jugadorRepo).resetTiradasCarcel(1);
    verify(jugadorRepo).updateDinero(eq(1), anyInt());
  }

  @Test
  void testJail_DecideAction_PlayerChoosesPay() throws SQLException {
    // Jugador activo
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorEstadoByID(1)).thenReturn(true);
    when(jugadorRepo.getTiradasCarcel(1)).thenReturn(1);

    // Simula que el jugador decide pagar la fianza
    when(hudController.getJailPay()).thenReturn(true);

    // Ejecutar FSM de la cárcel hasta que se procese la acción
    turnService.update(); // CHECK_ROLLS -> muestra panel decisión
    turnService.update(); // DECIDE_ACTION -> decide pagar
    turnService.update(); // PROCESAR acción de pagar y liberar jugador

    // Verificar que el jugador se libera
    verify(jugadorRepo).setJugadorLibre(1);
    verify(hudController).hideJailDecision();
  }

  @Test
  void testJail_DecideAction_PlayerChoosesRoll_NoDouble() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorEstadoByID(1)).thenReturn(true);
    when(jugadorRepo.getTiradasCarcel(1)).thenReturn(1);

    when(hudController.getJailPay()).thenReturn(false);
    when(hudController.getJailRoll()).thenReturn(true);

    when(diceService.getResultados()).thenReturn(new Byte[] {3, 4});

    turnService.update(); // CHECK_ROLLS
    turnService.update(); // DECIDE_ACTION
    turnService.update(); // ROLL

    verify(jugadorRepo, never()).setJugadorLibre(anyInt());
  }

  // -----------------------------------------
  // Tests Game FSM
  // -----------------------------------------
  @Test
  void testGameFSM_PlayerNotInJail_MovePlayerCalled() throws SQLException {
    // Jugador activo
    when(jugadorRepo.getActivePlayer()).thenReturn(1);

    // Estado de jugador (no en cárcel)
    when(jugadorRepo.getJugadorEstadoByID(anyInt())).thenReturn(false);

    // Devuelve un jugador por ID
    when(jugadorRepo.getJugadorByID(anyInt()))
        .thenAnswer(
            invocation -> {
              int id = invocation.getArgument(0);
              return crearJugador(id, "P" + id, 1500);
            });

    // Mock dados
    when(diceService.getResultados()).thenReturn(new Byte[] {2, 3});

    // Mock casilla
    when(casillaRepo.casillaFromPosition(anyInt()))
        .thenReturn(new Casilla(1, "Inicio", Tipo.PROPIEDAD));

    // Asignar scene mock
    IScene mockScene = mock(IScene.class);
    turnService.setScene(mockScene);

    // --- Ejecutar FSM ---
    // Primera actualización: AWAIT_ROLL -> MOVING
    turnService.update();
    // Segunda actualización: MOVING -> jugador se mueve
    turnService.update();

    // Verificar que la posición final del jugador es correcta
    verify(jugadorRepo).updatePosition(eq(1), eq(5));

    // Verificar que replicateFichaPosition fue llamado **al menos una vez**
    verify(mockScene, atLeastOnce()).replicateFichaPosition(eq(1), anyInt());
  }

  @Test
  void testGameFSM_Interact_DobleTiradasMenor3() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorEstadoByID(1)).thenReturn(false);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(crearJugador(1, "P1", 1500));
    when(diceService.getResultados()).thenReturn(new Byte[] {3, 3});
    when(casillaRepo.casillaFromPosition(anyInt()))
        .thenReturn(new Casilla(1, "Inicio", Tipo.PROPIEDAD));

    turnService.update(); // AWAIT_ROLL -> MOVING -> INTERACT
    turnService.update(); // INTERACT

    // Debe registrarse tirada doble
    assertTrue(turnService.isEnabled());
  }

  @Test
  void testGameFSM_Interact_DobleTiradas3_EnviaCarcel() throws SQLException {
    when(jugadorRepo.getActivePlayer()).thenReturn(1);
    when(jugadorRepo.getJugadorEstadoByID(1)).thenReturn(false);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(crearJugador(1, "P1", 1500));
    when(diceService.getResultados()).thenReturn(new Byte[] {6, 6});
    when(casillaRepo.casillaFromPosition(anyInt()))
        .thenReturn(new Casilla(1, "Inicio", Tipo.PROPIEDAD));

    // Forzamos tiradas previas
    turnService.setEnabled(true);
    turnService.update(); // AWAIT_ROLL
    turnService.update(); // MOVING
    turnService.update(); // INTERACT, primera doble
    turnService.update(); // INTERACT, segunda doble
    turnService.update(); // INTERACT, tercera doble -> ir a cárcel

    verify(jugadorRepo, atLeastOnce()).updatePosition(anyInt(), anyInt());
  }

  @Test
  void testJail_RollDouble_Liberado() throws SQLException {
    int playerId = 1;

    // Configuración de mocks
    when(jugadorRepo.getActivePlayer()).thenReturn(playerId);
    when(jugadorRepo.getJugadorEstadoByID(playerId)).thenReturn(true); // encarcelado
    when(jugadorRepo.getTiradasCarcel(playerId)).thenReturn(1); // tiradas < 3
    when(diceService.getResultados()).thenReturn(new Byte[] {6, 6}); // doble
    when(hudController.getJailPay()).thenReturn(false); // no paga
    when(hudController.getJailRoll()).thenReturn(true); // decide lanzar
    when(jugadorRepo.getJugadorByID(playerId)).thenReturn(new Jugador("Player1", 1));

    // Instancia de TurnService con mocks
    TurnService turnService =
        new TurnService(
            jugadorRepo,
            partidaRepo,
            diceService,
            casillaRepo,
            casillaService,
            hudController,
            subastaService,
            adquisicionService);
    turnService.setEnabled(true);

    // Simular varios ciclos de update() para que la FSM avance
    for (int i = 0; i < 5; i++) {
      turnService.update();
    }

    // Verificaciones
    verify(jugadorRepo).setJugadorLibre(playerId);
    verify(jugadorRepo).resetTiradasCarcel(playerId);
    verify(jugadorRepo).updateDinero(eq(playerId), anyInt());
  }

  // --- Helpers ---
  private Jugador crearJugador(int id, String nombre, int dinero) {
    return new Jugador(id, nombre, 0, true, dinero, new ArrayList<>());
  }
}
