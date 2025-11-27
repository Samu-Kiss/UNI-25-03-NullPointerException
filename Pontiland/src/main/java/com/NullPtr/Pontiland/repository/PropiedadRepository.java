package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.services.IDataService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PropiedadRepository implements IPropiedadRepository {
  private IDataService dataService;
  private long partidaID;

  /**
   * Crea un repositorio de propiedades que usará el servicio de datos proporcionado para acceder a
   * la base de datos.
   *
   * @param dataService fábrica/servicio para obtener conexiones a la base de datos
   */
  public PropiedadRepository(IDataService dataService) {
    this.dataService = dataService;
  }

  /**
   * Establece el identificador de la partida que se usará en las consultas que dependen del
   * contexto de partida (por ejemplo, para filtrar jugadores y adquisiciones).
   *
   * @param partidaID id de la partida actual
   */
  @Override
  public void setPartidaID(long partidaID) {
    this.partidaID = partidaID;
  }

  /**
   * Obtiene el identificador de la propiedad que se encuentra en la posición del tablero indicada.
   *
   * <p>Si no existe una propiedad en esa posición devuelve -1.
   *
   * @param position posición en el tablero (PosicionTablero)
   * @return el PropiedadID asociado a la posición, o -1 si no existe
   * @throws SQLException si hay un error al ejecutar la consulta
   */
  int getPropiedadIdByPosition(int position) throws SQLException {
    String query = "SELECT PropiedadID FROM Propiedad WHERE PosicionTablero = ?";
    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(query)) {
      ps.setInt(1, position);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return -1;
        }
        return rs.getInt("PropiedadID");
      }
    } catch (SQLException e) {
      throw new SQLException(
          "Error al intentar obtener el ID de la propiedad en la posición " + position, e);
    }
  }

  /**
   * Recupera la información completa de una propiedad por su posición en el tablero.
   *
   * <p>El método intenta determinar el nivel (NivelPropiedad) de la propiedad consultando si tiene
   * dueño en la partida actual; si no tiene dueño se asume nivel = 1.
   *
   * @param position posición en el tablero (PosicionTablero)
   * @return un objeto {@link Propiedad} con los datos de la casilla y la propiedad, o null si no
   *     existe una propiedad en esa posición
   * @throws SQLException si ocurre un error al consultar la base de datos
   */
  @Override
  public Propiedad getPropiedadByPosition(int position) throws SQLException {
    int nivel = 1;
    int propiedadID = getPropiedadIdByPosition(position);

    if (propiedadHasOwner(propiedadID) != null) {
      nivel = getNivelPropiedad(propiedadID);
    }

    String query =
        "SELECT * FROM Propiedad "
            + "INNER JOIN Casilla ON Propiedad.PosicionTablero = Casilla.PosicionTablero "
            + "WHERE Propiedad.PosicionTablero = ?";

    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(query)) {
      ps.setInt(1, position);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return null;
        }

        return new Propiedad(
            rs.getInt("Casilla.PosicionTablero"),
            rs.getInt("PropiedadID"),
            rs.getInt("GrupoPropiedades"),
            nivel, // Nivel depende si tiene o no dueño
            rs.getInt("PrecioCompra"),
            new int[] {
              rs.getInt("RentaNivel1"),
              rs.getInt("RentaNivel2"),
              rs.getInt("RentaNivel3"),
              rs.getInt("RentaNivel4"),
              rs.getInt("RentaNivel5")
            });
      }
    } catch (SQLException e) {
      throw new SQLException(
          "Error al intentar obtener la propiedad en la posición " + position, e);
    }
  }

  /**
   * Comprueba si una propiedad (por su id) tiene un propietario en la partida actual.
   *
   * @param propiedadID id de la propiedad a consultar
   * @return el {@link Jugador} que posee la propiedad en la partida actual, o null si no tiene
   *     propietario
   * @throws SQLException si ocurre un error al ejecutar la consulta
   */
  @Override
  public Jugador propiedadHasOwner(int propiedadID) throws SQLException {
    String query =
        "SELECT * "
            + "FROM Jugador "
            + "INNER JOIN Adquisiciones ON Jugador.JugadorID = Adquisiciones.JugadorID "
            + "WHERE Jugador.Partida = ? AND Adquisiciones.PropiedadID = ?";

    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(query)) {
      ps.setLong(1, partidaID);
      ps.setInt(2, propiedadID);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return null;
        } else {
          return new Jugador(
              rs.getInt("Jugador.JugadorID"),
              rs.getString("Jugador.NombreJugador"),
              rs.getInt("Jugador.Posicion"),
              rs.getBoolean("Jugador.Encarcelado"),
              rs.getInt("Jugador.Dinero"),
              getPropiedadesByJugador(rs.getInt("Jugador.JugadorID")));
        }
      }
    } catch (SQLException e) {
      throw new SQLException(
          "Error al intentar verificar si la propiedad con id=" + propiedadID + " tiene dueño", e);
    }
  }

  /**
   * Obtiene el nivel actual de una propiedad (NivelPropiedad) para la partida actual.
   *
   * @param propiedadID id de la propiedad
   * @return el nivel de la propiedad (entero). Devuelve 0 si no se encuentra registro de nivel
   * @throws SQLException si ocurre un error al consultar la base de datos
   */
  @Override
  public int getNivelPropiedad(int propiedadID) throws SQLException {
    String query =
        "SELECT NivelPropiedad "
            + "FROM Adquisiciones "
            + "INNER JOIN Jugador ON Adquisiciones.JugadorID = Jugador.JugadorID "
            + "WHERE Adquisiciones.PropiedadID = ? AND Jugador.Partida = ?";

    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(query)) {
      ps.setInt(1, propiedadID);
      ps.setLong(2, partidaID);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return 0; // o el valor que corresponda si no hay nivel
        }
        return rs.getInt("NivelPropiedad");
      }
    } catch (SQLException e) {
      throw new SQLException(
          "Error al intentar obtener el nivel de la propiedad con id=" + propiedadID, e);
    }
  }

  /**
   * Incrementa en 1 el nivel de la propiedad en la tabla Adquisiciones.
   *
   * @param propiedadID id de la propiedad cuyo nivel se incrementará
   * @throws SQLException si ocurre un error al ejecutar la actualización
   */
  @Override
  public void incrementarNivelPropiedad(int propiedadID) throws SQLException {
    String update =
        "UPDATE Adquisiciones "
            + "SET NivelPropiedad = NivelPropiedad + 1 "
            + "WHERE PropiedadID = ?";

    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(update)) {
      ps.setInt(1, propiedadID);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new SQLException("Error al intentar incrementar el nivel de la propiedad", e);
    }
  }

  /**
   * Obtiene todas las propiedades asociadas a un jugador (por jugadorID).
   *
   * @param jugadorID id del jugador
   * @return lista (posiblemente vacía) de {@link Propiedad} que posee el jugador
   * @throws SQLException si ocurre un error al ejecutar la consulta
   */
  @Override
  public List<Propiedad> getPropiedadesByJugador(int jugadorID) throws SQLException {
    String query =
        "SELECT * "
            + "FROM Propiedad "
            + "INNER JOIN Adquisiciones ON Propiedad.PropiedadID = Adquisiciones.PropiedadID "
            + "WHERE Adquisiciones.JugadorID = ?";

    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(query)) {
      ps.setInt(1, jugadorID);
      try (ResultSet rs = ps.executeQuery()) {
        List<Propiedad> propiedades = new java.util.ArrayList<>();
        while (rs.next()) {
          propiedades.add(
              new Propiedad(
                  rs.getInt("Propiedad.PosicionTablero"),
                  rs.getInt("Propiedad.PropiedadID"),
                  rs.getInt("Propiedad.GrupoPropiedades"),
                  rs.getInt("Adquisiciones.NivelPropiedad"),
                  rs.getInt("Propiedad.PrecioCompra"),
                  new int[] {
                    rs.getInt("Propiedad.RentaNivel1"),
                    rs.getInt("Propiedad.RentaNivel2"),
                    rs.getInt("Propiedad.RentaNivel3"),
                    rs.getInt("Propiedad.RentaNivel4"),
                    rs.getInt("Propiedad.RentaNivel5")
                  }));
        }
        return propiedades;
      }
    } catch (SQLException e) {
      throw new SQLException(
          "Error al intentar obtener las propiedades del jugador con id=" + jugadorID, e);
    }
  }

  /**
   * Obtiene el nombre de la casilla asociada a una propiedad indicada por su id.
   *
   * @param propiedadID id de la propiedad
   * @return el nombre de la casilla (NombreCasilla) o null si no existe
   * @throws SQLException si ocurre un error al ejecutar la consulta
   */
  String getPropiedadNombreById(int propiedadID) throws SQLException {
    String query =
        "SELECT Casilla.NombreCasilla FROM Propiedad "
            + "INNER JOIN Casilla ON Propiedad.PosicionTablero = Casilla.PosicionTablero "
            + "WHERE Propiedad.PropiedadID = ?";

    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(query)) {
      ps.setInt(1, propiedadID);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return null;
        }
        return rs.getString("NombreCasilla");
      }
    } catch (SQLException e) {
      throw new SQLException(
          "Error al intentar obtener el nombre de la propiedad con id=" + propiedadID, e);
    }
  }

  /**
   * Devuelve el identificador del jugador que posee la propiedad indicada, si existe.
   *
   * @param propiedadId id de la propiedad
   * @return el JugadorID del propietario, o null si no hay propietario registrado
   * @throws SQLException si ocurre un error al consultar la base de datos
   */
  @Override
  public Integer getOwnerIdByPropiedadId(int propiedadId) throws SQLException {
    String query = "SELECT JugadorID FROM Adquisiciones WHERE PropiedadID = ?";
    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(query)) {
      ps.setInt(1, propiedadId);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return null;
        }
        return rs.getInt("JugadorID");
      }
    } catch (SQLException e) {
      throw new SQLException("Error al intentar obtener el dueño de la propiedad", e);
    }
  }

  /**
   * Inserta una nueva adquisición (vínculo propiedad-jugador con nivel) en la tabla Adquisiciones.
   *
   * @param jugadorId id del jugador comprador
   * @param propiedadId id de la propiedad adquirida
   * @param nivel nivel inicial de la propiedad en la adquisición
   * @throws SQLException si ocurre un error al insertar el registro
   */
  @Override
  public void addAdquisicion(int jugadorId, int propiedadId, int nivel) throws SQLException {
    String insert =
        "INSERT INTO Adquisiciones(JugadorID, PropiedadID, NivelPropiedad) VALUES(?, ?, ?)";
    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(insert)) {
      ps.setInt(1, jugadorId);
      ps.setInt(2, propiedadId);
      ps.setInt(3, nivel);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new SQLException("Error al intentar agregar la adquisición de la propiedad", e);
    }
  }

  @Override
  public void updateAdquisicionNivel(int propiedadId, int jugadorId, int nuevoNivel)
      throws SQLException {
    String update =
        "UPDATE Adquisiciones SET NivelPropiedad = ? WHERE PropiedadID = ? AND JugadorID = ?";
    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(update)) {
      ps.setInt(1, nuevoNivel);
      ps.setInt(2, propiedadId);
      ps.setInt(3, jugadorId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new SQLException("Error al intentar actualizar el nivel de la adquisición", e);
    }
  }

  /**
   * Calcula el patrimonio total de un jugador sumando los precios de compra de todas sus
   * propiedades.
   *
   * @param jugadorId id del jugador
   * @return el patrimonio total (suma de precios de compra)
   * @throws SQLException si ocurre un error al ejecutar la consulta
   */
  @Override
  public int getPatrimonioTotalJugador(int jugadorId) throws SQLException {
    String query =
        "SELECT SUM(Propiedad.PrecioCompra) AS PatrimonioTotal "
            + "FROM Adquisiciones "
            + "INNER JOIN Propiedad ON Adquisiciones.PropiedadID = Propiedad.PropiedadID "
            + "WHERE Adquisiciones.JugadorID = ?";

    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(query)) {
      ps.setInt(1, jugadorId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("PatrimonioTotal");
        } else {
          return 0;
        }
      }
    } catch (SQLException e) {
      throw new SQLException(
          "Error al intentar obtener el patrimonio total del jugador con id=" + jugadorId, e);
    }
  }

  @Override
  public void venderAdquisicion(int propiedadId, int jugadorId) throws SQLException {
    String delete = "DELETE FROM Adquisiciones WHERE PropiedadID = ? AND JugadorID = ?";
    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(delete)) {
      ps.setInt(1, propiedadId);
      ps.setInt(2, jugadorId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new SQLException("Error al intentar vender la adquisición de la propiedad", e);
    }
  }
}
