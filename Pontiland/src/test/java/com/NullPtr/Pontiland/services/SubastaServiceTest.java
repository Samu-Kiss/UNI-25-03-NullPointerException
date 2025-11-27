package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias para SubastaService con cobertura completa de ramas. */
class SubastaServiceTest {

  private SubastaService service;
  private IAdquisicionService adquisicionService;
  private IJugadorRepository jugadorRepository;
  private IHUDcontroller hudController;
  private IPropiedadRepository propiedadRepository;

  // Jugadores Mock
  private Jugador jugadorActivo;
  private Jugador jugador2;
  private Jugador jugador3;
  private Propiedad propiedadMock;

  @BeforeEach
  void setUp() throws SQLException {
    // 1. Inicialización de Mocks
    adquisicionService = mock(IAdquisicionService.class);
    jugadorRepository = mock(IJugadorRepository.class);
    hudController = mock(IHUDcontroller.class);
    propiedadRepository = mock(IPropiedadRepository.class);

    // 2. Inicialización de Jugadores Mockeados
    jugadorActivo = mock(Jugador.class); // NumJugador 1
    when(jugadorActivo.getJugadorId()).thenReturn(10);
    when(jugadorActivo.getNumJugador()).thenReturn(1);
    when(jugadorActivo.getPosicion()).thenReturn(10);
    when(jugadorActivo.getNombreJugador()).thenReturn("Jugador1");
    when(jugadorActivo.getDinero()).thenReturn(500);

    jugador2 = mock(Jugador.class); // NumJugador 2
    when(jugador2.getJugadorId()).thenReturn(20);
    when(jugador2.getNumJugador()).thenReturn(2);
    when(jugador2.getNombreJugador()).thenReturn("Jugador2");
    when(jugador2.getDinero()).thenReturn(500);

    jugador3 = mock(Jugador.class); // NumJugador 3
    when(jugador3.getJugadorId()).thenReturn(30);
    when(jugador3.getNumJugador()).thenReturn(3);
    when(jugador3.getNombreJugador()).thenReturn("Jugador3");
    when(jugador3.getDinero()).thenReturn(500);

    // 3. Inicialización de Propiedad Mockeada
    propiedadMock = mock(Propiedad.class);
    when(propiedadMock.getPrecioCompra()).thenReturn(200);

    // 4. Configuración Común del Repositorio (Mapeo de IDs)
    when(jugadorRepository.getPlayerCount()).thenReturn(3);
    when(jugadorRepository.getActivePlayer()).thenReturn(10);
    when(jugadorRepository.getJugadorByID(10)).thenReturn(jugadorActivo);
    when(jugadorRepository.getJugadorByID(20)).thenReturn(jugador2);
    when(jugadorRepository.getJugadorByID(30)).thenReturn(jugador3);
    when(jugadorRepository.getNumJugadorByPlayerId(10)).thenReturn(1);
    when(jugadorRepository.getNumJugadorByPlayerId(20)).thenReturn(2);
    when(jugadorRepository.getNumJugadorByPlayerId(30)).thenReturn(3);
    when(jugadorRepository.getPlayerIdByNumJugador(1)).thenReturn(10);
    when(jugadorRepository.getPlayerIdByNumJugador(2)).thenReturn(20);
    when(jugadorRepository.getPlayerIdByNumJugador(3)).thenReturn(30);

    // 5. Inicialización del Servicio
    service =
        new SubastaService(
            adquisicionService, jugadorRepository, hudController, propiedadRepository);
  }

  // --- Helpers ---
  /** Ejecuta el inicio de subasta con el jugador activo por defecto. */
  private void setupSubastaActiva() throws SQLException {
    when(adquisicionService.prepararSubasta(anyInt())).thenReturn(propiedadMock);
    service.iniciarSubasta();
    clearInvocations(hudController, jugadorRepository, adquisicionService);
  }

  // ---------------------------------------------------------------------------------------------
  // Pruebas para iniciarSubasta()
  // ---------------------------------------------------------------------------------------------

  @Test
  void testIniciarSubasta_Exito_PrecioInicialYAvance() throws SQLException {
    when(adquisicionService.prepararSubasta(jugadorActivo.getPosicion())).thenReturn(propiedadMock);

    boolean resultado = service.iniciarSubasta();

    assertTrue(resultado, "La subasta debe iniciar correctamente");
    // iniciarSubasta llama a avanzarAlSiguienteJugador que llama a setAuctionPlayerName
    // y luego iniciarSubasta llama nuevamente a setAuctionPlayerName
    verify(hudController, times(1)).showAuction(eq("Subasta"), anyString());
    verify(hudController, atLeast(1)).setAuctionPlayerName(anyString());
    verify(jugadorRepository, atLeastOnce()).getPlayerCount();
  }

  @Test
  void testIniciarSubasta_YaActiva() throws SQLException {
    when(adquisicionService.prepararSubasta(anyInt())).thenReturn(propiedadMock);
    boolean primera = service.iniciarSubasta();
    assertTrue(primera);

    clearInvocations(hudController, jugadorRepository, adquisicionService);

    boolean segunda = service.iniciarSubasta();

    assertFalse(segunda, "No debe permitir iniciar subasta si ya está activa");
    verify(adquisicionService, never()).prepararSubasta(anyInt());
  }

  @Test
  void testIniciarSubasta_PropiedadNula() throws SQLException {
    when(adquisicionService.prepararSubasta(anyInt())).thenReturn(null);

    boolean resultado = service.iniciarSubasta();

    assertFalse(resultado, "Debe retornar false si no hay propiedad para subastar");
    verify(hudController, never()).showAuction(any(), any());
  }

  @Test
  void testIniciarSubasta_SQLException() throws SQLException {
    when(jugadorRepository.getActivePlayer()).thenThrow(new SQLException("Error DB"));

    boolean resultado = service.iniciarSubasta();

    assertFalse(resultado, "Debe retornar false ante un error de SQL");
    verify(hudController, never()).showAuction(any(), any());
  }

  @Test
  void testIniciarSubasta_HudControllerNulo() throws SQLException {
    service = new SubastaService(adquisicionService, jugadorRepository, null, propiedadRepository);
    when(adquisicionService.prepararSubasta(anyInt())).thenReturn(propiedadMock);

    boolean resultado = service.iniciarSubasta();

    assertTrue(resultado, "La subasta debe iniciar incluso sin hudController");
  }

  // ---------------------------------------------------------------------------------------------
  // Pruebas para avanzarAlSiguienteJugador()
  // ---------------------------------------------------------------------------------------------

  @Test
  void testAvanzarAlSiguienteJugador_NormalYEnvolvente() throws SQLException {
    setupSubastaActiva();
    clearInvocations(hudController);

    service.avanzarAlSiguienteJugador();

    verify(hudController, times(1)).setAuctionPlayerName(anyString());
    verify(adquisicionService, never()).comprarPropiedadEnSubasta(anyInt(), any(), anyInt());
  }

  @Test
  void testAvanzarAlSiguienteJugador_SQLException() throws SQLException {
    setupSubastaActiva();
    clearInvocations(jugadorRepository);

    when(jugadorRepository.getPlayerCount()).thenThrow(new SQLException("Error DB"));

    assertThrows(SQLException.class, () -> service.avanzarAlSiguienteJugador());
    verify(hudController, never()).setAuctionPlayerName(any());
  }

  @Test
  void testAvanzarAlSiguienteJugador_HudControllerNulo() throws SQLException {
    service = new SubastaService(adquisicionService, jugadorRepository, null, propiedadRepository);
    when(adquisicionService.prepararSubasta(anyInt())).thenReturn(propiedadMock);
    service.iniciarSubasta();

    assertDoesNotThrow(() -> service.avanzarAlSiguienteJugador());
  }

  // ---------------------------------------------------------------------------------------------
  // Pruebas para aumentarPrecio(int delta)
  // ---------------------------------------------------------------------------------------------

  @Test
  void testAumentarPrecio_SubastaInactiva() {
    boolean resultado = service.aumentarPrecio(10);
    assertFalse(resultado, "Debe ser false si la subasta no está activa");
    verifyNoInteractions(jugadorRepository);
  }

  @Test
  void testAumentarPrecio_DeltaInvalido() throws SQLException {
    setupSubastaActiva();

    boolean resultado = service.aumentarPrecio(0);

    assertFalse(resultado, "Debe ser false si delta es <= 0");

    boolean resultadoNegativo = service.aumentarPrecio(-10);

    assertFalse(resultadoNegativo, "Debe ser false si delta es negativo");
  }

  @Test
  void testAumentarPrecio_NoTieneDinero() throws SQLException {
    setupSubastaActiva();

    // Configurar que el jugador actual (2) no tiene suficiente dinero
    when(jugadorRepository.getPlayerIdByNumJugador(2)).thenReturn(20);
    when(jugadorRepository.getJugadorByID(20)).thenReturn(jugador2);
    when(jugador2.getDinero()).thenReturn(50); // Precio actual es 100, intenta 150

    boolean resultado = service.aumentarPrecio(100);

    assertFalse(resultado, "Debe ser false si no tiene dinero suficiente");
    verify(hudController, never()).showAuction(any(), any());
  }

  @Test
  void testAumentarPrecio_Exitoso() throws SQLException {
    setupSubastaActiva();

    when(jugadorRepository.getPlayerIdByNumJugador(2)).thenReturn(20);
    when(jugadorRepository.getJugadorByID(20)).thenReturn(jugador2);
    when(jugador2.getDinero()).thenReturn(500);

    boolean resultado = service.aumentarPrecio(50);

    assertTrue(resultado, "Debe ser true si tiene dinero y delta es válido");
    verify(hudController, times(1)).showAuction(eq("Subasta"), anyString());
    verify(hudController, times(1)).setAuctionPlayerName(anyString());
  }

  @Test
  void testAumentarPrecio_SQLException() throws SQLException {
    setupSubastaActiva();

    when(jugadorRepository.getPlayerIdByNumJugador(anyInt()))
        .thenThrow(new SQLException("Error DB"));

    boolean resultado = service.aumentarPrecio(10);

    assertFalse(resultado, "Debe retornar false ante error de SQL");
    verify(hudController, never()).showAuction(any(), any());
  }

  @Test
  void testAumentarPrecio_HudControllerNulo() throws SQLException {
    service = new SubastaService(adquisicionService, jugadorRepository, null, propiedadRepository);
    when(adquisicionService.prepararSubasta(anyInt())).thenReturn(propiedadMock);
    service.iniciarSubasta();

    when(jugadorRepository.getPlayerIdByNumJugador(2)).thenReturn(20);
    when(jugadorRepository.getJugadorByID(20)).thenReturn(jugador2);
    when(jugador2.getDinero()).thenReturn(500);

    boolean resultado = service.aumentarPrecio(50);

    assertTrue(resultado, "Debe funcionar incluso sin hudController");
  }

  // ---------------------------------------------------------------------------------------------
  // Pruebas para comprarActual()
  // ---------------------------------------------------------------------------------------------

  @Test
  void testComprarActual_SubastaInactiva() {
    boolean resultado = service.comprarActual();
    assertFalse(resultado, "Debe ser false si la subasta no está activa");
    verify(hudController, never()).hideAuction();
  }

  @Test
  void testComprarActual_Exitoso() throws SQLException {
    setupSubastaActiva();

    when(jugadorRepository.getPlayerIdByNumJugador(2)).thenReturn(20);
    when(jugadorRepository.getJugadorByID(20)).thenReturn(jugador2);
    when(adquisicionService.comprarPropiedadEnSubasta(anyInt(), eq(jugador2), anyInt()))
        .thenReturn(true);

    boolean resultado = service.comprarActual();

    assertTrue(resultado, "Debe ser true si la compra fue exitosa");
    verify(adquisicionService, times(1))
        .comprarPropiedadEnSubasta(anyInt(), eq(jugador2), anyInt());
    verify(hudController, times(1)).hideAuction();
    verify(hudController, times(1)).terminarTurno();
  }

  @Test
  void testComprarActual_CompraFallida() throws SQLException {
    setupSubastaActiva();

    when(jugadorRepository.getPlayerIdByNumJugador(2)).thenReturn(20);
    when(jugadorRepository.getJugadorByID(20)).thenReturn(jugador2);
    when(adquisicionService.comprarPropiedadEnSubasta(anyInt(), eq(jugador2), anyInt()))
        .thenReturn(false);

    boolean resultado = service.comprarActual();

    assertFalse(resultado, "Debe ser false si la compra falló");
    verify(hudController, never()).hideAuction();
    verify(hudController, times(1)).terminarTurno(); // Se llama igual después del reset
  }

  @Test
  void testComprarActual_SQLException() throws SQLException {
    setupSubastaActiva();

    when(jugadorRepository.getPlayerIdByNumJugador(anyInt()))
        .thenThrow(new SQLException("Error DB"));

    boolean resultado = service.comprarActual();

    assertFalse(resultado, "Debe retornar false ante error de SQL");
    verify(hudController, never()).hideAuction();
    verify(hudController, never()).terminarTurno();
  }

  @Test
  void testComprarActual_HudControllerNulo() throws SQLException {
    service = new SubastaService(adquisicionService, jugadorRepository, null, propiedadRepository);
    when(adquisicionService.prepararSubasta(anyInt())).thenReturn(propiedadMock);
    service.iniciarSubasta();

    when(jugadorRepository.getPlayerIdByNumJugador(2)).thenReturn(20);
    when(jugadorRepository.getJugadorByID(20)).thenReturn(jugador2);
    when(adquisicionService.comprarPropiedadEnSubasta(anyInt(), any(), anyInt())).thenReturn(true);

    boolean resultado = service.comprarActual();

    assertTrue(resultado, "Debe funcionar incluso sin hudController");
  }

  // ---------------------------------------------------------------------------------------------
  // Pruebas para salirSubasta()
  // ---------------------------------------------------------------------------------------------

  @Test
  void testSalirSubasta_SubastaInactiva() {
    service.salirSubasta();
    verifyNoInteractions(jugadorRepository);
  }

  @Test
  void testSalirSubasta_SQLException() throws SQLException {
    setupSubastaActiva();

    when(jugadorRepository.getPlayerCount()).thenThrow(new SQLException("Error DB"));

    service.salirSubasta();

    verify(hudController, never()).setAuctionPlayerName(any());
    verify(adquisicionService, never()).comprarPropiedadEnSubasta(anyInt(), any(), anyInt());
  }

  @Test
  void testSalirSubasta_HudControllerNulo() throws SQLException {
    service = new SubastaService(adquisicionService, jugadorRepository, null, propiedadRepository);
    when(jugadorRepository.getPlayerCount()).thenReturn(3);
    when(adquisicionService.prepararSubasta(anyInt())).thenReturn(propiedadMock);
    service.iniciarSubasta();

    assertDoesNotThrow(() -> service.salirSubasta());
  }

  // ---------------------------------------------------------------------------------------------
  // Pruebas para getPropiedadRepository() - Cobertura del getter
  // ---------------------------------------------------------------------------------------------

  @Test
  void testGetPropiedadRepository() {
    assertNotNull(service.getPropiedadRepository());
    assertEquals(propiedadRepository, service.getPropiedadRepository());
  }
}
