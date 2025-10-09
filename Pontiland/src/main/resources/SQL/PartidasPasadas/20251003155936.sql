-- H2 2.3.232;
SET DB_CLOSE_DELAY -1;        
;             
CREATE USER IF NOT EXISTS "SA" SALT 'a01a6f0adaaba1db' HASH '9b5aaa9fbe463d415799561b133367397dd9d9ff7370bdf7d6f49df5ba3e170a' ADMIN;         
CREATE MEMORY TABLE "PUBLIC"."ICONO"(
    "ICONOID" INTEGER NOT NULL,
    "ICONONOMBRE" CHARACTER VARYING(50) NOT NULL
);  
ALTER TABLE "PUBLIC"."ICONO" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_4" PRIMARY KEY("ICONOID");   
-- 7 +/- SELECT COUNT(*) FROM PUBLIC.ICONO;   
INSERT INTO "PUBLIC"."ICONO" VALUES
(1, 'Kiwi'),
(2, 'Balon'),
(3, 'Maleta'),
(4, 'Pescadito'),
(5, 'Carnet'),
(6, 'Ignacito'),
(7, 'Nave');           
CREATE MEMORY TABLE "PUBLIC"."TIPOCASILLA"(
    "TIPOID" INTEGER NOT NULL,
    "TIPONOMBRE" CHARACTER VARYING(50) NOT NULL
);              
ALTER TABLE "PUBLIC"."TIPOCASILLA" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_3" PRIMARY KEY("TIPOID");              
-- 7 +/- SELECT COUNT(*) FROM PUBLIC.TIPOCASILLA;             
INSERT INTO "PUBLIC"."TIPOCASILLA" VALUES
(1, 'Salida'),
(2, U&'C\00e1rcel'),
(3, 'ParadaLibre'),
(4, 'Evento'),
(5, 'Propiedad'),
(6, 'Movimiento'),
(7, 'IrALaCarcel');              
CREATE MEMORY TABLE "PUBLIC"."CASILLA"(
    "POSICIONTABLERO" INTEGER NOT NULL,
    "NOMBRECASILLA" CHARACTER VARYING(50) NOT NULL,
    "TIPOCASILLA" INTEGER NOT NULL
); 
ALTER TABLE "PUBLIC"."CASILLA" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_4B" PRIMARY KEY("POSICIONTABLERO");        
-- 40 +/- SELECT COUNT(*) FROM PUBLIC.CASILLA;
INSERT INTO "PUBLIC"."CASILLA" VALUES
(1, 'El Tunel (Salida)', 1),
(2, U&'B\00e1sicas Viejo', 5),
(3, 'Evento1', 4),
(4, 'El italiano', 5),
(5, U&'B\00e1sicas Nuevo', 5),
(6, 'Calle 45 - American Schoolway', 6),
(7, U&'Arquidise\00f1o', 5),
(8, 'Evento2', 4),
(9, 'Urapanes', 5),
(10, 'Carrizoza', 5),
(11, 'Carcel', 2),
(12, 'El 94', 5),
(13, 'Evento3', 4),
(14, 'Biblioteca Arrupe', 5),
(15, 'CFJD', 5),
(16, 'Avenida 39', 6),
(17, 'Giraldo', 5),
(18, 'Evento4', 4),
(19, 'El Mexicano', 5),
(20, U&'Bar\00f3n', 5),
(21, 'Cubos (Parada Libre)', 3),
(22, 'El 67', 5),
(23, 'Evento5', 4),
(24, U&'La Estaci\00f3n', 5),
(25, 'HUSI', 5),
(26, 'SITP', 6),
(27, 'Artes', 5),
(28, 'Parque Nacional', 5),
(29, 'Evento6', 4),
(30, U&'Centro \00c1tico', 5),
(31, 'Capi (Ir a la Carcel)', 7),
(32, 'El Arca', 5),
(33, 'Il Posto', 5),
(34, 'Evento7', 4),
(35, U&'Javeriana Est\00e9reo', 5),
(36, U&'Primera L\00ednea del Metro', 6),
(37, 'Playita', 5),
(38, 'Atrio', 5),
(39, 'Evento8', 4),
(40, U&'Ingenier\00eda', 5);      
CREATE MEMORY TABLE "PUBLIC"."GRUPOPROPIEDADES"(
    "GRUPOID" INTEGER NOT NULL,
    "NUMEROPROPIEDADES" INTEGER NOT NULL
);               
ALTER TABLE "PUBLIC"."GRUPOPROPIEDADES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_C" PRIMARY KEY("GRUPOID");        
-- 8 +/- SELECT COUNT(*) FROM PUBLIC.GRUPOPROPIEDADES;        
INSERT INTO "PUBLIC"."GRUPOPROPIEDADES" VALUES
(1, 3),
(2, 3),
(3, 3),
(4, 3),
(5, 3),
(6, 3),
(7, 3),
(8, 3);        
CREATE MEMORY TABLE "PUBLIC"."PROPIEDAD"(
    "PROPIEDADID" INTEGER NOT NULL,
    "POSICIONTABLERO" INTEGER NOT NULL,
    "GRUPOPROPIEDADES" INTEGER NOT NULL,
    "PRECIOCOMPRA" INTEGER NOT NULL,
    "RENTANIVEL1" INTEGER NOT NULL,
    "RENTANIVEL2" INTEGER NOT NULL,
    "RENTANIVEL3" INTEGER NOT NULL,
    "RENTANIVEL4" INTEGER NOT NULL,
    "RENTANIVEL5" INTEGER NOT NULL
);           
ALTER TABLE "PUBLIC"."PROPIEDAD" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_7" PRIMARY KEY("PROPIEDADID");           
-- 24 +/- SELECT COUNT(*) FROM PUBLIC.PROPIEDAD;              
INSERT INTO "PUBLIC"."PROPIEDAD" VALUES
(1, 2, 1, 60, 70, 130, 220, 370, 750),
(2, 4, 1, 60, 70, 130, 220, 370, 750),
(3, 5, 1, 80, 70, 130, 220, 370, 750),
(4, 7, 2, 100, 80, 140, 240, 410, 800),
(5, 9, 2, 100, 80, 140, 240, 410, 800),
(6, 10, 2, 120, 100, 160, 260, 440, 860),
(7, 12, 3, 140, 110, 180, 290, 460, 900),
(8, 14, 3, 140, 110, 180, 290, 460, 900),
(9, 15, 3, 160, 130, 200, 310, 490, 980),
(10, 17, 4, 180, 140, 210, 330, 520, 1000),
(11, 19, 4, 180, 140, 210, 330, 520, 1000),
(12, 20, 4, 200, 160, 230, 350, 550, 1100),
(13, 22, 5, 220, 170, 250, 380, 580, 1160),
(14, 24, 5, 220, 170, 250, 380, 580, 1160),
(15, 25, 5, 240, 190, 270, 400, 610, 1200),
(16, 27, 6, 260, 200, 280, 420, 640, 1300),
(17, 28, 6, 260, 200, 280, 420, 640, 1300),
(18, 30, 6, 280, 220, 300, 440, 670, 1340),
(19, 32, 7, 300, 230, 320, 460, 700, 1400),
(20, 33, 7, 300, 230, 320, 460, 700, 1400),
(21, 35, 7, 320, 250, 340, 480, 730, 1440),
(22, 37, 8, 350, 270, 360, 510, 740, 1500),
(23, 38, 8, 350, 270, 360, 510, 740, 1500),
(24, 40, 8, 400, 300, 400, 560, 810, 1600);              
CREATE MEMORY TABLE "PUBLIC"."PARTIDA"(
    "PARTIDAID" BIGINT NOT NULL,
    "ACTIVA" BOOLEAN DEFAULT TRUE NOT NULL,
    "NUMEROJUGADORES" INTEGER NOT NULL
);            
ALTER TABLE "PUBLIC"."PARTIDA" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_F" PRIMARY KEY("PARTIDAID");               
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.PARTIDA; 
INSERT INTO "PUBLIC"."PARTIDA" VALUES
(20251003183630, TRUE, 3);             
CREATE MEMORY TABLE "PUBLIC"."JUGADOR"(
    "JUGADORID" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL,
    "NUMJUGADOR" INTEGER NOT NULL,
    "NOMBREJUGADOR" CHARACTER VARYING(50) NOT NULL,
    "ICONOID" INTEGER NOT NULL,
    "POSICION" INTEGER DEFAULT 1 NOT NULL,
    "ENCARCELADO" BOOLEAN DEFAULT FALSE NOT NULL,
    "DINERO" INTEGER DEFAULT 1500 NOT NULL,
    "PARTIDA" BIGINT NOT NULL
);            
ALTER TABLE "PUBLIC"."JUGADOR" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_D" PRIMARY KEY("JUGADORID");               
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.JUGADOR; 
CREATE MEMORY TABLE "PUBLIC"."ADQUISICIONES"(
    "JUGADORID" INTEGER NOT NULL,
    "PROPIEDADID" INTEGER NOT NULL,
    "NIVELPROPIEDAD" INTEGER NOT NULL
);              
ALTER TABLE "PUBLIC"."ADQUISICIONES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_54D" PRIMARY KEY("JUGADORID", "PROPIEDADID");        
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.ADQUISICIONES;           
CREATE MEMORY TABLE "PUBLIC"."JUGADORACTIVO"(
    "PARTIDAID" BIGINT NOT NULL,
    "JUGADORACTUALID" INTEGER NOT NULL
);   
ALTER TABLE "PUBLIC"."JUGADORACTIVO" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_A7" PRIMARY KEY("PARTIDAID", "JUGADORACTUALID");     
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.JUGADORACTIVO;           
CREATE MEMORY TABLE "PUBLIC"."TIPOEVENTO"(
    "TIPOEVENTOID" INTEGER NOT NULL,
    "TIPOEVENTO" CHARACTER VARYING(50) NOT NULL
);         
ALTER TABLE "PUBLIC"."TIPOEVENTO" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_B" PRIMARY KEY("TIPOEVENTOID");         
-- 8 +/- SELECT COUNT(*) FROM PUBLIC.TIPOEVENTO;              
INSERT INTO "PUBLIC"."TIPOEVENTO" VALUES
(1, 'PropiedadANivel5'),
(2, 'PropiedadANivel1'),
(3, 'PropiedadNivel-1'),
(4, 'PropiedadNivel+1'),
(5, 'Gana200'),
(6, 'Pierde50porPropiedad'),
(7, 'Gana50'),
(8, 'Gana100');              
CREATE MEMORY TABLE "PUBLIC"."EVENTO"(
    "EVENTOID" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1 RESTART WITH 81) NOT NULL,
    "NOMBRE" CHARACTER VARYING(50) NOT NULL,
    "DESCRIPCION" CHARACTER VARYING(150) NOT NULL,
    "TIPOEVENTO" INTEGER NOT NULL
);              
ALTER TABLE "PUBLIC"."EVENTO" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_7A" PRIMARY KEY("EVENTOID");
-- 80 +/- SELECT COUNT(*) FROM PUBLIC.EVENTO; 
INSERT INTO "PUBLIC"."EVENTO" VALUES
(1, U&'Centro de Investigaci\00f3n \00c9lite', U&'Un fil\00e1ntropo dona equipos de \00faltima generaci\00f3n y tu edificio alcanza el m\00e1ximo nivel. Tu propiedad sube a nivel 5.', 1),
(2, 'Laboratorio Vanguardista', U&'Tus instalaciones reciben certificaci\00f3n internacional y suben al nivel 5.', 1),
(3, U&'Auditorio Emblem\00e1tico', U&'Se inaugura un espacio con tecnolog\00eda punta y tu edificio se convierte en la joya del campus. Tu propiedad sube a nivel 5.', 1),
(4, U&'Beca de Innovaci\00f3n', U&'Obtienes fondos para transformar tu bloque en referencia acad\00e9mica. Tu propiedad sube a nivel 5.', 1),
(5, U&'Renovaci\00f3n Magistral', U&'Arquitectos dise\00f1an un ambiente universitario de primer orden. Tu propiedad sube a nivel 5.', 1),
(6, 'Patrocinio Empresarial', U&'Una gran compa\00f1\00eda equipa todo tu edificio con recursos premium. Tu propiedad sube a nivel 5.', 1),
(7, 'Aula del Futuro', U&'Tu propiedad estrena mobiliario y pantallas interactivas de \00faltima generaci\00f3n. Tu propiedad sube a nivel 5.', 1),
(8, 'Laboratorio de Excelencia', U&'Ganas un concurso nacional y tu laboratorio alcanza el est\00e1ndar m\00e1s alto. Tu propiedad sube a nivel 5.', 1),
(9, 'Edificio Insignia', U&'Se convierte en s\00edmbolo de la universidad: nivel 5 asegurado.', 1),
(10, U&'Milagro Acad\00e9mico', U&'Gracias a alianzas y gesti\00f3n brillante, tu edificio salta al m\00e1ximo. Tu propiedad sube a nivel 5.', 1),
(11, 'Filtraciones en el Techo', U&'Una falla obliga a cerrar \00e1reas, dejando tu edificio en lo b\00e1sico. Tu propiedad baja a nivel 1.', 2),
(12, 'Equipos Obsoletos', U&'Los aparatos se da\00f1an y solo queda lo esencial funcionando. Tu propiedad baja a nivel 1.', 2),
(13, 'Recorte Presupuestal', 'El presupuesto no alcanza, tu edificio se simplifica al nivel 1.', 2),
(14, 'Laboratorio en Pausa', U&'Se retiran recursos por mantenimiento y baja su categor\00eda. Tu propiedad baja a nivel 1.', 2),
(15, U&'Reforma Acad\00e9mica', 'Se reestructuran los espacios, pierdes todas las mejoras. Tu propiedad baja a nivel 1.', 2),
(16, 'Estudiantes Inquietos', 'Una protesta obliga a cerrar pisos avanzados. Tu propiedad baja a nivel 1.', 2),
(17, 'Obra Mal Planeada', 'Un error en planos obliga a volver al nivel inicial. Tu propiedad baja a nivel 1.', 2),
(18, 'Sillas Rotas y Pizarras Viejas', U&'Los salones pierden atractivo y caen al nivel b\00e1sico. Tu propiedad baja a nivel 1.', 2),
(19, U&'Reparaci\00f3n Urgente', U&'El edificio sufre da\00f1os y se limita a lo m\00ednimo. Tu propiedad baja a nivel 1.', 2),
(20, 'Desgaste Natural', 'El tiempo cobra factura y tu propiedad vuelve al origen. Tu propiedad baja a nivel 1.', 2),
(21, U&'Aire Acondicionado Da\00f1ado', 'El mal clima te obliga a cerrar un piso. El nivel de tu propiedad disminuye en 1.', 3),
(22, 'Computadores Desactualizados', 'Un nivel queda inservible por equipos lentos. El nivel de tu propiedad disminuye en 1.', 3),
(23, 'Fugas en el Laboratorio', U&'Una fuga qu\00edmica reduce la capacidad del edificio. El nivel de tu propiedad disminuye en 1.', 3),
(24, U&'Menos Presupuesto de Investigaci\00f3n', 'Pierdes financiamiento, retrocedes un nivel.', 3),
(25, 'Ascensor Fuera de Servicio', 'Cierras un piso hasta reparar el elevador. El nivel de tu propiedad disminuye en 1.', 3),
(26, U&'Inspecci\00f3n Universitaria', U&'Las normas de seguridad te hacen clausurar un \00e1rea. El nivel de tu propiedad disminuye en 1.', 3),
(27, U&'Ca\00edda en el Ranking', U&'La reputaci\00f3n baja y con ella un nivel del edificio. El nivel de tu propiedad disminuye en 1.', 3),
(28, 'Clases Suspendidas', U&'Menos actividad acad\00e9mica reduce el estatus. El nivel de tu propiedad disminuye en 1.', 3),
(29, U&'Problemas El\00e9ctricos', 'Pierdes un piso hasta instalar nuevo cableado. El nivel de tu propiedad disminuye en 1.', 3),
(30, U&'Reducci\00f3n de Aulas', U&'Ajustas espacios y bajas un escal\00f3n. El nivel de tu propiedad disminuye en 1.', 3),
(31, 'Nueva Biblioteca', 'Agregas un ala de lectura y tu edificio crece. El nivel de tu propiedad aumenta en 1.', 4);             
INSERT INTO "PUBLIC"."EVENTO" VALUES
(32, 'Laboratorio Mejorado', U&'Equipas un espacio con microscopios de \00faltima tecnolog\00eda. El nivel de tu propiedad aumenta en 1.', 4),
(33, U&'Caf\00e9 Estudiantil', U&'Un rinc\00f3n acogedor eleva la calidad del lugar. El nivel de tu propiedad aumenta en 1.', 4),
(34, U&'Sal\00f3n Multimedia', 'Instalas proyectores y pizarras digitales: +1 nivel.', 4),
(35, U&'Acreditaci\00f3n Acad\00e9mica', U&'La certificaci\00f3n impulsa el valor del edificio. El nivel de tu propiedad aumenta en 1.', 4),
(36, 'Aula Maker', U&'A\00f1ades un taller de innovaci\00f3n para los estudiantes. El nivel de tu propiedad aumenta en 1.', 4),
(37, 'Patrocinio de Exalumnos', 'Antiguos graduados financian mejoras. El nivel de tu propiedad aumenta en 1.', 4),
(38, 'Huerta Universitaria', 'Un toque verde eleva el prestigio del edificio. El nivel de tu propiedad aumenta en 1.', 4),
(39, U&'Centro de Tutor\00edas', 'Un nuevo servicio aumenta la utilidad del bloque. El nivel de tu propiedad aumenta en 1.', 4),
(40, 'Piso de Coworking', 'Espacios colaborativos enriquecen tu propiedad. El nivel de tu propiedad aumenta en 1.', 4),
(41, U&'Subvenci\00f3n del Ministerio', U&'Un proyecto acad\00e9mico obtiene fondos especiales. Ganas $200.', 5),
(42, 'Beca Institucional', U&'Tu gesti\00f3n te hace merecedor de un apoyo econ\00f3mico. Ganas $200.', 5),
(43, U&'Donaci\00f3n de Exalumnos', 'Graduados aportan $200 para tus proyectos.', 5),
(44, U&'Premio a la Innovaci\00f3n Docente', 'Reconocen tus aportes con un cheque. Ganas $200.', 5),
(45, 'Venta de Publicaciones', 'Tus libros y papers generan ingresos. Ganas $200.', 5),
(46, 'Evento Universitario', 'Organizar una feria cultural te deja ganancias. Ganas $200.', 5),
(47, 'Congreso Internacional', U&'Recibes honorarios por tu participaci\00f3n. Ganas $200.', 5),
(48, U&'Rifa Acad\00e9mica', U&'Tu n\00famero sale ganador en el sorteo del campus. Ganas $200.', 5),
(49, 'Patrocinio Privado', U&'Una empresa financia parte de tu investigaci\00f3n. Ganas $200.', 5),
(50, 'Alquiler de Auditorios', 'Tus instalaciones producen ingresos extra. Ganas $200.', 5),
(51, 'Mantenimiento Semestral', 'Pintura, techos y limpieza cuestan $50 por bloque.', 6),
(52, U&'Revisi\00f3n de Seguridad', 'Inspecciones oficiales generan gastos. Pagas $50 por cada una de tus propiedades.', 6),
(53, U&'Reparaci\00f3n de Mobiliario', 'Cambiar sillas y mesas tiene su precio. Pagas $50 por cada una de tus propiedades.', 6),
(54, U&'Actualizaci\00f3n Tecnol\00f3gica', 'Cada edificio necesita nuevos equipos. Pagas $50 por cada una de tus propiedades.', 6),
(55, 'Limpieza Profunda', 'Contratas personal extra para pulir tus espacios. Pagas $50 por cada una de tus propiedades.', 6),
(56, 'Control de Plagas en el Campus', 'Cuidar las zonas comunes cuesta $50 cada uno.', 6),
(57, U&'Certificaci\00f3n de Calidad', 'Cada propiedad paga por evaluaciones. Pagas $50 por cada una de tus propiedades.', 6),
(58, 'Seguro de Instalaciones', 'Proteges tus edificios ante accidentes. Pagas $50 por cada una de tus propiedades.', 6),
(59, U&'Revisi\00f3n El\00e9ctrica', 'Los inspectores cobran por cada bloque. Pagas $50 por cada una de tus propiedades.', 6),
(60, 'Ajuste de Impuestos', 'El gobierno sube tasas universitarias. Pagas $50 por cada una de tus propiedades.', 6),
(61, U&'Venta de Caf\00e9', U&'Tu cafeter\00eda gana $50 esta semana.', 7),
(62, 'Fotocopias Populares', U&'El centro de impresi\00f3n del campus deja ganancias. Ganas $50.', 7),
(63, 'Reembolso de Materiales', 'Recuperas $50 por compras no usadas.', 7),
(64, 'Taller de Fin de Semana', U&'Dictas un curso r\00e1pido y obtienes $50.', 7),
(65, 'Mercado de Libros Usados', 'Vendes textos de semestres pasados. Ganas $50.', 7),
(66, 'Tour Universitario', 'Cobras por mostrar el campus a visitantes. Ganas $50.', 7),
(67, 'Arriendo de Salones', 'Alquilas un aula para un evento. Ganas $50.', 7),
(68, 'Venta de Souvenirs', 'Camisetas y pines dejan ingresos. Ganas $50.', 7),
(69, U&'Peque\00f1a Donaci\00f3n', 'Un profesor invitado deja $50 para el fondo.', 7),
(70, U&'Campa\00f1a de Reciclaje', 'Recolectas materiales y obtienes ganancias. Ganas $50.', 7);             
INSERT INTO "PUBLIC"."EVENTO" VALUES
(71, 'Curso de Verano', 'Dictas un taller y ganas $100.', 8),
(72, 'Premio al Mejor Proyecto', 'Tus estudiantes te ayudan a obtener un cheque. Ganas $100.', 8),
(73, 'Venta de Entradas a un Evento', 'Organizar un concierto en el campus deja ingresos. Ganas $100.', 8),
(74, U&'Campa\00f1a de Donaciones', 'La comunidad universitaria apoya tu trabajo. Ganas $100.', 8),
(75, 'Arriendo del Teatro', 'Cedes el auditorio para una obra. Ganas $100.', 8),
(76, 'Venta de Insignias', 'Souvenirs oficiales del campus generan $100.', 8),
(77, 'Clase Magistral', U&'Un p\00fablico externo paga por asistir. Ganas $100.', 8),
(78, 'Convenio con Editorial', U&'Publicas material acad\00e9mico con ganancias. Ganas $100.', 8),
(79, U&'Exposici\00f3n de Ciencia', 'La feria atrae patrocinadores. Ganas $100.', 8),
(80, 'Patrocinio Estudiantil', 'Un grupo de alumnos apoya tu edificio. Ganas $100.', 8);       
ALTER TABLE "PUBLIC"."ADQUISICIONES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_54" CHECK("NIVELPROPIEDAD" BETWEEN 1 AND 5) NOCHECK; 
ALTER TABLE "PUBLIC"."PARTIDA" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_FB" CHECK("NUMEROJUGADORES" BETWEEN 2 AND 4) NOCHECK;      
ALTER TABLE "PUBLIC"."JUGADORACTIVO" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_A" UNIQUE NULLS DISTINCT ("PARTIDAID");              
ALTER TABLE "PUBLIC"."ICONO" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_42" UNIQUE NULLS DISTINCT ("ICONONOMBRE");   
ALTER TABLE "PUBLIC"."ADQUISICIONES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_5" UNIQUE NULLS DISTINCT ("PROPIEDADID");            
ALTER TABLE "PUBLIC"."EVENTO" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_7A9" UNIQUE NULLS DISTINCT ("NOMBRE");      
ALTER TABLE "PUBLIC"."CASILLA" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_4BE" UNIQUE NULLS DISTINCT ("NOMBRECASILLA");              
ALTER TABLE "PUBLIC"."JUGADORACTIVO" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_A71" FOREIGN KEY("PARTIDAID") REFERENCES "PUBLIC"."PARTIDA"("PARTIDAID") ON DELETE CASCADE NOCHECK;  
ALTER TABLE "PUBLIC"."JUGADOR" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_DF" FOREIGN KEY("ICONOID") REFERENCES "PUBLIC"."ICONO"("ICONOID") ON DELETE CASCADE NOCHECK;               
ALTER TABLE "PUBLIC"."JUGADOR" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_DFA0" FOREIGN KEY("POSICION") REFERENCES "PUBLIC"."CASILLA"("POSICIONTABLERO") ON DELETE CASCADE NOCHECK;  
ALTER TABLE "PUBLIC"."ADQUISICIONES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_54DE" FOREIGN KEY("JUGADORID") REFERENCES "PUBLIC"."JUGADOR"("JUGADORID") ON DELETE CASCADE NOCHECK; 
ALTER TABLE "PUBLIC"."ADQUISICIONES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_54DE4" FOREIGN KEY("PROPIEDADID") REFERENCES "PUBLIC"."PROPIEDAD"("PROPIEDADID") ON DELETE CASCADE NOCHECK;          
ALTER TABLE "PUBLIC"."CASILLA" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_4BE0" FOREIGN KEY("TIPOCASILLA") REFERENCES "PUBLIC"."TIPOCASILLA"("TIPOID") ON DELETE CASCADE NOCHECK;    
ALTER TABLE "PUBLIC"."JUGADOR" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_DFA" FOREIGN KEY("PARTIDA") REFERENCES "PUBLIC"."PARTIDA"("PARTIDAID") ON DELETE CASCADE NOCHECK;          
ALTER TABLE "PUBLIC"."PROPIEDAD" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_7B" FOREIGN KEY("POSICIONTABLERO") REFERENCES "PUBLIC"."CASILLA"("POSICIONTABLERO") ON DELETE CASCADE NOCHECK;           
ALTER TABLE "PUBLIC"."EVENTO" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_7A9A" FOREIGN KEY("TIPOEVENTO") REFERENCES "PUBLIC"."TIPOEVENTO"("TIPOEVENTOID") ON DELETE CASCADE NOCHECK; 
ALTER TABLE "PUBLIC"."JUGADORACTIVO" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_A71A" FOREIGN KEY("JUGADORACTUALID") REFERENCES "PUBLIC"."JUGADOR"("JUGADORID") ON DELETE CASCADE NOCHECK;           
ALTER TABLE "PUBLIC"."PROPIEDAD" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_7B3" FOREIGN KEY("GRUPOPROPIEDADES") REFERENCES "PUBLIC"."GRUPOPROPIEDADES"("GRUPOID") ON DELETE CASCADE NOCHECK;        
