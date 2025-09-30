package com.NullPtr.Pontiland.view;

import com.jme3.app.Application;
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
 * Pantalla de inicio del juego Pontiland usando Lemur para la UI. Permite seleccionar el número de
 * jugadores (2, 3 o 4) mediante botones.
 */
public class StartScreen extends AbstractAppState {

  private Node guiNode;
  private InputManager inputManager;
  private Camera camera;
  private GameApplication app;

  private Container backdrop;
  // Contenedores izquierda/derecha
  private Container leftPane;
  private Container rightPane;

  // Escala para los sprites de los botones (más pequeño que el tamaño original)
  private static final float SPRITE_SCALE = 0.6f;
  // Hover
  private static final float HOVER_SCALE_FACTOR = 1.08f; // +8%
  private static final float SCALE_LERP_SPEED = 10f; // rapidez de animación
  // Espaciado entre botones
  private static final float BUTTON_SPACING_Y = 18f;
  private static final float BUTTON_SPACING_X = 8f;

  // Estado para la animación de escala (escala del spatial del botón)
  private final Map<Button, Float> currentScales = new HashMap<>();
  private final Map<Button, Float> targetScales = new HashMap<>();
  // Tamaño base (preferido) y traducción base para escalar desde el centro
  private final Map<Button, Vector3f> basePrefSizes = new HashMap<>();
  private final Map<Button, Vector3f> baseTranslations = new HashMap<>();
  // Ángulo de rotación fijo por botón (grados)
  private final Map<Button, Float> rotationDeg = new HashMap<>();
  // Capa Z por botón (para orden de apilado) y offset manual (para ajustes finos)
  private final Map<Button, Float> zLayers = new HashMap<>();
  private final Map<Button, Vector3f> manualOffsets = new HashMap<>();

  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    this.app = (GameApplication) app;
    this.inputManager = app.getInputManager();
    this.camera = app.getCamera();
    this.guiNode = ((GameApplication) app).getGuiNode();

    // Mostrar el cursor del ratón para la UI
    inputManager.setCursorVisible(true);

    // Inicializar Lemur (idempotente)
    if (GuiGlobals.getInstance() == null) {
      GuiGlobals.initialize(app);
    }
    setupStyles();

    // Construir la pantalla de inicio
    buildUI();
  }

  /** Define estilos personalizados ligeros. */
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

  /** Construye la UI de la pantalla de inicio usando Lemur. */
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

    // Centrado del pane izquierdo dentro de la mitad izquierda de la pantalla
    Vector3f leftPref = leftPane.getPreferredSize();
    leftPane.setPreferredSize(leftPref);
    float leftX = (halfW - leftPref.x) / 2f;
    float leftY = (camera.getHeight() + leftPref.y) / 2f; // centrar verticalmente
    leftPane.setLocalTranslation(leftX, leftY, 0);

    // Pane derecho con opciones
    rightPane = new Container("pontiland");

    Label subtitle = new Label("Nueva Partida", "pontiland");
    subtitle.setFontSize(20);
    subtitle.setInsets(new com.simsilica.lemur.Insets3f(0, 0, 20, 0));
    subtitle.setColor(new ColorRGBA(0.85f, 0.86f, 0.92f, 1f));
    // Centrar el texto horizontalmente
    subtitle.setTextHAlignment(com.simsilica.lemur.HAlignment.Center);
    rightPane.addChild(subtitle);

    Container buttonsRow = rightPane.addChild(new Container(new SpringGridLayout(), "pontiland"));
    buttonsRow.setInsets(new com.simsilica.lemur.Insets3f(10, 20, 20, 20));

    // Botones como sprites con rotación ligera sobre su centro
    Button btn2 = createSpriteButton("graphics/sprites/2 Jugadores.png", 2, -8f);
    Button btn3 = createSpriteButton("graphics/sprites/3 Jugadores.png", 3, 7f);
    Button btn4 = createSpriteButton("graphics/sprites/4 Jugadores.png", 4, -5f);

    // Orden de apilado: rosa (2) encima del azul (3) encima del verde (4)
    zLayers.put(btn2, 0.3f);
    zLayers.put(btn3, 0.2f);
    zLayers.put(btn4, 0.1f);
    // Mover el sprite de 2 jugadores un poco más hacia abajo
    manualOffsets.put(btn2, new Vector3f(+10f, -10f, 0f));
    manualOffsets.put(btn3, new Vector3f(+10f, -20f, 0f));
    manualOffsets.put(btn4, new Vector3f(+10f, +30f, 0f));

    // Envolver cada botón en un contenedor que aporta el espaciado externo
    buttonsRow.addChild(wrapWithSpacing(btn2));
    buttonsRow.addChild(wrapWithSpacing(btn3));
    buttonsRow.addChild(wrapWithSpacing(btn4));

    // Ajustar el ancho preferido del título al ancho de la fila de botones para centrarlo
    // visualmente
    Vector3f rowPref = buttonsRow.getPreferredSize();
    Vector3f titlePref = subtitle.getPreferredSize().clone();
    subtitle.setPreferredSize(new Vector3f(rowPref.x, titlePref.y, 0));

    // Contenedor separado para "Cargar partida"
    Container loadContainer = rightPane.addChild(new Container("pontiland"));
    loadContainer.setInsets(new com.simsilica.lemur.Insets3f(24, 0, 10, 0));
    Button loadBtn = createLoadButton();
    loadContainer.addChild(loadBtn);

    // Centrado del pane derecho dentro de la mitad derecha de la pantalla
    Vector3f rightPref = rightPane.getPreferredSize();
    rightPane.setPreferredSize(rightPref);
    float rightX = halfW + (halfW - rightPref.x) / 2f;
    float rightY = (camera.getHeight() + rightPref.y) / 2f;
    rightPane.setLocalTranslation(rightX, rightY, 0);

    // Adjuntar al guiNode (backdrop primero, luego panes)
    guiNode.attachChild(leftPane);
    guiNode.attachChild(rightPane);
  }

  private Container wrapWithSpacing(Button b) {
    Container slot = new Container("pontiland");
    slot.setBackground(null);
    slot.setInsets(
        new com.simsilica.lemur.Insets3f(
            BUTTON_SPACING_Y, BUTTON_SPACING_X, BUTTON_SPACING_Y, BUTTON_SPACING_X));
    slot.addChild(b);
    return slot;
  }

  // Sobrecarga con ángulo
  private Button createSpriteButton(String assetPath, int playerCount, float angleDeg) {
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

    b.addClickCommands(source -> startGame(playerCount));
    return b;
  }

  // Mantener la versión previa por compatibilidad
  private Button createSpriteButton(String assetPath, int playerCount) {
    return createSpriteButton(assetPath, playerCount, 0f);
  }

  private Button createLoadButton() {
    Button b = new Button("Cargar partida", "pontiland");
    b.setTextHAlignment(com.simsilica.lemur.HAlignment.Center);
    b.setFontSize(20);
    // Fondo neutro
    b.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.22f, 0.24f, 0.29f, 1f)));
    b.setColor(ColorRGBA.White);
    // Padding interno para un tamaño cómodo
    b.setInsets(new com.simsilica.lemur.Insets3f(10, 18, 10, 18));

    // Fijar tamaño preferido (sin animación)
    Vector3f pref = b.getPreferredSize().clone();
    b.setPreferredSize(pref);

    // Sin efecto de hover: no registrar en mapas ni añadir listeners

    b.addClickCommands(source -> app.loadSavedGame());
    return b;
  }

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

      if (Math.abs(tgt - cur) >= 0.0005f) {
        float next = cur + (tgt - cur) * alpha;
        currentScales.put(btn, next);
      }
      float s = currentScales.get(btn);

      float angleRad = (float) Math.toRadians(rotationDeg.getOrDefault(btn, 0f));
      Quaternion rot = new Quaternion().fromAngleAxis(angleRad, Vector3f.UNIT_Z);
      btn.setLocalRotation(rot);

      Vector3f baseLoc = baseTranslations.get(btn);
      if (baseLoc == null) {
        baseTranslations.put(btn, btn.getLocalTranslation().clone());
        baseLoc = baseTranslations.get(btn);
      }

      Vector3f size = basePrefSizes.get(btn);
      if (size != null && baseLoc != null) {
        Vector3f center = new Vector3f(size.x * 0.5f, -size.y * 0.5f, 0);
        Vector3f offset = rot.mult(center).subtract(rot.mult(center.mult(s)));
        Vector3f manual = manualOffsets.getOrDefault(btn, Vector3f.ZERO);
        float z = zLayers.getOrDefault(btn, baseLoc.z);
        btn.setLocalTranslation(
            baseLoc.x + offset.x + manual.x, baseLoc.y + offset.y + manual.y, z);
      }

      btn.setLocalScale(s, s, 1f);
    }
  }

  /** Inicia el juego con el número de jugadores seleccionado. */
  private void startGame(int playerCount) {
    cleanup();
    app.startMainGame(playerCount);
  }

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
