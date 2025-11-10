package com.NullPtr.Pontiland.services;

public interface ISubastaService {
  boolean iniciarSubasta();
  boolean pujar();
  boolean aumentarPrecio(int delta);
  boolean comprarActual();
}
