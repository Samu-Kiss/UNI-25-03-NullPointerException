package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.services.IDataService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Repositorio encargado de gestionar las operaciones relacionadas con la entidad Jugador en la base
 * de datos, como la inserción de nuevos jugadores.
 */
public class JugadorRepository implements IJugadorRepository {
  /**
   * Servicio de acceso a datos para la gestión de conexiones y operaciones con la base de datos.
   */
  IDataService dataService;

  /**
   * Constructor que permite la inyección del servicio de datos.
   *
   * @param dataService Servicio de datos utilizado para la conexión a la base de datos.
   */
  public JugadorRepository(IDataService dataService) {
    this.dataService = dataService;
  }

  /**
   * Inserta un nuevo jugador en la base de datos asociado a una partida y un icono.
   *
   * @param newPlayer Objeto Jugador que contiene la información del nuevo jugador.
   * @param partidaID Identificador de la partida a la que pertenece el jugador.
   * @param icono Identificador del icono asociado al jugador.
   * @throws RuntimeException si ocurre un error de SQL durante la inserción.
   */
  public void newPlayer(Jugador newPlayer, long partidaID, int icono) throws SQLException {
    Connection conn = dataService.createConnection();
    String nuevoJugador =
        "INSERT INTO Jugador(NumJugador, NombreJugador, IconoID, Partida) VALUES(?, ? , ? , ? )";
    try {
      PreparedStatement crearJugador = conn.prepareStatement(nuevoJugador);
      crearJugador.setInt(1, newPlayer.getJugadorId());
      crearJugador.setString(2, newPlayer.getNombreJugador());
      crearJugador.setInt(3, icono);
      crearJugador.setLong(4, partidaID);
      crearJugador.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        conn.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Actualiza el jugador activo en una partida específica.
   *
   * @param id Identificador del nuevo jugador activo.
   * @param partidaID Identificador de la partida en la que se realiza el cambio.
   * @throws SQLException si ocurre un error al acceder a la base de datos.
   */
  public void changeActivePlayer(int id, long partidaID) throws SQLException {
    Connection conn = dataService.createConnection();
    String cambiarTurno = "UPDATE JugadorActivo SET JugadorActualID = ? WHERE PartidaID = ?";
    try {
      PreparedStatement cambiarJugador = conn.prepareStatement(cambiarTurno);
      cambiarJugador.setInt(1, id);
      cambiarJugador.setLong(2, partidaID);
      cambiarJugador.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        conn.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public int getPlayerIdByNumJugador(int numJugador, long partidaID) throws SQLException {
    Connection conn = dataService.createConnection();
    String consulta = "SELECT JugadorID FROM Jugador WHERE NumJugador = ? AND Partida = ?";
    try {
      PreparedStatement stmt = conn.prepareStatement(consulta);
      stmt.setInt(1, numJugador);
      stmt.setLong(2, partidaID);
      var rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt("JugadorID");
      } else {
        throw new RuntimeException("No se encontró el jugador con NumJugador: " + numJugador);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        conn.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void insertActivePlayer(int jugadorID, long partidaID) throws SQLException {
    Connection conn = dataService.createConnection();
    String insertarJugadorActivo =
        "INSERT INTO JugadorActivo(JugadorActualID, PartidaID) VALUES(?, ?)";
    try {
      PreparedStatement crearJugadorActivo = conn.prepareStatement(insertarJugadorActivo);
      crearJugadorActivo.setInt(1, jugadorID);
      crearJugadorActivo.setLong(2, partidaID);
      crearJugadorActivo.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        conn.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }
  /**
   * public Jugador obtenerJugador(int id){ Connection conn = dataService.createConnection(); String
   * jugador = "SELECT * FROM JUGADOR LEFT JOIN ADQUISICIONES ON
   * JUGADOR.JUGADORID=ADQUISICIONES.JUGADORID WHERE JUGADOR.JUGADORID= ?"; try { PreparedStatement
   * consultarJugador = conn.prepareStatement(jugador); consultarJugador.setInt(1, id); ResultSet
   * rsPartida = consultarJugador.executeQuery(); Jugador jugador1 = new
   * Jugador(rsPartida.getInt("Jugador.Dinero"), rsPartida.getString("Jugador.Nombre"), ) } catch
   * (SQLException e) { throw new RuntimeException(e); } }
   */
}
