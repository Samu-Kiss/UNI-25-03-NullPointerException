package com.NullPtr.Pontiland.entities;


 enum Accion {
    PROPIEDAD_A_NIVEL_5("PropiedadANivel5"),
    PROPIEDAD_A_NIVEL_1("PropiedadANivel1"),
    PROPIEDAD_NIVEL_MINUS_1("PropiedadNivel-1"),
    PROPIEDAD_NIVEL_PLUS_1("PropiedadNivel+1"),
    GANA_200("Gana200"),
    PIERDE_50_POR_PROPIEDAD("Pierde50porPropiedad"),
    GANA_50("Gana50"),
    GANA_100("Gana100"),
    IR_A_LA_CARCEL("IrALaCarcel");

    private final String label;

    Accion(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}

/**
 * Clase que representa una tarjeta de evento en el juego. Cada tarjeta tiene un nombre, una
 * descripción y una acción asociada
 */
public class TarjetaEvento {
  private String nombre = null;
  private String descripcion = null;
    private Accion accion = null;
  /**
   * Constructor de la clase TarjetaEvento
   *
   * @param nombre Nombre de la tarjeta
   * @param descripcion Descripción de la tarjeta
   * @throws IllegalArgumentException si algún parámetro es nulo o si el nombre o la descripción
   *     están vacíos
   */
  public TarjetaEvento(String nombre, String descripcion) {
    if (nombre == null || descripcion == null) {
      throw new IllegalArgumentException("Ningún parámetro puede ser nulo");
    }
    if (nombre.isEmpty() || descripcion.isEmpty()) {
      throw new IllegalArgumentException("El nombre y la descripción no pueden estar vacíos");
    }
    this.nombre = nombre;
    this.descripcion = descripcion;
  }

    public void setAccion(String accion) {
        this.accion = Accion.valueOf(accion);
    }

    Accion getAccion(){
        return accion;
    }
}

