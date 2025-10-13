package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Tipo;
import com.NullPtr.Pontiland.services.IDataService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CasillaRepository implements ICasillaRepository{

    IDataService dataService;

    @Override
    public Casilla casillaFromPosition(int posicion) {
        if ( posicion < 1 || posicion > 40){
            throw new RuntimeException("No existen casillas antes de 0 o después de 40");
        }

        String obtenerCasillaPosicion = "SELECT Casilla.NombreCasilla, TipoCasilla.TipoNombre " +
                                            "FROM Casilla " +
                                            "JOIN TipoCasilla ON Casilla.TipoCasilla=TipoCasilla.TipoID " +
                                            "WHERE Casilla.PosicionTablero = ?";

        try (Connection connect = dataService.createConnection()){
            PreparedStatement preparedStatement = connect.prepareStatement(obtenerCasillaPosicion);
            preparedStatement.setInt(1, posicion);

            ResultSet resultSet = preparedStatement.executeQuery();

            Tipo tipo = Tipo.valueOf(resultSet.getString("TipoNombre"));
            String nombreCasilla = resultSet.getString("NombreCasilla");
            return new Casilla(posicion,
                                nombreCasilla,
                                tipo);

        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
}
