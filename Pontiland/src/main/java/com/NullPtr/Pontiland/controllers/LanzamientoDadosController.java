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

        public void beginInput() {
          // No es necesario implementar nada aquí
        }

        public void endInput() {
          // No es necesario implementar nada aquí
        }

        public void onMouseMotionEvent(MouseMotionEvent evt) {
          // No es necesario implementar nada aquí
        }

        public void onMouseButtonEvent(MouseButtonEvent evt) {
          // No es necesario implementar nada aquí
        }

        public void onJoyAxisEvent(JoyAxisEvent evt) {
          // No es necesario implementar nada aquí
        }

        public void onJoyButtonEvent(JoyButtonEvent evt) {
          // No es necesario implementar nada aquí
        }

        public void onTouchEvent(TouchEvent evt) {
          // No es necesario implementar nada aquí
        }
      };

  /**
   * Registra el listener crudo (sin mappings).
   *
   * @param inputManager gestor de entradas donde se añadirá el listener
   */
  public void registerInputs(InputManager inputManager) {
    if (inputManager == null) return;
    inputManager.addRawInputListener(rawKeys);
  }

  /**
   * Constructor con inyección de dependencias.
   *
   * @param diceService Servicio encargado de la lógica de dados
   */
  public LanzamientoDadosController(DiceService diceService) {
    this.diceService = diceService;
  }

  public void onDadosCreados(Spatial dado1, Spatial dado2) {
    diceService.setDados(dado1, dado2);
  }

  public void enableThrow(boolean canInteract) {
    diceService.enableInteract(canInteract);
  }

  public void update() {
    diceService.update();
  }
}
