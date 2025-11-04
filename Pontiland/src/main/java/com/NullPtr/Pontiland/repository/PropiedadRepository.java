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
   * Constructor de la clase PropiedadRepository
   *
   * @param dataService
   */
  public PropiedadRepository(IDataService dataService) {
    this.dataService = dataService;
  }

  @Override
  public void setPartidaID(long partidaID) {
    this.partidaID = partidaID;
  }

  /**
   * Obtiene el ID de la propiedad que esté en la casilla dada
   *
   * @param position Posición en el tablero
   * @return
   */
  int getPropiedadIdByPosition(int position) {
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
      throw new RuntimeException(e);
    }
  }

  /**
   * Obtiene una propiedad por su posición en el tablero
   *
   * @param position Posición en el tablero
   * @return Objeto Propiedad o null si no existe
   */
  @Override
  public Propiedad getPropiedadByPosition(int position) {
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
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  Jugador propiedadHasOwner(int propiedadID) {
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
      throw new RuntimeException(e);
    }
  }

  int getNivelPropiedad(int propiedadID) {
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
      throw new RuntimeException(e);
    }
  }

  List<Propiedad> getPropiedadesByJugador(int jugadorID) {
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
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  String getPropiedadNombreById(int propiedadID) {
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
      throw new RuntimeException(e);
    }
  }

  @Override
  public Integer getOwnerIdByPropiedadId(int propiedadId) {
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
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addAdquisicion(int jugadorId, int propiedadId, int nivel) {
    String insert =
        "INSERT INTO Adquisiciones(JugadorID, PropiedadID, NivelPropiedad) VALUES(?, ?, ?)";
    try (Connection conex = dataService.createConnection();
        PreparedStatement ps = conex.prepareStatement(insert)) {
      ps.setInt(1, jugadorId);
      ps.setInt(2, propiedadId);
      ps.setInt(3, nivel);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
