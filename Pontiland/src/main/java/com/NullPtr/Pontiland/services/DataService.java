package com.NullPtr.Pontiland.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class DataService implements IDataService {
    String url;
    DataService(){}
    DataService(String url) {
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public Connection createConnection(){
        Connection conn = null;
        try {
            return conn = DriverManager.getConnection(url, "sa", "");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void newDataBase(){
        Connection conn = createConnection();
        try {
            Statement stmt = conn.createStatement();
            // Cargar y ejecutar DDL.sql
            String schemaSql = Files.readString(Paths.get("src/main/resources/SQL/NuevaPartida/DDL.sql"));
            stmt.execute(schemaSql);
            // Cargar y ejecutar data.sql
            String dataSql = Files.readString(Paths.get("src/main/resources/SQL/NuevaPartida/Insertions.sql"));
            for (String sql : dataSql.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql.trim());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void loadDataBase(String archivoSeleccionado){
        Connection conn = createConnection();
        try {
            Statement stmt = conn.createStatement();
            String ubicacionArchivo = "src/main/resources/SQL/PartidasPasadas/";
            ubicacionArchivo = ubicacionArchivo + archivoSeleccionado;
            Path path = Paths.get(ubicacionArchivo);
            if(Files.exists(path) && Files.isRegularFile(path)) {
                String runscript = "RUNSCRIPT FROM ?";
                PreparedStatement cargarPartida = conn.prepareStatement(runscript);
                cargarPartida.setString(1, ubicacionArchivo);
                cargarPartida.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Map<String, String> listarPartidasPasadas(){
        Map<String, String> mapaArchivo = new LinkedHashMap<>();

        File scriptsPartidas = new File("src/main/resources");
        File[] archivos = scriptsPartidas.listFiles();

        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isFile()) {
                    String fileName = archivo.getName();

                    // Ejemplo de posible nombre de un archivo de partida pasada 20251003155300.sql
                    if (fileName.endsWith(".sql")) {
                        String fechaRepresentada = fileName.replace(".sql", "");

                        try {
                            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyyMMddHHmmss");
                            Date date = formatoEntrada.parse(fechaRepresentada);
                            SimpleDateFormat formatoSalida = new SimpleDateFormat("yyyy-MM-dd HH:mm::ss");
                            String formattedDate = formatoSalida.format(date);
                            mapaArchivo.put(formattedDate, fileName);

                        } catch (ParseException e) {
                            System.err.println("Archivo con formato incorrecto: " + fileName);
                        }
                    }
                }
            }
        }
        return mapaArchivo;
    }
/**
     * Guarda el estado actual de la base de datos de la partida en un archivo SQL.
     *
     * El archivo se almacena en la ruta 'src/main/resources/SQL/PartidasPasadas/' y su nombre
     * corresponde al identificador de la partida seguido de la extensión .sql. Si el archivo ya existe
     * y es un archivo regular, se sobrescribe con el nuevo estado de la base de datos.
     *
     * @param partidaID Identificador único de la partida, utilizado para nombrar el archivo de guardado.
     * @throws RuntimeException si ocurre un error de SQL durante el proceso de guardado.
     */
    public void saveDataBase(long partidaID){
        Connection conn = createConnection();
        try {
            Statement stmt = conn.createStatement();
            String ubicacionArchivo = "src/main/resources/SQL/PartidasPasadas/";
            ubicacionArchivo = ubicacionArchivo + partidaID + ".sql";
            Path path = Paths.get(ubicacionArchivo);
            if(Files.exists(path) && Files.isRegularFile(path)) {
                String script = "SCRIPT TO ?";
                PreparedStatement guardarPartida = conn.prepareStatement(script);
                guardarPartida.setString(1, ubicacionArchivo);
                guardarPartida.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
