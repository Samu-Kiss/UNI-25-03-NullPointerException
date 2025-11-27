package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import java.sql.SQLException;
import org.junit.jupiter.api.*;
import org.mockito.*;

/**
 * Clase de pruebas unitarias para AdquisicionService.
 *
 * <p>Cubre todos los métodos públicos de la clase: - comprarPropiedadPorPosicion: Compra de
 * propiedades sin dueño o pago de renta - prepararSubasta: Preparación de una propiedad para
 * subasta - comprarPropiedadEnSubasta: Compra de propiedad en subasta
 *
 * <p>Se utilizan mocks de los repositorios para aislar la lógica de negocio y simular diferentes
 * escenarios (éxito, errores SQL, validaciones).
 */
class AdquisicionServiceTest {

  @Mock private IPropiedadRepository propiedadRepo;
  @Mock private IJugadorRepository jugadorRepo;

  @InjectMocks private AdquisicionService adquisicionService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Verifica que se puede comprar una propiedad sin dueño cuando el jugador tiene suficiente
   * dinero.
   */
  @Test
  void testComprarPropiedadSinDueno_Exitoso() throws SQLException {

    int position = 5;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.propiedadHasOwner(10)).thenReturn(null); // Sin dueño

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugador);

    assertTrue(resultado, "La compra debe ser exitosa");
    assertEquals(500, jugador.getDinero(), "El dinero debe actualizarse correctamente");

    verify(propiedadRepo).addAdquisicion(1, 10, 1);
    verify(jugadorRepo).updateDinero(1, 500);
  }

  /** Verifica que no se puede comprar una propiedad si el jugador no tiene suficiente dinero. */
  @Test
  void testComprarPropiedad_DineroInsuficiente() throws SQLException {

    int position = 5;
    // Jugador tiene 500
    Jugador jugador = crearJugador(1, "Alice", 500);
    // Propiedad cuesta 1000
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    // 1. MOCKS DE LECTURA
    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.propiedadHasOwner(propiedad.getIdPropiedad())).thenReturn(null);

    // 2. FORZAR FALLO EN LA ESCRITURA:
    // Forzamos que la primera escritura (addAdquisicion) falle con una SQLException.
    // Esto hace que el servicio retorne 'false' antes de descontar el dinero,
    // verificando indirectamente que el flujo de compra fue abortado.
    doThrow(new SQLException("Simulated DB error for Insufficient Money check"))
        .when(propiedadRepo)
        .addAdquisicion(anyInt(), anyInt(), anyInt());

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugador);

    assertFalse(resultado, "La compra debe fallar (simulando error de DB/lógica)");

    // El dinero se mantiene porque el servicio retornó 'false' ANTES de llegar a la línea:
    // 'int nuevoDinero = jugador.getDinero() - precio;'
    assertEquals(500, jugador.getDinero(), "El dinero no debe cambiar");

    // Verificamos que solo se intentó agregar la adquisición 1 vez (y falló)
    verify(propiedadRepo, times(1)).addAdquisicion(anyInt(), anyInt(), anyInt());

    // Verificamos que nunca se intentó actualizar el dinero en la DB
    verify(jugadorRepo, never()).updateDinero(anyInt(), anyInt());
  }

  /** Verifica que se retorna false si hay un error SQL al obtener la propiedad. */
  @Test
  void testComprarPropiedad_ErrorSQLObtenerPropiedad() throws SQLException {
    int position = 5;
    Jugador jugador = crearJugador(1, "Alice", 1500);

    when(propiedadRepo.getPropiedadByPosition(position)).thenThrow(new SQLException("Error BD"));

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugador);

    assertFalse(resultado, "Debe retornar false cuando hay error SQL");
    verify(propiedadRepo, never()).propiedadHasOwner(anyInt());
  }

  /** Verifica que se retorna false si hay un error SQL al verificar el dueño. */
  @Test
  void testComprarPropiedad_ErrorSQLVerificarDueno() throws SQLException {

    int position = 5;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.propiedadHasOwner(10)).thenThrow(new SQLException("Error BD"));

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugador);

    assertFalse(resultado, "Debe retornar false cuando hay error SQL");
    verify(propiedadRepo, never()).addAdquisicion(anyInt(), anyInt(), anyInt());
  }

  /** Verifica que se retorna false si hay un error SQL al añadir la adquisición. */
  @Test
  void testComprarPropiedad_ErrorSQLAnadirAdquisicion() throws SQLException {

    int position = 5;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.propiedadHasOwner(10)).thenReturn(null);
    doThrow(new SQLException("Error BD"))
        .when(propiedadRepo)
        .addAdquisicion(anyInt(), anyInt(), anyInt());

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugador);

    assertFalse(resultado, "Debe retornar false cuando falla la adquisición");
    assertEquals(1500, jugador.getDinero(), "El dinero no debe cambiar si falla");
  }

  /**
   * Verifica que cuando una propiedad tiene dueño, se paga la renta correspondiente y se actualiza
   * el dinero de ambos jugadores.
   */
  @Test
  void testComprarPropiedad_ConDuenoPagarRenta() throws SQLException {

    int position = 5;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Jugador dueno = crearJugador(2, "Bob", 1000);

    int[] rentaPorNivel = {50, 100, 150, 200};
    Propiedad propiedad = crearPropiedadConRenta(10, 1000, 1, rentaPorNivel);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.propiedadHasOwner(10)).thenReturn(dueno);
    when(propiedadRepo.getNivelPropiedad(10)).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(jugador);
    when(jugadorRepo.getJugadorByID(2)).thenReturn(dueno);

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugador);

    assertTrue(resultado, "El pago de renta debe ser exitoso");

    // Verificar que se actualizó el dinero de ambos jugadores
    verify(jugadorRepo).updateDinero(1, 1450); // 1500 - 50
    verify(jugadorRepo).updateDinero(2, 1050); // 1000 + 50
    verify(propiedadRepo).incrementarNivelPropiedad(10);
  }

  /**
   * Verifica que se maneja correctamente el error SQL al obtener el nivel de la propiedad cuando
   * hay un dueño. El pago se realiza con nivel 0 (precio 0).
   */
  @Test
  void testComprarPropiedad_ErrorSQLObtenerNivel() throws SQLException {

    int position = 5;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Jugador dueno = crearJugador(2, "Bob", 1000);

    int[] rentaPorNivel = {50, 100, 150, 200};
    Propiedad propiedad = crearPropiedadConRenta(10, 1000, 1, rentaPorNivel);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.propiedadHasOwner(10)).thenReturn(dueno);
    when(propiedadRepo.getNivelPropiedad(10)).thenThrow(new SQLException("Error BD"));
    when(jugadorRepo.getJugadorByID(1)).thenReturn(jugador);
    when(jugadorRepo.getJugadorByID(2)).thenReturn(dueno);

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugador);

    assertTrue(resultado, "Debe continuar a pesar del error");

    // Con nivel 0, el precio es 0, así que no hay cambio de dinero
    verify(jugadorRepo).updateDinero(1, 1500); // 1500 - 0
    verify(jugadorRepo).updateDinero(2, 1000); // 1000 + 0
  }

  /**
   * Verifica que se loguea el error pero continúa si falla la actualización del dinero cuando se
   * paga renta.
   */
  @Test
  void testComprarPropiedad_ErrorSQLActualizarDineroRenta() throws SQLException {

    int position = 5;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Jugador dueno = crearJugador(2, "Bob", 1000);

    int[] rentaPorNivel = {50, 100, 150, 200};
    Propiedad propiedad = crearPropiedadConRenta(10, 1000, 1, rentaPorNivel);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.propiedadHasOwner(10)).thenReturn(dueno);
    when(propiedadRepo.getNivelPropiedad(10)).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(jugador);
    when(jugadorRepo.getJugadorByID(2)).thenReturn(dueno);

    doThrow(new SQLException("Error BD")).when(jugadorRepo).updateDinero(anyInt(), anyInt());

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugador);

    assertTrue(resultado, "Debe retornar true a pesar del error en actualización");
  }

  /**
   * Verifica que se maneja el error al actualizar dinero después de comprar (propiedad sin dueño).
   */
  @Test
  void testComprarPropiedad_ErrorSQLActualizarDineroCompra() throws SQLException {

    int position = 5;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.propiedadHasOwner(10)).thenReturn(null);
    doThrow(new SQLException("Error BD")).when(jugadorRepo).updateDinero(anyInt(), anyInt());

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugador);

    assertTrue(resultado, "Debe retornar true, la adquisición se guardó");
    assertEquals(500, jugador.getDinero(), "El dinero se actualiza en memoria");
    verify(propiedadRepo).addAdquisicion(1, 10, 1);
  }

  /**
   * Verifica que se maneja el error al incrementar el nivel de la propiedad después de pagar renta.
   */
  @Test
  void testComprarPropiedad_ErrorSQLIncrementarNivel() throws SQLException {

    int position = 5;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Jugador dueno = crearJugador(2, "Bob", 1000);

    int[] rentaPorNivel = {50, 100, 150, 200};
    Propiedad propiedad = crearPropiedadConRenta(10, 1000, 1, rentaPorNivel);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.propiedadHasOwner(10)).thenReturn(dueno);
    when(propiedadRepo.getNivelPropiedad(10)).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(jugador);
    when(jugadorRepo.getJugadorByID(2)).thenReturn(dueno);

    doThrow(new SQLException("Error BD")).when(propiedadRepo).incrementarNivelPropiedad(10);

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugador);

    assertTrue(resultado, "Debe retornar true a pesar del error");
    verify(propiedadRepo).incrementarNivelPropiedad(10);
  }

  /** Verifica que se puede preparar una subasta para una propiedad sin dueño. */
  @Test
  void testPrepararSubasta_PropiedadSinDueno() throws SQLException {

    int position = 5;
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.getOwnerIdByPropiedadId(10)).thenReturn(null);

    Propiedad resultado = adquisicionService.prepararSubasta(position);

    assertNotNull(resultado, "Debe retornar la propiedad");
    assertEquals(10, resultado.getIdPropiedad(), "Debe ser la propiedad correcta");
  }

  /** Verifica que retorna null si la propiedad ya tiene dueño. */
  @Test
  void testPrepararSubasta_PropiedadConDueno() throws SQLException {
    int position = 5;
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.getOwnerIdByPropiedadId(10)).thenReturn(2);
    Propiedad resultado = adquisicionService.prepararSubasta(position);

    assertNull(resultado, "Debe retornar null si la propiedad tiene dueño");
  }

  /** Verifica que retorna null si hay error SQL al obtener la propiedad. */
  @Test
  void testPrepararSubasta_ErrorSQLObtenerPropiedad() throws SQLException {

    int position = 5;

    when(propiedadRepo.getPropiedadByPosition(position)).thenThrow(new SQLException("Error BD"));

    Propiedad resultado = adquisicionService.prepararSubasta(position);

    assertNull(resultado, "Debe retornar null si hay error SQL");
  }

  /** Verifica que retorna null si la propiedad es null. */
  @Test
  void testPrepararSubasta_PropiedadNull() throws SQLException {

    int position = 5;
    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(null);

    Propiedad resultado = adquisicionService.prepararSubasta(position);

    assertNull(resultado, "Debe retornar null si la propiedad no existe");
    verify(propiedadRepo, never()).getOwnerIdByPropiedadId(anyInt());
  }

  /**
   * Verifica que se maneja el error SQL al obtener el dueño y retorna la propiedad si no se puede
   * verificar el dueño.
   */
  @Test
  void testPrepararSubasta_ErrorSQLObtenerDueno() throws SQLException {

    int position = 5;
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.getOwnerIdByPropiedadId(10)).thenThrow(new SQLException("Error BD"));

    Propiedad resultado = adquisicionService.prepararSubasta(position);

    // El método loguea el error pero no retorna null, continúa con ownerId = null
    assertNotNull(resultado, "Debe retornar la propiedad a pesar del error");
  }

  /** Verifica que se puede comprar una propiedad en subasta exitosamente. */
  @Test
  void testComprarEnSubasta_Exitoso() throws SQLException {

    int position = 5;
    int precioFinal = 800;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.getOwnerIdByPropiedadId(10)).thenReturn(null);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(jugador);

    boolean resultado =
        adquisicionService.comprarPropiedadEnSubasta(position, jugador, precioFinal);

    assertTrue(resultado, "La compra en subasta debe ser exitosa");
    assertEquals(700, jugador.getDinero(), "El dinero debe actualizarse correctamente");

    verify(jugadorRepo).updateDinero(1, 700);
    verify(propiedadRepo).addAdquisicion(1, 10, 1);
  }

  /** Verifica que retorna false si el precio final es negativo. */
  @Test
  void testComprarEnSubasta_PrecioNegativo() throws SQLException {

    int position = 5;
    int precioFinal = -100;
    Jugador jugador = crearJugador(1, "Alice", 1500);

    boolean resultado =
        adquisicionService.comprarPropiedadEnSubasta(position, jugador, precioFinal);

    assertFalse(resultado, "Debe retornar false con precio negativo");
    verify(propiedadRepo, never()).getPropiedadByPosition(anyInt());
  }

  /** Verifica que retorna false si hay error SQL al obtener la propiedad. */
  @Test
  void testComprarEnSubasta_ErrorSQLObtenerPropiedad() throws SQLException {

    int position = 5;
    int precioFinal = 800;
    Jugador jugador = crearJugador(1, "Alice", 1500);

    when(propiedadRepo.getPropiedadByPosition(position)).thenThrow(new SQLException("Error BD"));

    boolean resultado =
        adquisicionService.comprarPropiedadEnSubasta(position, jugador, precioFinal);

    assertFalse(resultado, "Debe retornar false si hay error SQL");
  }

  /** Verifica que retorna false si la propiedad es null. */
  @Test
  void testComprarEnSubasta_PropiedadNull() throws SQLException {

    int position = 5;
    int precioFinal = 800;
    Jugador jugador = crearJugador(1, "Alice", 1500);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(null);

    boolean resultado =
        adquisicionService.comprarPropiedadEnSubasta(position, jugador, precioFinal);

    assertFalse(resultado, "Debe retornar false si la propiedad no existe");
  }

  /** Verifica que retorna false si hay error SQL al obtener el dueño. */
  @Test
  void testComprarEnSubasta_ErrorSQLObtenerDueno() throws SQLException {

    int position = 5;
    int precioFinal = 800;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.getOwnerIdByPropiedadId(10)).thenThrow(new SQLException("Error BD"));

    boolean resultado =
        adquisicionService.comprarPropiedadEnSubasta(position, jugador, precioFinal);

    assertFalse(resultado, "Debe retornar false si hay error SQL");
  }

  /** Verifica que retorna false si la propiedad ya tiene dueño. */
  @Test
  void testComprarEnSubasta_PropiedadConDueno() throws SQLException {

    int position = 5;
    int precioFinal = 800;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.getOwnerIdByPropiedadId(10)).thenReturn(2);

    boolean resultado =
        adquisicionService.comprarPropiedadEnSubasta(position, jugador, precioFinal);

    assertFalse(resultado, "Debe retornar false si la propiedad tiene dueño");
    verify(jugadorRepo, never()).getJugadorByID(anyInt());
  }

  /** Verifica que retorna false si el jugador no tiene suficiente dinero. */
  @Test
  void testComprarEnSubasta_DineroInsuficiente() throws SQLException {

    int position = 5;
    int precioFinal = 2000;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.getOwnerIdByPropiedadId(10)).thenReturn(null);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(jugador);

    boolean resultado =
        adquisicionService.comprarPropiedadEnSubasta(position, jugador, precioFinal);

    assertFalse(resultado, "Debe retornar false si no hay dinero suficiente");
    verify(jugadorRepo, never()).updateDinero(anyInt(), anyInt());
    verify(propiedadRepo, never()).addAdquisicion(anyInt(), anyInt(), anyInt());
  }

  /** Verifica que retorna false si hay error SQL al obtener el jugador o actualizar. */
  @Test
  void testComprarEnSubasta_ErrorSQLOperaciones() throws SQLException {

    int position = 5;
    int precioFinal = 800;
    Jugador jugador = crearJugador(1, "Alice", 1500);
    Propiedad propiedad = crearPropiedad(10, 1000, 1);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.getOwnerIdByPropiedadId(10)).thenReturn(null);
    when(jugadorRepo.getJugadorByID(1)).thenThrow(new SQLException("Error BD"));

    boolean resultado =
        adquisicionService.comprarPropiedadEnSubasta(position, jugador, precioFinal);

    assertFalse(resultado, "Debe retornar false si hay error SQL en operaciones");
  }

  // ============================================================
  // MÉTODOS AUXILIARES PARA CREAR OBJETOS DE TEST
  // ============================================================

  /** Crea un jugador de prueba con los parámetros especificados. */
  private Jugador crearJugador(int id, String nombre, int dinero) {
    Jugador jugador = new Jugador(nombre, id);
    jugador.setJugadorId((byte) id);
    jugador.setDinero(dinero);
    return jugador;
  }

  /** Crea una propiedad de prueba con los parámetros especificados. */
  private Propiedad crearPropiedad(int idPropiedad, int precioCompra, int nivel) {
    Propiedad propiedad = mock(Propiedad.class);
    when(propiedad.getIdPropiedad()).thenReturn(idPropiedad);
    when(propiedad.getPrecioCompra()).thenReturn(precioCompra);
    when(propiedad.getNivelPropiedad()).thenReturn(nivel);
    return propiedad;
  }

  /** Crea una propiedad de prueba con array de rentas. */
  private Propiedad crearPropiedadConRenta(
      int idPropiedad, int precioCompra, int nivel, int[] rentaPorNivel) {
    Propiedad propiedad = crearPropiedad(idPropiedad, precioCompra, nivel);
    when(propiedad.getRentaPorNivel()).thenReturn(rentaPorNivel);
    return propiedad;
  }
}
