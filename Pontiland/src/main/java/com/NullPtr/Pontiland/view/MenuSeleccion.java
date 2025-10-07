package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.controllers.IMenuActions;
import com.NullPtr.Pontiland.entities.Jugador;
import com.jme3.app.Application;
import com.jme3.app.LegacyApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.Styles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pantalla de selección de personajes y nickname para cada jugador. Flujo: viene desde
 * MenuJugadores (playerCount). Aquí se capturan nombres y personaje (ID 1..7) y se llama a
 * actions.startMainGame(playerCount, jugadores, personajesSeleccionados).
 *
 * <p>NOTA: Los modelos 3D de personajes aún no están en assets/models/characters, por lo que se
 * usan placeholders (solo texto). Cuando se agreguen, se puede cargar el Spatial sobre el sprite
 * Bill correspondiente.
 */
public class MenuSeleccion extends AbstractAppState {
  private static final double STARTING_MONEY = 0.0;
  private static final String[] CHARACTER_NAMES = {
    "Kiwi", "Balon", "Maleta", "Pescadito", "Carnet", "Ignacito", "Nave"
  };
  private static final String[] BILL_SPRITES = {
    "graphics/sprites/Bill_Rosado.png",
    "graphics/sprites/Bill_Morado.png",
    "graphics/sprites/Bill_Azul.png",
    "graphics/sprites/Bill_Verde.png"
  };

  private static final String ERR_DUPLICADOS = "Personajes repetidos";
  private static final String ERR_SIN_SUFFICIENTES = "No hay suficientes personajes";

  private final IMenuActions actions;
  private final int playerCount;
  private LegacyApplication app;
  private Node guiNode;
  private Camera cam;

  private Container backdrop;
  private Container backBar;
  private Button startButton;
  private Label errorLabel; // reemplaza topBar por backBar
  // Ya no usamos mainPane/playersRow para grid automático: posicionaremos manualmente
  private final List<TextField> nameFields = new ArrayList<>();
  private final List<Integer> characterIndex = new ArrayList<>();
  private final List<Label> characterLabels = new ArrayList<>();
  private final List<Container> playerPanels = new ArrayList<>();
  private final List<Label> headerLabels = new ArrayList<>();
  private boolean lastReadyState = false;

  private static final int BILL_WIDTH = 400;
  private static final int BILL_HEIGHT = 140;

  public MenuSeleccion(IMenuActions actions, int playerCount) {
    this.actions = actions;
    this.playerCount = playerCount;
  }

  @Override
  public void initialize(AppStateManager sm, Application application) {
    super.initialize(sm, application);
    Launcher L = (Launcher) application;
    this.app = L;
    this.guiNode = L.getGuiNode();
    this.cam = L.getCamera();
    if (GuiGlobals.getInstance() == null) {
      GuiGlobals.initialize(application);
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
    // Fondo igual que otras pantallas
    backdrop = new Container("pontiland");
    backdrop.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.08f, 0.09f, 0.11f, 1f)));
    backdrop.setLocalTranslation(0, cam.getHeight(), -1);
    backdrop.setPreferredSize(new Vector3f(cam.getWidth(), cam.getHeight(), 0));
    guiNode.attachChild(backdrop);

    // Barra volver (mismo estilo que MenuJugadores)
    backBar = new Container("pontiland");
    backBar.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.16f, 0.18f, 0.22f, 0.85f)));
    backBar.setInsets(new Insets3f(6, 10, 6, 10));
    Container row = new Container(new BorderLayout(), "pontiland");

    TextureKey key = new TextureKey("graphics/sprites/Icon_Back_White.png", true);
    key.setGenerateMips(false);
    Texture2D iconTex = (Texture2D) this.app.getAssetManager().loadTexture(key);
    iconTex.setWrap(Texture.WrapMode.EdgeClamp);
    iconTex.setMagFilter(Texture.MagFilter.Bilinear);
    iconTex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

    Button iconBtn = new Button("", "pontiland");
    iconBtn.setBackground(new QuadBackgroundComponent(iconTex));
    iconBtn.setPreferredSize(new Vector3f(24, 24, 0));
    iconBtn.setInsets(new Insets3f(0, 0, 0, 6));
    iconBtn.addClickCommands(src -> actions.startPlayerSelection());

    Button textBtn = new Button("Volver", "pontiland");
    textBtn.setFontSize(16);
    textBtn.setInsets(new Insets3f(2, 6, 2, 6));
    textBtn.addClickCommands(src -> actions.startPlayerSelection());

    row.addChild(iconBtn, BorderLayout.Position.West);
    row.addChild(textBtn, BorderLayout.Position.Center);
    backBar.addChild(row);
    backBar.setLocalTranslation(10, cam.getHeight() - 10, 1);
    guiNode.attachChild(backBar);

    // Crear paneles de jugadores y posicionar
    for (int i = 0; i < playerCount; i++) createPlayerPanel(i);
    layoutPlayerPanels();

    // Botón PLAY centrado (mantiene estilo anterior, pero ahora texto blanco sobre fondo amarillo)
    startButton = new Button("PLAY", "pontiland");
    startButton.setFontSize(28);
    startButton.setEnabled(false);
    startButton.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.95f, 0.9f, 0.3f, 1f)));
    startButton.addClickCommands(src -> attemptStart());
    Vector3f pref = startButton.getPreferredSize();
    startButton.setLocalTranslation((cam.getWidth() - pref.x) / 2f, 90, 0);
    guiNode.attachChild(startButton);

    errorLabel = new Label("", "pontiland");
    errorLabel.setColor(new ColorRGBA(1f, 0.4f, 0.4f, 1f));
    Vector3f ep = errorLabel.getPreferredSize();
    errorLabel.setLocalTranslation((cam.getWidth() - ep.x) / 2f, 60, 0);
    guiNode.attachChild(errorLabel);

    // Validación inmediata si jugadores > personajes
    if (playerCount > CHARACTER_NAMES.length) {
      errorLabel.setText(ERR_SIN_SUFFICIENTES);
    }
  }

  private void createPlayerPanel(int idx) {
    Container panel = new Container("pontiland");
    panel.setBackground(null);

    Label header = new Label("Nickname J" + (idx + 1), "pontiland");
    header.setFontSize(22);
    panel.addChild(header);
    headerLabels.add(header);

    // Fila central con flechas y billete usando BorderLayout para alinear horizontalmente
    Container middle = new Container(new BorderLayout(), "pontiland");
    middle.setBackground(null);

    // Wrapper izquierda para centrar verticalmente
    Container leftWrap = new Container(new BorderLayout(), "pontiland");
    leftWrap.setBackground(null);
    leftWrap.setPreferredSize(new Vector3f(40, BILL_HEIGHT, 0));
    Button left = createArrowButton(false, idx);
    leftWrap.addChild(left, BorderLayout.Position.Center);
    middle.addChild(leftWrap, BorderLayout.Position.West);

    // Bill sprite central
    Container bill = new Container("pontiland");
    bill.setBackground(loadBillSprite(idx));
    bill.setPreferredSize(new Vector3f(BILL_WIDTH, BILL_HEIGHT, 0));

    // Asignación inicial única mientras existan personajes libres
    int initialCharIdx = getFirstFreeCharacter();
    characterIndex.add(initialCharIdx);
    Label charName = new Label(CHARACTER_NAMES[initialCharIdx], "pontiland");
    charName.setFontSize(20);
    characterLabels.add(charName);
    Vector3f cs = charName.getPreferredSize();
    charName.setLocalTranslation((BILL_WIDTH - cs.x) / 2f, (BILL_HEIGHT + cs.y) / 2f, 0);
    bill.setUserData("charLabel", charName);
    bill.attachChild(charName);

    middle.addChild(bill, BorderLayout.Position.Center);

    // Wrapper derecha para centrar verticalmente
    Container rightWrap = new Container(new BorderLayout(), "pontiland");
    rightWrap.setBackground(null);
    rightWrap.setPreferredSize(new Vector3f(40, BILL_HEIGHT, 0));
    Button right = createArrowButton(true, idx);
    rightWrap.addChild(right, BorderLayout.Position.Center);
    middle.addChild(rightWrap, BorderLayout.Position.East);
    panel.addChild(middle);

    TextField nameField = new TextField("", "pontiland");
    nameField.setPreferredWidth(220);
    nameFields.add(nameField);
    panel.addChild(nameField);

    playerPanels.add(panel);
    guiNode.attachChild(panel);
  }

  // Devuelve el primer índice de personaje no usado; si todos ocupados devuelve 0 (se mostrará
  // error aparte)
  private int getFirstFreeCharacter() {
    for (int i = 0; i < CHARACTER_NAMES.length; i++) {
      if (!characterIndex.contains(i)) return i;
    }
    return 0; // fallback si jugadores > personajes
  }

  private QuadBackgroundComponent loadBillSprite(int idx) {
    String path = BILL_SPRITES[Math.min(idx, BILL_SPRITES.length - 1)];
    TextureKey key = new TextureKey(path, true);
    key.setGenerateMips(false);
    Texture2D tex = (Texture2D) app.getAssetManager().loadTexture(key);
    tex.setMagFilter(Texture.MagFilter.Bilinear);
    tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
    tex.setWrap(Texture.WrapMode.EdgeClamp);
    return new QuadBackgroundComponent(tex);
  }

  private Button createArrowButton(boolean forward, int playerIdx) {
    String asset =
        forward
            ? "graphics/sprites/Icon_Forward_Black.png"
            : "graphics/sprites/Icon_Back_Black.png";
    IconComponent icon = new IconComponent(asset);
    icon.setIconSize(new Vector2f(40, 40));
    Button b = new Button("", "pontiland");
    b.setBackground(null);
    b.setIcon(icon);
    b.setPreferredSize(new Vector3f(40, 40, 0));
    b.addClickCommands(
        src -> {
          int current = characterIndex.get(playerIdx);
          int len = CHARACTER_NAMES.length;
          int dir = forward ? 1 : -1;
          int attempts = 0;
          int next = current;
          // Buscar siguiente personaje libre
          do {
            next = (next + dir + len) % len;
            attempts++;
          } while (attempts < len && isCharacterTaken(next, playerIdx));

          if (next == current && isCharacterTaken(next, playerIdx)) {
            // No hay personajes libres (caso extremo: jugadores > personajes)
            errorLabel.setText(ERR_SIN_SUFFICIENTES);
            return;
          }

          characterIndex.set(playerIdx, next);
          Label lbl = characterLabels.get(playerIdx);
          lbl.setText(CHARACTER_NAMES[next]);
          Vector3f size = lbl.getPreferredSize();
          lbl.setLocalTranslation((BILL_WIDTH - size.x) / 2f, (BILL_HEIGHT + size.y) / 2f, 0);
          if (!hasDuplicateCharacters()
              && (ERR_DUPLICADOS.equals(errorLabel.getText())
                  || ERR_SIN_SUFFICIENTES.equals(errorLabel.getText()))) {
            errorLabel.setText("");
          }
        });
    return b;
  }

  private boolean isCharacterTaken(int charIdx, int exceptPlayer) {
    for (int i = 0; i < characterIndex.size(); i++) {
      if (i == exceptPlayer) continue;
      if (characterIndex.get(i) == charIdx) return true;
    }
    return false;
  }

  private boolean hasDuplicateCharacters() {
    Set<Integer> seen = new HashSet<>();
    for (int idx : characterIndex) {
      if (!seen.add(idx)) return true;
    }
    return false;
  }

  private void layoutPlayerPanels() {
    int columns = Math.min(2, playerCount);
    int rows = (playerCount + 1) / 2;
    float panelW = 520; // ancho incluyendo flechas
    float panelH = 210; // más compacto tras rediseño
    float hGap = 70f;
    float vGap = 60f;
    float totalH = rows * panelH + (rows - 1) * vGap;
    float topY = (cam.getHeight() + totalH) / 2f - 100; // ligera elevación
    for (int i = 0; i < playerCount; i++) {
      int row = i / 2;
      int col = i % 2;
      int panelsInRow = (row == rows - 1 && playerCount % 2 == 1) ? 1 : columns;
      float effectiveRowWidth = panelsInRow * panelW + (panelsInRow - 1) * hGap;
      float startX = (cam.getWidth() - effectiveRowWidth) / 2f;
      if (panelsInRow == 1) col = 0;
      float x = startX + col * (panelW + hGap);
      float y = topY - row * (panelH + vGap);
      playerPanels.get(i).setLocalTranslation(x, y, 0);
    }
  }

  private void attemptStart() {
    errorLabel.setText("");
    if (playerCount > CHARACTER_NAMES.length) {
      errorLabel.setText(ERR_SIN_SUFFICIENTES);
      return;
    }
    if (hasDuplicateCharacters()) {
      errorLabel.setText(ERR_DUPLICADOS);
      return;
    }
    List<Jugador> jugadores = new ArrayList<>();
    List<Integer> personajeIds = new ArrayList<>();
    for (int i = 0; i < playerCount; i++) {
      String nombre = nameFields.get(i).getText();
      if (nombre == null || nombre.isBlank()) {
        errorLabel.setText("Faltan nombres");
        return;
      }
      int charIdx = characterIndex.get(i);
      personajeIds.add(charIdx + 1);
      try {
        jugadores.add(new Jugador(STARTING_MONEY, nombre.trim(), (byte) (i + 1)));
      } catch (IllegalArgumentException ex) {
        errorLabel.setText(ex.getMessage());
        return;
      }
    }
    actions.startMainGame(playerCount, jugadores, personajeIds);
  }

  @Override
  public void update(float tpf) {
    super.update(tpf);
    boolean ready = true;
    for (int i = 0; i < playerCount; i++) {
      String n = nameFields.get(i).getText();
      if (n == null || n.isBlank()) {
        ready = false;
        headerLabels.get(i).setText("Nickname J" + (i + 1));
      } else headerLabels.get(i).setText(n.trim());
    }
    boolean duplicates = hasDuplicateCharacters();
    if (playerCount > CHARACTER_NAMES.length) {
      errorLabel.setText(ERR_SIN_SUFFICIENTES);
    } else if (duplicates && (errorLabel.getText() == null || errorLabel.getText().isBlank())) {
      errorLabel.setText(ERR_DUPLICADOS);
    } else if (!duplicates && (ERR_DUPLICADOS.equals(errorLabel.getText()))) {
      errorLabel.setText("");
    }
    boolean enable = ready && !duplicates && playerCount <= CHARACTER_NAMES.length;
    if (ready != lastReadyState || startButton.isEnabled() != enable) {
      lastReadyState = ready;
      if (startButton != null) startButton.setEnabled(enable);
    }
  }

  @Override
  public void cleanup() {
    if (backdrop != null && backdrop.getParent() != null) backdrop.removeFromParent();
    if (backBar != null && backBar.getParent() != null) backBar.removeFromParent();
    if (startButton != null && startButton.getParent() != null) startButton.removeFromParent();
    if (errorLabel != null && errorLabel.getParent() != null) errorLabel.removeFromParent();
    for (Container c : playerPanels) {
      if (c.getParent() != null) c.removeFromParent();
    }
    nameFields.clear();
    characterIndex.clear();
    characterLabels.clear();
    playerPanels.clear();
    headerLabels.clear();
    super.cleanup();
  }
}
