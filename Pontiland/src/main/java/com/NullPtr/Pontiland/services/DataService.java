package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.SavedGame;
import com.NullPtr.Pontiland.utils.PropertiesReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class DataService implements IDataService {
  String url;

  private String savesDir;
  private String ddlResource;
  private String insResource;

  /**
   * Constructor que inicializa el servicio de datos con una URL de conexión específica.
   *
   * @param url URL de la base de datos a utilizar para la conexión.
   */
  public DataService(String url) {

    this.url = url;

    this.savesDir = PropertiesReader.getProperty("saves");
    this.ddlResource = PropertiesReader.getProperty("nuevaPartida.ddl");
    this.insResource = PropertiesReader.getProperty("nuevaPartida.inserts");
  }

  /**
   * Establece la URL de la base de datos.
   *
   * @param url Nueva URL de la base de datos.
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Obtiene la URL actual de la base de datos.
   *
   * @return URL de la base de datos.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Crea y retorna una nueva conexión a la base de datos utilizando la URL configurada.
   *
   * @return Objeto Connection para interactuar con la base de datos.
   * @throws RuntimeException si ocurre un error de SQL al intentar conectarse.
   */
  public Connection createConnection() {
    try {
      return DriverManager.getConnection(url, "sa", "");

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Crea una nueva base de datos para una partida, ejecutando los scripts de DDL y de inserciones
   * iniciales.
   *
   * <p>Lee y ejecuta los archivos DDL.sql e Insertions.sql desde la carpeta correspondiente para
   * inicializar la estructura y los datos de la partida.
   *
   * @throws RuntimeException si ocurre un error de SQL o de lectura de archivos.
   */
  public void newDataBase() {
    try (Connection conn = createConnection();
        Statement stmt = conn.createStatement();
        InputStream ddlStream = getResourceStream(ddlResource);
        InputStream insStream = getResourceStream(insResource)) {

      String schemaSql = new String(ddlStream.readAllBytes(), StandardCharsets.UTF_8);
      stmt.execute(schemaSql);

      String dataSql = new String(insStream.readAllBytes(), StandardCharsets.UTF_8);
      stmt.execute(dataSql);

    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Carga el estado de una partida desde un archivo SQL seleccionado.
   *
   * <p>El archivo debe encontrarse en la ruta 'src/main/resources/SQL/PartidasPasadas/'.
   *
   * @param archivoSeleccionado Nombre del archivo SQL de la partida a cargar.
   * @throws RuntimeException si ocurre un error de SQL durante la carga.
   */
  public void loadDataBase(String archivoSeleccionado) {
    String ubicacionArchivo = savesDir + archivoSeleccionado;
    Path path = Paths.get(ubicacionArchivo);
    if (!Files.exists(path) || !Files.isRegularFile(path)) {
      return;
    }

    try (Connection conn = createConnection();
        PreparedStatement cargarPartida = conn.prepareStatement("RUNSCRIPT FROM ?")) {
      cargarPartida.setString(1, ubicacionArchivo);
      cargarPartida.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Lista los archivos de partidas pasadas disponibles en la carpeta de recursos.
   *
   * <p>Busca archivos con extensión .sql y extrae la fecha de creación del nombre del archivo,
   * devolviendo un mapa con la fecha formateada como clave y el nombre del archivo como valor.
   *
   * @return Mapa con fechas formateadas y nombres de archivos de partidas pasadas.
   */
  public List<SavedGame> listarPartidasPasadas() {
    Map<String, String> mapaArchivo = new LinkedHashMap<>();

    File scriptsPartidas =
        new File(this.getClass().getResource(PropertiesReader.getProperty("saves")).getFile());
    File[] archivos = scriptsPartidas.listFiles();

    if (archivos != null) {
      for (File archivo : archivos) {
        if (archivo.isFile()) {
          String fileName = archivo.getName();

          // Ejemplo de posible nombre de un archivo de partida pasada 20251003155300.sql
          if (fileName.endsWith(".sql")) {
            String fechaRepresentada = fileName.replace(".sql", "");

            try {
              SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyyMMddHHmmss");
              Date date = formatoEntrada.parse(fechaRepresentada);
              SimpleDateFormat formatoSalida = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
              String formattedDate = formatoSalida.format(date);
              mapaArchivo.put(formattedDate, fileName);

            } catch (ParseException e) {
              System.err.println("Archivo con formato incorrecto: " + fileName);
            }
          }
        }
      }
    }

    List<SavedGame> listaPartidas = new ArrayList<>();

    for (Map.Entry<String, String> entry : mapaArchivo.entrySet()) {
      listaPartidas.add(new SavedGame(entry.getValue(), entry.getKey()));
    }

    return listaPartidas;
  }

  /**
   * Guarda el estado actual de la base de datos de la partida en un archivo SQL.
   *
   * <p>El archivo se almacena en la ruta 'src/main/resources/SQL/PartidasPasadas/' y su nombre
   * corresponde al identificador de la partida seguido de la extensión .sql. Si el archivo ya
   * existe y es un archivo regular, se sobrescribe con el nuevo estado de la base de datos.
   *
   * @param partidaID Identificador único de la partida, utilizado para nombrar el archivo de
   *     guardado.
   * @throws RuntimeException si ocurre un error de SQL durante el proceso de guardado.
   */
  public void saveDataBase(long partidaID) {
    String ubicacionArchivo = savesDir + partidaID + ".sql";
    try (Connection conn = createConnection();
        PreparedStatement guardarPartida = conn.prepareStatement("SCRIPT TO ?")) {
      guardarPartida.setString(1, ubicacionArchivo);
      guardarPartida.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private InputStream getResourceStream(String resourcePath) throws IOException {
    InputStream stream = this.getClass().getResourceAsStream(resourcePath);
    if (stream == null) {
      throw new IOException("No se encontró el recurso: " + resourcePath);
    }
    return stream;
  }
}
