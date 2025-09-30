package com.NullPtr.Pontiland.view;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.style.Styles;

/**
 * Pantalla de inicio del juego Pontiland usando Lemur para la UI. Permite seleccionar el número de
 * jugadores (2, 3 o 4) mediante botones.
 */
public class StartScreen extends AbstractAppState {

  private Node guiNode;
  private AssetManager assetManager;
  private InputManager inputManager;
  private Camera camera;
  private GameApplication app;

  // Raíz de la UI Lemur para poder limpiarla fácilmente
  private Container root;
  private Container backdrop;

  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    this.app = (GameApplication) app;
    this.assetManager = app.getAssetManager();
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

    // Contenedor raíz con layout por defecto (SpringGridLayout)
    root = new Container(new SpringGridLayout(), "pontiland");

    // Título
    Label title = new Label("PONTILAND", "pontiland");
    title.setFontSize(54);
    title.setInsets(new com.simsilica.lemur.Insets3f(20, 30, 10, 30));
    title.setColor(new ColorRGBA(0.95f, 0.96f, 1f, 1f));
    root.addChild(title);

    // Subtítulo opcional
    Label subtitle = new Label("Selecciona el número de jugadores", "pontiland");
    subtitle.setFontSize(20);
    subtitle.setInsets(new com.simsilica.lemur.Insets3f(0, 0, 20, 0));
    subtitle.setColor(new ColorRGBA(0.85f, 0.86f, 0.92f, 1f));
    root.addChild(subtitle);

    // Fila de botones
    Container buttonsRow = root.addChild(new Container(new SpringGridLayout(), "pontiland"));
    buttonsRow.setInsets(new com.simsilica.lemur.Insets3f(10, 20, 20, 20));

    Button btn2 = new Button("2 Jugadores", "pontiland");
    stylePrimaryButton(btn2);
    btn2.setPreferredSize(new com.jme3.math.Vector3f(220, 70, 0));
    btn2.addClickCommands((Command<Button>) source -> startGame(2));

    Button btn3 = new Button("3 Jugadores", "pontiland");
    stylePrimaryButton(btn3);
    btn3.setPreferredSize(new com.jme3.math.Vector3f(220, 70, 0));
    btn3.addClickCommands((Command<Button>) source -> startGame(3));

    Button btn4 = new Button("4 Jugadores", "pontiland");
    stylePrimaryButton(btn4);
    btn4.setPreferredSize(new com.jme3.math.Vector3f(220, 70, 0));
    btn4.addClickCommands((Command<Button>) source -> startGame(4));

    buttonsRow.addChild(btn2);
    buttonsRow.addChild(btn3);
    buttonsRow.addChild(btn4);

    // Espaciador inferior opcional
    Label hint = new Label("Haz clic en un botón para comenzar", "pontiland");
    hint.setFontSize(16);
    hint.setInsets(new com.simsilica.lemur.Insets3f(15, 0, 15, 0));
    hint.setColor(new ColorRGBA(0.75f, 0.78f, 0.85f, 1f));
    root.addChild(hint);

    // Centrar en pantalla usando el tamaño preferido
    Vector3f pref = root.getPreferredSize();
    root.setPreferredSize(pref);
    float x = (camera.getWidth() - pref.x) / 2f;
    float y = (camera.getHeight() + pref.y) / 2f; // Y es alto de baseline
    root.setLocalTranslation(x, y, 0);

    // Fondo a pantalla completa detrás del contenedor
    backdrop = new Container("pontiland");
    backdrop.setBackground(
        new QuadBackgroundComponent(new ColorRGBA(0.08f, 0.09f, 0.11f, 1f))); // base oscura
    backdrop.setLocalTranslation(0, camera.getHeight(), -1); // detrás
    backdrop.setPreferredSize(new Vector3f(camera.getWidth(), camera.getHeight(), 0));

    // Adjuntar al guiNode (backdrop primero, luego la UI)
    guiNode.attachChild(backdrop);
    guiNode.attachChild(root);
  }

  private void stylePrimaryButton(Button b) {
    b.setTextHAlignment(com.simsilica.lemur.HAlignment.Center);
    b.setFontSize(22);
    b.setInsets(new com.simsilica.lemur.Insets3f(12, 18, 12, 18));
    // Colores modernos
    b.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.23f, 0.51f, 0.98f, 1f)));
    b.setColor(ColorRGBA.White);
  }

  /** Inicia el juego con el número de jugadores seleccionado. */
  private void startGame(int playerCount) {
    cleanup();
    app.startMainGame(playerCount);
  }

  @Override
  public void cleanup() {
    // Eliminar lo que hayamos añadido a la GUI
    if (root != null && root.getParent() != null) {
      root.removeFromParent();
    }
    if (backdrop != null && backdrop.getParent() != null) {
      backdrop.removeFromParent();
    }
    super.cleanup();
  }
}
