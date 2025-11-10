package com.NullPtr.Pontiland.entities;

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
  public TarjetaEvento(String nombre, String descripcion, Accion accion) {
    if (nombre == null || descripcion == null) {
      throw new IllegalArgumentException("Ningún parámetro puede ser nulo");
    }
    if (nombre.isEmpty() || descripcion.isEmpty()) {
      throw new IllegalArgumentException("El nombre y la descripción no pueden estar vacíos");
    }
    if (accion == null) {
      throw new IllegalArgumentException("La acción no puede ser nula");
    }
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.accion = accion;
  }

  @Override
  public String toString() {
    return "TarjetaEvento{"
        + "nombre='"
        + nombre
        + '\''
        + ", descripcion='"
        + descripcion
        + '\''
        + ", accion="
        + accion
        + '}';
  }
}
