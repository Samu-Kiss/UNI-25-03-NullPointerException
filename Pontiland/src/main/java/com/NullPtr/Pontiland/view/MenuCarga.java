package com.NullPtr.Pontiland.view;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.Styles;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Menú de carga de partidas basado en el layout del menú principal.
 *
 * <p>Recibe una lista de partidas y las muestra en el contenedor de la derecha. Al hacer clic en
 * una partida, se invoca el callback con el id seleccionado.
 */
public class MenuCarga extends AbstractAppState {

  // Modelo simple de partida mostrable en la lista
  public static class SavedGame {
    public final String id;
    public final String titulo; // texto a mostrar (puede incluir fecha/slot)

    public SavedGame(String id, String titulo) {
      this.id = Objects.requireNonNull(id, "id");
      this.titulo = Objects.requireNonNull(titulo, "titulo");
    }
  }

  // Entradas externas
  private final List<SavedGame> saves;
  private final Consumer<String> onSelect;

  // Referencias JME/Lemur
  private GameApplication app;
  private Node guiNode;
  private InputManager inputManager;
  private Camera camera;

  // Contenedores UI
  private Container backdrop;
  private Container leftPane;
  private Container rightPane;

  public MenuCarga(List<SavedGame> saves, Consumer<String> onSelect) {
    this.saves = saves == null ? Collections.emptyList() : saves;
    this.onSelect = onSelect;
  }

  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    this.app = (GameApplication) app;
    this.guiNode = this.app.getGuiNode();
    this.inputManager = app.getInputManager();
    this.camera = app.getCamera();

    inputManager.setCursorVisible(true);

    if (GuiGlobals.getInstance() == null) {
      GuiGlobals.initialize(app);
    }
    setupStyles();
    buildUI();
  }

  private void setupStyles() {
    Styles styles = GuiGlobals.getInstance().getStyles();
    styles
        .getSelector("container", "pontiland")
        .set(
            "background",
            new QuadBackgroundComponent(new ColorRGBA(0.10f, 0.11f, 0.14f, 0.88f)),
            false);
    styles.getSelector("button", "pontiland").set("color", ColorRGBA.White, false);
    styles.getSelector("label", "pontiland").set("color", ColorRGBA.White, false);
  }

  private void buildUI() {
    cleanup();

    // Backdrop a pantalla completa
    backdrop = new Container("pontiland");
    backdrop.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.08f, 0.09f, 0.11f, 1f)));
    backdrop.setLocalTranslation(0, camera.getHeight(), -1);
    backdrop.setPreferredSize(new Vector3f(camera.getWidth(), camera.getHeight(), 0));
    guiNode.attachChild(backdrop);

    float halfW = camera.getWidth() / 2f;

    // Panel izquierdo: título
    leftPane = new Container("pontiland");
    Label title = new Label("PONTILAND", "pontiland");
    title.setFontSize(54);
    title.setInsets(new com.simsilica.lemur.Insets3f(20, 30, 10, 30));
    title.setColor(new ColorRGBA(0.95f, 0.96f, 1f, 1f));
    leftPane.addChild(title);

    Vector3f leftPref = leftPane.getPreferredSize();
    leftPane.setPreferredSize(leftPref);
    float leftX = (halfW - leftPref.x) / 2f;
    float leftY = (camera.getHeight() + leftPref.y) / 2f;
    leftPane.setLocalTranslation(leftX, leftY, 0);

    // Panel derecho: subtítulo + lista
    rightPane = new Container("pontiland");

    Label subtitle = new Label("Cargar partida", "pontiland");
    subtitle.setFontSize(22);
    subtitle.setInsets(new com.simsilica.lemur.Insets3f(0, 0, 16, 0));
    subtitle.setColor(new ColorRGBA(0.85f, 0.86f, 0.92f, 1f));
    subtitle.setTextHAlignment(com.simsilica.lemur.HAlignment.Center);
    rightPane.addChild(subtitle);

    // Contenedor de lista (sin scroll por compatibilidad)
    Container listContainer = new Container("pontiland");
    listContainer.setBackground(null);

    if (saves.isEmpty()) {
      Label empty = new Label("No hay partidas guardadas", "pontiland");
      empty.setColor(new ColorRGBA(0.85f, 0.86f, 0.92f, 1f));
      empty.setTextHAlignment(com.simsilica.lemur.HAlignment.Center);
      listContainer.addChild(empty);
    } else {
      for (SavedGame sg : saves) {
        Button item = createSaveItemButton(sg);
        Container slot = new Container("pontiland");
        slot.setBackground(null);
        slot.setInsets(new com.simsilica.lemur.Insets3f(8, 4, 8, 4));
        slot.addChild(item);
        listContainer.addChild(slot);
      }
    }

    // Fijar tamaño preferido del contenedor de lista para mantener el ancho
    float prefWidth = Math.max(420f, halfW * 0.8f);
    Vector3f listPref = listContainer.getPreferredSize().clone();
    listPref.x = prefWidth;
    listContainer.setPreferredSize(listPref);
    rightPane.addChild(listContainer);

    // Centrar el panel derecho en su mitad de pantalla
    Vector3f rightPref = rightPane.getPreferredSize();
    rightPane.setPreferredSize(rightPref);
    float rightX = halfW + (halfW - rightPref.x) / 2f;
    float rightY = (camera.getHeight() + rightPref.y) / 2f;
    rightPane.setLocalTranslation(rightX, rightY, 0);

    guiNode.attachChild(leftPane);
    guiNode.attachChild(rightPane);
  }

  private Button createSaveItemButton(SavedGame sg) {
    Button b = new Button(sg.titulo, "pontiland");
    b.setTextHAlignment(com.simsilica.lemur.HAlignment.Left);
    b.setFontSize(18);
    b.setInsets(new com.simsilica.lemur.Insets3f(10, 14, 10, 14));
    // Fondo para ítem
    b.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.22f, 0.24f, 0.29f, 1f)));

    // Tamaño preferido: ancho fijo cómodo, alto en base al texto
    Vector3f pref = b.getPreferredSize().clone();
    pref.x = Math.max(pref.x, 360f);
    b.setPreferredSize(pref);

    b.addClickCommands(source -> onItemSelected(sg.id));
    return b;
  }

  private void onItemSelected(String id) {
    try {
      if (onSelect != null) {
        onSelect.accept(id);
      }
    } finally {
      // Cerrar este menú
      cleanup();
      if (app != null) {
        app.getStateManager().detach(this);
      }
    }
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
  }
}
