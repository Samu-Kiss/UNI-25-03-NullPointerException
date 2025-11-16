package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.services.IDataService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Repositorio encargado de gestionar las operaciones relacionadas con la entidad Partida en la base
 * de datos, como la creación y consulta de partidas.
 */
public class PartidaRepository implements IPartidaRepository {
  /**
   * Servicio de acceso a datos para la gestión de conexiones y operaciones con la base de datos.
   */
  IDataService dataService;

  long partidaID;

  /**
   * Crea una nueva instancia de {@code PartidaRepository} con el servicio de datos especificado.
   *
   * @param dataService Servicio de datos utilizado para abrir conexiones y ejecutar sentencias SQL.
   */
  public PartidaRepository(IDataService dataService) {
    this.dataService = dataService;
  }

  /**
   * Crea e inserta una nueva partida en la base de datos.
   *
   * <p>Genera un identificador de partida único basado en la fecha y hora actual con el patrón
   * {@code yyyyMMddHHmmss} y lo inserta en la tabla {@code PARTIDA} junto con el número de
   * jugadores.
   *
   * @param numJugadores el número de jugadores que participarán en la partida (debe ser mayor que
   *     cero según las reglas del dominio).
   * @return el identificador (PartidaID) generado para la nueva partida.
   * @throws SQLException si ocurre cualquier error al abrir la conexión o ejecutar la sentencia
   *     SQL.
   */
  public long newPartida(int numJugadores) throws SQLException {
    LocalDateTime myDateObj = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    String formatted = myDateObj.format(formatter);
    partidaID = Long.parseLong(formatted);

    String creacionPartida = "INSERT INTO PARTIDA(PartidaID, NumeroJugadores) VALUES( ? , ? )";
    try (Connection conn = dataService.createConnection();
        PreparedStatement crearPartida = conn.prepareStatement(creacionPartida)) {
      crearPartida.setLong(1, partidaID);
      crearPartida.setInt(2, numJugadores);
      crearPartida.executeUpdate();
    } catch (SQLException e) {
      throw new SQLException("Error al intentar crear una nueva partida", e);
    }
    return partidaID;
  }

  @Override
  /**
   * Obtiene el número de jugadores asociado a la partida actualmente referenciada por {@code
   * partidaID} consultando la tabla {@code Partida}.
   *
   * @return el número de jugadores de la partida.
   * @throws SQLException si no existe una partida con el {@code partidaID} actual o si ocurre un
   *     error al consultar la base de datos.
   */
  public int getNumJugadores() throws SQLException {
    String consulta = "SELECT NumeroJugadores FROM Partida WHERE PartidaID = ?";
    try (Connection conn = dataService.createConnection();
        PreparedStatement stmt = conn.prepareStatement(consulta)) {
      stmt.setLong(1, partidaID);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("NumeroJugadores");
        } else {
          throw new SQLException("No se encontro la partida con Id=" + partidaID);
        }
      }
    } catch (SQLException e) {
      throw new SQLException(
          "Error al intentar tomar el numero de jugadores de la partida con Id=" + partidaID, e);
    }
  }

  /**
   * Devuelve la instancia de {@link IDataService} que proporciona las conexiones a la base de
   * datos.
   *
   * <p>Útil para pruebas unitarias o cuando se necesita acceder al servicio de datos desde fuera
   * del repositorio.
   *
   * @return la instancia de {@code IDataService} inyectada en este repositorio.
   */
  public IDataService getDataService() {
    return dataService;
  }

  @Override
  /**
   * Obtiene el identificador de la partida actualmente almacenado en este repositorio.
   *
   * <p>El valor se establece al llamar a {@link #newPartida(int)}. Si no se ha creado ninguna
   * partida todavía, el valor por defecto es {@code 0}.
   *
   * @return el {@code partidaID} actual o {@code 0} si aún no se ha generado ninguno.
   */
  public long getPartidaID() {
    return partidaID;
  }
}
