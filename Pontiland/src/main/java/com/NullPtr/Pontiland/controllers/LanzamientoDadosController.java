package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.services.DiceService;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.*;
import com.jme3.scene.Spatial;

/**
 * Controlador encargado de registrar entradas y orquestar el servicio de dados. No extiende
 * AppState; expone registerInputs(...) y update(tpf).
 */
public class LanzamientoDadosController {
  private final DiceService diceService;
  /**
   * Listener de acciones para gestionar las teclas de control del dado. Se registra en el
   * InputManager a través de {@link #registerInputs(InputManager)}.
   */
  private final RawInputListener rawKeys =
      new RawInputListener() {
        @Override
        public void onKeyEvent(KeyInputEvent evt) {
          if (!evt.isPressed()) return;

          int code = evt.getKeyCode();
          if (code == KeyInput.KEY_Y && diceService.getCanThrowDice()) {
            diceService.lanzamientoDados();
          }
        }

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
   */
  public LanzamientoDadosController(
          DiceService diceService) {
    this.diceService = diceService;
  }

  public void onDadosCreados(Spatial dado1, Spatial dado2) {
    diceService.setDados(dado1, dado2);
  }

  public void enableThrow(boolean canInteract){
    diceService.enableInteract(canInteract);
  }

  public void update() {
    diceService.update();
  }


}
