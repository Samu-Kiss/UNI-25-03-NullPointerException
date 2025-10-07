package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.services.IDataService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Repositorio encargado de gestionar las operaciones relacionadas con la entidad Partida
 * en la base de datos, como la creación y consulta de partidas.
 */
public class PartidaRepository implements IPartidaRepository {
    /**
     * Servicio de acceso a datos para la gestión de conexiones y operaciones con la base de datos.
     */
    IDataService dataService;

    /**
     * Constructor por defecto.
     */
    public PartidaRepository() {}

    /**
     * Constructor que permite la inyección del servicio de datos.
     * @param dataService Servicio de datos utilizado para la conexión a la base de datos.
     */
    public PartidaRepository(IDataService dataService) {this.dataService = dataService;}

    /**
     * Crea una nueva partida en la base de datos con el número de jugadores especificado.
     * Genera un identificador único basado en la fecha y hora actual.
     *
     * @param numJugadores Número de jugadores que participarán en la partida.
     * @return El identificador único de la partida creada.
     * @throws RuntimeException si ocurre un error de SQL durante la inserción.
     */
    public long newPartida(int numJugadores){
        Connection conn = dataService.createConnection();
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formatted = myDateObj.format(formatter);
        long partidaID = Long.parseLong(formatted);

        String creacionPartida = "INSERT INTO PARTIDA(PartidaID, NumeroJugadores) VALUES( ? , ? )";
        try {
            PreparedStatement crearPartida = conn.prepareStatement(creacionPartida);
            crearPartida.setLong(1, partidaID);
            crearPartida.setInt(2, numJugadores);
            crearPartida.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return partidaID;
    }

    /**
     * Obtiene el identificador de la partida almacenada en la base de datos.
     *
     * @return El identificador de la partida.
     * @throws RuntimeException si ocurre un error de SQL durante la consulta.
     */
    public long getPartidaID(){
        Connection conn = dataService.createConnection();
        String consultarPartida = "SELECT * FROM PARTIDA";
        try {
            PreparedStatement partida = conn.prepareStatement(consultarPartida);
            ResultSet rsPartida = partida.executeQuery();
            return rsPartida.getLong("PartidaID");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
