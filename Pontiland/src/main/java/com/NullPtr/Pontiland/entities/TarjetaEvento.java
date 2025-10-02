package com.NullPtr.Pontiland.entities;

import com.NullPtr.Pontiland.enums.Accion;

/**
 * Clase que representa una tarjeta de evento en el juego. Cada tarjeta tiene un nombre, una
 * descripción y una acción asociada
 */
public class TarjetaEvento {
  private String nombre = null;
  private String descripcion = null;
  private Accion accion;

    /**
     * Constructor de la clase TarjetaEvento
     * @param nombre Nombre de la tarjeta
     * @param descripcion Descripción de la tarjeta
     * @param accion Acción asociada a la tarjeta
     * @throws IllegalArgumentException si algún parámetro es nulo o si el nombre o la descripción están vacíos
     */
    public TarjetaEvento(String nombre, String descripcion, Accion accion) {
        if (nombre == null || descripcion == null || accion == null) {
            throw new IllegalArgumentException("Ningún parámetro puede ser nulo");
        }
        if (nombre.isEmpty() || descripcion.isEmpty()) {
            throw new IllegalArgumentException("El nombre y la descripción no pueden estar vacíos");
        }

        this.nombre = nombre;
        this.descripcion = descripcion;
        this.accion = accion;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo o vacío");
        }
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede ser nula o vacía");
        }
        this.descripcion = descripcion;
    }

    public Accion getAccion() {
        return accion;
    }
    public void setAccion(Accion accion) {
        if (accion == null) {
            throw new IllegalArgumentException("La acción no puede ser nula");
        }
        this.accion = accion;
    }
}
