package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.services.DiceService;
import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.*;
import com.jme3.scene.Spatial;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Controlador encargado de registrar entradas y orquestar el servicio de dados. No extiende
 * AppState; expone registerInputs(...) y update(tpf).
 */
public class LanzamientoDadosController {
  private final DiceService diceService;
  private final AtomicReferenceArray<Byte> resultados;

  /**
   * Listener de acciones para gestionar las teclas de control del dado. Se registra en el
   * InputManager a través de {@link #registerInputs(InputManager)}.
   */
  private final RawInputListener rawKeys =
      new RawInputListener() {
        @Override
        public void onKeyEvent(KeyInputEvent evt) {
          if (!evt.isPressed()) return; // reaccionar solo a key-down

          int code = evt.getKeyCode();
          if (code == KeyInput.KEY_Y) {
            diceService.lanzamientoDadosNoBloqueante(resultados);
          }
        }

        // Métodos requeridos por la interfaz (sin uso)
        public void beginInput() {}

        public void endInput() {}

        public void onMouseMotionEvent(MouseMotionEvent evt) {}

        public void onMouseButtonEvent(MouseButtonEvent evt) {}

        public void onJoyAxisEvent(JoyAxisEvent evt) {}

        public void onJoyButtonEvent(JoyButtonEvent evt) {}

        public void onTouchEvent(TouchEvent evt) {}
      };

  /** Registrar el listener crudo (sin mappings). */
  public void registerInputs(InputManager inputManager) {
    if (inputManager == null) return;
    inputManager.addRawInputListener(rawKeys);
  }

  /**
   * Constructor con inyección de dependencias.
   *
   * @param diceService Servicio encargado de la lógica de dados
   * @param resultados Estructura compartida donde se escribirán resultados no bloqueantes
   */
  public LanzamientoDadosController(
      DiceService diceService, AtomicReferenceArray<Byte> resultados) {
    this.diceService = diceService;
    this.resultados = resultados;
  }

  public void onDadosCreados(Spatial dado1, Spatial dado2) {
    diceService.setDados(dado1, dado2);
  }

  // Método opcional para compatibilidad con código heredado: solo registra entradas.
  public void initialize(SimpleApplication app) {
    if (app != null) {
      registerInputs(app.getInputManager());
    }
  }

  /** Actualización por frame. Se debe llamar desde el método simpleUpdate() de la aplicación. */
  public void update() {
    // Actualizar la lógica de lanzamiento del servicio
    diceService.update();

    // Si hay resultados listos, imprimirlos y limpiar
    Byte r0 = resultados.get(0);
    Byte r1 = resultados.get(1);
    if (r0 != null || r1 != null) {
      System.out.println("Resultados dados: [" + r0 + ", " + r1 + "]");
      resultados.set(0, null);
      resultados.set(1, null);
    }
  }
}
