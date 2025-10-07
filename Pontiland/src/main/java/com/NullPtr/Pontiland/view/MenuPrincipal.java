package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.controllers.IMenuActions;
import com.jme3.app.Application;
import com.jme3.app.LegacyApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.TextureKey;
import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.CursorMotionEvent;
import com.simsilica.lemur.event.DefaultCursorListener;
import com.simsilica.lemur.style.Styles;
import java.util.HashMap;
import java.util.Map;

/**
 * Pantalla de inicio del juego Pontiland usando Lemur para la UI.
 *
 * <p>Responsabilidades clave: - Inicializar y aplicar estilos Lemur. - Construir el layout de
 * pantalla con dos paneles (título a la izquierda y opciones a la derecha). - Renderizar los
 * sprites-botón opciones con rotación ligera, escala animada en hover y pivote centrado. -
 * Administrar el orden de apilado (Z) y offsets manuales para un look superpuesto. - Exponer un
 * botón adicional “Cargar partida” sin animación de escala.
 */
public class MenuPrincipal extends AbstractAppState {

  // Referencias del entorno JME3/Lemur necesarias para la UI en pantalla
  private Node guiNode;
  private InputManager inputManager;
  private Camera camera;
  private IMenuActions actions;
  private LegacyApplication app;

  // Contenedor de fondo (backdrop) y paneles principales
  private Container backdrop;
  // Contenedores izquierda/derecha
  private Container leftPane;
  private Container rightPane;

  // Parámetros visuales de los sprites (tamaño y hover)
  private static final float SPRITE_SCALE = 0.6f;
  private static final float HOVER_SCALE_FACTOR = 1.08f; // +8% de escala en hover
  private static final float SCALE_LERP_SPEED = 10f; // velocidad de interpolación de escala
  // Espaciado entre botones (en el contenedor envolvente)
  private static final float BUTTON_SPACING_Y = 18f;
  private static final float BUTTON_SPACING_X = 8f;

  // Estado de animación por botón: escala actual/objetivo
  private final Map<Button, Float> currentScales = new HashMap<>();
  private final Map<Button, Float> targetScales = new HashMap<>();
  // Tamaño preferido base del botón y traducción base (calculada por el layout)
  private final Map<Button, Vector3f> basePrefSizes = new HashMap<>();
  private final Map<Button, Vector3f> baseTranslations = new HashMap<>();
  // Ángulo de rotación fijo por botón (en grados)
  private final Map<Button, Float> rotationDeg = new HashMap<>();
  // Capa Z por botón para controlar el orden de apilado y ajustes finos de posición
  private final Map<Button, Float> zLayers = new HashMap<>();
  private final Map<Button, Vector3f> manualOffsets = new HashMap<>();

  public MenuPrincipal(IMenuActions actions) {
    this.actions = actions;
  }

  /**
   * Inicializa el estado al adjuntarse al gestor de estados. - Guarda referencias a
   * app/cámara/entrada. - Asegura la inicialización de Lemur. - Construye la UI.
   */
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    Launcher L = (Launcher) app;
    this.app = L;
    this.inputManager = L.getInputManager();
    this.camera = L.getCamera();
    this.guiNode = L.getGuiNode();

    inputManager.setCursorVisible(true);
    if (GuiGlobals.getInstance() == null) {
      GuiGlobals.initialize(app);
    }
    setupStyles();
    buildUI();
  }

  /**
   * Define estilos básicos para contenedores y botones bajo el estilo "pontiland". Mantiene fondo
   * oscuro semi-transparente y texto claro por defecto.
   */
  private void setupStyles() {
    Styles styles = GuiGlobals.getInstance().getStyles();

    // Definir una apariencia base para contenedores y botones con estilo "pontiland"
    styles
        .getSelector("container", "pontiland")
        .set(
            "background",
            new QuadBackgroundComponent(new ColorRGBA(0.10f, 0.11f, 0.14f, 0.88f)),
            false);

    styles.getSelector("button", "pontiland").set("color", ColorRGBA.White, false);
  }

  /**
   * Construye el layout de la pantalla: - Backdrop a pantalla completa. - Panel izquierdo con el
   * título. - Panel derecho con subtítulo, fila de sprites-botón y botón “Cargar partida”. -
   * Centrado manual de los paneles según el tamaño de la cámara.
   */
  private void buildUI() {
    // Limpiar si ya existía
    cleanup();

    // Fondo a pantalla completa detrás
    backdrop = new Container("pontiland");
    backdrop.setBackground(
        new QuadBackgroundComponent(new ColorRGBA(0.08f, 0.09f, 0.11f, 1f))); // base oscura
    backdrop.setLocalTranslation(0, camera.getHeight(), -1); // detrás
    backdrop.setPreferredSize(new Vector3f(camera.getWidth(), camera.getHeight(), 0));
    guiNode.attachChild(backdrop);

    float halfW = camera.getWidth() / 2f;

    // Pane izquierdo con solo el título
    leftPane = new Container("pontiland");
    Label title = new Label("PONTILAND", "pontiland");
    title.setFontSize(54);
    title.setInsets(new com.simsilica.lemur.Insets3f(20, 30, 10, 30));
    title.setColor(new ColorRGBA(0.95f, 0.96f, 1f, 1f));
    leftPane.addChild(title);

    // Centrar el panel izquierdo en su mitad de pantalla
    Vector3f leftPref = leftPane.getPreferredSize();
    leftPane.setPreferredSize(leftPref);
    float leftX = (halfW - leftPref.x) / 2f;
    float leftY = (camera.getHeight() + leftPref.y) / 2f; // centrar verticalmente
    leftPane.setLocalTranslation(leftX, leftY, 0);

    // Pane derecho con opciones
    rightPane = new Container("pontiland");

    // Subtítulo centrado
    Label subtitle = new Label("Nueva Partida", "pontiland");
    subtitle.setFontSize(20);
    subtitle.setInsets(new com.simsilica.lemur.Insets3f(0, 0, 20, 0));
    subtitle.setColor(new ColorRGBA(0.85f, 0.86f, 0.92f, 1f));
    // Centrar el texto horizontalmente
    subtitle.setTextHAlignment(com.simsilica.lemur.HAlignment.Center);
    rightPane.addChild(subtitle);

    // Fila que alberga los tres sprites-botón
    Container buttonsRow = rightPane.addChild(new Container(new SpringGridLayout(), "pontiland"));
    buttonsRow.setInsets(new com.simsilica.lemur.Insets3f(10, 20, 20, 20));

    // Botones como sprites con rotación ligera sobre su centro
    Button btnIniciar = createSpriteButton("graphics/sprites/Button_Iniciar.png", 1, -8f);
    Button btnCargar = createSpriteButton("graphics/sprites/Button_Cargar.png", 2, 7f);
    Button btnCredits = createSpriteButton("graphics/sprites/Button_Credits.png", 3, -5f);

    // Orden de apilado: rosa (2) encima del azul (3) encima del verde (4)
    zLayers.put(btnIniciar, 0.3f);
    zLayers.put(btnCargar, 0.2f);
    zLayers.put(btnCredits, 0.1f);
    // Offsets manuales para un look “apilado” y para bajar un poco el 2J
    manualOffsets.put(btnIniciar, new Vector3f(+10f, -10f, 0f));
    manualOffsets.put(btnCargar, new Vector3f(+10f, -20f, 0f));
    manualOffsets.put(btnCredits, new Vector3f(+10f, +30f, 0f));

    // Envolver cada botón en un contenedor que aporta el espaciado externo
    buttonsRow.addChild(wrapWithSpacing(btnIniciar));
    buttonsRow.addChild(wrapWithSpacing(btnCargar));
    buttonsRow.addChild(wrapWithSpacing(btnCredits));

    // Ajustar el ancho del subtítulo al ancho de la fila de botones para centrarlo visualmente
    Vector3f rowPref = buttonsRow.getPreferredSize();
    Vector3f titlePref = subtitle.getPreferredSize().clone();
    subtitle.setPreferredSize(new Vector3f(rowPref.x, titlePref.y, 0));

    // Contenedor separado para "Cargar partida" (sin animación)
    Container loadContainer = rightPane.addChild(new Container("pontiland"));
    loadContainer.setInsets(new com.simsilica.lemur.Insets3f(24, 0, 10, 0));

    // Centrar el panel derecho en su mitad de pantalla
    Vector3f rightPref = rightPane.getPreferredSize();
    rightPane.setPreferredSize(rightPref);
    float rightX = halfW + (halfW - rightPref.x) / 2f;
    float rightY = (camera.getHeight() + rightPref.y) / 2f;
    rightPane.setLocalTranslation(rightX, rightY, 0);

    // Adjuntar al guiNode (backdrop primero, luego panes)
    guiNode.attachChild(leftPane);
    guiNode.attachChild(rightPane);
  }

  /**
   * Envuelve un botón dentro de un contenedor “slot” que aporta espaciado externo sin deformar el
   * tamaño del sprite. Esto evita estirar el background del botón.
   *
   * @param b botón a envolver
   * @return contenedor con insets que crea separación exterior
   */
  private Container wrapWithSpacing(Button b) {
    Container slot = new Container("pontiland");
    slot.setBackground(null);
    slot.setInsets(
        new com.simsilica.lemur.Insets3f(
            BUTTON_SPACING_Y, BUTTON_SPACING_X, BUTTON_SPACING_Y, BUTTON_SPACING_X));
    slot.addChild(b);
    return slot;
  }

  /**
   * Crea un botón-sprite con: - Textura como fondo (sin mipmaps y con clamp para evitar bleeding).
   * - Flip vertical para GUI. - Rotación fija ligera. - Escala base y animación de hover (solo
   * escala) con pivote centrado.
   *
   * @param assetPath ruta del sprite (classpath)
   * @param option número de jugadores que disparará este botón
   * @param angleDeg ángulo fijo de rotación en grados (positivo antihorario)
   * @return botón Lemur configurado
   */
  private Button createSpriteButton(String assetPath, int option, float angleDeg) {
    Button b = new Button("", "pontiland");
    b.setBackground(null);

    // Cargar textura con ajustes para evitar bleeding y corregir flip Y para GUI
    TextureKey key = new TextureKey(assetPath, true); // flipY = true para GUI
    key.setGenerateMips(false);
    Texture2D tex = (Texture2D) this.app.getAssetManager().loadTexture(key);
    tex.setWrap(Texture.WrapMode.EdgeClamp);
    tex.setMagFilter(Texture.MagFilter.Bilinear);
    tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

    // Usar la textura como fondo del botón
    QuadBackgroundComponent bg = new QuadBackgroundComponent(tex);
    b.setBackground(bg);

    // Ajustar tamaño del botón al de la textura escalada
    int w = tex.getImage().getWidth();
    int h = tex.getImage().getHeight();
    Vector3f pref = new Vector3f(w * SPRITE_SCALE, h * SPRITE_SCALE, 0);
    b.setPreferredSize(pref);
    basePrefSizes.put(b, pref.clone());

    // Estado animación + rotación
    currentScales.put(b, 1f);
    targetScales.put(b, 1f);
    rotationDeg.put(b, angleDeg);
    b.setLocalRotation(
        new Quaternion().fromAngleAxis((float) Math.toRadians(angleDeg), Vector3f.UNIT_Z));

    // Hover: solo escala (rotación fija)
    final Button btnRef = b;
    CursorEventControl.addListenersToSpatial(
        b,
        new DefaultCursorListener() {
          @Override
          public void cursorEntered(CursorMotionEvent event, Spatial target, Spatial capture) {
            targetScales.put(btnRef, HOVER_SCALE_FACTOR);
          }

          @Override
          public void cursorExited(CursorMotionEvent event, Spatial target, Spatial capture) {
            targetScales.put(btnRef, 1f);
          }
        });

    b.addClickCommands(source -> nextMenu(option));
    return b;
  }

  /**
   * Bucle de actualización para animación y posicionamiento: - Interpola la escala del botón hacia
   * el objetivo (hover in/out). - Aplica la rotación fija (Quaternion sobre Z). - Recalcula la
   * traslación para pivotar en el centro geométrico del sprite: offset = rot(center) - rot(center *
   * s); posición = baseLoc + offset + manualOffset - Ajusta la componente Z según zLayers para
   * controlar orden de apilado.
   *
   * @param tpf tiempo por frame (segundos)
   */
  @Override
  public void update(float tpf) {
    super.update(tpf);
    if (targetScales.isEmpty()) {
      return;
    }
    float alpha = Math.min(1f, SCALE_LERP_SPEED * tpf);

    for (Map.Entry<Button, Float> e : targetScales.entrySet()) {
      Button btn = e.getKey();
      float cur = currentScales.getOrDefault(btn, 1f);
      float tgt = e.getValue();

      // Interpolar escala hacia el objetivo con un lerp estable
      if (Math.abs(tgt - cur) >= 0.0005f) {
        float next = cur + (tgt - cur) * alpha;
        currentScales.put(btn, next);
      }
      try {
        float s = currentScales.getOrDefault(btn, 1f);

        // Rotación fija
        float angleRad = (float) Math.toRadians(rotationDeg.getOrDefault(btn, 0f));
        Quaternion rot = new Quaternion().fromAngleAxis(angleRad, Vector3f.UNIT_Z);
        btn.setLocalRotation(rot);

        // Capturar baseLoc una sola vez (posición calculada por el layout, sin offset)
        Vector3f baseLoc = baseTranslations.get(btn);
        if (baseLoc == null) {
          baseTranslations.put(btn, btn.getLocalTranslation().clone());
          baseLoc = baseTranslations.get(btn);
        }

        // Calcular offset centrado y aplicar offsets/orden Z
        Vector3f size = basePrefSizes.get(btn);
        if (size != null && baseLoc != null) {
          Vector3f center = new Vector3f(size.x * 0.5f, -size.y * 0.5f, 0);
          Vector3f offset = rot.mult(center).subtract(rot.mult(center.mult(s)));
          Vector3f manual = manualOffsets.getOrDefault(btn, Vector3f.ZERO);
          float z = zLayers.getOrDefault(btn, baseLoc.z);
          btn.setLocalTranslation(
              baseLoc.x + offset.x + manual.x, baseLoc.y + offset.y + manual.y, z);
        }

        // Aplicar escala final
        btn.setLocalScale(s, s, 1f);
      } catch (NullPointerException | IllegalArgumentException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Solicita al GameApplication el juego para el número de jugadores indicado. Limpia la UI de
   * inicio antes de cambiar de estado.
   *
   * @param option 1 -> Iniciar, 2 -> Cargar, 3 -> Créditos
   */
  private void nextMenu(int option) {
    cleanup();
    switch (option) {
      case 1 -> actions.startPlayerSelection();
      case 2 -> actions.loadSavedGame();
      case 3 -> actions.showCredits("Samu-Kiss", "UNI-25-03-NullPointerException", 15);
      default -> actions.goToMainMenu();
    }
  }

  /**
   * Limpia y desengancha la UI de inicio: - Elimina contenedores del guiNode. - Resetea los mapas
   * de animación/posición para evitar fugas de estado.
   */
  @Override
  public void cleanup() {
    // Eliminar lo que hayamos añadido a la GUI
    if (leftPane != null && leftPane.getParent() != null) {
      leftPane.removeFromParent();
    }
    if (rightPane != null && rightPane.getParent() != null) {
      rightPane.removeFromParent();
    }
    if (backdrop != null && backdrop.getParent() != null) {
      backdrop.removeFromParent();
    }
    super.cleanup();
    // Limpiar estado de animación
    currentScales.clear();
    targetScales.clear();
    basePrefSizes.clear();
    baseTranslations.clear();
    rotationDeg.clear();
  }
}
