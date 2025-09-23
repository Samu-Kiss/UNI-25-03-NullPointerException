-- Tabla Icono (Se refiere al ícono que está usando el jugador para identificarse dentro del juego)
CREATE TABLE Icono (
    IconoID INT PRIMARY KEY,
    IconoNombre VARCHAR(50) NOT NULL UNIQUE
);

-- Tabla TipoCasilla (Si es evento, salida, parada libre, propiedad, cárcel, movimiento o ir a la carcel)
CREATE TABLE TipoCasilla (
    TipoID INT PRIMARY KEY,
    TipoNombre VARCHAR(50) NOT NULL
);

-- Tabla Casilla (Descripción detodas las casillas del tablero, considerando su tipo y posición)
CREATE TABLE Casilla (
    PosicionTablero INT PRIMARY KEY,
    NombreCasilla VARCHAR(50) NOT NULL UNIQUE,
    TipoCasilla INT NOT NULL,
    FOREIGN KEY (TipoCasilla) REFERENCES TipoCasilla(TipoID) ON DELETE CASCADE
);

-- Tabla GrupoPropiedades (Grupos de propiedades del mismo color en el tablero)
CREATE TABLE GrupoPropiedades (
    GrupoID INT PRIMARY KEY,
    NumeroPropiedades INT NOT NULL
);

-- Tabla Propiedad (Propiedades en el juego, incluyendo el costo para comprarlas, y el valor de la renta por cada nivel, además del grupo de propiedades al que pertenece)
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

-- Tabla Partida (Representación de la partida en la base de datos)
CREATE TABLE Partida (
    PartidaID INT PRIMARY KEY,
    Activa BOOLEAN NOT NULL DEFAULT TRUE,
    NumeroJugadores INT NOT NULL CHECK (NumeroJugadores BETWEEN 2 AND 4)
);

-- Tabla Jugador (Representación del jugador en el juego, incluyendo su número de jugador dentro del juego, su nombre, el ícono que usa, si está encarcelado y su posición (casilla en la que se encuentra ubicado))
CREATE TABLE Jugador (
    JugadorID INT auto_increment PRIMARY KEY,
    NumJugador INT NOT NULL,
    NombreJugador VARCHAR(50) NOT NULL,
    IconoID INT NOT NULL,
    Posicion INT NOT NULL DEFAULT 1,
    Encarcelado BOOLEAN NOT NULL DEFAULT FALSE,
    Dinero INT NOT NULL DEFAULT 1500,
    Partida INT NOT NULL,
    FOREIGN KEY (IconoID) REFERENCES Icono(IconoID) ON DELETE CASCADE,
    FOREIGN KEY (Partida) REFERENCES Partida(PartidaID) ON DELETE CASCADE
);

-- Tabla Adquisiciones (Relación entre un jugador y las propiedades que ha adquirido en la partida, almacenando en nivel de renta en el que se encuentra la propiedad)
CREATE TABLE Adquisiciones (
    JugadorID INT NOT NULL,
    PropiedadID INT NOT NULL UNIQUE,
    NivelPropiedad INT NOT NULL CHECK (NivelPropiedad BETWEEN 1 AND 5),
    PRIMARY KEY (JugadorID, PropiedadID),
    FOREIGN KEY (JugadorID) REFERENCES Jugador(JugadorID) ON DELETE CASCADE,
    FOREIGN KEY (PropiedadID) REFERENCES Propiedad(PropiedadID) ON DELETE CASCADE
);


--Relación partida-jugador activo (Jugador de turno)
CREATE TABLE JugadorActivo (
    PartidaID INT NOT NULL UNIQUE,
    JugadorActualID INT NOT NULL,
    PRIMARY KEY (PartidaID, JugadorActualID),
    FOREIGN KEY (PartidaID) REFERENCES Partida(PartidaID) ON DELETE CASCADE,
    FOREIGN KEY (JugadorActualID) REFERENCES Jugador(JugadorID) ON DELETE CASCADE
);

-- Tabla TipoEvento (El mismo tipo de evento representa el mismo efecto sobre la base de datos)
CREATE TABLE TipoEvento (
    TipoEventoID INT PRIMARY KEY,
    TipoEvento VARCHAR(50) NOT NULL
);

-- Tabla Evento (Descripciones-cartas de evento en el juego)
CREATE TABLE Evento (
    EventoID INT auto_increment PRIMARY KEY,
    Nombre VARCHAR(50) NOT NULL UNIQUE,
    Descripcion VARCHAR(150) NOT NULL,
    TipoEvento INT NOT NULL,
    FOREIGN KEY (TipoEvento) REFERENCES TipoEvento(TipoEventoID) ON DELETE CASCADE
);