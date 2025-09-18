-- Tabla Icono
CREATE TABLE Icono (
    IconoID INT PRIMARY KEY,
    IconoNombre VARCHAR NOT NULL UNIQUE
);

-- Tabla TipoCasilla
CREATE TABLE TipoCasilla (
    TipoID INT PRIMARY KEY,
    TipoNombre VARCHAR NOT NULL
);

-- Tabla Casilla
CREATE TABLE Casilla (
    PosicionTablero INT PRIMARY KEY,
    NombreCasilla VARCHAR NOT NULL UNIQUE,
    TipoCasilla INT NOT NULL,
    FOREIGN KEY (TipoCasilla) REFERENCES TipoCasilla(TipoID) ON DELETE CASCADE
);

-- Tabla GrupoPropiedades
CREATE TABLE GrupoPropiedades (
    GrupoID INT PRIMARY KEY,
    NumeroPropiedades INT NOT NULL
);

-- Tabla Propiedad
CREATE TABLE Propiedad (
    PropiedadID INT PRIMARY KEY,
    PosicionTablero INT NOT NULL,
    GrupoPropiedades INT NOT NULL,
    PrecioCompra INT NOT NULL,
    RentaNivel1 INT NOT NULL,
    RentaNivel2 INT NOT NULL,
    RentaNivel3 INT NOT NULL,
    RentaNivel4 INT NOT NULL,
    RentaNivel5 INT NOT NULL,
    FOREIGN KEY (PosicionTablero) REFERENCES Casilla(PosicionTablero) ON DELETE CASCADE,
    FOREIGN KEY (GrupoPropiedades) REFERENCES GrupoPropiedades(GrupoID) ON DELETE CASCADE
);

-- Tabla Partida
CREATE TABLE Partida (
    PartidaID INT PRIMARY KEY,
    Activa BOOLEAN NOT NULL DEFAULT TRUE,
    NumeroJugadores INT NOT NULL
);

-- Tabla Jugador
CREATE TABLE Jugador (
    JugadorID INT auto_increment PRIMARY KEY,
    NumJugador INT NOT NULL,
    NombreJugador VARCHAR NOT NULL,
    IconoID INT NOT NULL,
    Posicion INT NOT NULL DEFAULT 1,
    Encarcelado BOOLEAN NOT NULL DEFAULT FALSE,
    Dinero INT NOT NULL DEFAULT 1500,
    Partida INT NOT NULL,
    FOREIGN KEY (IconoID) REFERENCES Icono(IconoID) ON DELETE CASCADE,
    FOREIGN KEY (Partida) REFERENCES Partida(PartidaID) ON DELETE CASCADE
);

-- Tabla Adquisiciones
CREATE TABLE Adquisiciones (
    JugadorID INT NOT NULL,
    PropiedadID INT NOT NULL UNIQUE,
    NivelPropiedad INT NOT NULL CHECK (NivelPropiedad BETWEEN 1 AND 5),
    PRIMARY KEY (JugadorID, PropiedadID),
    FOREIGN KEY (JugadorID) REFERENCES Jugador(JugadorID) ON DELETE CASCADE,
    FOREIGN KEY (PropiedadID) REFERENCES Propiedad(PropiedadID) ON DELETE CASCADE
);


--Relación partida-jugador activo
CREATE TABLE JugadorActivo (
    PartidaID INT NOT NULL UNIQUE,
    JugadorActualID INT NOT NULL,
    PRIMARY KEY (PartidaID, JugadorActualID),
    FOREIGN KEY (PartidaID) REFERENCES Partida(PartidaID) ON DELETE CASCADE,
    FOREIGN KEY (JugadorActualID) REFERENCES Jugador(JugadorID) ON DELETE CASCADE
);

-- Tabla TipoEvento
CREATE TABLE TipoEvento (
    TipoEventoID INT PRIMARY KEY,
    TipoEvento VARCHAR NOT NULL
);

-- Tabla Evento
CREATE TABLE Evento (
    EventoID INT auto_increment PRIMARY KEY,
    Nombre VARCHAR NOT NULL UNIQUE,
    Descripcion VARCHAR NOT NULL,
    TipoEvento INT NOT NULL,
    FOREIGN KEY (TipoEvento) REFERENCES TipoEvento(TipoEventoID) ON DELETE CASCADE
);