package com.NullPtr.Pontiland.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.entities.SavedGame;
import com.NullPtr.Pontiland.services.IDataService;
import java.sql.*;
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

  /** Implementación simple de IDataService para usar H2 en memoria. */
  static class MockDataService implements IDataService {

    @Override
    public Connection createConnection() {
      try {
        return DriverManager.getConnection("jdbc:h2:mem:testPropiedad;DB_CLOSE_DELAY=-1");
      } catch (SQLException e) {
        throw new RuntimeException("Error al crear la conexión H2: " + e.getMessage(), e);
      }
    }

    @Override
    public void newDataBase() {
      // Mocked
    }

    @Override
    public void loadDataBase(String archivoSeleccionado) {
      // Mocked
    }

    @Override
    public void saveDataBase(long partidaID) {
      // Mocked
    }

    @Override
    public List<SavedGame> listarPartidasPasadas() {
      return Collections.emptyList();
    }

    @Override
    public void deleteDataBase() {
      // Mocked
    }
  }

  @BeforeAll
  void initAll() throws SQLException {
    dataService = new MockDataService();
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {

      stmt.execute("CREATE TABLE Casilla (PosicionTablero INT PRIMARY KEY, NombreCasilla VARCHAR)");
      stmt.execute(
          "CREATE TABLE Propiedad ("
              + "PropiedadID INT PRIMARY KEY, "
              + "PosicionTablero INT, "
              + "GrupoPropiedades INT, "
              + "PrecioCompra INT, "
              + "RentaNivel1 INT, RentaNivel2 INT, RentaNivel3 INT, RentaNivel4 INT, RentaNivel5 INT, "
              + "FOREIGN KEY (PosicionTablero) REFERENCES Casilla(PosicionTablero))");
      stmt.execute(
          "CREATE TABLE Jugador ("
              + "JugadorID INT PRIMARY KEY, "
              + "Partida BIGINT, "
              + // Cambiado de PartidaID a Partida
              "NombreJugador VARCHAR, "
              + "Posicion INT, "
              + // Cambiado de PosicionTablero a Posicion
              "Encarcelado BOOLEAN, "
              + "Dinero INT)"); // Cambiado de SaldoDinero a Dinero
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

  @BeforeEach
  void setUp() throws SQLException {
    repository = new PropiedadRepository(dataService);
    repository.setPartidaID(MOCK_PARTIDA_ID); // Configurar partidaID

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {

      stmt.execute("DELETE FROM Adquisiciones");
      stmt.execute("DELETE FROM Jugador");
      stmt.execute("DELETE FROM Propiedad");
      stmt.execute("DELETE FROM Casilla");

      stmt.execute(
          String.format("INSERT INTO Casilla VALUES (%d, 'Propiedad de Prueba')", MOCK_POSITION));
      stmt.execute(
          String.format(
              "INSERT INTO Propiedad VALUES (%d, %d, 1, 100, 10, 30, 90, 270, 810)",
              MOCK_PROPIEDAD_ID, MOCK_POSITION));
      stmt.execute(
          String.format(
              "INSERT INTO Jugador VALUES (%d, %d, 'TestPlayer', 1, FALSE, 1500)",
              MOCK_JUGADOR_ID, MOCK_PARTIDA_ID));
    }
  }

  @Test
  void testConstructor() {
    assertNotNull(repository, "El repositorio no debe ser nulo.");
  }

  @Test
  void testSetPartidaID() {
    assertDoesNotThrow(() -> repository.setPartidaID(999L));
  }

  @Test
  void testGetPropiedadIdByPosition_Success() throws SQLException {
    int id = repository.getPropiedadIdByPosition(MOCK_POSITION);
    assertEquals(
        MOCK_PROPIEDAD_ID,
        id,
        "Debe retornar el ID correcto de la propiedad para la posición dada.");
  }

  @Test
  void testGetPropiedadIdByPosition_NotFound() throws SQLException {
    int id = repository.getPropiedadIdByPosition(999);
    assertEquals(-1, id, "Debe retornar -1 si no se encuentra ninguna propiedad en la posición.");
  }

  @Test
  void testGetPropiedadIdByPosition_ThrowsException() throws SQLException {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad RENAME TO Propiedad_Backup");
    }

    assertThrows(SQLException.class, () -> repository.getPropiedadIdByPosition(MOCK_POSITION));

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad_Backup RENAME TO Propiedad");
    }
  }

  @Test
  void testGetPropiedadByPosition_NoOwner() throws SQLException {
    Propiedad p = repository.getPropiedadByPosition(MOCK_POSITION);

    assertNotNull(p, "La propiedad no debe ser nula.");
    assertEquals(MOCK_PROPIEDAD_ID, p.getIdPropiedad(), "Debe coincidir el ID de la propiedad.");
    assertEquals(1, p.getNivelPropiedad(), "Debe ser nivel 1 si no tiene dueño.");
    assertEquals(10, p.getRentaPorNivel()[0], "Renta nivel 1 debe ser 10.");
  }

  @Test
  void testGetPropiedadByPosition_WithOwner() throws SQLException {
    final int EXPECTED_LEVEL = 3;
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          String.format(
              "INSERT INTO Adquisiciones VALUES (%d, %d, %d)",
              MOCK_JUGADOR_ID, MOCK_PROPIEDAD_ID, EXPECTED_LEVEL));
    }

    Propiedad p = repository.getPropiedadByPosition(MOCK_POSITION);

    assertNotNull(p);
    assertEquals(EXPECTED_LEVEL, p.getNivelPropiedad());
    assertEquals(90, p.getRentaPorNivel()[2]);
  }

  @Test
  void testGetPropiedadByPosition_PropertyNotFound() throws SQLException {
    Propiedad p = repository.getPropiedadByPosition(999);
    assertNull(p, "Debe retornar null si no existe la propiedad.");
  }

  @Test
  void testGetPropiedadByPosition_ThrowsException() throws SQLException {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad RENAME TO Propiedad_Backup");
    }

    assertThrows(
        SQLException.class,
        () -> repository.getPropiedadByPosition(MOCK_POSITION),
        "Debe lanzar SQLException ante un error de SQL.");

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad_Backup RENAME TO Propiedad");
    }
  }

  @Test
  void testPropiedadHasOwner_NoOwner() throws SQLException {
    Jugador owner = repository.propiedadHasOwner(MOCK_PROPIEDAD_ID);
    assertNull(owner, "Debe retornar null si no tiene dueño.");
  }

  @Test
  void testPropiedadHasOwner_HasOwner() throws SQLException {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          String.format(
              "INSERT INTO Adquisiciones VALUES (%d, %d, 1)", MOCK_JUGADOR_ID, MOCK_PROPIEDAD_ID));
    }

    Jugador owner = repository.propiedadHasOwner(MOCK_PROPIEDAD_ID);

    assertNotNull(owner);
    assertEquals(MOCK_JUGADOR_ID, owner.getJugadorId());
    assertFalse(owner.getPropiedades().isEmpty());
    assertEquals(MOCK_PROPIEDAD_ID, owner.getPropiedades().get(0).getIdPropiedad());
  }

  @Test
  void testPropiedadHasOwner_ThrowsException() throws SQLException {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Jugador RENAME TO Jugador_Backup");
    }

    assertThrows(SQLException.class, () -> repository.propiedadHasOwner(MOCK_PROPIEDAD_ID));

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Jugador_Backup RENAME TO Jugador");
    }
  }

  @Test
  void testGetNivelPropiedad_Success() throws SQLException {
    final int EXPECTED_LEVEL = 5;
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          String.format(
              "INSERT INTO Adquisiciones VALUES (%d, %d, %d)",
              MOCK_JUGADOR_ID, MOCK_PROPIEDAD_ID, EXPECTED_LEVEL));
    }

    int nivel = repository.getNivelPropiedad(MOCK_PROPIEDAD_ID);
    assertEquals(EXPECTED_LEVEL, nivel, "Debe retornar el NivelPropiedad (5).");
  }

  @Test
  void testGetNivelPropiedad_NotFound() throws SQLException {
    int nivel = repository.getNivelPropiedad(MOCK_PROPIEDAD_ID);
    assertEquals(0, nivel);
  }

  @Test
  void testGetNivelPropiedad_ThrowsException() throws SQLException {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Adquisiciones RENAME TO Adquisiciones_Backup");
    }

    assertThrows(SQLException.class, () -> repository.getNivelPropiedad(MOCK_PROPIEDAD_ID));

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Adquisiciones_Backup RENAME TO Adquisiciones");
    }
  }

  @Test
  void testIncrementarNivelPropiedad_Success() throws SQLException {
    final int INITIAL_LEVEL = 2;
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          String.format(
              "INSERT INTO Adquisiciones VALUES (%d, %d, %d)",
              MOCK_JUGADOR_ID, MOCK_PROPIEDAD_ID, INITIAL_LEVEL));
    }

    repository.incrementarNivelPropiedad(MOCK_PROPIEDAD_ID);

    int nuevoNivel = repository.getNivelPropiedad(MOCK_PROPIEDAD_ID);
    assertEquals(INITIAL_LEVEL + 1, nuevoNivel, "El nivel debe incrementarse en 1.");
  }

  @Test
  void testIncrementarNivelPropiedad_ThrowsException() throws SQLException {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Adquisiciones RENAME TO Adquisiciones_Backup");
    }

    assertThrows(SQLException.class, () -> repository.incrementarNivelPropiedad(MOCK_PROPIEDAD_ID));

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Adquisiciones_Backup RENAME TO Adquisiciones");
    }
  }

  @Test
  void testGetPropiedadesByJugador_Success() throws SQLException {
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
    assertEquals(1, propiedades.size());
    assertEquals(MOCK_PROPIEDAD_ID, propiedades.get(0).getIdPropiedad());
    assertEquals(EXPECTED_LEVEL, propiedades.get(0).getNivelPropiedad());
    assertEquals(30, propiedades.get(0).getRentaPorNivel()[1]);
  }

  @Test
  void testGetPropiedadesByJugador_NoProperties() throws SQLException {
    List<Propiedad> propiedades = repository.getPropiedadesByJugador(MOCK_JUGADOR_ID);
    assertTrue(propiedades.isEmpty());
  }

  @Test
  void testGetPropiedadesByJugador_ThrowsException() throws SQLException {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad RENAME TO Propiedad_Backup");
    }

    assertThrows(SQLException.class, () -> repository.getPropiedadesByJugador(MOCK_JUGADOR_ID));

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Propiedad_Backup RENAME TO Propiedad");
    }
  }

  @Test
  void testGetPropiedadNombreById_Success() throws SQLException {
    String name = repository.getPropiedadNombreById(MOCK_PROPIEDAD_ID);
    assertEquals("Propiedad de Prueba", name);
  }

  @Test
  void testGetPropiedadNombreById_NotFound() throws SQLException {
    String name = repository.getPropiedadNombreById(9999);
    assertNull(name);
  }

  @Test
  void testGetPropiedadNombreById_ThrowsException() throws SQLException {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Casilla RENAME TO Casilla_Backup");
    }

    assertThrows(SQLException.class, () -> repository.getPropiedadNombreById(MOCK_PROPIEDAD_ID));

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Casilla_Backup RENAME TO Casilla");
    }
  }

  @Test
  void testGetOwnerIdByPropiedadId_HasOwner() throws SQLException {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          String.format(
              "INSERT INTO Adquisiciones VALUES (%d, %d, 1)", MOCK_JUGADOR_ID, MOCK_PROPIEDAD_ID));
    }

    Integer ownerId = repository.getOwnerIdByPropiedadId(MOCK_PROPIEDAD_ID);
    assertNotNull(ownerId);
    assertEquals(MOCK_JUGADOR_ID, ownerId);
  }

  @Test
  void testGetOwnerIdByPropiedadId_NoOwner() throws SQLException {
    Integer ownerId = repository.getOwnerIdByPropiedadId(MOCK_PROPIEDAD_ID);
    assertNull(ownerId);
  }

  @Test
  void testGetOwnerIdByPropiedadId_ThrowsException() throws SQLException {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Adquisiciones RENAME TO Adquisiciones_Backup");
    }

    assertThrows(SQLException.class, () -> repository.getOwnerIdByPropiedadId(MOCK_PROPIEDAD_ID));

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Adquisiciones_Backup RENAME TO Adquisiciones");
    }
  }

  @Test
  void testAddAdquisicion_Success() throws SQLException {
    final int NIVEL = 1;

    repository.addAdquisicion(MOCK_JUGADOR_ID, MOCK_PROPIEDAD_ID, NIVEL);

    // Verificar que se insertó correctamente
    Integer ownerId = repository.getOwnerIdByPropiedadId(MOCK_PROPIEDAD_ID);
    assertNotNull(ownerId);
    assertEquals(MOCK_JUGADOR_ID, ownerId);

    int nivel = repository.getNivelPropiedad(MOCK_PROPIEDAD_ID);
    assertEquals(NIVEL, nivel);
  }

  @Test
  void testAddAdquisicion_ThrowsException() throws SQLException {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Adquisiciones RENAME TO Adquisiciones_Backup");
    }

    assertThrows(
        SQLException.class, () -> repository.addAdquisicion(MOCK_JUGADOR_ID, MOCK_PROPIEDAD_ID, 1));

    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("ALTER TABLE Adquisiciones_Backup RENAME TO Adquisiciones");
    }
  }
}
