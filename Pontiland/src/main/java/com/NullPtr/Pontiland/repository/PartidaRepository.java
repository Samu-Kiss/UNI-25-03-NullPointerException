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
   * Constructor que permite la inyección del servicio de datos.
   *
   * @param dataService Servicio de datos utilizado para la conexión a la base de datos.
   */
  public PartidaRepository(IDataService dataService) {
    this.dataService = dataService;
  }

  /**
   * Crea una nueva partida en la base de datos con el número de jugadores especificado. Genera un
   * identificador único basado en la fecha y hora actual.
   *
   * @param numJugadores Número de jugadores que participarán en la partida.
   * @return El identificador único de la partida creada.
   * @throws RuntimeException si ocurre un error de SQL durante la inserción.
   */
  public long newPartida(int numJugadores) {
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
      throw new RuntimeException(e);
    }
    return partidaID;
  }

  @Override
  public int getNumJugadores() {
    String consulta = "SELECT NumeroJugadores FROM Partida WHERE PartidaID = ?";
    try (Connection conn = dataService.createConnection();
        PreparedStatement stmt = conn.prepareStatement(consulta)) {
      stmt.setLong(1, partidaID);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("NumeroJugadores");
        } else {
          throw new RuntimeException(
              "No se encontró un jugador activo para la partida: " + partidaID);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public IDataService getDataService() {
    return dataService;
  }

  @Override
  public long getPartidaID() {
    return partidaID;
  }
}
