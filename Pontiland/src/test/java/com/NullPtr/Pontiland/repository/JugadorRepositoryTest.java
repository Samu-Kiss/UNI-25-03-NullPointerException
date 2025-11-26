package com.NullPtr.Pontiland.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.NullPtr.Pontiland.entities.Ficha;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.services.DataService;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.*;

/**
 * Clase de prueba para JugadorRepository. Configura una base de datos H2 en memoria para probar las
 * operaciones CRUD y transacciones relacionadas con la entidad Jugador.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JugadorRepositoryTest {

  private static DataService dataService;
  private JugadorRepository repository;
  private long partidaID = 1L;

  /** Inicializa el DataService una única vez para toda la suite de pruebas. */
  @BeforeAll
  void initAll() {
    dataService = new DataService("jdbc:h2:mem:testJugador;DB_CLOSE_DELAY=-1");
  }

  /**
   * Configura el entorno de prueba antes de cada test: 1. Crea la base de datos con el esquema
   * necesario. 2. Inicializa el repositorio. 3. Inserta datos base (Partida, Iconos, Tipos de
   * Casilla, Casillas, Jugadores 1 y 2). 4. Ajusta la secuencia de auto-incremento de JugadorID
   * para evitar colisiones.
   */
  @BeforeEach
  void setUp() throws SQLException {
    dataService.newDataBase();
    repository = new JugadorRepository(dataService);
    repository.setPartidaID(partidaID);

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {

      // Limpiar tablas para asegurar un estado limpio.
      // La tabla JugadorActivo es limpiada para el test de getActivePlayer = -1.
      stmt.execute("DELETE FROM JugadorActivo");
      stmt.execute("DELETE FROM Jugador");
      stmt.execute("DELETE FROM Partida");
      stmt.execute("DELETE FROM Casilla");
      stmt.execute("DELETE FROM TipoCasilla");
      stmt.execute("DELETE FROM Icono");

      // Insertar partida de prueba
      stmt.execute("INSERT INTO Partida(PartidaID, NumeroJugadores) VALUES(" + partidaID + ", 2)");

      // Insertar iconos de prueba (IconoID 1, 2 y 3)
      stmt.execute("INSERT INTO Icono(IconoID, IconoNombre) VALUES (1, 'Ignacito')");
      stmt.execute("INSERT INTO Icono(IconoID, IconoNombre) VALUES (2, 'Balon')");
      stmt.execute("INSERT INTO Icono(IconoID, IconoNombre) VALUES (3, 'Tren')");

      // Insertar tipos de casilla (necesario para las casillas)
      stmt.execute("INSERT INTO TipoCasilla(TipoID, TipoNombre) VALUES (1, 'ParadaLibre')");
      stmt.execute("INSERT INTO TipoCasilla(TipoID, TipoNombre) VALUES (3, 'Propiedad')");

      // Insertar casillas necesarias (Salida, Cárcel, Posición 5)
      stmt.execute(
          "INSERT INTO Casilla(PosicionTablero, NombreCasilla, TipoCasilla) VALUES (1, 'El Tunel (Salida)', 1)");
      stmt.execute(
          "INSERT INTO Casilla(PosicionTablero, NombreCasilla, TipoCasilla) VALUES (11, 'Carcel', 1)"); // Posición de la Cárcel
      stmt.execute(
          "INSERT INTO Casilla(PosicionTablero, NombreCasilla, TipoCasilla) VALUES (5, 'CasillaGenerica', 3)");

      // Insertar jugador 1 (usando JugadorID=1, NumJugador=1)
      stmt.execute(
          "INSERT INTO Jugador(JugadorID, NumJugador, NombreJugador, IconoID, Posicion, Dinero, Encarcelado, Partida) "
              + "VALUES (1, 1, 'Jugador1', 1, 1, 1500, FALSE, "
              + partidaID
              + ")");

      // Insertar jugador 2 (usando JugadorID=2, NumJugador=2)
      stmt.execute(
          "INSERT INTO Jugador(JugadorID, NumJugador, NombreJugador, IconoID, Posicion, Dinero, Encarcelado, Partida) "
              + "VALUES (2, 2, 'Jugador2', 2, 1, 1500, FALSE, "
              + partidaID
              + ")");

      // FIX: Reiniciar la secuencia de auto-incremento de JugadorID a 3.
      stmt.execute("ALTER TABLE Jugador ALTER COLUMN JugadorID RESTART WITH 3");
    }
  }

  /** Elimina la base de datos en memoria después de cada test para liberar recursos. */
  @AfterEach
  void tearDown() {
    dataService.deleteDataBase();
  }

  // --- TESTS DE COBERTURA DE EXCEPCIONES (RuntimeExceptions en JugadorRepository) ---

  /**
   * Prueba que el método newPlayer lance una RuntimeException al violar una restricción FK. Cubre
   * el bloque 'catch' de newPlayer.
   */
  @Test
  void testNewPlayer_ThrowsExceptionOnForeignKeyViolation() {
    // Crear un objeto Jugador e intentar usar un IconoID (99) que no existe.
    Jugador jugador = new Jugador(3, 3, "Duplicado", 3, 1, false, 1500, partidaID);
    int nonExistentIconoID = 99; // Iconos válidos son 1, 2, 3.

    // Se espera una RuntimeException debido a la violación de clave foránea (FK).
    assertThrows(
        RuntimeException.class,
        () -> repository.newPlayer(jugador, nonExistentIconoID),
        "Debe lanzar RuntimeException por violación de clave foránea (IconoID=99), cubriendo el catch block de newPlayer.");
  }

  /**
   * Prueba que el método newActivePlayer lance una RuntimeException al violar una restricción FK.
   * Cubre el bloque 'catch' de newActivePlayer.
   */
  @Test
  void testNewActivePlayer_ThrowsExceptionOnForeignKeyViolation() {
    // Intentar establecer como activo un JugadorID (99) que no existe.
    int nonExistentJugadorID = 99;

    // Se espera una RuntimeException debido a la violación de FK.
    assertThrows(
        RuntimeException.class,
        () -> repository.newActivePlayer(nonExistentJugadorID),
        "Debe lanzar RuntimeException por violación de clave foránea (JugadorID=99), cubriendo el catch block de newActivePlayer.");
  }

  /**
   * Prueba que el método changeActivePlayer lance una RuntimeException al violar una restricción
   * FK. Cubre el bloque 'catch' de changeActivePlayer.
   */
  @Test
  void testChangeActivePlayer_ThrowsExceptionOnForeignKeyViolation() throws SQLException {
    // Inicializar JugadorActivo (necesario para que 'change' se ejecute como UPDATE).
    repository.newActivePlayer(1);

    // Intentar establecer un ID (99) que no existe en JugadorID.
    int nonExistentJugadorID = 99;

    assertThrows(
        RuntimeException.class,
        () -> repository.changeActivePlayer(nonExistentJugadorID),
        "Debe lanzar RuntimeException por violación de clave foránea (JugadorID=99), cubriendo el catch block de changeActivePlayer.");
  }

  /**
   * Prueba que el método goToJail lance una RuntimeException al fallar la actualización (violación
   * de FK). Cubre el bloque 'catch' de goToJail.
   */
  @Test
  void testGoToJail_ThrowsExceptionOnUpdateFailure() throws SQLException {
    //  Eliminar la posición de la cárcel (11) de la tabla Casilla.
    // Esto asegura que al intentar actualizar Jugador.Posicion = 11, se viole la FK.
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("DELETE FROM Casilla WHERE PosicionTablero = 11");
    }

    //  Intentar enviar al Jugador 1 a la cárcel (posición 11).
    assertThrows(
        RuntimeException.class,
        () -> repository.goToJail(1),
        "Debe lanzar RuntimeException si la operación de UPDATE falla (ej. violación de FK en Posicion), cubriendo el catch block de goToJail.");
  }

  /**
   * Prueba que el método updateJugador lance una RuntimeException al fallar la actualización
   * (violación de FK). Cubre el bloque 'catch' de updateJugador.
   */
  @Test
  void testUpdateJugador_ThrowsExceptionOnUpdateFailure() throws SQLException {
    // Eliminar una posición válida (5) de la tabla Casilla.
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("DELETE FROM Casilla WHERE PosicionTablero = 5");
    }

    // Modificar el jugador en memoria a la posición 5 (la eliminada)
    Jugador jugador = repository.getJugadorByID(1);
    jugador.setPosicion(5);

    // Intentar actualizar al jugador a la posición 5.
    assertThrows(
        RuntimeException.class,
        () -> repository.updateJugador(jugador),
        "Debe lanzar RuntimeException si la operación de UPDATE falla (ej. violación de FK en Posicion), cubriendo el catch block de updateJugador.");
  }

  /**
   * Prueba que getJugadorByID lance RuntimeException al fallar la consulta. Cubre el bloque 'catch
   * (SQLException e)' de getJugadorByID.
   */
  @Test
  void testGetJugadorByID_ThrowsExceptionOnQueryFailure() throws SQLException {
    // Renombrar temporalmente la tabla Jugador para forzar un error de SQL.
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Jugador RENAME TO Jugador_Backup");
    }

    // Se espera una RuntimeException al intentar consultar la tabla renombrada
    assertThrows(
        RuntimeException.class,
        () -> repository.getJugadorByID(1),
        "Debe lanzar RuntimeException si la consulta SQL de getJugadorByID falla (ej. tabla renombrada), cubriendo el catch block de excepción.");

    // Volver a renombrar la tabla para que el tearDown no falle (opcional, pero buena práctica)
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Jugador_Backup RENAME TO Jugador");
    }
  }

  /**
   * Prueba que getJugadorByID lance RuntimeException cuando no encuentra datos. (Este test cubre el
   * escenario de ResultSet vacío, si el repositorio no devuelve null y lanza excepción.)
   */
  @Test
  void testGetJugadorByID_NotFound() {
    // Se espera una RuntimeException (asumiendo que el repositorio la lanza si no encuentra)
    assertThrows(
        RuntimeException.class,
        () -> repository.getJugadorByID(99),
        "Debe lanzar RuntimeException si el JugadorID 99 no se encuentra, cubriendo el catch block.");
  }

  /**
   * Prueba que getPlayerIdByNumJugador lance RuntimeException cuando no encuentra datos (cubre
   * catch de consulta sin resultados).
   */
  @Test
  void testGetPlayerIdByNumJugador_NotFound() {
    // Se espera una RuntimeException al buscar NumJugador 99
    assertThrows(
        RuntimeException.class,
        () -> repository.getPlayerIdByNumJugador(99),
        "Debe lanzar RuntimeException si el NumJugador 99 no se encuentra, cubriendo el catch block.");
  }

  /**
   * Prueba que getNumJugadorByPlayerId lance RuntimeException cuando no encuentra datos. Cubre el
   * bloque 'catch' de getNumJugadorByPlayerId.
   */
  @Test
  void testGetNumJugadorByPlayerId_NotFound() {
    // Se espera una RuntimeException al buscar JugadorID 99
    assertThrows(
        RuntimeException.class,
        () -> repository.getNumJugadorByPlayerId(99),
        "Debe lanzar RuntimeException si el JugadorID 99 no se encuentra, cubriendo el catch block de getNumJugadorByPlayerId.");
  }

  /**
   * Prueba que getFichas lance RuntimeException al fallar la consulta. Cubre el bloque 'catch' de
   * getFichas.
   */
  @Test
  void testGetFichas_ThrowsExceptionOnQueryFailure() throws SQLException {
    // Renombrar temporalmente la tabla Icono para forzar un error de SQL al hacer el JOIN.
    // Esto evita la violación de clave foránea al intentar eliminar la tabla.
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Icono RENAME TO Icono_Backup");
    }

    // Intentar obtener las fichas.
    assertThrows(
        RuntimeException.class,
        () -> repository.getFichas(2),
        "Debe lanzar RuntimeException si la consulta SQL de getFichas falla (ej. tabla renombrada), cubriendo el catch block.");

    // Volver a renombrar la tabla para que el tearDown no falle (opcional, pero buena práctica)
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Icono_Backup RENAME TO Icono");
    }
  }

  /**
   * Prueba que rentPaymentTransaction lance la excepción original (SQLException) después de un
   * rollback. Esto asegura que el bloque catch y el rollback se ejecuten.
   */
  @Test
  void testRentPaymentTransaction_ThrowsExceptionAndRollback() throws SQLException {
    // Jugador existente
    Jugador j1 = repository.getJugadorByID(1); // Pagador: 1500
    Jugador j2 = repository.getJugadorByID(2); // Cobrador: 1500

    // Renombrar temporalmente la tabla Jugador para forzar el fallo de la transacción (los UPDATEs
    // fallarán).
    // Esto evita la violación de clave foránea al intentar eliminar la tabla.
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      // Renombrar la tabla para que los UPDATEs de la transacción fallen
      stmt.execute("ALTER TABLE Jugador RENAME TO Jugador_Backup");
    }

    // Se espera que el método lance una SQLException (o RuntimeException si el repositorio la
    // envuelve)
    assertThrows(
        SQLException.class,
        () -> repository.rentPaymentTransaction(j1, j2),
        "Debe lanzar una SQLException (o la excepción envuelta) y asegurar que se intenta el rollback.");

    // Volver a renombrar la tabla para que el tearDown no falle (opcional, pero buena práctica)
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Jugador_Backup RENAME TO Jugador");
    }
  }

  // --- TESTS DE FUNCIONALIDAD ---

  /**
   * Prueba la funcionalidad de insertar un nuevo jugador en la base de datos y verifica que sus
   * datos sean correctos al recuperarlo.
   */
  @Test
  void testNewPlayer() throws SQLException {
    // Crear un objeto Jugador nuevo (ID 3, NumJugador 3)
    Jugador jugador = new Jugador(3, 3, "Jugador3", 3, 1, false, 1500, partidaID);

    // Insertar el nuevo jugador
    repository.newPlayer(jugador, 3);

    // Recuperar el jugador de la DB
    Jugador jugadorDB = repository.getJugadorByID(3);

    assertNotNull(jugadorDB, "El jugador insertado debe existir en la DB y no ser nulo.");
    assertEquals(
        "Jugador3",
        jugadorDB.getNombreJugador(),
        "El nombre del jugador insertado debe ser 'Jugador3'.");
    assertEquals(1500, jugadorDB.getDinero(), "El dinero inicial del jugador debe ser 1500.");
    assertEquals(1, jugadorDB.getPosicion(), "La nueva posición debe ser 1 (Salida) por defecto.");
  }

  /** Prueba el cambio y la recuperación del ID del jugador activo de la partida. */
  @Test
  void testChangeActivePlayer() throws SQLException {
    // Inicializar el jugador activo al ID 1
    repository.newActivePlayer(1);
    assertEquals(1, repository.getActivePlayer(), "El jugador activo inicial debe ser el ID 1.");

    // Cambiar el jugador activo al ID 2
    repository.changeActivePlayer(2);
    int active = repository.getActivePlayer();

    // El ID del jugador activo debe ser 2
    assertEquals(
        2, active, "El jugador activo debe ser el ID 2 después de la operación de cambio.");
  }

  /**
   * Prueba la rama 'else' del método getActivePlayer, que retorna -1 cuando no hay un jugador
   * activo registrado para la partida (cubre el 'else' de getActivePlayer).
   */
  @Test
  void testGetActivePlayer_NoActivePlayer() throws SQLException {
    // La tabla JugadorActivo está vacía (asegurado por setUp).

    // EJECUCIÓN
    int active = repository.getActivePlayer();

    // Debe retornar -1 ya que no hay registro en JugadorActivo.
    assertEquals(
        -1,
        active,
        "Debe retornar -1 si no hay registro de jugador activo para la partida, cubriendo la rama 'else'.");
  }

  /**
   * Prueba que el método goToJail actualice correctamente la posición del jugador a la casilla de
   * Cárcel (11) y cambie su estado a 'encarcelado' (true).
   */
  @Test
  void testGoToJail() throws SQLException {
    // Enviar al Jugador 1 a la cárcel
    repository.goToJail(1);

    // Recuperar el jugador y comprobar el estado y posición
    Jugador jugadorDB = repository.getJugadorByID(1);

    assertEquals(
        11,
        jugadorDB.getPosicion(),
        "La posición del jugador debe actualizarse a la posición de la cárcel (11).");
    assertTrue(
        jugadorDB.getEstado(), "El estado 'Encarcelado' debe ser TRUE después de la operación.");
  }

  /**
   * Prueba que el método updateJugador persista correctamente los cambios de dinero, posición y
   * estado en la base de datos.
   */
  @Test
  void testUpdateJugador() throws SQLException {

    // Obtener el jugador y modificar sus propiedades
    Jugador jugador = repository.getJugadorByID(1);
    assertNotNull(jugador, "El jugador con ID 1 debe existir antes de actualizar.");

    jugador.setDinero(1200);
    jugador.setPosicion(5);
    jugador.setEstado(true);

    // Actualizar en la base de datos
    repository.updateJugador(jugador);

    // Leer de nuevo y comprobar los valores actualizados
    Jugador jugadorDB = repository.getJugadorByID(1);
    assertEquals(
        1200,
        jugadorDB.getDinero(),
        "El dinero del jugador debe ser 1200 después de la actualización.");
    assertEquals(
        5,
        jugadorDB.getPosicion(),
        "La posición del jugador debe ser 5 después de la actualización.");
    assertTrue(
        jugadorDB.getEstado(),
        "El estado del jugador debe ser 'true' (encarcelado) después de la actualización.");
  }

  /** Prueba la conversión de NumJugador (orden en la partida) a JugadorID (clave primaria). */
  @Test
  void testGetPlayerIdByNumJugador() throws SQLException {
    // Comprobar que NumJugador 1 corresponde a JugadorID 1
    int jugadorID1 = repository.getPlayerIdByNumJugador(1);
    assertEquals(1, jugadorID1, "El ID de Jugador para el NumJugador 1 debe ser 1.");

    // Comprobar que NumJugador 2 corresponde a JugadorID 2
    int jugadorID2 = repository.getPlayerIdByNumJugador(2);
    assertEquals(2, jugadorID2, "El ID de Jugador para el NumJugador 2 debe ser 2.");
  }

  /** Prueba la conversión de JugadorID (clave primaria) a NumJugador (orden en la partida). */
  @Test
  void testGetNumJugadorByPlayerId() throws SQLException {
    // Comprobar que JugadorID 1 corresponde a NumJugador 1
    int numJugador1 = repository.getNumJugadorByPlayerId(1);
    assertEquals(1, numJugador1, "El NumJugador para el JugadorID 1 debe ser 1.");

    // Comprobar que JugadorID 2 corresponde a NumJugador 2
    int numJugador2 = repository.getNumJugadorByPlayerId(2);
    assertEquals(2, numJugador2, "El NumJugador para el JugadorID 2 debe ser 2.");
  }

  /**
   * Prueba que el método getFichas devuelva la información de ficha (ID y Nombre) correcta para
   * todos los jugadores en la partida.
   */
  @Test
  void testGetFichas() throws SQLException {
    // Obtener las fichas para una partida de 2 jugadores
    Ficha[] fichas = repository.getFichas(2);

    // VERIFICACIÓN GENERAL
    assertNotNull(fichas, "El array de fichas no debe ser nulo.");
    assertEquals(2, fichas.length, "Debe retornar un array con 2 fichas (una por jugador).");

    // VERIFICACIÓN FICHA JUGADOR 1 (ID=1, Icono=1, Nombre='Ignacito')
    assertEquals(1, fichas[0].getJugadorId(), "La primera ficha debe corresponder al JugadorID 1.");
    assertEquals(1, fichas[0].getIdFicha(), "El IconoID de la primera ficha debe ser 1.");
    assertEquals(
        "Ignacito",
        fichas[0].getNombreFicha(),
        "El Nombre del Icono de la primera ficha debe ser 'Ignacito'.");

    // VERIFICACIÓN FICHA JUGADOR 2 (ID=2, Icono=2, Nombre='Balon')
    assertEquals(2, fichas[1].getJugadorId(), "La segunda ficha debe corresponder al JugadorID 2.");
    assertEquals(2, fichas[1].getIdFicha(), "El IconoID de la segunda ficha debe ser 2.");
    assertEquals(
        "Balon",
        fichas[1].getNombreFicha(),
        "El Nombre del Icono de la segunda ficha debe ser 'Balon'.");
  }

  /**
   * Prueba la atomicidad y la corrección de la transacción de pago de renta, verificando que el
   * dinero se reste del pagador y se sume al cobrador.
   */
  @Test
  void testRentPaymentTransaction() throws SQLException {
    // Obtener los jugadores iniciales
    Jugador j1 = repository.getJugadorByID(1); // Pagador
    Jugador j2 = repository.getJugadorByID(2); // Cobrador
    int renta = 200;

    // Modificar los objetos en memoria para simular el resultado esperado
    j1.setDinero(j1.getDinero() - renta); // Esperado: 1300
    j2.setDinero(j2.getDinero() + renta); // Esperado: 1700

    // Ejecutar la transacción
    repository.rentPaymentTransaction(j1, j2);

    // Leer los jugadores de la base de datos y comprobar saldos
    Jugador j1DB = repository.getJugadorByID(1);
    Jugador j2DB = repository.getJugadorByID(2);

    assertEquals(
        1300,
        j1DB.getDinero(),
        "El jugador pagador (J1) debe tener 1300 después del pago de la renta.");
    assertEquals(
        1700,
        j2DB.getDinero(),
        "El jugador cobrador (J2) debe tener 1700 después de recibir la renta.");
  }
}
