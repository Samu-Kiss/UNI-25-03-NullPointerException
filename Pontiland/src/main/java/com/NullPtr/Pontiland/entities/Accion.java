// java
package com.NullPtr.Pontiland.entities;

import java.util.HashMap;
import java.util.Map;

/** Enum que representa las diferentes acciones que pueden ocurrir en una casilla de tipo Evento */
public enum Accion {
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
  private static final Map<String, Accion> LABEL_MAP = new HashMap<>();

  static {
    for (Accion a : values()) {
      LABEL_MAP.put(a.label, a);
      LABEL_MAP.put(a.label.toLowerCase(), a); // entrada en minúsculas
      LABEL_MAP.put(a.name(), a); // por si acaso se pasa el nombre del enum
      LABEL_MAP.put(a.name().toLowerCase(), a);
    }
  }

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

  /**
   * Convierte una cadena a Accion. Acepta tanto el nombre del enum (ej. PROPIEDAD_A_NIVEL_1) como
   * la etiqueta (ej. PropiedadANivel1), sin distinguir mayúsculas/minúsculas. Lanza
   * IllegalArgumentException si no hay coincidencia.
   */
  public static Accion fromString(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Accion string is null");
    }
    Accion found = LABEL_MAP.get(s);
    if (found != null) {
      return found;
    }
    // intento adicional insensible a mayúsculas
    found = LABEL_MAP.get(s.toLowerCase());
    if (found != null) {
      return found;
    }
    throw new IllegalArgumentException(
        "No enum constant com.NullPtr.Pontiland.entities.Accion for input: " + s);
  }
}
