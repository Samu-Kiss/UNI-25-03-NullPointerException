package com.NullPtr.Pontiland.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.NullPtr.Pontiland.services.DataService;
import com.NullPtr.Pontiland.services.IDataService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.*;

/**
 * Clase de prueba para PartidaRepository. Configura una base de datos H2 en memoria para probar las
 * operaciones CRUD relacionadas con la entidad Partida, cubriendo todas las ramas de código.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PartidaRepositoryTest {

  private static IDataService dataService;
  private PartidaRepository repository;
  private long testPartidaID = 123456789L; // ID fijo para pruebas de consulta

  /** Inicializa el DataService y crea el esquema de la tabla Partida. */
  @BeforeAll
  void initAll() throws SQLException {
    // Usar una URL de conexión única para este test
    dataService = new DataService("jdbc:h2:mem:testPartida;DB_CLOSE_DELAY=-1");

    // Crear la tabla Partida
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      // Borrar por si acaso y recrear para asegurar el esquema
      stmt.execute("DROP TABLE IF EXISTS Partida");
      stmt.execute("CREATE TABLE Partida(PartidaID BIGINT PRIMARY KEY, NumeroJugadores INT)");
    }
  }

  /**
   * Configura el entorno de prueba antes de cada test: inicializa el repositorio y limpia datos.
   */
  @BeforeEach
  void setUp() throws SQLException {
    repository = new PartidaRepository(dataService);
    // Establecer un ID de partida conocido para las pruebas de consulta
    repository.partidaID = testPartidaID;

    // Limpiar tabla antes de cada test
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("DELETE FROM Partida");
    }
  }

  /** Prueba la inyección de dependencia en el constructor y el método getDataService. */
  @Test
  void testConstructorAndGetDataService() {
    assertNotNull(repository, "El repositorio no debe ser nulo.");
    assertEquals(
        dataService, repository.getDataService(), "Debe retornar el IDataService inyectado.");
  }

  /**
   * Prueba la creación exitosa de una nueva partida y verifica el retorno del ID y la inserción en
   * la DB.
   */
  @Test
  void testNewPartida_Success() throws SQLException {

    int expectedNumJugadores = 4;

    long newID = repository.newPartida(expectedNumJugadores);

    // ASSERT
    assertTrue(
        newID > 0,
        "El ID de la nueva partida debe ser un número positivo (basado en el timestamp).");

    // Verificar en la base de datos
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs =
            stmt.executeQuery("SELECT NumeroJugadores FROM Partida WHERE PartidaID = " + newID)) {
      assertTrue(rs.next(), "Debe encontrar la partida recién insertada.");
      assertEquals(
          expectedNumJugadores,
          rs.getInt("NumeroJugadores"),
          "El número de jugadores debe ser el insertado (4).");
    }
  }

  /** Prueba la recuperación exitosa del número de jugadores de una partida existente. */
  @Test
  void testGetNumJugadores_Success() throws SQLException {
    // Insertar partida de prueba
    int expectedNumJugadores = 2;
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          "INSERT INTO Partida(PartidaID, NumeroJugadores) VALUES("
              + testPartidaID
              + ", "
              + expectedNumJugadores
              + ")");
    }

    int numJugadores = repository.getNumJugadores();

    assertEquals(
        expectedNumJugadores, numJugadores, "Debe retornar el número de jugadores insertado (2).");
  }

  /**
   * Prueba que newPartida lance RuntimeException si hay un error de SQL. Cubre el bloque 'catch
   * (SQLException e)' de newPartida.
   */
  @Test
  void testNewPartida_ThrowsExceptionOnQueryFailure() throws SQLException {
    // Renombrar la tabla para forzar un error de SQL al intentar insertar
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Partida RENAME TO Partida_Backup");
    }

    // Esperar RuntimeException
    assertThrows(
        RuntimeException.class,
        () -> repository.newPartida(4),
        "Debe lanzar RuntimeException por fallo en la consulta SQL, cubriendo el catch block.");

    // Volver a renombrar la tabla para restaurar el estado
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Partida_Backup RENAME TO Partida");
    }
  }

  /**
   * Prueba que getNumJugadores lance RuntimeException si no encuentra la partida. Cubre la rama
   * 'else' dentro del bloque 'try (ResultSet rs = ...)' de getNumJugadores.
   */
  @Test
  void testGetNumJugadores_NotFound_ThrowsException() {
    // La tabla Partida está vacía (asegurado por setUp)
    // repository.partidaID es testPartidaID (no existe)

    // Esperar RuntimeException
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> repository.getNumJugadores(),
            "Debe lanzar RuntimeException si la partidaID no se encuentra, cubriendo la rama 'else'.");

    assertTrue(
        exception
            .getMessage()
            .contains("No se encontró un jugador activo para la partida: " + testPartidaID),
        "El mensaje de excepción debe indicar que la partida no fue encontrada.");
  }

  /**
   * Prueba que getNumJugadores lance RuntimeException si hay un error de SQL. Cubre el bloque
   * 'catch (SQLException e)' de getNumJugadores.
   */
  @Test
  void testGetNumJugadores_ThrowsExceptionOnQueryFailure() throws SQLException {
    // Renombrar la tabla para forzar un error de SQL al intentar consultar
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Partida RENAME TO Partida_Backup");
    }

    // Esperar RuntimeException
    assertThrows(
        RuntimeException.class,
        () -> repository.getNumJugadores(),
        "Debe lanzar RuntimeException por fallo en la consulta SQL, cubriendo el catch block.");

    // Volver a renombrar la tabla para restaurar el estado
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Partida_Backup RENAME TO Partida");
    }
  }
}
