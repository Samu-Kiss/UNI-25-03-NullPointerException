package com.NullPtr.Pontiland.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.services.DataService;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.*;

/**
 * Pruebas de integración para {@link CasillaRepository} usando una base de datos H2 en memoria.
 *
 * <p>Se prueba la obtención de casillas por posición y los distintos casos de error: - Posición
 * inválida (menor que 1 o mayor que 40) - Posición válida que no existe en la base de datos
 *
 * <p>Antes de cada test se inicializa la base de datos para asegurar un entorno limpio.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// Configura el ciclo de vida de los tests a "por clase".
// Esto significa que todos los métodos @Test comparten la misma instancia de esta clase,
// lo que permite mantener campos compartidos (como dataService) y usar @BeforeAll no estático.

class CasillaRepositoryTest {

  private static DataService dataService;
  private CasillaRepository repository;

  /** Inicializa el servicio de datos con H2 en memoria antes de ejecutar cualquier test. */
  @BeforeAll
  void initialize() {
    dataService = new DataService("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
  }

  /** Antes de cada test, crea la base de datos y inicializa el repositorio. */
  @BeforeEach
  void setUp() {
    // Crea DDL + inserts
    dataService.newDataBase();
    repository = new CasillaRepository(dataService);
  }

  /** Limpia toda la base de datos después de cada test. */
  @AfterEach
  void tearDown() {
    dataService.deleteDataBase();
  }

  /**
   * Test obtener una casilla existente.
   *
   * <p>Verifica que la posición 1 esté correctamente insertada en los inserts.
   */
  @Test
  void testCasillaFromPositionValid() throws SQLException {
    Casilla casilla = repository.casillaFromPosition(1);

    assertNotNull(casilla, "La casilla obtenida no debe ser null");
    assertEquals(1, casilla.getPosicionTablero(), "La posición de la casilla debe ser 1");
    assertNotNull(casilla.getNombreCasilla(), "El nombre de la casilla no debe ser null");
    assertNotNull(casilla.getTipoCasilla(), "El tipo de la casilla no debe ser null");
  }

  /** Test para posición menor a 1, debe lanzar RuntimeException. */
  @Test
  void testCasillaFromPositionInvalidLow() {
    assertThrows(
        SQLException.class,
        () -> repository.casillaFromPosition(0),
        "Debe lanzar SQLException para posición menor que 1");
  }

  /** Test para posición mayor a 40, debe lanzar RuntimeException. */
  @Test
  void testCasillaFromPositionInvalidHigh() {
    assertThrows(
        SQLException.class,
        () -> repository.casillaFromPosition(41),
        "Debe lanzar SQLException para posición mayor que 40");
  }

  /**
   * Test para una posición válida que no existe en la base de datos.
   *
   * <p>Se limpia la tabla Casilla y se insertan solo algunas posiciones conocidas.
   */
  @Test
  void testCasillaFromPositionNonexistentInDB() {
    try (Connection conn = dataService.createConnection();
        Statement stmt = conn.createStatement()) {

      // Limpiar solo la tabla Casilla
      stmt.execute("DELETE FROM Casilla");

      // Asegurar que el Tipo exista para poder insertar casilla
      stmt.execute("MERGE INTO TipoCasilla (TipoID, TipoNombre) KEY (TipoID) VALUES (1, 'SALIDA')");

      // Insertamos solo posición 1
      stmt.execute(
          "INSERT INTO Casilla (PosicionTablero, NombreCasilla, TipoCasilla) VALUES (1, 'Salida', 1)");

    } catch (SQLException e) {
      fail("Error al insertar datos de prueba: " + e.getMessage());
    }

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> repository.casillaFromPosition(5),
            "Debería lanzar IllegalStateException si la posición no existe en la base de datos");
    assertTrue(
        exception.getMessage().contains("No existe casilla en posición 5"),
        "El mensaje de la excepción debe indicar la posición que no existe");
  }
}
