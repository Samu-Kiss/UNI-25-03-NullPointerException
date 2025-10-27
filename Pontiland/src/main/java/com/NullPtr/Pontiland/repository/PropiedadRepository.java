package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.services.IDataService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PropiedadRepository {
  IDataService dataService;

  /**
   * Constructor de la clase PropiedadRepository
   *
   * @param dataService
   */
  public PropiedadRepository(IDataService dataService) {
    this.dataService = dataService;
  }

  /**
   * Obtiene el ID de la propiedad que esté en la casilla dada
   *
   * @param position
   * @return
   */
  int getPropiedadIdByPosition(int position) {
    Connection conex = dataService.createConnection();

    String query = "SELECT PropiedadID FROM Propiedad " + "WHERE PosicionTablero = ?";

    try {
      PreparedStatement ps = conex.prepareStatement(query);
      ps.setInt(1, position);
      ResultSet rs = ps.executeQuery();
      if (!rs.next()) {
        return 1;
      }

      return rs.getInt("PropiedadID");

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Obtiene una propiedad por su posición en el tablero
   *
   * @param position Posición en el tablero
   * @return Objeto Propiedad
   */
  Propiedad getPropiedadByPosition(int position, long partidaID) {
    int nivel = 1; // Nivel inicial si no tiene dueño
    int propiedadID = getPropiedadIdByPosition(position);

    // Si la propiedad tiene dueño, se debería obtener el nivel real
    if (propiedadHasOwner(propiedadID, partidaID) != null) {
      nivel = getNivelPropiedad(propiedadID, partidaID);
    }

    Connection conex = dataService.createConnection();

    String query =
        "SELECT * FROM Propiedad "
            + "INNER JOIN Casilla "
            + "ON Propiedad.PosicionTablero = Casilla.PosicionTablero "
            + "WHERE Propiedad.PosicionTablero = ?";

    try {
      PreparedStatement ps = conex.prepareStatement(query);
      ps.setInt(1, position);
      ResultSet rs = ps.executeQuery();
      if (!rs.next()) {
        return null;
      }

      return new Propiedad(
          rs.getInt("Casilla.PosicionTablero"),
          rs.getString("Casilla.NombreCasilla"),
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

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      // Cerrar la conexion
      try {
        conex.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  Jugador propiedadHasOwner(int propiedadID, long partidaID) {
    Connection conex = dataService.createConnection();

    String query =
        "SELECT * "
            + "FROM Jugador "
            + "INNER JOIN Adquisiciones ON Jugador.JugadorID = Adquisiciones.JugadorID "
            + "WHERE Jugador.PartidaID = ? AND Adquisiciones.PropiedadID = ?";

    try {
      PreparedStatement ps = conex.prepareStatement(query);
      ps.setLong(1, partidaID);
      ps.setInt(2, propiedadID);
      ResultSet rs = ps.executeQuery();
      rs.next();

      if (rs.getRow() == 0) {
        return null;
      } else {
        return new Jugador(
            rs.getInt("Jugador.JugadorID"),
            rs.getString("Jugador.NombreJugador"),
            rs.getInt("Jugador.PosicionTablero"),
            rs.getBoolean("Jugador.Encarcelado"),
            rs.getInt("Jugador.SaldoDinero"),
            getPropiedadesByJugador(rs.getInt("Jugador.JugadorID")));
      }

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      // Cerrar la conexion
      try {
        conex.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  int getNivelPropiedad(int propiedadID, long partidaID) {
    Connection conex = dataService.createConnection();

    String query =
        "SELECT NivelPropiedad "
            + "FROM Adquisiciones "
            + "INNER JOIN Jugador ON Adquisiciones.JugadorID = Jugador.JugadorID "
            + "WHERE Adquisiciones.PropiedadID = ? AND Jugador.PartidaID = ?";

    try (PreparedStatement ps = conex.prepareStatement(query); ) {
      ps.setInt(1, propiedadID);
      ps.setLong(2, partidaID);
      ResultSet rs = ps.executeQuery();
      rs.next();

      return rs.getInt("NivelConstruido");

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      // Cerrar la conexion
      try {
        conex.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  List<Propiedad> getPropiedadesByJugador(int jugadorID) {
    Connection conex = dataService.createConnection();

    String query =
        "SELECT * "
            + "FROM Propiedad "
            + "INNER JOIN Adquisiciones ON Propiedad.PropiedadID = Adquisiciones.PropiedadID "
            + "WHERE Adquisiciones.JugadorID = ?";

    try (PreparedStatement ps = conex.prepareStatement(query)) {

      ps.setInt(1, jugadorID);
      ResultSet rs = ps.executeQuery();

      List<Propiedad> propiedades = new java.util.ArrayList<>();

      while (rs.next()) {
        propiedades.add(
            new Propiedad(
                rs.getInt("Propiedad.PosicionTablero"),
                getPropiedadNombreById(rs.getInt("Propiedad.PropiedadID")),
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

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      // Cerrar la conexion
      try {
        conex.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  String getPropiedadNombreById(int propiedadID) {
    Connection conex = dataService.createConnection();

    String query =
        "SELECT Casilla.NombreCasilla FROM Propiedad "
            + "INNER JOIN Casilla ON Propiedad.PosicionTablero = Casilla.PosicionTablero "
            + "WHERE Propiedad.PropiedadID = ?";

    try {
      PreparedStatement ps = conex.prepareStatement(query);
      ps.setInt(1, propiedadID);
      ResultSet rs = ps.executeQuery();
      rs.next();

      return rs.getString("NombreCasilla");

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
