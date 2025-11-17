package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.controllers.LanzamientoDadosController;
import com.NullPtr.Pontiland.entities.Ficha;
import com.NullPtr.Pontiland.services.DiceService;
import com.jme3.app.LegacyApplication;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Clase de vista que construye y mantiene la escena 3D de Pontiland.
 *
 * <p>Esta clase no extiende {@link SimpleApplication}; en su lugar, recibe una referencia a una
 * instancia de SimpleApplication y a un estado de físicas para poder cargar modelos, luces y
 * cámara. Además, inyecta el dado creado en la escena en el {@link DiceService} para permitir su
 * lógica.
 */
public class Scene implements IScene {
  private static final int TOTAL_CASILLAS = 40;
  private static final String DEFAULT_MATERIAL = "Common/MatDefs/Misc/Unshaded.j3md";
  private final Vector3f boardFirstPosition = new Vector3f(9.3f, 0.5f, 9.3f);

  private LegacyApplication app;

  private AssetManager assetManager;
  private Node rootNode;
  private Camera cam;
  private LanzamientoDadosController lanzamientoController;
  private BulletAppState bullet;

  // New collaborators
  private CameraController cameraController;
  private MovementAnimator movementAnimator;
  private FichaController fichaController;

  // Logging
  private static Logger logger = LogManager.getLogger(Scene.class);

  public Scene(
      LegacyApplication app,
      BulletAppState bullet,
      LanzamientoDadosController lanzamientoController) {
    this.app = app;
    Launcher launcher = (Launcher) app;
    this.assetManager = launcher.getAssetManager();
    this.rootNode = launcher.getRootNode();
    this.cam = launcher.getCamera();
    this.bullet = bullet;
    this.lanzamientoController = lanzamientoController;

    // Inicializar controladores
    this.cameraController = new CameraController(cam, rootNode);
    this.movementAnimator = new MovementAnimator(lanzamientoController, cameraController);
    this.fichaController =
        new FichaController(assetManager, bullet, rootNode, boardFirstPosition, TOTAL_CASILLAS);

    // Cargar escena
    loadBoardModel();
    loadConitoModel();
    loadDiceModels();
    setupSkyEnvironment();
    setupLighting();
    cameraController.setupCamera();
  }

  /**
   * Método de actualización por frame. Actualmente no realiza ninguna acción, pero queda disponible
   * para animaciones o actualizaciones de la escena.
   *
   * @param tpf Tiempo por frame
   */
  @Override
  public void update(float tpf) {
    movementAnimator.update(tpf);
    cameraController.update(tpf);
  }

  @Override
  public void resetCamera() {
    cameraController.resetCamera();
  }

  @Override
  public void loadFichasModels(Ficha[] data) {
    fichaController.loadFichasModels(data);
  }

  @Override
  public void replicateFichaPosition(int jugadorId, int casillaIndex) {
    // delegar al controlador de fichas y pasar el animator
    fichaController.replicateFichaPosition(jugadorId, casillaIndex, movementAnimator);
    lanzamientoController.enableThrow(false);
  }

  // Revisar si se puede
  private Vector3f posFromCell(int c) {
    return fichaController != null ? fichaController.posFromCell(c) : new Vector3f();
  }

  /** Carga el modelo del tablero e inicializa su cuerpo físico estático. */
  private void loadBoardModel() {
    try {
      Spatial board = assetManager.loadModel("graphics/models/Board.glb");

      board.scale(5f);
      board.setLocalTranslation(0, 0, 0);
      rootNode.attachChild(board);

      // float tamCasilla = medirTamanoCasilla(board); System.out.println("≈ tamaño casilla: " +
      // tamCasilla);

      // Añadir un RigidBodyControl con masa cero para que colisione pero no se mueva
      RigidBodyControl boardPhysics = new RigidBodyControl(0f);
      board.addControl(boardPhysics);
      bullet.getPhysicsSpace().add(boardPhysics);
    } catch (Exception ex) {
      // Objeto de respaldo si falla la carga del tablero
      Box fallback = new Box(1, 1, 1);
      Geometry geom = new Geometry("FallbackBoard", fallback);
      Material mat = new Material(assetManager, DEFAULT_MATERIAL);
      mat.setColor("ColorRojo", ColorRGBA.Red);
      geom.setMaterial(mat);
      rootNode.attachChild(geom);
    }
  }

  /** Carga el modelo del peón (conito) con cuerpo físico estático. */
  private void loadConitoModel() {
    try {
      Spatial conito = assetManager.loadModel("graphics/models/Conito.glb");
      conito.scale(10f);
      conito.setLocalTranslation(0, -0.5f, 0);
      conito.setName("Conito");
      rootNode.attachChild(conito);
      RigidBodyControl conitoPhysics = new RigidBodyControl(0f);
      conito.addControl(conitoPhysics);
      bullet.getPhysicsSpace().add(conitoPhysics);
    } catch (Exception ex) {
      // Objeto de respaldo si falla la carga del conito
      Box fallback = new Box(1, 1, 1);
      Geometry geom = new Geometry("FallbackConito", fallback);
      Material mat = new Material(assetManager, DEFAULT_MATERIAL);
      mat.setColor("ColorAzuñ", ColorRGBA.Blue);
      geom.setMaterial(mat);
      rootNode.attachChild(geom);
    }
  }

  /**
   * Carga dos modelos de dados, crea sus cuerpos físicos y los inyecta en el servicio. Se activan
   * funciones de detección de colisión continua (CCD) para evitar que atraviesen otros objetos
   * cuando ruedan a gran velocidad.
   */
  private void loadDiceModels() {
    try {
      Spatial dice1 = assetManager.loadModel("graphics/models/Dice.glb");
      dice1.scale(10f);
      dice1.setName("Dice1");
      RigidBodyControl dice1Physics = new RigidBodyControl(1.5f);
      dice1.addControl(dice1Physics);
      dice1Physics.setRestitution(1f);
      // Configurar Continuous Collision Detection (CCD)
      dice1Physics.setCcdMotionThreshold(0.25f);
      dice1Physics.setCcdSweptSphereRadius(0.25f);
      dice1Physics.setPhysicsLocation(new Vector3f(0f, 5f, 0f));
      rootNode.attachChild(dice1);
      bullet.getPhysicsSpace().add(dice1Physics);

      // === DADO 2 ===
      Spatial dice2 = assetManager.loadModel("graphics/models/Dice.glb");
      dice2.setName("Dice2");
      dice2.scale(10f);
      RigidBodyControl dice2Physics = new RigidBodyControl(1.5f);
      dice2.addControl(dice2Physics);
      dice2Physics.setRestitution(0f);
      // Configurar CCD para el segundo dado
      dice2Physics.setCcdMotionThreshold(0.25f);
      dice2Physics.setCcdSweptSphereRadius(0.25f);
      dice2Physics.setPhysicsLocation(new Vector3f(0f, 6f, 0f));
      rootNode.attachChild(dice2);
      bullet.getPhysicsSpace().add(dice2Physics);

      lanzamientoController.onDadosCreados(dice1, dice2);

    } catch (Exception ex) {
      // Si falla la carga de los modelos, crear cubos de respaldo para ambos dados
      Box fallback1 = new Box(1, 1, 1);
      Geometry geom1 = new Geometry("FallbackDice1", fallback1);
      Material mat1 = new Material(assetManager, DEFAULT_MATERIAL);
      mat1.setColor("ColorVerde", ColorRGBA.Green);
      geom1.setMaterial(mat1);
      geom1.setLocalTranslation(-2f, 10f, 0f);
      rootNode.attachChild(geom1);
      Box fallback2 = new Box(1, 1, 1);
      Geometry geom2 = new Geometry("FallbackDice2", fallback2);
      Material mat2 = new Material(assetManager, DEFAULT_MATERIAL);
      mat2.setColor("ColorCyan", ColorRGBA.Cyan);
      geom2.setMaterial(mat2);
      geom2.setLocalTranslation(2f, 10f, 0f);
      rootNode.attachChild(geom2);
    }
  }

  /** Configura un cielo HDRi alrededor de la escena para iluminar correctamente. */
  private void setupSkyEnvironment() {
    try {
      Texture hdr =
          assetManager.loadTexture("graphics/HDRi/kloofendal_48d_partly_cloudy_puresky_4k.hdr");
      rootNode.attachChild(
          SkyFactory.createSky(assetManager, hdr, SkyFactory.EnvMapType.EquirectMap));
    } catch (Exception ex) {
      logger.error("Failed to load HDRi sky", ex);
    }
  }

  /** Configura las luces direccional y ambiental de la escena. */
  private void setupLighting() {
    DirectionalLight luz = new DirectionalLight();
    luz.setColor(ColorRGBA.White);
    luz.setDirection(new Vector3f(-1f, -2f, -1f).normalizeLocal());
    rootNode.addLight(luz);
    AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White.mult(0.25f));
    rootNode.addLight(ambient);
  }
}
