package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Ficha;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.services.IDataService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

  private long partidaID;

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
   * @param icono Identificador del icono asociado al jugador.
   * @throws RuntimeException si ocurre un error de SQL durante la inserción.
   */
  @Override
  public void newPlayer(Jugador newPlayer, int icono) throws SQLException {
    String nuevoJugador =
        "INSERT INTO Jugador(NumJugador, NombreJugador, IconoID, Partida) VALUES(?, ? , ? , ? )";
    try (Connection conn = dataService.createConnection();
        PreparedStatement crearJugador = conn.prepareStatement(nuevoJugador)) {
      crearJugador.setInt(1, newPlayer.getJugadorId());
      crearJugador.setString(2, newPlayer.getNombreJugador());
      crearJugador.setInt(3, icono);
      crearJugador.setLong(4, partidaID);
      crearJugador.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Actualiza el jugador activo en una partida específica.
   *
   * @throws SQLException si ocurre un error al acceder a la base de datos.
   */
  @Override
  public void changeActivePlayer(int nuevoID) throws SQLException {
    String cambiarTurno = "UPDATE JugadorActivo SET JugadorActualID = ? WHERE PartidaID = ?";
    try (Connection conn = dataService.createConnection();
        PreparedStatement cambiarJugador = conn.prepareStatement(cambiarTurno)) {
      cambiarJugador.setInt(1, nuevoID);
      cambiarJugador.setLong(2, partidaID);
      cambiarJugador.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Obtiene el ID del jugador activo en la partida actual.
   *
   * @return ID del jugador activo.
   * @throws SQLException si ocurre un error al acceder a la base de datos o si no se encuentra un
   *     jugador activo.
   */
  @Override
  public int getActivePlayer() throws SQLException {
    final String sql = "SELECT JugadorActualID FROM JugadorActivo WHERE PartidaID = ?";
    try (Connection conn = dataService.createConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, partidaID);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("JugadorActualID");
        } else {
          return -1;
        }
      }
    }
  }

  @Override
  public void goToJail(int jugadorID) throws SQLException {
    Connection conn = dataService.createConnection();
    String consulta =
        "UPDATE Jugador SET Posicion = ?, Encarcelado = ? WHERE JugadorID = ? AND Partida = ?";
    try {
      PreparedStatement stmt = conn.prepareStatement(consulta);
      stmt.setInt(1, 11); // Posición de la cárcel
      stmt.setBoolean(2, true); // Encarcelado
      stmt.setInt(3, jugadorID);
      stmt.setLong(4, partidaID);

      stmt.executeUpdate();

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
   * Obtiene el ID del jugador asociado a un número de jugador específico dentro de la partida
   * actual.
   *
   * @param numJugador Número del jugador cuyo ID se desea obtener.
   * @return ID del jugador correspondiente al número de jugador proporcionado.
   * @throws SQLException si ocurre un error al acceder a la base de datos o si no se encuentra el
   *     jugador.
   */
  @Override
  public int getPlayerIdByNumJugador(int numJugador) throws SQLException {
    String consulta = "SELECT JugadorID FROM Jugador WHERE NumJugador = ? AND Partida = ?";
    try (Connection conn = dataService.createConnection();
        PreparedStatement stmt = conn.prepareStatement(consulta)) {
      stmt.setInt(1, numJugador);
      stmt.setLong(2, partidaID);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("JugadorID");
        }
        throw new RuntimeException("No se encontró el jugador con NumJugador: " + numJugador);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Obtiene el número de jugadores (NumJugador) asociado a un ID de jugador específico dentro de la
   * partida actual.
   *
   * @param playerId ID del jugador cuyo número de jugador se desea obtener.
   * @return Número de jugadores (NumJugador) correspondiente al ID proporcionado.
   * @throws SQLException si ocurre un error al acceder a la base de datos o si no se encuentra el
   *     jugador.
   */
  @Override
  public int getNumJugadorByPlayerId(int playerId) throws SQLException {
    Connection conn = dataService.createConnection();
    String consulta = "SELECT NumJugador FROM Jugador WHERE JugadorID = ? AND Partida = ?";
    try {
      PreparedStatement stmt = conn.prepareStatement(consulta);
      stmt.setInt(1, playerId);
      stmt.setLong(2, partidaID);
      var rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt("NumJugador");
      } else {
        throw new RuntimeException("No se encontró el jugador con playerID: " + playerId);
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

  /**
   * Inserta un jugador activo en la tabla JugadorActivo para la partida actual.
   *
   * @param jugadorID ID del jugador que se va a establecer como activo.
   * @throws SQLException si ocurre un error al acceder a la base de datos.
   */
  @Override
  public void newActivePlayer(int jugadorID) throws SQLException {
    String insertarJugadorActivo =
        "INSERT INTO JugadorActivo(JugadorActualID, PartidaID) VALUES(?, ?)";
    try (Connection conn = dataService.createConnection();
        PreparedStatement crearJugadorActivo = conn.prepareStatement(insertarJugadorActivo)) {
      crearJugadorActivo.setInt(1, jugadorID);
      crearJugadorActivo.setLong(2, partidaID);
      crearJugadorActivo.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Obtiene un jugador por su ID dentro de la partida actual.
   *
   * @param jugadorID ID del jugador a buscar.
   * @return Objeto Jugador con la información del jugador encontrado.
   * @throws SQLException si ocurre un error al acceder a la base de datos o si el jugador no se
   *     encuentra.
   */
  @Override
  public Jugador getJugadorByID(int jugadorID) throws SQLException {
    Connection conn = dataService.createConnection();
    String obtenerJugadores =
        "SELECT JugadorID, NumJugador, NombreJugador, IconoID, Posicion, Encarcelado, Dinero, Partida FROM JUGADOR WHERE Partida = ? AND JugadorID = ?";

    try {
      PreparedStatement preparedStatement = conn.prepareStatement(obtenerJugadores);
      preparedStatement.setLong(1, partidaID);
      preparedStatement.setInt(2, jugadorID);
      ResultSet rs = preparedStatement.executeQuery();
      if (!rs.next()) {
        throw new SQLException(
            "Jugador no encontrado (partida=" + partidaID + ", id=" + jugadorID + ")");
      }
      Jugador jugador =
          new Jugador(
              rs.getInt("JugadorID"),
              rs.getInt("NumJugador"),
              rs.getString("NombreJugador"),
              rs.getInt("IconoID"),
              rs.getInt("Posicion"),
              rs.getBoolean("Encarcelado"),
              rs.getInt("Dinero"),
              rs.getLong("Partida"));

      return jugador;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setPartidaID(long partidaID) {
    this.partidaID = partidaID;
  }

  @Override
  public Ficha[] getFichas(int numJugadores) throws SQLException {
    Connection conn = dataService.createConnection();
    Ficha[] fichas = new Ficha[numJugadores];
    String consulta =
        "SELECT Jugador.JugadorID, Jugador.NumJugador, Icono.IconoID, Icono.IconoNombre "
            + "FROM Jugador "
            + "INNER JOIN Icono ON Icono.IconoID = Jugador.IconoID "
            + "WHERE Partida = ? ORDER BY NumJugador ASC";
    try {
      PreparedStatement stmt = conn.prepareStatement(consulta);
      stmt.setLong(1, partidaID);
      var rs = stmt.executeQuery();
      int index = 0;
      while (rs.next()) {
        fichas[index] =
            new Ficha(rs.getInt("JugadorID"), rs.getInt("IconoID"), rs.getString("IconoNombre"));
        index++;
      }
      return fichas;
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

  @Override
  public void updateJugador(Jugador jugador) throws SQLException {

    Connection conn = dataService.createConnection();
    String consulta =
        "UPDATE Jugador SET Posicion = ?, Encarcelado = ?, Dinero = ? WHERE JugadorID = ? AND Partida = ?";
    try {
      PreparedStatement stmt = conn.prepareStatement(consulta);
      stmt.setInt(1, jugador.getPosicion());
      stmt.setBoolean(2, jugador.getEstado());
      stmt.setInt(3, jugador.getDinero());
      stmt.setInt(4, jugador.getJugadorId());
      stmt.setLong(5, partidaID);

      stmt.executeUpdate();

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

  public void rentPaymentTransaction(Jugador j1, Jugador j2) throws SQLException {
    Connection conn = dataService.createConnection();
    String updateSql = "UPDATE Jugador SET Dinero = ? WHERE NumJugador = ? AND Partida = ?";
    boolean originalAutoCommit = conn.getAutoCommit();
    try {
      conn.setAutoCommit(false);

      try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
        // Actualizar primer jugador
        ps.setInt(1, j1.getDinero());
        ps.setInt(2, j1.getNumJugador());
        ps.setLong(3, j1.getPartida());
        ps.executeUpdate();

        // Actualizar segundo jugador
        ps.setInt(1, j2.getDinero());
        ps.setInt(2, j2.getNumJugador());
        ps.setLong(3, j2.getPartida());
        ps.executeUpdate();
      }

      conn.commit();
    } catch (SQLException ex) {
      try {
        conn.rollback();
      } catch (SQLException rbEx) {
        // ignorar o registrar según necesidad
      }
      throw ex;
    } finally {
      conn.setAutoCommit(originalAutoCommit);
      conn.close();
    }
  }
}
