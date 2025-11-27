package com.NullPtr.Pontiland;

import com.NullPtr.Pontiland.controllers.*;
import com.NullPtr.Pontiland.repository.*;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.repository.JugadorRepository;
import com.NullPtr.Pontiland.repository.PartidaRepository;
import com.NullPtr.Pontiland.services.*;
import com.NullPtr.Pontiland.services.IDataService;
import com.NullPtr.Pontiland.services.IStartGameService;
import com.NullPtr.Pontiland.view.HUD;
import com.NullPtr.Pontiland.view.IScene;
import com.NullPtr.Pontiland.view.MenuPrincipal;
import com.NullPtr.Pontiland.view.Scene;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.InputManager;
import com.jme3.system.AppSettings;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;

public class Launcher extends SimpleApplication {

  public Launcher() {
    super(new AudioListenerState());
  }

  private final BulletAppState bulletAppState = new BulletAppState();
  private DiceService diceService;
  private ITurnService turnService;
  private LanzamientoDadosController lanzamientoDadosController;
  private MenuPausaController menuPausaController;
  private IScene scene;

  private IDataService dataService;
  private ICasillaRepository casillaRepository;
  private IStartGameService startGameService;

  private IJugadorRepository jugadorRepository;
  private IPartidaRepository partidaRepository;
  private ICasillaService casillaService;

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
    // dataService = new DataService("jdbc:h2:./data/PontilandDB;AUTO_SERVER=TRUE");

    partidaRepository = new PartidaRepository(dataService);
    jugadorRepository = new JugadorRepository(dataService);
    casillaRepository = new CasillaRepository(dataService);
    HUD hud = new HUD();

    IHUDcontroller hudController = new HUDController(this);
    hudController.setHud(hud);

    hud.setHudController(hudController);

    diceService = new DiceService();

    PropiedadRepository propiedadRepository = new PropiedadRepository(dataService);
    AdquisicionService adquisicionService =
        new AdquisicionService(propiedadRepository, jugadorRepository);
    SubastaService subastaService =
        new SubastaService(
            adquisicionService, jugadorRepository, hudController, propiedadRepository);
    casillaService =
        new CasillaService(
            hudController,
            diceService,
            propiedadRepository,
            adquisicionService,
            new TarjetaEventoRepository(dataService),
            jugadorRepository);
    turnService =
        new TurnService(
            jugadorRepository,
            partidaRepository,
            diceService,
            casillaRepository,
            casillaService,
            hudController,
            subastaService,
            adquisicionService);
    lanzamientoDadosController = new LanzamientoDadosController(diceService);
    lanzamientoDadosController.registerInputs(getInputManager());

    hudController.setTurnService(turnService);

    scene = new Scene(this, bulletAppState, lanzamientoDadosController);
    startGameService =
        new StartGameService(
            jugadorRepository, partidaRepository, dataService, scene, propiedadRepository);
    turnService.setScene(scene);
    MenuController menuController =
        new MenuController(this, startGameService, dataService, hudController, turnService);

    // Inyectar dependencias circulares mediante setters
    menuController.setPartidaRepository((PartidaRepository) partidaRepository);
    turnService.setMenuActions(menuController);

    InputManager inputManager = getInputManager();

    menuPausaController =
        new MenuPausaController(
            this,
            inputManager,
            turnService,
            lanzamientoDadosController,
            diceService,
            hudController,
            dataService,
            partidaRepository,
            menuController);

    stateManager.attach(new MenuPrincipal(menuController));
  }

  @Override
  public void simpleUpdate(float tpf) {
    if (scene != null) scene.update(tpf);

    turnService.update();

    lanzamientoDadosController.update();
  }

  public MenuPausaController getMenuPausaController() {
    return menuPausaController;
  }
}
