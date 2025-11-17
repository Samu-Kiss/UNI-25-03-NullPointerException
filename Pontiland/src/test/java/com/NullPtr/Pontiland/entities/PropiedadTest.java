package com.NullPtr.Pontiland.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test unitario para la clase {@link Propiedad}.
 * Se prueban constructor, getters, setters y validaciones.
 */
class PropiedadTest {

    /**
     * Verifica que el constructor con valores válidos inicializa correctamente los campos.
     */
    @Test
    void validConstructorTest() {
        int[] rents = {10, 20, 30, 40, 50};
        Propiedad propiedad = new Propiedad(5, "Av. Central", 1, 1, 1, 100, rents);

        assertEquals(1, propiedad.getIdPropiedad());
        assertEquals(1, propiedad.getNivelPropiedad());
        assertEquals(100, propiedad.getPrecioCompra());
        assertArrayEquals(rents, propiedad.getRentaPorNivel());
        assertNull(propiedad.getNombreCasilla(), "El nombre de la casilla no se asigna en el constructor");
    }

    /**
     * Verifica que el constructor lance excepciones con parámetros inválidos.
     */
    @Test
    void invalidConstructorTest() {
        int[] invalidRents = {10, 20};

        assertThrows(
                IllegalArgumentException.class,
                () -> new Propiedad(5, "Av. Central", 0, 1, 1, 100, new int[]{10, 20, 30, 40, 50}));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Propiedad(5, "Av. Central", 1, 0, 1, 100, new int[]{10, 20, 30, 40, 50}));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Propiedad(5, "Av. Central", 1, 1, 0, 100, new int[]{10, 20, 30, 40, 50}));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Propiedad(5, "Av. Central", 1, 1, 1, -1, new int[]{10, 20, 30, 40, 50}));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Propiedad(5, "Av. Central", 1, 1, 1, 100, invalidRents));
    }

    /**
     * Verifica que los setters y getters funcionen correctamente con valores válidos.
     */
    @Test
    void gettersAndSettersTest() {
        int[] rents = {10, 20, 30, 40, 50};
        Propiedad propiedad = new Propiedad(5, "Av. Central", 1, 1, 1, 100, rents);

        propiedad.setNombreCasilla("Nueva Calle");
        assertEquals("Nueva Calle", propiedad.getNombreCasilla());

        propiedad.setTipoCasilla(Tipo.PROPIEDAD);
        assertEquals(Tipo.PROPIEDAD, propiedad.getTipoCasilla());

        propiedad.setPosicionTablero((byte) 10);
        assertEquals(10, propiedad.getPosicionTablero());

        propiedad.setNivelPropiedad(3);
        assertEquals(3, propiedad.getNivelPropiedad());

        propiedad.setPrecioCompra(200);
        assertEquals(200, propiedad.getPrecioCompra());

        int[] newRents = {15, 25, 35, 45, 55};
        propiedad.setRentaPorNivel(newRents);
        assertArrayEquals(newRents, propiedad.getRentaPorNivel());

        Jugador jugadorMock = new Jugador(1000, "Jugador1", 1);
        propiedad.setDueno(jugadorMock);
        assertEquals(jugadorMock, propiedad.getDueno());
    }

    /**
     * Verifica que los setters lancen excepciones al recibir valores inválidos.
     */
    @Test
    void invalidSettersTest() {
        int[] rents = {10, 20, 30, 40, 50};
        Propiedad propiedad = new Propiedad(5, "Av. Central", 1, 1, 1, 100, rents);

        assertThrows(IllegalArgumentException.class, () -> propiedad.setNombreCasilla(""));
        assertThrows(IllegalArgumentException.class, () -> propiedad.setNombreCasilla(null));

        assertThrows(IllegalArgumentException.class, () -> propiedad.setNivelPropiedad(0));
        assertThrows(IllegalArgumentException.class, () -> propiedad.setNivelPropiedad(6));

        assertThrows(IllegalArgumentException.class, () -> propiedad.setPrecioCompra(-10));

        assertThrows(
                IllegalArgumentException.class, () -> propiedad.setRentaPorNivel(new int[]{1, 2}));
    }

    /**
     * Verifica que setIdPropiedad acepte valores válidos dentro del rango permitido.
     */
    @Test
    void validSetIdPropiedadTest() {
        int[] rents = {10, 20, 30, 40, 50};
        Propiedad propiedad = new Propiedad(5, "Av. Central", 1, 1, 1, 100, rents);

        propiedad.setIdPropiedad(5);
        assertEquals(5, propiedad.getIdPropiedad(), "El idPropiedad debe actualizarse correctamente");

        propiedad.setIdPropiedad(24);
        assertEquals(24, propiedad.getIdPropiedad(), "El idPropiedad debe aceptar el valor máximo permitido");
    }

    /**
     * Verifica que setIdPropiedad lance excepciones al recibir valores fuera del rango permitido.
     */
    @Test
    void invalidSetIdPropiedadTest() {
        int[] rents = {10, 20, 30, 40, 50};
        Propiedad propiedad = new Propiedad(5, "Av. Central", 1, 1, 1, 100, rents);

        assertThrows(IllegalArgumentException.class, () -> propiedad.setIdPropiedad(0),
                "Debe lanzar excepción si idPropiedad es menor que 1");
        assertThrows(IllegalArgumentException.class, () -> propiedad.setIdPropiedad(25),
                "Debe lanzar excepción si idPropiedad es mayor que 24");
    }
}
