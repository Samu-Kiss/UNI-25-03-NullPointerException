package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Accion;
import com.NullPtr.Pontiland.entities.TarjetaEvento;
import com.NullPtr.Pontiland.services.IDataService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TarjetaEventoRepository {

  IDataService dataService;

  public TarjetaEventoRepository(IDataService dataService) {
    this.dataService = dataService;
  }

  public TarjetaEvento getTarjetaEventoById(int id) {
    String obtenerTarjetaEventoPorId =
        "SELECT Nombre, Descripcion, TipoEvento.TipoEvento FROM Evento "
            + "INNER JOIN TipoEvento ON Evento.TipoEvento = TipoEvento.TipoEventoID "
            + "WHERE EventoID = ?";

    try (Connection conn = dataService.createConnection();
        PreparedStatement ps = conn.prepareStatement(obtenerTarjetaEventoPorId)) {
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        return new TarjetaEvento(
            rs.getString("Nombre"),
            rs.getString("Descripcion"),
            Accion.fromString(rs.getString("TipoEvento")));
      } else {
        throw new IllegalStateException("No se pudo obtener el ID máximo de TarjetaEvento.");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public TarjetaEvento getRandomTarjetaEvento() {

    int MaxTarjetaEventoId = getMaxEventoId();

    int randomId = (int) (Math.random() * MaxTarjetaEventoId) + 1;

    return getTarjetaEventoById(randomId);
  }

  public int getMaxEventoId() {
    String obtenerMaxIdEvento = "SELECT MAX(EventoID) AS MaxID FROM Evento";

    try (Connection conn = dataService.createConnection();
        PreparedStatement ps = conn.prepareStatement(obtenerMaxIdEvento);
        ResultSet rs = ps.executeQuery()) {

      if (rs.next()) {
        return rs.getInt("MaxID");
      } else {
        throw new IllegalStateException("No se pudo obtener el ID máximo de TarjetaEvento.");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
