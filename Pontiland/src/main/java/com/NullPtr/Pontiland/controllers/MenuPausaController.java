package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.services.IDataService;
import com.NullPtr.Pontiland.services.IDiceService;
import com.NullPtr.Pontiland.services.ITurnService;
import com.NullPtr.Pontiland.view.MenuPausa;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Controlador encargado de mostrar y ocultar el menú de pausa durante la partida. */
public class MenuPausaController implements IMenuPausaActions {

  private final Launcher app;
  private final InputManager inputManager;
  private final ITurnService turnService;
  private final LanzamientoDadosController diceController;
  private final IDiceService diceService;
  private final IHUDcontroller hudController;
  private final IDataService dataService;
  private final IPartidaRepository partidaRepository;
  private final MenuController menuController;

  private MenuPausa pauseMenu;
  private boolean menuVisible;

  private boolean turnWasEnabled;
  private boolean diceWasInteractable;

  // Logging
  private static Logger logger = LogManager.getLogger(MenuPausaController.class);

  private final RawInputListener keyListener =
      new RawInputListener() {
        @Override
        public void onKeyEvent(KeyInputEvent evt) {
          if (!evt.isPressed()) {
            return;
          }
          if (evt.getKeyCode() == KeyInput.KEY_TAB) {
            togglePauseMenu();
          }
        }

        @Override
        public void onMouseMotionEvent(MouseMotionEvent evt) {
          // No es necesario implementar nada aquí
        }

        @Override
        public void onMouseButtonEvent(MouseButtonEvent evt) {
          // No es necesario implementar nada aquí
        }

        @Override
        public void onTouchEvent(TouchEvent evt) {
          // No es necesario implementar nada aquí
        }

        @Override
        public void onJoyAxisEvent(JoyAxisEvent evt) {
          // No es necesario implementar nada aquí
        }

        @Override
        public void onJoyButtonEvent(JoyButtonEvent evt) {
          // No es necesario implementar nada aquí
        }

        @Override
        public void beginInput() {
          // No es necesario implementar nada aquí
        }

        @Override
        public void endInput() {
          // No es necesario implementar nada aquí
        }
      };

  public MenuPausaController(
      Launcher app,
      InputManager inputManager,
      ITurnService turnService,
      LanzamientoDadosController diceController,
      IDiceService diceService,
      IHUDcontroller hudController,
      IDataService dataService,
      IPartidaRepository partidaRepository,
      MenuController menuController) {
    this.app = app;
    this.inputManager = inputManager;
    this.turnService = turnService;
    this.diceController = diceController;
    this.diceService = diceService;
    this.hudController = hudController;
    this.dataService = dataService;
    this.partidaRepository = partidaRepository;
    this.menuController = menuController;

    if (this.inputManager != null) {
      this.inputManager.addRawInputListener(keyListener);
    }
  }

  /** Alterna la visibilidad del menú de pausa. Solo funciona si el juego está activo. */
  public void togglePauseMenu() {
    // Solo permitir acceso al menú de pausa si el juego está en curso
    if (menuController == null || !menuController.isGameStarted()) {
      return;
    }

    if (menuVisible) {
      resumeGame();
    } else {
      showMenu();
    }
  }

  private void showMenu() {
    if (menuVisible) {
      return;
    }

    turnWasEnabled = turnService != null && turnService.isEnabled();
    diceWasInteractable = diceService != null && diceService.getCanInteract();

    if (turnService != null) {
      turnService.setEnabled(false);
    }
    if (diceController != null) {
      diceController.enableThrow(false);
    }
    if (hudController != null) {
      hudController.hideHUD();
    }

    if (pauseMenu == null) {
      pauseMenu = new MenuPausa(this);
    }

    AppStateManager stateManager = app.getStateManager();
    if (!stateManager.hasState(pauseMenu)) {
      stateManager.attach(pauseMenu);
    }

    menuVisible = true;
  }

  @Override
  public void resumeGame() {
    if (!menuVisible) {
      return;
    }

    AppStateManager stateManager = app.getStateManager();
    if (pauseMenu != null && stateManager.hasState(pauseMenu)) {
      stateManager.detach(pauseMenu);
    }

    menuVisible = false;

    if (turnService != null && turnWasEnabled) {
      turnService.setEnabled(true);
    }
    if (diceController != null) {
      diceController.enableThrow(diceWasInteractable);
    }
    if (hudController != null) {
      hudController.showHUD();
    }
  }

  @Override
  public void saveAndExit() {
    long partidaId = -1L;
    if (partidaRepository != null) {
      partidaId = partidaRepository.getPartidaID();
    }

    if (dataService != null && partidaId > 0) {
      try {
        dataService.saveDataBase(partidaId);
      } catch (RuntimeException ex) {
        // Registrar el error sin interrumpir el flujo para evitar perder el control de la UI.
        logger.error("Error al guardar la partida con ID {}", partidaId, ex);
      }
    }

    AppStateManager stateManager = app.getStateManager();
    if (pauseMenu != null && stateManager.hasState(pauseMenu)) {
      stateManager.detach(pauseMenu);
    }
    menuVisible = false;

    if (turnService != null) {
      turnService.setEnabled(false);
    }
    if (diceController != null) {
      diceController.enableThrow(false);
    }
    if (hudController != null) {
      hudController.detachHUD();
    }

    if (menuController != null) {
      menuController.goToMainMenu();
    }
  }
}
