package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Dado;
import com.jme3.scene.Spatial;

/**
 * Servicio encargado de gestionar el lanzamiento de dados.
 *
 * <p>Este servicio encapsula toda la lógica relacionada con los dados, de forma que el controlador
 * sólo invoque sus métodos sin conocer detalles de la implementación interna. Incluye una máquina
 * de estados para permitir lanzamientos no bloqueantes en jMonkeyEngine.
 */
public class DiceService implements IDiceService {
  private final Dado[] dados = new Dado[2];
  private ITurnService turnService;

  /** Máquina de estados interna para el lanzamiento no bloqueante. */
  private enum Estado {
    IDLE,
    LAUNCH,
    CHECK_MOVIMIENTO,
    READ,
    DONE
  }

  private Estado estado = Estado.IDLE;

  private Byte[] resultados = new Byte[2];

  /**
   * Inyecta ambos dados en el servicio.
   *
   * @param dado1 Spatial que representa el modelo 3D del primer dado
   * @param dado2 Spatial que representa el modelo 3D del segundo dado
   */
  @Override
  public void setDados(Spatial dado1, Spatial dado2) {

    dados[0] = new Dado(dado1);
    dados[1] = new Dado(dado2);
  }

  /** Realiza el lanzamiento simultáneo de todos los dados cargados. */
  @Override
  public void lanzarDados() {
    for (Dado dado : dados) {
      if (dado != null && turnService.canThrowDice()) {
        dado.lanzar();
      }
    }
  }

  /**
   * Devuelve el valor superior de cada dado cargado.
   *
   * @return Un array de dos bytes con los valores de los dados, o null si el dado no está definido
   */
  @Override
  public Byte[] leerDados() {
    Byte[] resultados = new Byte[2];
    resultados[0] = (dados[0] != null) ? dados[0].getCaraSuperior() : null;
    resultados[1] = (dados[1] != null) ? dados[1].getCaraSuperior() : null;
    return resultados;
  }

  @Override
  public Byte[] getResultados() {
      return  (resultados[0] != null && resultados[1] != null) ? resultados : new Byte[]{null, null};
  }

    /**
   * Inicia la secuencia de lanzamiento no bloqueante.
   */
    @Override
    public void lanzamientoDados() {
    this.estado = Estado.LAUNCH;
  }

  @Override
  public void update() {
    switch (estado) {
      case IDLE:
        break;
      case LAUNCH:
        lanzarDados();
        estado = Estado.CHECK_MOVIMIENTO;
        break;
      case CHECK_MOVIMIENTO:
        boolean enMovimiento = false;
        for (Dado dado : dados) {
          if (dado != null && dado.enMovimiento()) {
            enMovimiento = true;
            break;
          }
        }
        if (!enMovimiento) {
          estado = Estado.READ;
        }
        break;
      case READ:
        Byte[] valores = leerDados();
        resultados[0] = valores[0];
        resultados[1] = valores[1];
        estado = Estado.DONE;

        break;
      case DONE:
        estado = Estado.IDLE;
          resultados = new Byte[]{null, null};
        break;
    }
  }

  @Override
  public void setTurnService(ITurnService turnService) {
    this.turnService = turnService;
  }
}

