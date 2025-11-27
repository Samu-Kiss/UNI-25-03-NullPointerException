package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.entities.SavedGame;
import com.NullPtr.Pontiland.utils.PropertiesReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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

    // Limpiar archivos existentes
    Files.walk(savesDirPath)
        .filter(Files::isRegularFile)
        .forEach(
            p -> {
              try {
                Files.delete(p);
              } catch (IOException e) {
                // Ignorar errores de limpieza
              }
            });

    // Crear DataService real y convertirlo en spy
    dataService = spy(new DataService("jdbc:h2:mem:testdb"));

    // Forzar que el método createConnection() del spy devuelva mockConnection
    doReturn(mockConnection).when(dataService).createConnection();

    // Configuración básica para la conexión simulada
    when(mockConnection.createStatement()).thenReturn(mockStatement);
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
  }

  /** Limpia los archivos de prueba después de cada test. */
  @AfterEach
  void tearDown() throws Exception {
    if (Files.exists(savesDirPath)) {
      Files.walk(savesDirPath)
          .filter(Files::isRegularFile)
          .forEach(
              p -> {
                try {
                  Files.delete(p);
                } catch (IOException e) {
                  // Ignorar errores de limpieza
                }
              });
    }
  }

  /**
   * Verifica que crear una conexión mediante createConnection() no lance excepción y devuelva un
   * objeto no nulo.
   */
  @Test
  void testCreateConnection() {
    Connection conn = dataService.createConnection();
    assertNotNull(conn, "La conexión creada no debe ser null");
  }

  /** Verifica que createConnection() devuelva null cuando ocurre un SQLException. */
  @Test
  void testCreateConnection_SQLException() {
    DataService realService = new DataService("jdbc:invalid:url");
    Connection conn = realService.createConnection();
    assertNull(conn, "Debe retornar null cuando hay un error de conexión");
  }

  /**
   * Verifica que el constructor de DataService cargue correctamente los valores provenientes de
   * PropertiesReader.
   */
  @Test
  void testConstructorInitializesProperties() throws Exception {
    try (MockedStatic<PropertiesReader> mockedProps = mockStatic(PropertiesReader.class)) {
      mockedProps.when(() -> PropertiesReader.getProperty("saves")).thenReturn("mock/savesDir/");
      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.ddl"))
          .thenReturn("mock/ddl.sql");
      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.inserts"))
          .thenReturn("mock/inserts.sql");

      DataService ds = new DataService("jdbc:h2:mem:testdb");

      Field savesDirField = DataService.class.getDeclaredField("savesDir");
      savesDirField.setAccessible(true);
      assertEquals("mock/savesDir/", savesDirField.get(ds));

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

  /** Verifica que setUrl() actualice el campo privado 'url'. */
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

  /** Verifica que newDataBase() ejecute correctamente los scripts DDL e INSERT. */
  @Test
  void testNewDatabase_success() throws Exception {
    // Crear recursos temporales para DDL e INSERT
    Path tempDir = Paths.get("target/test-classes/temp-resources/");
    Files.createDirectories(tempDir);

    Path ddlFile = tempDir.resolve("test-ddl.sql");
    Path insertFile = tempDir.resolve("test-insert.sql");

    Files.writeString(ddlFile, "CREATE TABLE TEST(ID INT);");
    Files.writeString(insertFile, "INSERT INTO TEST VALUES(1);");

    try (MockedStatic<PropertiesReader> mockedProps = mockStatic(PropertiesReader.class)) {
      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.ddl"))
          .thenReturn("/temp-resources/test-ddl.sql");
      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.inserts"))
          .thenReturn("/temp-resources/test-insert.sql");

      DataService ds = spy(new DataService("jdbc:h2:mem:testdb"));
      doReturn(mockConnection).when(ds).createConnection();

      assertDoesNotThrow(ds::newDataBase);

      verify(mockStatement, times(2)).execute(anyString());
    } finally {
      // Limpiar archivos temporales
      Files.deleteIfExists(ddlFile);
      Files.deleteIfExists(insertFile);
      Files.deleteIfExists(tempDir);
    }
  }

  /**
   * Verifica que newDataBase() lance AssertionError cuando los recursos no existen. El código usa
   * assert que lanza AssertionError en producción si los recursos son null.
   */
  @Test
  void testNewDatabase_resourcesMissing() {
    try (MockedStatic<PropertiesReader> mockedProps = mockStatic(PropertiesReader.class)) {
      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.ddl"))
          .thenReturn("/archivo_inexistente_ddl.sql");
      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.inserts"))
          .thenReturn("/archivo_inexistente_inserts.sql");

      DataService ds = spy(new DataService("jdbc:h2:mem:testdb"));
      doReturn(mockConnection).when(ds).createConnection();

      // El método lanza AssertionError cuando los recursos no existen
      assertThrows(AssertionError.class, ds::newDataBase);
    }
  }

  /** Verifica que newDataBase() maneje SQLException correctamente. */
  @Test
  void testNewDatabase_SQLException() throws Exception {
    when(mockStatement.execute(anyString())).thenThrow(new SQLException("Test error"));

    Path tempDir = Paths.get("target/test-classes/temp-resources/");
    Files.createDirectories(tempDir);

    Path ddlFile = tempDir.resolve("test-ddl.sql");
    Path insertFile = tempDir.resolve("test-insert.sql");

    Files.writeString(ddlFile, "CREATE TABLE TEST(ID INT);");
    Files.writeString(insertFile, "INSERT INTO TEST VALUES(1);");

    try (MockedStatic<PropertiesReader> mockedProps = mockStatic(PropertiesReader.class)) {
      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.ddl"))
          .thenReturn("/temp-resources/test-ddl.sql");
      mockedProps
          .when(() -> PropertiesReader.getProperty("nuevaPartida.inserts"))
          .thenReturn("/temp-resources/test-insert.sql");

      DataService ds = spy(new DataService("jdbc:h2:mem:testdb"));
      doReturn(mockConnection).when(ds).createConnection();

      // El método no lanza excepción, solo registra el error
      assertDoesNotThrow(ds::newDataBase);
    } finally {
      Files.deleteIfExists(ddlFile);
      Files.deleteIfExists(insertFile);
      Files.deleteIfExists(tempDir);
    }
  }

  /** Verifica que loadDataBase() no lance excepción cuando el archivo no existe. */
  @Test
  void testLoadDataBase_fileDoesNotExist() throws Exception {
    assertDoesNotThrow(() -> dataService.loadDataBase("archivo_inexistente.sql"));
    verify(mockConnection, never()).prepareStatement(anyString());
  }

  /** Verifica que loadDataBase() procese correctamente un archivo SQL existente. */
  @Test
  void testLoadDataBase_success() throws Exception {
    Path file = savesDirPath.resolve("testload.sql");
    Files.writeString(file, "SELECT 1;");

    Field savesDirField = DataService.class.getDeclaredField("savesDir");
    savesDirField.setAccessible(true);
    savesDirField.set(dataService, savesDirPath.toAbsolutePath().toString() + File.separator);

    assertDoesNotThrow(() -> dataService.loadDataBase("testload.sql"));

    verify(mockPreparedStatement).setString(eq(1), contains("testload.sql"));
    verify(mockPreparedStatement).executeUpdate();
  }

  /** Verifica que loadDataBase() maneje SQLException correctamente. */
  @Test
  void testLoadDataBase_SQLException() throws Exception {
    Path file = savesDirPath.resolve("testload.sql");
    Files.writeString(file, "SELECT 1;");

    Field savesDirField = DataService.class.getDeclaredField("savesDir");
    savesDirField.setAccessible(true);
    savesDirField.set(dataService, savesDirPath.toAbsolutePath().toString() + File.separator);

    when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test error"));

    // El método no lanza excepción, solo registra el error
    assertDoesNotThrow(() -> dataService.loadDataBase("testload.sql"));
  }

  /** Verifica que saveDataBase() ejecute correctamente el comando SCRIPT TO. */
  @Test
  void testSaveDataBase_success() throws Exception {
    Field savesDirField = DataService.class.getDeclaredField("savesDir");
    savesDirField.setAccessible(true);
    savesDirField.set(dataService, savesDirPath.toAbsolutePath().toString() + File.separator);

    assertDoesNotThrow(() -> dataService.saveDataBase(12345L));

    verify(mockPreparedStatement).setString(eq(1), contains("12345.sql"));
    verify(mockPreparedStatement).execute();
  }

  /** Verifica que saveDataBase() maneje SQLException correctamente. */
  @Test
  void testSaveDataBase_SQLException() throws Exception {
    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test error"));

    // El método no lanza excepción, solo registra el error
    assertDoesNotThrow(() -> dataService.saveDataBase(22L));
  }

  /** Verifica que deleteDataBase() ejecute correctamente DROP ALL OBJECTS. */
  @Test
  void testDeleteDataBase_success() throws Exception {
    assertDoesNotThrow(() -> dataService.deleteDataBase());
    verify(mockStatement).execute("DROP ALL OBJECTS");
  }

  /** Verifica que deleteDataBase() maneje SQLException correctamente. */
  @Test
  void testDeleteDataBase_SQLException() throws Exception {
    when(mockConnection.createStatement()).thenThrow(new SQLException("Test error"));

    // El método no lanza excepción, solo registra el error
    assertDoesNotThrow(() -> dataService.deleteDataBase());
  }

  /** Verifica que se devuelve una lista vacía cuando no existen archivos de partidas guardadas. */
  @Test
  void testListarPartidasPasadas_noFiles() {
    try (MockedStatic<PropertiesReader> mocked = mockStatic(PropertiesReader.class)) {
      mocked.when(() -> PropertiesReader.getProperty("saves")).thenReturn("/saves/");

      List<SavedGame> list = dataService.listarPartidasPasadas();
      assertTrue(list.isEmpty(), "Debe devolver lista vacía al no haber archivos");
    }
  }

  /** Verifica que un archivo válido yyyyMMddHHmmss.sql sea formateado correctamente. */
  @Test
  void testListarPartidasPasadas_validDate() throws Exception {
    try (MockedStatic<PropertiesReader> mocked = mockStatic(PropertiesReader.class)) {
      mocked.when(() -> PropertiesReader.getProperty("saves")).thenReturn("/saves/");

      Path valid = savesDirPath.resolve("20250101123000.sql");
      Files.writeString(valid, "-- OK");

      List<SavedGame> list = dataService.listarPartidasPasadas();

      assertEquals(1, list.size(), "Debe encontrar un archivo válido");

      SavedGame sg = list.get(0);
      assertEquals("20250101123000.sql", sg.id, "El ID debe ser el nombre del archivo");
      assertEquals("2025-01-01 12:30:00", sg.titulo, "El título debe ser la fecha formateada");
    }
  }

  /** Verifica que archivos con formato de nombre inválido sean ignorados. */
  @Test
  void testListarPartidasPasadas_invalidFormat() throws Exception {
    try (MockedStatic<PropertiesReader> mocked = mockStatic(PropertiesReader.class)) {
      mocked.when(() -> PropertiesReader.getProperty("saves")).thenReturn("/saves/");

      // Crear archivos con formatos inválidos
      Files.writeString(savesDirPath.resolve("invalid.sql"), "-- invalid");
      Files.writeString(savesDirPath.resolve("20250101.sql"), "-- too short");
      Files.writeString(savesDirPath.resolve("notasqlfile.txt"), "-- not sql");

      List<SavedGame> list = dataService.listarPartidasPasadas();

      assertTrue(list.isEmpty(), "No debe encontrar archivos con formato inválido");
    }
  }

  /** Verifica que múltiples archivos válidos se listen correctamente. */
  @Test
  void testListarPartidasPasadas_multipleFiles() throws Exception {
    try (MockedStatic<PropertiesReader> mocked = mockStatic(PropertiesReader.class)) {
      mocked.when(() -> PropertiesReader.getProperty("saves")).thenReturn("/saves/");

      Files.writeString(savesDirPath.resolve("20250101120000.sql"), "-- file 1");
      Files.writeString(savesDirPath.resolve("20250102150000.sql"), "-- file 2");
      Files.writeString(savesDirPath.resolve("20250103180000.sql"), "-- file 3");

      List<SavedGame> list = dataService.listarPartidasPasadas();

      assertEquals(3, list.size(), "Debe encontrar 3 archivos válidos");
    }
  }

  /**
   * Verifica que listarPartidasPasadas() devuelva lista vacía cuando no hay archivos SQL válidos.
   */
  @Test
  void testListarPartidasPasadas_emptyDirectory() throws Exception {
    try (MockedStatic<PropertiesReader> mocked = mockStatic(PropertiesReader.class)) {
      mocked.when(() -> PropertiesReader.getProperty("saves")).thenReturn("/saves/");

      // Asegurarse de que no hay archivos .sql
      Files.walk(savesDirPath)
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".sql"))
          .forEach(
              p -> {
                try {
                  Files.delete(p);
                } catch (IOException e) {
                  // Ignorar
                }
              });

      // Crear un archivo que no sea .sql
      Files.writeString(savesDirPath.resolve("readme.txt"), "not a sql file");

      List<SavedGame> list = dataService.listarPartidasPasadas();
      assertTrue(list.isEmpty(), "Debe retornar lista vacía cuando no hay archivos .sql válidos");
    }
  }
}
