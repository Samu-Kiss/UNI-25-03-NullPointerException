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
import com.jme3.cinematic.MotionPath;
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

import javax.naming.ldap.Control;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Clase de vista que construye y mantiene la escena 3D de Pontiland.
 *
 * <p>Esta clase no extiende {@link SimpleApplication}; en su lugar, recibe una referencia a una
 * instancia de SimpleApplication y a un estado de físicas para poder cargar modelos, luces y
 * cámara. Además, inyecta el dado creado en la escena en el {@link DiceService} para permitir su
 * lógica.
 */
public class Scene {
    private static final int TOTAL_CASILLAS = 40;
    private static final float CELL_SIZE = 1.4f;
    private static final float Y_PLANO = 1f;
    private static final int SIDE = TOTAL_CASILLAS / 4;
    private static final float HALF = (SIDE - 1) * CELL_SIZE * 0.5f;
    private final Vector3f BOARD_FIRST_POSITION = new Vector3f(8.3f, 0.5f, 8.5f);

    private final ExecutorService animator = Executors.newSingleThreadExecutor(r -> {Thread t = new Thread(r, "SceneAnimator");
                                                                          t.setDaemon(true);
                                                                          return t;});

  /** Referencias a la aplicación y a sus componentes para cargar assets y manipular la escena. */
  private LegacyApplication app;

  private AssetManager assetManager;
  private Node rootNode;
  private Camera cam;
  private LanzamientoDadosController lanzamientoController;
  private BulletAppState bullet;


  /**
   * Servicio de dados que gestionará la lógica; se inyecta para poder establecer el Spatial del
   * dado.
   */

  /**
   * Inicializa la escena cargando modelos, creando luces y configurando la cámara.
   *
   * @param app Instancia de {@link LegacyApplication} usada para acceder a rootNode, assetManager y
   *     cámara
   * @param bullet Estado de físicas adjunto al stateManager de la aplicación
   */
  public Scene(
      LegacyApplication app,
      BulletAppState bullet,
      LanzamientoDadosController lanzamientoController) {
    this.app = app;
    Launcher L = (Launcher) app;
    this.assetManager = L.getAssetManager();
    this.rootNode = L.getRootNode();
    this.cam = L.getCamera();
    this.bullet = bullet;
    this.lanzamientoController = lanzamientoController;
    loadBoardModel();
    loadConitoModel();
    loadDiceModels();
    setupSkyEnvironment();
    setupLighting();
    setupCamera();

  }

  /**
   * Método de actualización por frame. Actualmente no realiza ninguna acción, pero queda disponible
   * para animaciones o actualizaciones de la escena.
   *
   * @param tpf Tiempo por frame
   */
  public void update(float tpf) {
    // Este método queda disponible para futuras animaciones u otras actualizaciones visuales.
  }

  public void loadFichasModels(Ficha[] data) {
    for (int i = 0; i < data.length; i++) {
      Spatial s = assetManager.loadModel(data[i].getRutaFicha());

      int jugadorId = data[i].getJugadorId();
      s.setName("Ficha_J" + jugadorId);
      s.setUserData("jugadorId", jugadorId);

      s.scale(1f);

      Vector3f placed;
      if (i < 2) {
        placed = new Vector3f(BOARD_FIRST_POSITION.getX() + i * 0.5f, 0.5f, BOARD_FIRST_POSITION.getZ());
      } else {
        placed = new Vector3f(BOARD_FIRST_POSITION.getX() + i % 2 * 0.5f, 0.5f, BOARD_FIRST_POSITION.getZ() + 0.5f);
      }

      s.setLocalTranslation(placed);

      s.setUserData("startPosition", placed.clone());

      // store the per-spatial initial offset relative to cell 0 so future moves reuse it
      Vector3f cell0 = posFromCell(0);
      Vector3f initialOffset = placed.subtract(cell0);
      s.setUserData("initialOffset", initialOffset);

      // authoritative logical position (start at cell 0)
      s.setUserData("cellIndex", 0);

      com.jme3.bullet.control.RigidBodyControl rb = new com.jme3.bullet.control.RigidBodyControl(1f);
      s.addControl(rb);
      rb.setPhysicsLocation(placed.clone());
      bullet.getPhysicsSpace().add(rb);
      rootNode.attachChild(s);
    }
  }

  public void replicateFichaPosition(int jugadorId, int casillaIndex) {
    Spatial s = rootNode.getChild("Ficha_J" + jugadorId);
    if (s == null) return;

    // get authoritative logical index (default to 0)
    int currentIndex = 0;
    Object ciUd = s.getUserData("cellIndex");
    if (ciUd instanceof Integer) currentIndex = (Integer) ciUd;

    // keep the same key used at load time
    s.setUserData("jugadorId", jugadorId);

    // compute number of forward steps on the circular board
    int steps = (casillaIndex - currentIndex + TOTAL_CASILLAS) % TOTAL_CASILLAS;
    if (steps == 0) {
      // already there, ensure authoritative index is set
      s.setUserData("cellIndex", casillaIndex);
      return;
    }

    // prepare per-step targets and indices using posFromCell directly
    List<Vector3f> targets = new ArrayList<>();
    List<Integer> indices = new ArrayList<>();
    for (int i = 1; i <= steps; i++) {
      int idx = (currentIndex + i) % TOTAL_CASILLAS;
      Vector3f center = posFromCell(idx);
      targets.add(center);
      indices.add(idx);
    }

    RigidBodyControl rb = s.getControl(RigidBodyControl.class);
    // animate through each intermediate cell sequentially
    animateMoveAlongPath(s, rb, targets, indices, 0.45f, 0.9f);
    //TODO: No resetea la cámara al mover la ficha
    resetCamera();
  }

  /**
   * Animate a spatial sequentially through the provided targets.
   * Updates the spatial's `cellIndex` after each reached target.
   */
  private void animateMoveAlongPath(Spatial s, RigidBodyControl rb, List<Vector3f> targets, List<Integer> indices, float durationSeconds, float jumpHeight) {
    animator.submit(() -> {
      try {
        for (int seg = 0; seg < targets.size(); seg++) {
          Vector3f target = targets.get(seg);
          final Vector3f start = (rb != null && rb.getPhysicsLocation() != null)
                  ? rb.getPhysicsLocation().clone()
                  : s.getLocalTranslation().clone();
          final float startY = start.y;
          final long startNs = System.nanoTime();
          final long durationNs = (long) (durationSeconds * 1_000_000_000L);

          while (true) {
            long nowNs = System.nanoTime();
            long elapsed = nowNs - startNs;
            float t = Math.min(1f, (float) elapsed / (float) durationNs);

            float x = start.x + (target.x - start.x) * t;
            float z = start.z + (target.z - start.z) * t;
            float y = startY + (float) Math.sin(Math.PI * t) * jumpHeight;

            final Vector3f pos = new Vector3f(x, y, z);

            app.enqueue(() -> {
              if (rb != null) {
                rb.setPhysicsLocation(pos.clone());
                rb.setLinearVelocity(Vector3f.ZERO);
                rb.setAngularVelocity(Vector3f.ZERO);
              }
              s.setLocalTranslation(pos);
              focusCameraOnFicha(s.getUserData("jugadorId"));
            });

            if (t >= 1f) break;
            Thread.sleep(16); // ~60 FPS
          }

          // finalize exact placement and update authoritative cellIndex on render thread
          final int finalIdx = indices.get(seg);
          app.enqueue(() -> {
            if (rb != null) {
              rb.setPhysicsLocation(target.clone());
              rb.setLinearVelocity(Vector3f.ZERO);
              rb.setAngularVelocity(Vector3f.ZERO);
            }
            s.setLocalTranslation(target);
            s.setUserData("cellIndex", finalIdx);
          });

          // small pause to ensure render thread applied final placement before next segment
          Thread.sleep(8);
        }
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    });
  }

  private Vector3f posFromCell(int c) {
    int idx = ((c % TOTAL_CASILLAS) + TOTAL_CASILLAS) % TOTAL_CASILLAS;
    final int slotsPerSide = SIDE;

    int side = idx / slotsPerSide;
    int pos = idx % slotsPerSide;

    float x = 0f;
    float z = 0f;

    switch (side) {
      // Bottom side: right -> left
      case 0:
        x = -pos * CELL_SIZE;
        z = 0;
        break;

      // Left side: bottom -> top
      case 1:
        x = -BOARD_FIRST_POSITION.getX() * 2f;
        z = -((pos * CELL_SIZE) + pos * 0.23f);
        break;


      // Top side: left -> right
      case 2:
        z = -BOARD_FIRST_POSITION.getZ() * 2f;
        if (pos == 0) {
          x = (-BOARD_FIRST_POSITION.getX() * 2f) + (pos * CELL_SIZE);
          break;
        } else {
          x = (-BOARD_FIRST_POSITION.getX() * 2f) + (pos * CELL_SIZE) + pos * 0.24f;
          break;
        }

      // Right side: top -> bottom
      case 3:
        z = (-BOARD_FIRST_POSITION.getZ() * 2f) + (pos * CELL_SIZE) + pos * 0.24f;
        break;
    }

    return new Vector3f(
            BOARD_FIRST_POSITION.x + x,
            BOARD_FIRST_POSITION.y,
            BOARD_FIRST_POSITION.z + z
    );
  }



  /** Carga el modelo del tablero e inicializa su cuerpo físico estático. */
  private void loadBoardModel() {
    try {
      Spatial board = assetManager.loadModel("graphics/models/Board.glb");

      board.scale(5f);
      board.setLocalTranslation(0, 0, 0);
      rootNode.attachChild(board);
      // Añadir un RigidBodyControl con masa cero para que colisione pero no se mueva
      RigidBodyControl boardPhysics = new RigidBodyControl(0f);
      board.addControl(boardPhysics);
      bullet.getPhysicsSpace().add(boardPhysics);
    } catch (Exception ex) {
      // Objeto de respaldo si falla la carga del tablero
      Box fallback = new Box(1, 1, 1);
      Geometry geom = new Geometry("FallbackBoard", fallback);
      Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      mat.setColor("Color", ColorRGBA.Red);
      geom.setMaterial(mat);
      rootNode.attachChild(geom);
    }
  }

  /** Carga el modelo del peón (conito) con cuerpo físico estático. */
  private void loadConitoModel() {
    try {
      Spatial conito = assetManager.loadModel("graphics/models/Conito.glb");
      conito.scale(10f);
      conito.setLocalTranslation(0, 0, 0);
      conito.setName("Conito");
      rootNode.attachChild(conito);
      RigidBodyControl conitoPhysics = new RigidBodyControl(0f);
      conito.addControl(conitoPhysics);
      bullet.getPhysicsSpace().add(conitoPhysics);
    } catch (Exception ex) {
      // Objeto de respaldo si falla la carga del conito
      Box fallback = new Box(1, 1, 1);
      Geometry geom = new Geometry("FallbackConito", fallback);
      Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      mat.setColor("Color", ColorRGBA.Blue);
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
      Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      mat1.setColor("Color", ColorRGBA.Blue);
      geom1.setMaterial(mat1);
      geom1.setLocalTranslation(-2f, 10f, 0f);
      rootNode.attachChild(geom1);
      Box fallback2 = new Box(1, 1, 1);
      Geometry geom2 = new Geometry("FallbackDice2", fallback2);
      Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      mat2.setColor("Color", ColorRGBA.Cyan);
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
      System.err.println("Failed to load HDRi sky: " + ex.getMessage());
    }
  }

  /** Configura las luces direccional y ambiental de la escena. */
  private void setupLighting() {
    DirectionalLight sun = new DirectionalLight();
    sun.setColor(ColorRGBA.White);
    sun.setDirection(new Vector3f(-1f, -2f, -1f).normalizeLocal());
    rootNode.addLight(sun);
    AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White.mult(0.25f));
    rootNode.addLight(ambient);
  }

  /** Posiciona la cámara y ajusta su perspectiva y velocidad de movimiento. */
  private void setupCamera() {
    cam.setLocation(new Vector3f(15, 15, 15));
    cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    cam.setFrustumPerspective(60f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 500f);
  }

  public void resetCamera() {
    cam.setLocation(new Vector3f(0, 15, 15));
    cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
  }

  public void focusCameraOnFicha(int jugadorId) {
    Spatial s = rootNode.getChild("Ficha_J" + jugadorId);
    if (s != null) {
      Vector3f fichaPos = s.getLocalTranslation();
      Vector3f camPos = fichaPos.add(new Vector3f(5, 5, 5));
      cam.setLocation(camPos);
      cam.lookAt(fichaPos, Vector3f.UNIT_Y);
    }
  }
}


