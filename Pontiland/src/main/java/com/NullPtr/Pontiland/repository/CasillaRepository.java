package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Tipo;
import com.NullPtr.Pontiland.services.IDataService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CasillaRepository implements ICasillaRepository{

    private final IDataService dataService;

    public CasillaRepository(IDataService dataService) {
        this.dataService = dataService;
    }
    @Override
    public Casilla casillaFromPosition(int posicion) {
        if ( posicion < 1 || posicion > 40){
            throw new RuntimeException("No existen casillas antes de 0 o después de 40");
        }

        String obtenerCasillaPosicion = "SELECT Casilla.NombreCasilla, TipoCasilla.TipoNombre " +
                                            "FROM Casilla " +
                                            "JOIN TipoCasilla ON Casilla.TipoCasilla=TipoCasilla.TipoID " +
                                            "WHERE Casilla.PosicionTablero = ?";

        try (Connection conn = dataService.createConnection();
             PreparedStatement ps = conn.prepareStatement(obtenerCasillaPosicion)) {

            ps.setInt(1, posicion);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // No hay fila en BD para esa posición
                    throw new IllegalStateException(
                            "No existe casilla en posición " + posicion +
                                    ". ¿Ejecutaste DDL + Insertions.sql antes de iniciar el juego?");
                }

                String nombre = rs.getString("NombreCasilla");
                String tipoDb = rs.getString("TipoNombre");
                Tipo tipo = Tipo.valueOf(tipoDb.toUpperCase());

                return new Casilla(posicion, nombre, tipo);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
