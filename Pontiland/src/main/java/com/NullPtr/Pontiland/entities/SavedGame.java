package com.NullPtr.Pontiland.entities;

import java.util.Objects;

public class SavedGame {
  public final String id;
  public final String titulo; // texto a mostrar (puede incluir fecha/slot)

  public SavedGame(String id, String titulo) {
    this.id = Objects.requireNonNull(id, "id");
    this.titulo = Objects.requireNonNull(titulo, "titulo");
  }
}
