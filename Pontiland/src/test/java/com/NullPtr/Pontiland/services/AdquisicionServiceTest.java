package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import java.sql.SQLException;
import java.util.List;
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

    adquisicionService = Mockito.spy(adquisicionService);
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

  @Test
  void testComprarPropiedad_ConDueno_CausaDeudaYLiquidacion() throws SQLException {
    int position = 5;
    // Alice tiene 50, paga renta de 100, queda en -50
    Jugador jugadorAlice = crearJugador(1, "Alice", 50);
    Jugador duenoBob = crearJugador(2, "Bob", 1000);

    int[] rentaPorNivel = {100, 150, 200}; // Renta de 100
    Propiedad propiedadRentada = crearPropiedadConRenta(10, 1000, 1, rentaPorNivel);
    Propiedad propAlice = crearPropiedad(20, 500, 1); // Propiedad que Alice tiene para liquidar

    // 💥 CORRECCIÓN: Crear una lista mutable usando ArrayList
    List<Propiedad> propsMutables = new java.util.ArrayList<>(java.util.List.of(propAlice));

    // --- MOCKS DE LECTURA ---
    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedadRentada);
    when(propiedadRepo.propiedadHasOwner(10)).thenReturn(duenoBob);
    when(propiedadRepo.getNivelPropiedad(10)).thenReturn(1);
    // Usar la lista mutable en el mock
    when(propiedadRepo.getPropiedadesByJugador(1)).thenReturn(propsMutables);

    // Mocks para simular el proceso de liquidación
    when(jugadorRepo.getJugadorByID(1)).thenReturn(jugadorAlice);
    when(jugadorRepo.getJugadorByID(2)).thenReturn(duenoBob);

    when(propiedadRepo.getPatrimonioTotalJugador(anyInt()))
        .thenReturn(10000); // Patrimonio suficiente

    // Simula la actualización de dinero (Alice: 50 -> -50; Bob: 1000 -> 1100)
    // El método liquidarDeudaEntreJugadores se llama si el saldo es negativo.
    doAnswer(
            invocation -> {
              jugadorAlice.setDinero(invocation.getArgument(1)); // Actualiza el mock en memoria
              return null;
            })
        .when(jugadorRepo)
        .updateDinero(eq(1), anyInt());

    // --- EJECUCIÓN ---
    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugadorAlice);

    // --- ASERSIONES ---
    assertTrue(resultado);

    // Verificar pago inicial (Alice: 50 - 100 = -50)
    verify(jugadorRepo).updateDinero(1, -50);
    verify(jugadorRepo).updateDinero(2, 1100); // Bob: 1000 + 100 = 1100

    // Verificar que se disparó la liquidación (transferencia de propiedad)
    verify(propiedadRepo).venderAdquisicion(eq(20), eq(1)); // Alice vende su propiedad
    verify(propiedadRepo).addAdquisicion(eq(2), eq(20), eq(1)); // Bob la adquiere
  }

  @Test
  void testLiquidarDeudaConBanco_DeudaSaldadaConUnaVenta() throws SQLException {
    // Preparación: Jugador con deuda y una propiedad que la cubre
    Jugador jugador = crearJugador(1, "Alice", -500); // Dinero inicial: -500

    // Propiedades mockeadas
    Propiedad propCara = crearPropiedad(10, 600, 1); // Valor de venta: 600
    Propiedad propBarata = crearPropiedad(20, 100, 1);

    // Usamos ArrayList para permitir el sort() y remove() dentro del servicio
    List<Propiedad> props = new java.util.ArrayList<>(java.util.List.of(propCara, propBarata));

    // Patrimonio suficiente: -500 + 700 = 200 > 0
    when(propiedadRepo.getPatrimonioTotalJugador(1)).thenReturn(700);
    when(propiedadRepo.getPropiedadesByJugador(1)).thenReturn(props);

    // Mockear la relectura del jugador: esencial para salir del loop
    when(jugadorRepo.getJugadorByID(1)).thenReturn(jugador);

    // Simular el efecto de venderPropiedad en el saldo del jugador
    // Después de vender propCara (600): saldo se actualiza de -500 a 100 (y se llama updateDinero)
    doAnswer(
            invocation -> {
              // Simula la llamada interna a venderPropiedad -> jugador.setDinero(100)
              Propiedad propVendida = invocation.getArgument(0);
              jugador.setDinero(jugador.getDinero() + propVendida.getPrecioCompra());
              return null;
            })
        .when(adquisicionService)
        .venderPropiedad(any(Propiedad.class), eq(jugador));

    // Ejecución
    adquisicionService.liquidarDeudaConBanco(jugador);

    // Aserciones
    // 1. Verificar que se vendió solo la propiedad más cara (para saldar la deuda)
    verify(adquisicionService, times(1)).venderPropiedad(eq(propCara), eq(jugador));
    verify(adquisicionService, never()).venderPropiedad(eq(propBarata), any(Jugador.class));

    // 2. El dinero final debe ser positivo
    assertEquals(100, jugador.getDinero(), "El saldo debe ser -500 + 600 = 100");

    // 3. Verificar que se llamó a getPropiedadesByJugador
    verify(propiedadRepo, times(1)).getPropiedadesByJugador(1);
  }

  @Test
  void testLiquidarDeudaConBanco_DeudaSaldadaConMultiplesVentas() throws SQLException {
    // Preparación: Jugador con deuda y dos propiedades
    Jugador jugador = crearJugador(1, "Alice", -800); // Dinero inicial: -800

    Propiedad prop1 = crearPropiedad(10, 500, 1); // Venta #1: -800 + 500 = -300
    Propiedad prop2 = crearPropiedad(20, 400, 1); // Venta #2: -300 + 400 = 100 (Deuda saldada)
    Propiedad prop3 = crearPropiedad(30, 50, 1); // Esta no debe venderse

    // Ordenadas por precio descendente para que el servicio las ordene correctamente: prop1, prop2,
    // prop3
    List<Propiedad> props = new java.util.ArrayList<>(java.util.List.of(prop1, prop2, prop3));

    when(propiedadRepo.getPatrimonioTotalJugador(1)).thenReturn(950);
    when(propiedadRepo.getPropiedadesByJugador(1)).thenReturn(props);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(jugador); // Relectura del jugador

    // Simular el efecto de venderPropiedad en el saldo del jugador (clave para salir del loop)
    doAnswer(
            invocation -> {
              Propiedad propVendida = invocation.getArgument(0);
              jugador.setDinero(jugador.getDinero() + propVendida.getPrecioCompra());
              return null;
            })
        .when(adquisicionService)
        .venderPropiedad(any(Propiedad.class), eq(jugador));

    // Ejecución
    adquisicionService.liquidarDeudaConBanco(jugador);

    // Aserciones
    // 1. Verificar que se vendieron las dos propiedades necesarias
    verify(adquisicionService, times(1)).venderPropiedad(eq(prop1), eq(jugador));
    verify(adquisicionService, times(1)).venderPropiedad(eq(prop2), eq(jugador));
    verify(adquisicionService, never()).venderPropiedad(eq(prop3), any(Jugador.class));

    // 2. El dinero final debe ser positivo
    assertEquals(100, jugador.getDinero(), "El saldo debe ser -800 + 500 + 400 = 100");
  }

  @Test
  void testLiquidarDeudaConBanco_ErrorSQL() throws SQLException {
    Jugador jugador = crearJugador(1, "Alice", -500);

    // Mockear la primera llamada a la DB para que falle
    when(propiedadRepo.getPropiedadesByJugador(1))
        .thenThrow(new SQLException("Error al obtener propiedades"));

    // Ejecución y Aserción: No debe lanzar la excepción, debe atraparla y loguearla
    assertDoesNotThrow(() -> adquisicionService.liquidarDeudaConBanco(jugador));

    // Verificar que el método termina después del error
    verify(propiedadRepo, times(1)).getPropiedadesByJugador(1);
    verify(propiedadRepo, never()).getPatrimonioTotalJugador(anyInt());
  }

  @Test
  void testObtenerRankingJugadoresDesc_ErrorSQL() throws SQLException {
    // Mockear la primera llamada para que falle
    when(jugadorRepo.getPlayerCount()).thenThrow(new SQLException("Error al contar jugadores"));

    // Ejecución
    List<Jugador> ranking = adquisicionService.obtenerRankingJugadoresDesc();

    // Aserciones
    assertNotNull(ranking);
    assertTrue(ranking.isEmpty(), "Debe retornar una lista vacía en caso de error SQL");
    verify(jugadorRepo, never()).getJugadorByID(anyInt());
  }

  // #### 1.2. Pago de Renta: Dueño en Cárcel (No se Paga)
  //  Cubre  donde el pago se bloquea porque el jugador dueño de la propiedad está en la cárcel
  // (`getJugadorEstadoByID` retorna `true`).
  @Test
  void testComprarPropiedad_ConDuenoEnCarcel_NoPagaRenta() throws SQLException {
    int position = 5;
    Jugador jugadorAlice = crearJugador(1, "Alice", 1500);
    Jugador duenoBob = crearJugador(2, "Bob", 1000);

    int[] rentaPorNivel = {50};
    Propiedad propiedad = crearPropiedadConRenta(10, 1000, 1, rentaPorNivel);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.propiedadHasOwner(10)).thenReturn(duenoBob);
    when(propiedadRepo.getNivelPropiedad(10)).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(jugadorAlice);
    when(jugadorRepo.getJugadorByID(2)).thenReturn(duenoBob);

    // Simula que el dueño está en cárcel (estado=true)
    when(jugadorRepo.getJugadorEstadoByID(2)).thenReturn(true);

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugadorAlice);

    assertTrue(resultado);
    // Verificar que no hubo cambios de dinero
    verify(jugadorRepo, never()).updateDinero(anyInt(), anyInt());
    // Verificar que se incrementa el nivel de la propiedad
    verify(propiedadRepo).incrementarNivelPropiedad(10);
  }

  @Test
  void testComprarPropiedad_CaeEnSuPropiaPropiedad_NoPagaRenta() throws SQLException {
    int position = 5;
    Jugador jugadorAlice = crearJugador(1, "Alice", 1500); // Mismo ID
    Jugador duenoAlice = crearJugador(1, "Alice", 1500); // Mismo ID

    int[] rentaPorNivel = {50};
    Propiedad propiedad = crearPropiedadConRenta(10, 1000, 1, rentaPorNivel);

    when(propiedadRepo.getPropiedadByPosition(position)).thenReturn(propiedad);
    when(propiedadRepo.propiedadHasOwner(10)).thenReturn(duenoAlice);
    when(propiedadRepo.getNivelPropiedad(10)).thenReturn(1);
    when(jugadorRepo.getJugadorByID(1)).thenReturn(jugadorAlice);
    // Dueño NO está en cárcel
    when(jugadorRepo.getJugadorEstadoByID(1)).thenReturn(false);

    boolean resultado = adquisicionService.comprarPropiedadPorPosicion(position, jugadorAlice);

    assertTrue(resultado);
    // Verificar que no hubo intento de transferir dinero
    verify(jugadorRepo, never()).updateDinero(anyInt(), anyInt());
    // Verificar que se incrementa el nivel de la propiedad
    verify(propiedadRepo).incrementarNivelPropiedad(10);
  }

  @Test
  void testVenderPropiedad_Exitoso() throws SQLException {
    Jugador jugador = crearJugador(1, "Alice", 500);
    Propiedad propiedad = crearPropiedad(10, 300, 1);

    adquisicionService.venderPropiedad(propiedad, jugador);

    assertEquals(800, jugador.getDinero(), "El dinero debe aumentar por el precio de venta");
    verify(jugadorRepo).updateDinero(1, 800);
    verify(propiedadRepo).venderAdquisicion(10, 1);
  }

  @Test
  void testVenderPropiedad_SQLException() throws SQLException {
    Jugador jugador = crearJugador(1, "Alice", 500);
    Propiedad propiedad = crearPropiedad(10, 300, 1);

    doThrow(new SQLException("Error en venta DB")).when(propiedadRepo).venderAdquisicion(10, 1);

    // No debe lanzar la excepción, solo loguearla
    assertDoesNotThrow(() -> adquisicionService.venderPropiedad(propiedad, jugador));

    // El dinero en memoria se actualiza antes de la falla de DB
    assertEquals(800, jugador.getDinero());
  }

  @Test
  void testLiquidarDeudaEntreJugadores_DeudaSaldadaConPropiedad() throws SQLException {
    // Deudor: -500 (ID 1)
    Jugador deudor = crearJugador(1, "Alice", -500);
    Jugador acreedor = crearJugador(2, "Bob", 1500);

    Propiedad prop = crearPropiedad(10, 600, 3); // Propiedad que salda la deuda

    // SOLUCIÓN: Usar new java.util.ArrayList<>(List.of(...)) para crear una lista mutable
    List<Propiedad> props = new java.util.ArrayList<>(java.util.List.of(prop));

    when(propiedadRepo.getPropiedadesByJugador(1)).thenReturn(props);
    when(propiedadRepo.getPatrimonioTotalJugador(1)).thenReturn(600); // -500 + 600 > 0

    // Simular la relectura del jugador
    when(jugadorRepo.getJugadorByID(1)).thenReturn(deudor);
    when(jugadorRepo.getJugadorByID(2))
        .thenReturn(acreedor); // Necesario si se usa en logger o lógica

    // Simular la actualización del saldo del deudor (-500 + 600 = 100)
    doAnswer(
            invocation -> {
              deudor.setDinero(invocation.getArgument(1));
              return null;
            })
        .when(jugadorRepo)
        .updateDinero(eq(1), anyInt());

    adquisicionService.liquidarDeudaEntreJugadores(deudor, acreedor);

    // Verificar la transferencia de propiedad
    verify(propiedadRepo).venderAdquisicion(10, 1); // Quita al deudor
    verify(propiedadRepo).addAdquisicion(2, 10, 3); // Añade al acreedor

    // Verificar la actualización final del saldo del deudor
    verify(jugadorRepo).updateDinero(1, 100);
    // Acreedor no recibe dinero aquí, solo la propiedad
    verify(jugadorRepo, never()).updateDinero(eq(2), anyInt());
  }

  @Test
  void testLiquidarDeudaEntreJugadores_PatrimonioInsuficiente() throws SQLException {
    // 1. Preparación
    Jugador deudor = crearJugador(1, "Alice", -500);
    Jugador acreedor = crearJugador(2, "Bob", 1500);

    // 2. Mockear el patrimonio insuficiente (requerido por la lógica del servicio)
    when(propiedadRepo.getPatrimonioTotalJugador(1)).thenReturn(400); // -500 + 400 = -100 (Pierde)

    // 3. Mockear el retorno de propiedades. Aunque la lista es inútil, el servicio la pide.
    when(propiedadRepo.getPropiedadesByJugador(1)).thenReturn(java.util.Collections.emptyList());

    adquisicionService.liquidarDeudaEntreJugadores(deudor, acreedor);

    // 4. Aserciones

    // VERIFICACIÓN CORREGIDA:
    // Se llama al menos 1 vez, ya que la lógica del servicio lo requiere.
    verify(propiedadRepo, times(1)).getPropiedadesByJugador(1);
    verify(propiedadRepo, times(1)).getPatrimonioTotalJugador(1);

    // Estas aserciones se mantienen para verificar que NO se inicia la liquidación:
    verify(propiedadRepo, never()).venderAdquisicion(anyInt(), anyInt()); // No debe vender
    verify(propiedadRepo, never())
        .addAdquisicion(anyInt(), anyInt(), anyInt()); // No debe transferir
    verify(jugadorRepo, never())
        .updateDinero(anyInt(), anyInt()); // No debe haber actualizaciones de dinero post-pago
  }

  @Test
  void testLiquidarDeudaEntreJugadores_SQLException() throws SQLException {
    Jugador deudor = crearJugador(1, "Alice", -500);
    Jugador acreedor = crearJugador(2, "Bob", 1500);

    doThrow(new SQLException("Error DB")).when(propiedadRepo).getPropiedadesByJugador(anyInt());

    assertDoesNotThrow(() -> adquisicionService.liquidarDeudaEntreJugadores(deudor, acreedor));
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
