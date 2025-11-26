package com.NullPtr.Pontiland.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.entities.SavedGame;
import com.NullPtr.Pontiland.services.IDataService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.*;

/**
 * Clase de prueba para PropiedadRepository.
 *
 * <p>Utiliza una base de datos H2 en memoria para simular las operaciones SQL y verificar la
 * funcionalidad de cada método, incluyendo manejo de casos de éxito, no encontrado y excepciones
 * SQL.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PropiedadRepositoryTest {

  private static MockDataService dataService;
  private PropiedadRepository repository;
  private final long MOCK_PARTIDA_ID = 1001L;
  private final int MOCK_JUGADOR_ID = 1;
  private final int MOCK_POSITION = 5;
  private final int MOCK_PROPIEDAD_ID = 5;

  /** Implementación simple de IDataService para usar una base de datos H2 en memoria. */
  static class MockDataService implements IDataService {
    @Override
    public Connection createConnection() {
      try {
        // Usamos H2 en modo MEMORY para que sea rápida y efímera
        return DriverManager.getConnection("jdbc:h2:mem:testPropiedad;DB_CLOSE_DELAY=-1");
      } catch (SQLException e) {
        // Se lanza como RuntimeException si la conexión falla en el mock.
        throw new RuntimeException("Error al crear la conexión H2: " + e.getMessage(), e);
      }
    }

    // Implementación de métodos faltantes en IDataService
    @Override
    public void newDataBase() {
      // Mocked
    }

    @Override
    public void loadDataBase(String archivoSeleccionado) {}

    @Override
    public List<SavedGame> listarPartidasPasadas() {
      return Collections.emptyList();
    }

    @Override
    public void deleteDataBase() {
      // Mocked
    }
  }

  /**
   * Inicializa el DataService y crea el esquema de la base de datos simulando las tablas
   * necesarias.
   */
  @BeforeAll
  void initAll() throws SQLException {
    dataService = new MockDataService();

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      // 1. Crear Casilla
      stmt.execute("CREATE TABLE Casilla (PosicionTablero INT PRIMARY KEY, NombreCasilla VARCHAR)");
      // 2. Crear Propiedad
      stmt.execute(
          "CREATE TABLE Propiedad ("
              + "PropiedadID INT PRIMARY KEY, "
              + "PosicionTablero INT, "
              + "GrupoPropiedades INT, "
              + "PrecioCompra INT, "
              + "RentaNivel1 INT, RentaNivel2 INT, RentaNivel3 INT, RentaNivel4 INT, RentaNivel5 INT, "
              + "FOREIGN KEY (PosicionTablero) REFERENCES Casilla(PosicionTablero))");
      // 3. Crear Jugador
      stmt.execute(
          "CREATE TABLE Jugador ("
              + "JugadorID INT PRIMARY KEY, "
              + "PartidaID BIGINT, "
              + "NombreJugador VARCHAR, "
              + "PosicionTablero INT, "
              + "Encarcelado BOOLEAN, "
              + "SaldoDinero INT)");
      // 4. Crear Adquisiciones
      stmt.execute(
          "CREATE TABLE Adquisiciones ("
              + "JugadorID INT, "
              + "PropiedadID INT, "
              + "NivelPropiedad INT, "
              + "PRIMARY KEY (JugadorID, PropiedadID), "
              + "FOREIGN KEY (JugadorID) REFERENCES Jugador(JugadorID), "
              + "FOREIGN KEY (PropiedadID) REFERENCES Propiedad(PropiedadID))");
    }
  }

  /** Configura el repositorio y asegura que la base de datos esté limpia antes de cada prueba. */
  @BeforeEach
  void setUp() throws SQLException {
    repository = new PropiedadRepository(dataService);
    // Limpiar todas las tablas antes de cada test
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("DELETE FROM Adquisiciones");
      stmt.execute("DELETE FROM Jugador");
      stmt.execute("DELETE FROM Propiedad");
      stmt.execute("DELETE FROM Casilla");

      // Insertar datos base para la propiedad MOCK
      stmt.execute(
          String.format("INSERT INTO Casilla VALUES (%d, 'Propiedad de Prueba')", MOCK_POSITION));
      // NOTA: Se usa 1 para GrupoPropiedades ya que el constructor de Propiedad.java lo requiere
      // (1-8)
      stmt.execute(
          String.format(
              "INSERT INTO Propiedad VALUES (%d, %d, 1, 100, 10, 30, 90, 270, 810)",
              MOCK_PROPIEDAD_ID, MOCK_POSITION));

      // Insertar datos base para el jugador MOCK
      stmt.execute(
          String.format(
              "INSERT INTO Jugador VALUES (%d, %d, 'TestPlayer', 1, FALSE, 1500)",
              MOCK_JUGADOR_ID, MOCK_PARTIDA_ID));
    }
  }

  /** Prueba la inyección de dependencia en el constructor. */
  @Test
  void testConstructor() {
    assertNotNull(repository, "El repositorio no debe ser nulo.");
  }

  /** Caso de éxito: La propiedad se encuentra en la posición. */
  @Test
  void testGetPropiedadIdByPosition_Success() {
    int id = repository.getPropiedadIdByPosition(MOCK_POSITION);
    assertEquals(MOCK_PROPIEDAD_ID, id, "Debe retornar el ID de la propiedad para la posición.");
  }

  /** Caso de fallo: No hay propiedad en la posición (retorna -1). */
  @Test
  void testGetPropiedadIdByPosition_NotFound() {
    int id = repository.getPropiedadIdByPosition(999);
    assertEquals(-1, id, "Debe retornar -1 si no se encuentra la propiedad.");
  }

  /** Caso de excepción: Error de SQL (cubre el catch block). */
  @Test
  void testGetPropiedadIdByPosition_ThrowsException() throws SQLException {
    // Renombrar tabla para forzar SQLException
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad RENAME TO Propiedad_Backup");
    }

    // ACT & ASSERT
    assertThrows(
        RuntimeException.class,
        () -> repository.getPropiedadIdByPosition(MOCK_POSITION),
        "Debe lanzar RuntimeException ante un error de SQL.");

    // CLEANUP: Restaurar
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad_Backup RENAME TO Propiedad");
    }
  }

  /** Caso de éxito: Propiedad encontrada, sin dueño (Nivel 1 por defecto). */
  @Test
  void testGetPropiedadByPosition_NoOwner() {
    Propiedad p = repository.getPropiedadByPosition(MOCK_POSITION, MOCK_PARTIDA_ID);
    assertNotNull(p);
    // USO DE MÉTODOS CORRECTOS SEGÚN ENTIDAD ORIGINAL
    assertEquals(MOCK_PROPIEDAD_ID, p.getIdPropiedad());
    assertEquals(1, p.getNivelPropiedad(), "Debe ser nivel 1 si no tiene dueño.");
    assertEquals(10, p.getRentaPorNivel()[0], "Renta nivel 1 debe ser 10.");
  }

  /** Caso de éxito: Propiedad encontrada, con dueño y nivel real. */
  @Test
  void testGetPropiedadByPosition_WithOwner() throws SQLException {
    final int EXPECTED_LEVEL = 3;
    // Asignar dueño y nivel
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          String.format(
              "INSERT INTO Adquisiciones VALUES (%d, %d, %d)",
              MOCK_JUGADOR_ID, MOCK_PROPIEDAD_ID, EXPECTED_LEVEL));
    }

    Propiedad p = repository.getPropiedadByPosition(MOCK_POSITION, MOCK_PARTIDA_ID);

    assertNotNull(p);
    // USO DE MÉTODOS CORRECTOS SEGÚN ENTIDAD ORIGINAL
    assertEquals(
        EXPECTED_LEVEL, p.getNivelPropiedad(), "Debe tener el nivel real (3) de la adquisición.");
    assertEquals(90, p.getRentaPorNivel()[2], "Renta nivel 3 debe ser 90.");
  }

  /** Caso de fallo: No se encuentra la propiedad (retorna null). */
  @Test
  void testGetPropiedadByPosition_PropertyNotFound() {
    Propiedad p = repository.getPropiedadByPosition(999, MOCK_PARTIDA_ID);
    assertNull(p, "Debe retornar null si la propiedad no existe en la posición.");
  }

  /** Caso de excepción: Error de SQL en la consulta principal (cubre el catch block). */
  @Test
  void testGetPropiedadByPosition_ThrowsException() throws SQLException {
    //  Renombrar tabla para forzar SQLException
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad RENAME TO Propiedad_Backup");
    }

    assertThrows(
        RuntimeException.class,
        () -> repository.getPropiedadByPosition(MOCK_POSITION, MOCK_PARTIDA_ID),
        "Debe lanzar RuntimeException ante un error de SQL en la consulta principal.");

    // Restaurar
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad_Backup RENAME TO Propiedad");
    }
  }

  /** Caso de éxito: Propiedad con dueño (retorna Jugador). */
  @Test
  void testPropiedadHasOwner_HasOwner() throws SQLException {
    // Asignar dueño
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          String.format(
              "INSERT INTO Adquisiciones VALUES (%d, %d, 1)", MOCK_JUGADOR_ID, MOCK_PROPIEDAD_ID));
    }

    Jugador owner = repository.propiedadHasOwner(MOCK_PROPIEDAD_ID, MOCK_PARTIDA_ID);

    assertNotNull(owner);
    // USO DE MÉTODOS CORRECTOS SEGÚN ENTIDAD ORIGINAL
    assertEquals(MOCK_JUGADOR_ID, owner.getJugadorId());
    // getPropiedadesByJugador se llama dentro de propiedadHasOwner y debe retornar la propiedad
    assertFalse(owner.getPropiedades().isEmpty());
    assertEquals(MOCK_PROPIEDAD_ID, owner.getPropiedades().get(0).getIdPropiedad());
  }

  /** Caso de fallo: Propiedad sin dueño (retorna null). */
  @Test
  void testPropiedadHasOwner_NoOwner() {
    // Adquisiciones vacío
    Jugador owner = repository.propiedadHasOwner(MOCK_PROPIEDAD_ID, MOCK_PARTIDA_ID);
    assertNull(owner, "Debe retornar null si no hay registro en Adquisiciones.");
  }

  /** Caso de excepción: Error de SQL (cubre el catch block). */
  @Test
  void testPropiedadHasOwner_ThrowsException() throws SQLException {
    //  Renombrar tabla para forzar SQLException
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Jugador RENAME TO Jugador_Backup");
    }

    //
    assertThrows(
        RuntimeException.class,
        () -> repository.propiedadHasOwner(MOCK_PROPIEDAD_ID, MOCK_PARTIDA_ID),
        "Debe lanzar RuntimeException ante un error de SQL.");

    // CLEANUP: Restaurar
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Jugador_Backup RENAME TO Jugador");
    }
  }

  /** Caso de éxito: Nivel encontrado. */
  @Test
  void testGetNivelPropiedad_Success() throws SQLException {
    final int EXPECTED_LEVEL = 5;
    //  Asignar dueño y nivel
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          String.format(
              "INSERT INTO Adquisiciones VALUES (%d, %d, %d)",
              MOCK_JUGADOR_ID, MOCK_PROPIEDAD_ID, EXPECTED_LEVEL));
    }

    int nivel = repository.getNivelPropiedad(MOCK_PROPIEDAD_ID, MOCK_PARTIDA_ID);

    assertEquals(EXPECTED_LEVEL, nivel, "Debe retornar el NivelPropiedad (5).");
  }

  /** Caso de fallo: Nivel no encontrado (retorna 0). */
  @Test
  void testGetNivelPropiedad_NotFound() {
    // Adquisiciones vacío
    int nivel = repository.getNivelPropiedad(MOCK_PROPIEDAD_ID, MOCK_PARTIDA_ID);
    assertEquals(0, nivel, "Debe retornar 0 si no se encuentra el nivel.");
  }

  /** Caso de excepción: Error de SQL (cubre el catch block). */
  @Test
  void testGetNivelPropiedad_ThrowsException() throws SQLException {
    // Renombrar tabla para forzar SQLException
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Adquisiciones RENAME TO Adquisiciones_Backup");
    }

    assertThrows(
        RuntimeException.class,
        () -> repository.getNivelPropiedad(MOCK_PROPIEDAD_ID, MOCK_PARTIDA_ID),
        "Debe lanzar RuntimeException ante un error de SQL.");

    //  Restaurar
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Adquisiciones_Backup RENAME TO Adquisiciones");
    }
  }

  /** Caso de éxito: Jugador con propiedades (retorna lista de Propiedad). */
  @Test
  void testGetPropiedadesByJugador_Success() throws SQLException {
    //  Asignar propiedad
    final int EXPECTED_COUNT = 1;
    final int EXPECTED_LEVEL = 2;
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          String.format(
              "INSERT INTO Adquisiciones VALUES (%d, %d, %d)",
              MOCK_JUGADOR_ID, MOCK_PROPIEDAD_ID, EXPECTED_LEVEL));
    }

    List<Propiedad> propiedades = repository.getPropiedadesByJugador(MOCK_JUGADOR_ID);

    assertFalse(propiedades.isEmpty());
    assertEquals(EXPECTED_COUNT, propiedades.size());
    // USO DE MÉTODOS CORRECTOS SEGÚN ENTIDAD ORIGINAL
    assertEquals(MOCK_PROPIEDAD_ID, propiedades.get(0).getIdPropiedad());
    assertEquals(EXPECTED_LEVEL, propiedades.get(0).getNivelPropiedad());
    assertEquals(30, propiedades.get(0).getRentaPorNivel()[1], "Renta nivel 2 debe ser 30.");
  }

  /** Caso de fallo: Jugador sin propiedades (retorna lista vacía). */
  @Test
  void testGetPropiedadesByJugador_NoProperties() {
    List<Propiedad> propiedades = repository.getPropiedadesByJugador(MOCK_JUGADOR_ID);
    assertTrue(propiedades.isEmpty(), "Debe retornar una lista vacía si no hay adquisiciones.");
  }

  /** Caso de excepción: Error de SQL (cubre el catch block). */
  @Test
  void testGetPropiedadesByJugador_ThrowsException() throws SQLException {
    // Renombrar tabla para forzar SQLException
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad RENAME TO Propiedad_Backup");
    }

    assertThrows(
        RuntimeException.class,
        () -> repository.getPropiedadesByJugador(MOCK_JUGADOR_ID),
        "Debe lanzar RuntimeException ante un error de SQL.");

    // Restaurar
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad_Backup RENAME TO Propiedad");
    }
  }

  /** Caso de éxito: Nombre encontrado. */
  @Test
  void testGetPropiedadNombreById_Success() {
    String name = repository.getPropiedadNombreById(MOCK_PROPIEDAD_ID);
    assertEquals("Propiedad de Prueba", name);
  }

  /** Caso de fallo: Nombre no encontrado (retorna null). */
  @Test
  void testGetPropiedadNombreById_NotFound() {
    String name = repository.getPropiedadNombreById(9999);
    assertNull(name, "Debe retornar null si la propiedad no existe.");
  }

  /** Caso de excepción: Error de SQL (cubre el catch block). */
  @Test
  void testGetPropiedadNombreById_ThrowsException() throws SQLException {
    //  Renombrar tabla para forzar SQLException
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Casilla RENAME TO Casilla_Backup");
    }

    assertThrows(
        RuntimeException.class,
        () -> repository.getPropiedadNombreById(MOCK_PROPIEDAD_ID),
        "Debe lanzar RuntimeException ante un error de SQL.");

    // Restaurar
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Casilla_Backup RENAME TO Casilla");
    }
  }
}
