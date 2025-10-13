package com.NullPtr.Pontiland;

import com.NullPtr.Pontiland.controllers.IMenuActions;
import com.NullPtr.Pontiland.controllers.LanzamientoDadosController;
import com.NullPtr.Pontiland.controllers.MenuController;
import com.NullPtr.Pontiland.repository.*;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.repository.JugadorRepository;
import com.NullPtr.Pontiland.repository.PartidaRepository;
import com.NullPtr.Pontiland.services.*;
import com.NullPtr.Pontiland.services.IDataService;
import com.NullPtr.Pontiland.services.IStartGameService;
import com.NullPtr.Pontiland.view.MenuPrincipal;
import com.NullPtr.Pontiland.view.Scene;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import com.jme3.system.AppSettings;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;

public class Launcher extends SimpleApplication {

  public Launcher() {
    super(new AudioListenerState());
  }

  private final BulletAppState bulletAppState = new BulletAppState();
  private final DiceService diceService = new DiceService();
  private ITurnService turnService;
  private LanzamientoDadosController lanzamientoDadosController;
  private Byte[] resultados = new Byte[2];
  private Scene scene;

  private IDataService dataService;
  private IStartGameService startGameService;

  private IJugadorRepository jugadorRepository;
  private IPartidaRepository partidaRepository;

  public static void main(String[] args) {
    Launcher app = new Launcher();
    AppSettings settings = new AppSettings(true);
    try {
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      int width = (int) (screen.width * 0.75);
      int height = (int) (screen.height * 0.75);
      settings.setResolution(width, height);
    } catch (HeadlessException e) {
      settings.setResolution(1280, 720);
    }
    settings.setTitle("Pontiland");
    settings.setVSync(true);
    settings.setFullscreen(false);
    settings.setGammaCorrection(true);

    app.setShowSettings(false);
    app.setSettings(settings);
    app.start();
  }

  @Override
  public void simpleInitApp() {
    inputManager.setCursorVisible(true);

    stateManager.attach(bulletAppState);
    bulletAppState.getPhysicsSpace().setAccuracy(1f / 120f);
    bulletAppState.getPhysicsSpace().setMaxSubSteps(2);

    dataService = new DataService("jdbc:h2:mem:Pontiland;DB_CLOSE_DELAY=-1");

    partidaRepository = new PartidaRepository(dataService);
    jugadorRepository = new JugadorRepository(dataService);
    turnService = new TurnService(jugadorRepository, partidaRepository, diceService);

    diceService.setTurnService(turnService);

    lanzamientoDadosController = new LanzamientoDadosController(diceService);
    lanzamientoDadosController.registerInputs(getInputManager());

    scene = new Scene(this, bulletAppState, lanzamientoDadosController);
    startGameService = new StartGameService(jugadorRepository, partidaRepository, dataService, scene);

    IMenuActions actions = new MenuController(this, startGameService, dataService);
    stateManager.attach(new MenuPrincipal(actions));
  }

  @Override
  public void simpleUpdate(float tpf) {
      if (scene != null) scene.update(tpf);

      turnService.update();

      if (scene != null && turnService.hasMovePending()) {
          int[] mv = turnService.consumeLastMove();
          if (mv != null) {
              scene.replicateFichaPosition(mv[0], mv[1]);
          }
      }

      lanzamientoDadosController.update();
  }


}
