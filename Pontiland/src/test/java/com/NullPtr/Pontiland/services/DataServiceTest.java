package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.entities.SavedGame;
import com.NullPtr.Pontiland.utils.PropertiesReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.List;
import org.junit.jupiter.api.*;
import org.mockito.*;

/**
 * Suite de pruebas unitarias para {@link DataService}.
 *
 * <p>La clase utiliza Mockito para simular conexiones JDBC, statements y prepared statements,
 * además de crear archivos reales dentro de <strong>target/test-classes</strong> para verificar el
 * funcionamiento de métodos que dependen del sistema de archivos.
 *
 * <p>Se usa reflexión para acceder a campos privados del servicio y verificar su inicialización.
 */
class DataServiceTest {

  /** Instancia del servicio bajo prueba con dependencias mockeadas. */
  @InjectMocks DataService dataService;

  /** Conexión JDBC simulada. */
  @Mock Connection mockConnection;

  /** Statement para ejecutar SQL simulado. */
  @Mock Statement mockStatement;

  /** PreparedStatement simulado. */
  @Mock PreparedStatement mockPreparedStatement;

  /** Carpeta real para almacenar archivos de prueba. */
  Path savesDirPath;

  /**
   * Configura mocks, crea directorios reales y prepara un DataService espiado para interceptar
   * llamadas a createConnection().
   */
  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

    // Crear carpeta real target/test-classes/saves
    savesDirPath = Paths.get("target/test-classes/saves/");
    Files.createDirectories(savesDirPath);

    // Crear DataService real y convertirlo en spy
    dataService = spy(new DataService("jdbc:h2:mem:testdb"));

    // Forzar que el método createConnection() del spy devuelva mockConnection
    doReturn(mockConnection).when(dataService).createConnection();

    // Configuración básica para la conexión simulada
    when(mockConnection.createStatement()).thenReturn(mockStatement);
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
  }

  /**
   * Verifica que crear una conexión mediante createConnection() no lance excepción y devuelva un
   * objeto no nulo.
   */
  @Test
  void testCreateConnection() {
    assertDoesNotThrow(
        () -> {
          Connection conn = dataService.createConnection();
          assertNotNull(conn, "La conexión creada no debe ser null");
        });
  }

  /**
   * Verifica que el constructor de DataService cargue correctamente los valores provenientes de
   * PropertiesReader.
   */
  @Test
  void testConstructorInitializesProperties() throws Exception {
    try (MockedStatic<PropertiesReader> mockedProps = mockStatic(PropertiesReader.class)) {
      mockedProps.when(() -> PropertiesReader.getProperty("saves")).thenReturn("mock/savesDir");
      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.ddl"))
          .thenReturn("mock/ddl.sql");
      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.inserts"))
          .thenReturn("mock/inserts.sql");

      DataService ds = new DataService("jdbc:h2:mem:testdb");

      Field savesDirField = DataService.class.getDeclaredField("savesDir");
      savesDirField.setAccessible(true);
      assertEquals("mock/savesDir", savesDirField.get(ds));

      Field ddlResourceField = DataService.class.getDeclaredField("ddlResource");
      ddlResourceField.setAccessible(true);
      assertEquals("mock/ddl.sql", ddlResourceField.get(ds));

      Field insResourceField = DataService.class.getDeclaredField("insResource");
      insResourceField.setAccessible(true);
      assertEquals("mock/inserts.sql", insResourceField.get(ds));

      Field urlField = DataService.class.getDeclaredField("url");
      urlField.setAccessible(true);
      assertEquals("jdbc:h2:mem:testdb", urlField.get(ds));
    }
  }

  /**
   * Verifica que setUrl() actualice el campo privado 'url'.
   *
   * @throws Exception si reflexión falla.
   */
  @Test
  void testSetUrlUpdatesField() throws Exception {
    DataService ds = new DataService("jdbc:h2:mem:testdb");
    ds.setUrl("jdbc:h2:mem:newdb");

    Field urlField = DataService.class.getDeclaredField("url");
    urlField.setAccessible(true);
    assertEquals("jdbc:h2:mem:newdb", urlField.get(ds));
  }

  /** Verifica que getUrl() devuelva correctamente el valor establecido. */
  @Test
  void testGetUrlReturnsCorrectValue() {
    DataService ds = new DataService("jdbc:h2:mem:testdb");
    assertEquals("jdbc:h2:mem:testdb", ds.getUrl());

    ds.setUrl("jdbc:h2:mem:newdb");
    assertEquals("jdbc:h2:mem:newdb", ds.getUrl());
  }

  /**
   * Verifica que loadDataBase() no lance excepción cuando el archivo no existe. Debe simplemente
   * ignorar la operación.
   */
  @Test
  void testLoadDataBaseFileDoesNotExist() {
    assertDoesNotThrow(() -> dataService.loadDataBase("archivo_inexistente.sql"));
  }

  /**
   * Verifica que newDataBase() lance excepción cuando los recursos (DDL e inserts) no existen. Se
   * logra mockeando PropertiesReader para devolver rutas inválidas.
   */
  @Test
  void testNewDatabase_resourcesMissing() throws Exception {

    try (MockedStatic<PropertiesReader> mockedProps = mockStatic(PropertiesReader.class)) {

      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.ddl"))
          .thenReturn("/archivo_inexistente_ddl.sql");

      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.inserts"))
          .thenReturn("/archivo_inexistente_inserts.sql");

      DataService ds = new DataService("jdbc:h2:mem:testdb");

      assertThrows(RuntimeException.class, ds::newDataBase);
    }
  }

  /**
   * Verifica que loadDataBase() no intente preparar un statement cuando el archivo no existe en el
   * sistema de archivos.
   */
  @Test
  void testLoadDatabase_fileNotExists() throws Exception {
    assertDoesNotThrow(() -> dataService.loadDataBase("noexiste.sql"));
    verify(mockConnection, never()).prepareStatement(anyString());
  }

  /**
   * Verifica que loadDataBase() procese correctamente un archivo SQL existente y ejecute las
   * sentencias sobre un PreparedStatement mockeado.
   */
  @Test
  void testLoadDatabase_success() throws Exception {
    Path file = savesDirPath.resolve("testload.sql");
    Files.writeString(file, "SELECT 1;");

    Field savesDirField = DataService.class.getDeclaredField("savesDir");
    savesDirField.setAccessible(true);
    savesDirField.set(dataService, savesDirPath.toAbsolutePath().toString() + File.separator);

    assertDoesNotThrow(() -> dataService.loadDataBase("testload.sql"));

    verify(mockPreparedStatement).setString(eq(1), contains("testload.sql"));
    verify(mockPreparedStatement).executeUpdate();
  }

  /**
   * Verifica que saveDataBase() lance excepción cuando preparar el statement SQL falla.
   *
   * @throws Exception si la configuración mock falla.
   */
  @Test
  void testSaveDatabase_error() throws Exception {
    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
    assertThrows(RuntimeException.class, () -> dataService.saveDataBase(22));
  }

  /**
   * Verifica que deleteDataBase() ejecute correctamente la operación de borrado total de objetos.
   */
  @Test
  void testDeleteDatabase_success() throws Exception {
    assertDoesNotThrow(() -> dataService.deleteDataBase());
    verify(mockStatement).execute("DROP ALL OBJECTS");
  }

  /** Verifica que deleteDataBase() lance excepción en caso de fallo SQL. */
  @Test
  void testDeleteDatabase_error() throws Exception {
    when(mockConnection.createStatement()).thenThrow(new SQLException("fail"));
    assertThrows(RuntimeException.class, () -> dataService.deleteDataBase());
  }

  /** Verifica que se devuelve una lista vacía cuando no existen archivos de partidas guardadas. */
  @Test
  void testListarPartidasPasadas_noFiles() throws Exception {
    try (MockedStatic<PropertiesReader> mocked = mockStatic(PropertiesReader.class)) {

      // Hacer que "saves" apunte a la carpeta de test que ya usamos
      mocked.when(() -> PropertiesReader.getProperty("saves")).thenReturn("/saves");

      // Asegurar que la carpeta esté vacía
      Files.walk(savesDirPath).filter(Files::isRegularFile).forEach(p -> p.toFile().delete());

      List<SavedGame> list = dataService.listarPartidasPasadas();
      assertTrue(list.isEmpty(), "Debe devolver lista vacía al no haber archivos");
    }
  }

  /**
   * Verifica que un archivo válido yyyyMMddHHmmss.sql sea formateado correctamente y convertido en
   * un SavedGame.
   */
  @Test
  void testListarPartidasPasadas_validDate() throws Exception {
    try (MockedStatic<PropertiesReader> mocked = mockStatic(PropertiesReader.class)) {

      mocked.when(() -> PropertiesReader.getProperty("saves")).thenReturn("/saves");

      // Crear archivo válido
      Path valid = savesDirPath.resolve("20250101123000.sql");
      Files.writeString(valid, "-- OK");

      List<SavedGame> list = dataService.listarPartidasPasadas();

      assertEquals(1, list.size(), "Debe encontrar un archivo válido");

      SavedGame sg = list.get(0);

      assertEquals("20250101123000.sql", sg.id, "El ID debe ser el nombre del archivo");

      assertEquals("2025-01-01 12:30:00", sg.titulo, "El título debe ser la fecha formateada");
    }
  }

  /**
   * Crea un archivo dentro de target/test-classes con contenido específico.
   *
   * @param name Nombre del archivo.
   * @param content Contenido escrito en el archivo.
   * @throws IOException si ocurre un error al escribir.
   */
  private void createResourceFile(String name, String content) throws IOException {
    Path path = Paths.get("target/test-classes/" + name);
    Files.write(path, content.getBytes(StandardCharsets.UTF_8));
  }
}
