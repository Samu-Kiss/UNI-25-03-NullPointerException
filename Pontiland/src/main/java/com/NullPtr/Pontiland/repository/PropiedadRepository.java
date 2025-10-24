package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Propiedad;
import com.NullPtr.Pontiland.services.IDataService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PropiedadRepository {
  IDataService dataService;

  /**
   * Constructor de la clase PropiedadRepository
   * @param dataService
   */
  public PropiedadRepository(IDataService dataService) {
    this.dataService = dataService;
  }

  /**
   * Obtiene el ID de la propiedad que esté en la casilla dada
   * @param position
   * @return
   */
  int getPropiedadIdByPosition(int position) {
    Connection conex = dataService.createConnection();

    String query = "SELECT PropiedadID FROM Propiedad " +
            "WHERE PosicionTablero = ?";

    try {
      PreparedStatement ps = conex.prepareStatement(query);
      ps.setInt(1, position);
      ResultSet rs = ps.executeQuery();
      rs.next();

      return rs.getInt("PropiedadID");

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Obtiene una propiedad por su posición en el tablero, usar cuando no tiene dueño/cuando se va a comprar o subastar
   * @param position Posición en el tablero
   * @return Objeto Propiedad
   */
  Propiedad getPropiedadByPosition(int position) {
    Connection conex = dataService.createConnection();

    String query = "SELECT * FROM Propiedad " +
                  "INNER JOIN Casilla " +
                  "ON Propiedad.PosicionTablero = Casilla.PosicionTablero " +
                  "WHERE Propiedad.PosicionTablero = ?";

    try {
      PreparedStatement ps = conex.prepareStatement(query);
      ps.setInt(1, position);
      ResultSet rs = ps.executeQuery();
      rs.next();

      return new Propiedad(
              rs.getInt("Casilla.PosicionTablero"),
              rs.getString("Casilla.NombreCasilla"),
              rs.getInt("PropiedadID"),
              rs.getInt("GrupoPropiedades"),
              1, /* Nivel inicial ya que no tiene dueño*/
              rs.getInt("PrecioCompra"),
              new int[] {
                      rs.getInt("RentaNivel1"),
                      rs.getInt("RentaNivel2"),
                      rs.getInt("RentaNivel3"),
                      rs.getInt("RentaNivel4"),
                      rs.getInt("RentaNivel5")
              }
      );

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  boolean propiedadHasOwner(int propiedadID, long partidaID) {
    Connection conex = dataService.createConnection();

    String query = "SELECT COUNT(*) AS Cantidad " +
            "FROM Jugador " +
            "INNER JOIN Adquisiciones ON Jugador.JugadorID = Adquisiciones.JugadorID " +
            "WHERE Jugador.PartidaID = ? AND Adquisiciones.PropiedadID = ?";


    try {
      PreparedStatement ps = conex.prepareStatement(query);
      ps.setLong(1, partidaID);
      ps.setInt(2, propiedadID);
      ResultSet rs = ps.executeQuery();
      rs.next();

      return rs.getInt("Cantidad") > 0;

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
