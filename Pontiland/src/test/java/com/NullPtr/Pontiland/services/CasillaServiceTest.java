package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.*;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import com.NullPtr.Pontiland.repository.TarjetaEventoRepository;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias para CasillaService. */
class CasillaServiceTest {

  private CasillaService service;
  private Jugador jugador;
  private IHUDcontroller hudController;
  private DiceService diceService;
  private IPropiedadRepository propiedadRepository;
  private IAdquisicionService adquisicionService;
  private TarjetaEventoRepository tarjetaEventoRepository;
  private IJugadorRepository jugadorRepository;

  // Clase Helper para crear mocks de TarjetaEvento fácilmente
  private TarjetaEvento createMockTarjeta(String nombre, String desc, Accion accion) {
    TarjetaEvento evento = mock(TarjetaEvento.class);
    when(evento.getAccion()).thenReturn(accion);
    when(evento.getNombre()).thenReturn(nombre);
    when(evento.getDescripcion()).thenReturn(desc);
    return evento;
  }

  @BeforeEach
  void setUp() {
    // Mocking de las dependencias
    hudController = mock(IHUDcontroller.class);
    diceService = mock(DiceService.class);
    propiedadRepository = mock(IPropiedadRepository.class);
    adquisicionService = mock(IAdquisicionService.class); // Añadido para las pruebas de compra
    tarjetaEventoRepository = mock(TarjetaEventoRepository.class);
    jugadorRepository = mock(IJugadorRepository.class);

    service =
        new CasillaService(
            hudController,
            diceService,
            propiedadRepository,
            adquisicionService,
            tarjetaEventoRepository,
            jugadorRepository);
    jugador = new Jugador("JugadorTest", 1);
  }

  // --- Pruebas para interaccion() (Manteniendo las pruebas existentes) ---
  @Test
  void testAllSwitchCasesInteraccion() {
    // PARADALIBRE
    Casilla libre = new Casilla(1, "Parada Libre", Tipo.PARADALIBRE);
    service.interaccion(jugador, libre);
    assertFalse(service.getIrACarcel(), "PARADALIBRE no debería activar irACarcel");

    // EVENTO
    Casilla evento = new Casilla(2, "Evento Sorpresa", Tipo.EVENTO);
    service.interaccion(jugador, evento);
    assertFalse(service.getIrACarcel(), "EVENTO no debería activar irACarcel");

    // PROPIEDAD
    Casilla propiedad = new Casilla(3, "Propiedad Central", Tipo.PROPIEDAD);
    service.interaccion(jugador, propiedad);
    assertFalse(service.getIrACarcel(), "PROPIEDAD no debería activar irACarcel");

    // MOVIMIENTO
    Casilla movimiento = new Casilla(4, "Movimiento Especial", Tipo.MOVIMIENTO);
    service.interaccion(jugador, movimiento);
    assertFalse(service.getIrACarcel(), "MOVIMIENTO no debería activar irACarcel");

    // IRALACARCEL
    Casilla carcel = new Casilla(5, "Ir a la cárcel", Tipo.IRALACARCEL);
    service.interaccion(jugador, carcel);
    assertTrue(service.getIrACarcel(), "IRALACARCEL debe activar irACarcel");
  }

  @Test
  void testIrACarcelInitiallyFalse() {
    assertFalse(service.getIrACarcel(), "Por defecto irACarcel debe ser falso");
  }

  // --- Nuevas Pruebas para terminarInteraccion() ---

  @Test
  void testTerminarInteraccion_ParadaLibre() {
    Casilla libre = new Casilla(1, "Parada Libre", Tipo.PARADALIBRE);
    service.terminarInteraccion(jugador, libre);
    // Verifica que se llamó a terminarTurno()
    verify(hudController, times(1)).terminarTurno();
    // Verifica que no se llamó a ninguna otra interacción de HUD relevante
    verify(hudController, never()).hidePropertyCard();
  }

  @Test
  void testTerminarInteraccion_Evento() {
    Casilla evento = new Casilla(2, "Evento Sorpresa", Tipo.EVENTO);
    service.terminarInteraccion(jugador, evento);
    // Verifica que se llamó a terminarTurno()
    verify(hudController, times(1)).terminarTurno();
  }

  @Test
  void testTerminarInteraccion_Movimiento() {
    Casilla movimiento = new Casilla(4, "Movimiento Especial", Tipo.MOVIMIENTO);
    service.terminarInteraccion(jugador, movimiento);
    // Verifica que se llamó a terminarTurno()
    verify(hudController, times(1)).terminarTurno();
  }

  @Test
  void testTerminarInteraccion_IraLaCarcel() {
    Casilla carcel = new Casilla(5, "Ir a la cárcel", Tipo.IRALACARCEL);
    service.terminarInteraccion(jugador, carcel);
    // Verifica que se llamó a terminarTurno()
    verify(hudController, times(1)).terminarTurno();
    // No podemos probar onCarcel(false) directamente ya que es privado, pero la llamada a
    // terminarTurno es suficiente.
  }

  @Test
  void testTerminarInteraccion_Propiedad_PuedeComprar_Exitoso() {
    Casilla propiedad = new Casilla(3, "Propiedad Central", Tipo.PROPIEDAD);

    // Simula que el jugador puede comprar
    when(hudController.getPuedeComprar()).thenReturn(true);
    // Simula que la compra es exitosa
    when(adquisicionService.comprarPropiedadPorPosicion(propiedad.getPosicionTablero(), jugador))
        .thenReturn(true);

    service.terminarInteraccion(jugador, propiedad);

    // Verifica la llamada al servicio de adquisición
    verify(adquisicionService, times(1))
        .comprarPropiedadPorPosicion(propiedad.getPosicionTablero(), jugador);
    // Verifica las interacciones con el HUD
    verify(hudController, times(1)).hidePropertyCard();
    verify(hudController, times(1)).terminarTurno();
  }

  @Test
  void testTerminarInteraccion_Propiedad_NoPuedeComprar() {
    Casilla propiedad = new Casilla(3, "Propiedad Central", Tipo.PROPIEDAD);

    // Simula que el jugador NO puede comprar
    when(hudController.getPuedeComprar()).thenReturn(false);

    service.terminarInteraccion(jugador, propiedad);

    // Verifica que NO se llamó al servicio de adquisición
    verify(adquisicionService, never()).comprarPropiedadPorPosicion(anyInt(), any(Jugador.class));
    // Verifica que NO se ocultó la tarjeta ni se terminó el turno por la compra
    verify(hudController, never()).hidePropertyCard();
    // Verifica que terminarTurno() NO se llamó
    verify(hudController, never()).terminarTurno();
  }

  @Test
  void testTerminarInteraccion_Propiedad_ExcepcionAlComprar() {
    Casilla propiedad = new Casilla(3, "Propiedad Central", Tipo.PROPIEDAD);

    // Simula que el jugador puede comprar
    when(hudController.getPuedeComprar()).thenReturn(true);
    // Simula que la compra lanza una excepción
    doThrow(new RuntimeException("Error de compra simulado"))
        .when(adquisicionService)
        .comprarPropiedadPorPosicion(propiedad.getPosicionTablero(), jugador);

    service.terminarInteraccion(jugador, propiedad);

    // Verifica la llamada al servicio de adquisición (que falla)
    verify(adquisicionService, times(1))
        .comprarPropiedadPorPosicion(propiedad.getPosicionTablero(), jugador);
    // Verifica que NO se ocultó la tarjeta ni se terminó el turno
    verify(hudController, never()).hidePropertyCard();
    verify(hudController, never()).terminarTurno();
  }

  @Test
  void testTerminarInteraccion_NullHudController() {
    // Crea un servicio sin HUDController para probar la rama 'if (hudController == null)'
    CasillaService serviceNoHud =
        new CasillaService(
            null,
            diceService,
            propiedadRepository,
            adquisicionService,
            tarjetaEventoRepository,
            jugadorRepository);
    Casilla libre = new Casilla(1, "Parada Libre", Tipo.PARADALIBRE);

    // Debería ejecutarse sin lanzar excepciones y salir inmediatamente.
    assertDoesNotThrow(() -> serviceNoHud.terminarInteraccion(jugador, libre));
    // Verifica que no hubo ninguna interacción con los mocks (aparte de hudController que es null)
    verifyNoInteractions(adquisicionService);
  }

  @Test
  void testUpdateActivePlayerPropertyTokens_ConPropiedades() throws SQLException {
    // Preparación
    Jugador jugadorActivo = new Jugador("JugadorTest", 1);

    // Crear propiedades simuladas
    Propiedad prop1 = mock(Propiedad.class);
    when(prop1.getIdPropiedad()).thenReturn(10);
    when(prop1.getNivelPropiedad()).thenReturn(1);
    when(prop1.getGrupo()).thenReturn(5);

    Propiedad prop2 = mock(Propiedad.class);
    when(prop2.getIdPropiedad()).thenReturn(20);
    when(prop2.getNivelPropiedad()).thenReturn(3);
    when(prop2.getGrupo()).thenReturn(8);

    List<Propiedad> propiedades = Arrays.asList(prop1, prop2);

    when(propiedadRepository.getPropiedadesByJugador(jugadorActivo.getJugadorId()))
        .thenReturn(propiedades);

    service.updateActivePlayerPropertyTokens(jugadorActivo);

    String[] expectedTokens = {"10|1|5", "20|3|8"};
    verify(hudController, times(1)).updatePropertyTokens(expectedTokens);
  }

  @Test
  void testUpdateActivePlayerPropertyTokens_SinPropiedades() throws SQLException {

    Jugador jugadorActivo = new Jugador("JugadorTest", 1);

    when(propiedadRepository.getPropiedadesByJugador(jugadorActivo.getJugadorId()))
        .thenReturn(Collections.emptyList());

    service.updateActivePlayerPropertyTokens(jugadorActivo);

    String[] expectedTokens = new String[0];
    verify(hudController, times(1)).updatePropertyTokens(expectedTokens);
  }

  @Test
  void testUpdateActivePlayerPropertyTokens_ListaNulaDePropiedades() throws SQLException {

    Jugador jugadorActivo = new Jugador("JugadorTest", 1);

    when(propiedadRepository.getPropiedadesByJugador(jugadorActivo.getJugadorId()))
        .thenReturn(null);

    service.updateActivePlayerPropertyTokens(jugadorActivo);

    String[] expectedTokens = new String[0];
    verify(hudController, times(1)).updatePropertyTokens(expectedTokens);
  }

  @Test
  void testUpdateActivePlayerPropertyTokens_JugadorNulo() {

    service.updateActivePlayerPropertyTokens(null);

    String[] expectedTokens = new String[0];
    verify(hudController, times(1)).updatePropertyTokens(expectedTokens);
    verifyNoInteractions(propiedadRepository); // Asegura que no intentó llamar al repositorio con
    // jugador.getJugadorId()
  }

  @Test
  void testUpdateActivePlayerPropertyTokens_SQLException() throws SQLException {

    Jugador jugadorActivo = new Jugador("JugadorTest", 1);

    doThrow(new SQLException("Error de base de datos simulado"))
        .when(propiedadRepository)
        .getPropiedadesByJugador(jugadorActivo.getJugadorId());

    service.updateActivePlayerPropertyTokens(jugadorActivo);

    String[] expectedTokens = new String[0];
    verify(hudController, times(1)).updatePropertyTokens(expectedTokens);
  }

  @Test
  void testUpdateActivePlayerPropertyTokens_NullHudController() {

    IAdquisicionService adquisicion = mock(IAdquisicionService.class);
    TarjetaEventoRepository eventos = mock(TarjetaEventoRepository.class);

    CasillaService serviceNoHud =
        new CasillaService(
            null, diceService, propiedadRepository, adquisicion, eventos, jugadorRepository);

    Jugador jugadorActivo = new Jugador("JugadorTest", 1);
    assertDoesNotThrow(() -> serviceNoHud.updateActivePlayerPropertyTokens(jugadorActivo));

    verifyNoInteractions(propiedadRepository);
  }

  /**
   * Prueba el flujo exitoso cuando un jugador cae en una casilla PROPIEDAD y se muestra su tarjeta.
   */
  @Test
  void testInteraccion_Propiedad_Exito() throws Exception {
    // Preparación
    Casilla propiedadCasilla = new Casilla(10, "Calle Marina", Tipo.PROPIEDAD);
    Propiedad propiedadMock = mock(Propiedad.class);
    String[] rentasSimuladas = {"10", "50", "150"};

    // Configuración del Mock de Propiedad
    when(propiedadMock.getPrecioCompra()).thenReturn(200);
    when(propiedadMock.getRentasText()).thenReturn(rentasSimuladas);
    when(propiedadMock.getGrupo()).thenReturn(3);

    when(propiedadRepository.getPropiedadByPosition(propiedadCasilla.getPosicionTablero()))
        .thenReturn(propiedadMock);

    service.interaccion(jugador, propiedadCasilla);

    verify(propiedadRepository, times(1)).getPropiedadByPosition(10);

    verify(hudController, times(1))
        .showPropertyCard(
            eq("Calle Marina"), // name
            eq("200"), // priceText (String.valueOf(200))
            eq(rentasSimuladas), // rentsText
            eq(3) // groupIndex
            );

    verify(diceService, times(1)).enableInteract(false);

    verify(hudController, times(1)).setPuedeComprar(false);
  }

  /** Prueba el flujo cuando el repositorio lanza una excepción (Error de lectura). */
  @Test
  void testInteraccion_Propiedad_ErrorAlLeerRepositorio() throws Exception {

    Casilla propiedadCasilla = new Casilla(10, "Calle Marina", Tipo.PROPIEDAD);

    doThrow(new RuntimeException("Error de BD simulado"))
        .when(propiedadRepository)
        .getPropiedadByPosition(propiedadCasilla.getPosicionTablero());

    service.interaccion(jugador, propiedadCasilla);

    verify(propiedadRepository, times(1)).getPropiedadByPosition(10);

    verify(hudController, never()).showPropertyCard(any(), any(), any(), anyInt());

    verify(diceService, never()).enableInteract(anyBoolean());

    verify(hudController, never()).setPuedeComprar(anyBoolean());
  }

  /** Prueba la lógica cuando el hudController es null (rama de cobertura inicial). */
  @Test
  void testInteraccion_Propiedad_NullHudController() {
    IAdquisicionService adquisicion = mock(IAdquisicionService.class);
    TarjetaEventoRepository eventos = mock(TarjetaEventoRepository.class);
    // Usamos el mock de DiceService y PropertyRepository existentes
    CasillaService serviceNoHud =
        new CasillaService(
            null, diceService, propiedadRepository, adquisicion, eventos, jugadorRepository);

    Casilla propiedadCasilla = new Casilla(10, "Calle Marina", Tipo.PROPIEDAD);

    assertDoesNotThrow(() -> serviceNoHud.interaccion(jugador, propiedadCasilla));

    // Verificación: Aseguramos que la ejecución fue suave y que los servicios posteriores no fueron
    // llamados.
    verifyNoInteractions(diceService);
  }

  @Test
  void testInteraccion_Evento_AccionGANA_200() throws SQLException {
    // Caso GANA_200
    TarjetaEvento evento = createMockTarjeta("Big Prize", "Ganas 200", Accion.GANA_200);
    when(tarjetaEventoRepository.getRandomTarjetaEvento()).thenReturn(evento);
    service.interaccion(jugador, new Casilla(2, "E", Tipo.EVENTO));

    verify(hudController, times(1)).showGoodEvent(anyString(), anyString());
    verify(jugadorRepository, times(1))
        .updateDinero(jugador.getJugadorId(), jugador.getDinero() + 200);
  }

  @Test
  void testInteraccion_Evento_PROPIEDAD_NIVEL_PLUS_1_Exito() throws SQLException {
    // Caso PROPIEDAD_NIVEL_PLUS_1 (Bad Event, llama a mejorarPropiedad(j, 1))
    TarjetaEvento evento = createMockTarjeta("Mejora", "Nivel +1", Accion.PROPIEDAD_NIVEL_PLUS_1);
    Propiedad prop = mock(Propiedad.class);
    when(prop.getIdPropiedad()).thenReturn(100);
    when(prop.getNivelPropiedad()).thenReturn(3); // Nivel inicial 3

    when(tarjetaEventoRepository.getRandomTarjetaEvento()).thenReturn(evento);
    when(propiedadRepository.getPropiedadesByJugador(jugador.getJugadorId()))
        .thenReturn(List.of(prop));

    service.interaccion(jugador, new Casilla(2, "E", Tipo.EVENTO));

    verify(hudController, times(1)).showBadEvent(anyString(), anyString());
    // Verifica mejora: 3 + 1 = 4
    verify(propiedadRepository, times(1)).updateAdquisicionNivel(100, jugador.getJugadorId(), 4);
  }

  @Test
  void testInteraccion_Evento_IR_A_LA_CARCEL() {
    // Caso IR_A_LA_CARCEL
    TarjetaEvento evento = createMockTarjeta("Carcel", "Go to jail", Accion.IR_A_LA_CARCEL);
    when(tarjetaEventoRepository.getRandomTarjetaEvento()).thenReturn(evento);
    service.interaccion(jugador, new Casilla(2, "E", Tipo.EVENTO));

    verify(hudController, times(1)).showBadEvent(anyString(), anyString());
    assertTrue(service.getIrACarcel(), "Debe activar irACarcel");
  }

  @Test
  void testInteraccion_Evento_AccionNoGana_NoModificaDinero() throws SQLException {
    // Caso de una Acción que no modifica dinero (p. ej., IR_A_LA_CARCEL)
    TarjetaEvento evento =
        createMockTarjeta("Ir a la Cárcel", "Tienes multa", Accion.IR_A_LA_CARCEL);

    when(tarjetaEventoRepository.getRandomTarjetaEvento()).thenReturn(evento);
    service.interaccion(jugador, new Casilla(2, "E", Tipo.EVENTO));

    // 1. Verifica la interacción con el HUD (Debe ser Bad Event)
    verify(hudController, times(1)).showBadEvent(eq("Ir a la Cárcel"), eq("Tienes multa"));

    // 2. Verifica la acción específica (Se activa la bandera irACarcel)
    assertTrue(service.getIrACarcel(), "La acción debe activar la bandera irACarcel.");

    // 3. Verifica que NO se llamó a ninguna acción de dinero (cubriendo el default/acciones sin DB)
    verify(jugadorRepository, never()).updateDinero(anyInt(), anyInt());

    // 4. Verifica que se deshabilitó la interacción
    verify(diceService, times(1)).enableInteract(false);
  }

  @Test
  void testInteraccion_Evento_GANA_100_ConExcepcion_ManejoError() throws SQLException {
    // Caso de prueba para cubrir el bloque 'catch (SQLException e)'
    TarjetaEvento evento = createMockTarjeta("Error", "Gana 100", Accion.GANA_100);
    when(tarjetaEventoRepository.getRandomTarjetaEvento()).thenReturn(evento);

    // Simula que la llamada a DB lanza SQLException
    doThrow(new SQLException("Error de DB"))
        .when(jugadorRepository)
        .updateDinero(anyInt(), anyInt());

    // El método no debe lanzar la excepción, solo loggearla.
    assertDoesNotThrow(() -> service.interaccion(jugador, new Casilla(2, "E", Tipo.EVENTO)));

    // Verifica que el flujo de HUD y deshabilitar dados se ejecutó
    verify(hudController, times(1)).showGoodEvent(anyString(), anyString());
    verify(diceService, times(1)).enableInteract(false);
  }

  @Test
  void testInteraccion_Evento_TarjetaNula() throws SQLException {
    Casilla eventoCasilla = new Casilla(10, "Evento", Tipo.EVENTO);
    // Simula que el repositorio retorna null (cubre 'if (evento != null)')
    when(tarjetaEventoRepository.getRandomTarjetaEvento()).thenReturn(null);

    service.interaccion(jugador, eventoCasilla);

    // Debe retornar inmediatamente, sin llamar a ninguna interacción del HUD o DiceService
    verify(hudController, never()).showGoodEvent(anyString(), anyString());
    verify(hudController, never()).showBadEvent(anyString(), anyString());
    verify(diceService, never()).enableInteract(anyBoolean());
  }

  @Test
  void testInteraccion_Evento_PIERDE_50_SinPropiedades_ListaVacia() throws SQLException {
    // Cubre la rama 'if (propiedades == null || propiedades.isEmpty())' en pierde50PorPropiedad
    TarjetaEvento evento =
        createMockTarjeta("Impuesto", "Pagas 50/prop", Accion.PIERDE_50_POR_PROPIEDAD);

    when(tarjetaEventoRepository.getRandomTarjetaEvento()).thenReturn(evento);
    when(propiedadRepository.getPropiedadesByJugador(jugador.getJugadorId()))
        .thenReturn(Collections.emptyList());

    service.interaccion(jugador, new Casilla(2, "E", Tipo.EVENTO));

    verify(jugadorRepository, never()).updateDinero(anyInt(), anyInt());
  }

  @Test
  void testInteraccion_Evento_PROPIEDAD_A_NIVEL_5_SinPropiedades_ListaNula() throws SQLException {
    // Cubre la rama 'if (propiedades == null || propiedades.isEmpty())' en cambiarNivelPropiedad
    TarjetaEvento evento = createMockTarjeta("Nivel Fijo", "Nivel 5", Accion.PROPIEDAD_A_NIVEL_5);

    when(tarjetaEventoRepository.getRandomTarjetaEvento()).thenReturn(evento);
    when(propiedadRepository.getPropiedadesByJugador(jugador.getJugadorId())).thenReturn(null);

    service.interaccion(jugador, new Casilla(2, "E", Tipo.EVENTO));

    verify(propiedadRepository, never()).updateAdquisicionNivel(anyInt(), anyInt(), anyInt());
  }

  @Test
  void testInteraccion_Evento_PROPIEDAD_NIVEL_PLUS_1_SinPropiedades_ListaNula()
      throws SQLException {
    // Cubre la rama 'if (propiedades == null || propiedades.isEmpty())' en mejorarPropiedad
    TarjetaEvento evento = createMockTarjeta("Mejora", "Nivel +1", Accion.PROPIEDAD_NIVEL_PLUS_1);

    when(tarjetaEventoRepository.getRandomTarjetaEvento()).thenReturn(evento);
    when(propiedadRepository.getPropiedadesByJugador(jugador.getJugadorId())).thenReturn(null);

    service.interaccion(jugador, new Casilla(2, "E", Tipo.EVENTO));

    verify(propiedadRepository, never()).updateAdquisicionNivel(anyInt(), anyInt(), anyInt());
  }
}
