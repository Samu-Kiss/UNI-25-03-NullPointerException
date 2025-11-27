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
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Pantalla de selección de personajes y nombres para los jugadores. Permite elegir el personaje y
 * el nickname de cada jugador antes de iniciar la partida. Valida que no haya personajes ni nombres
 * duplicados y que todos los campos estén completos.
 *
 * <p>Flujo: - Se recibe el número de jugadores desde MenuJugadores. - Se muestran paneles para cada
 * jugador con su billete y campos de nombre. - Se puede navegar entre personajes disponibles usando
 * flechas. - Al presionar PLAY, se valida la información y se inicia el juego principal.
 *
 * <p>NOTA: Los modelos 3D de personajes aún no están disponibles, se usan sprites de billetes como
 * placeholder.
 */
public class MenuSeleccion extends AbstractAppState {

  /** Nombres de los personajes disponibles. */
  private static final String[] CHARACTER_NAMES = {
    "Kiwi", "Balon", "Maleta", "Pescadito", "Ignacito", "Nave", /*"Carnet",*/
  };

  /** Rutas de los sprites de billetes. */
  private static final String[] BILL_SPRITES = {
    "graphics/sprites/Common/Bill_Rosado.png",
    "graphics/sprites/Common/Bill_Morado.png",
    "graphics/sprites/Common/Bill_Azul.png",
    "graphics/sprites/Common/Bill_Verde.png"
  };

  /** Mensaje de error para personajes repetidos. */
  private static final String ERR_DUPLICADOS = "Personajes repetidos";

  /** Mensaje de error para falta de personajes suficientes. */
  private static final String ERR_SIN_SUFFICIENTES = "No hay suficientes personajes";

  /** Mensaje de error para nombres duplicados. */
  private static final String ERR_NOMBRES_DUP = "Nombres duplicados";

  /** Estilo de UI usado en los componentes Lemur. */
  private static final String UI_STYLE = "pontiland";

  /** Referencia a las acciones del menú. */
  private final IMenuActions actions;

  /** Número de jugadores. */
  private final int playerCount;

  /** Referencia a la aplicación principal. */
  private LegacyApplication app;

  /** Nodo de la interfaz gráfica. */
  private Node guiNode;

  /** Cámara principal. */
  private Camera cam;

  private Container backdrop;
  private Container backBar;
  private Button startButton;
  private Label errorLabel; // reemplaza topBar por backBar
  // Ya no usamos mainPane/playersRow para grid automático: posicionaremos manualmente
  private final List<Integer> characterIndex = new ArrayList<>();
  private final List<Label> characterLabels = new ArrayList<>();
  private final List<Picture> characterIcons = new ArrayList<>();
  private final List<Container> playerPanels = new ArrayList<>();
  private final List<TextField> headerFields = new ArrayList<>();
  private final List<Boolean> placeholderActive = new ArrayList<>();
  private boolean lastReadyState = false;

  // Altura objetivo de los billetes (el ancho se ajustará para mantener aspecto original)
  private static final float TARGET_BILL_HEIGHT = 140f;
  // Márgenes laterales reservados para flechas (40 + 40)
  private static final float ARROWS_WIDTH_SUM = 80f;
  private static final float ARROW_ICON_MAX_DIM = 40f; // dimensión máxima para icono de flecha
  private static final float ARROW_GAP = 6f; // espacio visual entre flecha y billete a cada lado
  private static final float ARROW_ADJUST_LEFT =
      4f; // compensación hacia la derecha para la flecha izquierda
  private static final float ARROW_ADJUST_RIGHT =
      4f; // compensación hacia la izquierda para la flecha derecha
  // Cache de desplazamientos calculados
  private final Map<String, Float> arrowShiftCache = new HashMap<>();

  // Variables calculadas dinámicamente para evitar estiramiento horizontal
  private float billWidth = 400f; // valor por defecto antes de cálculo
  private float billHeight = TARGET_BILL_HEIGHT;
  private float panelTotalWidth = billWidth + ARROWS_WIDTH_SUM; // se recalcula

  private static final float PANELS_Y_OFFSET =
      50f; // Ajusta este valor si quieres más o menos desplazamiento
  private static final int MAX_NICK_LENGTH = 12; // Límite de caracteres para nickname
  private static final ColorRGBA PLACEHOLDER_COLOR = new ColorRGBA(0.7f, 0.7f, 0.7f, 1f);
  private static final ColorRGBA INPUT_COLOR = ColorRGBA.White;
  private static final ColorRGBA INPUT_BG_INNER = new ColorRGBA(0.24f, 0.24f, 0.28f, 0.98f);
  // Icon size relative to bill height (1.0 = same height as bill)
  private static final float ICON_HEIGHT_FACTOR = 1.0f;
  // Cache for loaded character sprite backgrounds (keyed by asset path)
  private final Map<String, Texture2D> characterSpriteCache = new HashMap<>();
  private final Map<String, Vector2f> characterSpriteSizeCache = new HashMap<>();

  // Logging
  private static Logger logger = LogManager.getLogger(MenuSeleccion.class);

  public MenuSeleccion(IMenuActions actions, int playerCount) {
    this.actions = actions;
    this.playerCount = playerCount;
  }

  @Override
  public void initialize(AppStateManager sm, Application application) {
    super.initialize(sm, application);
    Launcher launcher = (Launcher) application;
    this.app = launcher;
    this.guiNode = launcher.getGuiNode();
    this.cam = launcher.getCamera();
    if (GuiGlobals.getInstance() == null) {
      GuiGlobals.initialize(application);
    }
    buildUI();
  }

  private void buildUI() {
    cleanup();
    // Calcular dimensiones reales del billete para preservar aspecto
    computeBillDimensions();

    backdrop = new Container(UI_STYLE);
    backdrop.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.08f, 0.09f, 0.11f, 1f)));
    backdrop.setLocalTranslation(0, cam.getHeight(), -1);
    backdrop.setPreferredSize(new Vector3f(cam.getWidth(), cam.getHeight(), 0));
    guiNode.attachChild(backdrop);

    backBar = new Container(UI_STYLE);
    backBar.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.16f, 0.18f, 0.22f, 0.85f)));
    backBar.setInsets(new Insets3f(6, 10, 6, 10));
    Container row = new Container(new BorderLayout(), UI_STYLE);

    TextureKey key = new TextureKey("graphics/sprites/Common/Icons/Icon_Back_White.png", true);
    key.setGenerateMips(false);
    Texture2D iconTex = (Texture2D) this.app.getAssetManager().loadTexture(key);
    iconTex.setWrap(Texture.WrapMode.EdgeClamp);
    iconTex.setMagFilter(Texture.MagFilter.Bilinear);
    iconTex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

    Button iconBtn = new Button("", UI_STYLE);
    iconBtn.setBackground(new QuadBackgroundComponent(iconTex));
    iconBtn.setPreferredSize(new Vector3f(24, 24, 0));
    iconBtn.setInsets(new Insets3f(0, 0, 0, 6));
    iconBtn.addClickCommands(ignored -> actions.startPlayerSelection());

    Button textBtn = new Button("Volver", UI_STYLE);
    textBtn.setFontSize(16);
    textBtn.setInsets(new Insets3f(2, 6, 2, 6));
    textBtn.addClickCommands(ignored -> actions.startPlayerSelection());

    row.addChild(iconBtn, BorderLayout.Position.West);
    row.addChild(textBtn, BorderLayout.Position.Center);
    backBar.addChild(row);
    backBar.setLocalTranslation(10, cam.getHeight() - 10f, 1);
    guiNode.attachChild(backBar);

    // Crear paneles de jugadores y posicionar
    for (int i = 0; i < playerCount; i++) createPlayerPanel(i);
    layoutPlayerPanels();

    // Botón PLAY centrado con renderer de botones comunes (sprite + texto + hover)
    com.NullPtr.Pontiland.view.Button renderer =
        new com.NullPtr.Pontiland.view.Button(this.app.getAssetManager())
            .setDefaultFontSize(30f) // texto grande por defecto
            .setHoverScale(1.08f); // ligera animación al pasar el cursor

    startButton =
        renderer.render(
            com.NullPtr.Pontiland.view.Button.Type.BASE, // tipo de sprite
            "PLAY",
            0.5f, // factor de escala del sprite
            com.NullPtr.Pontiland.view.Button.Variant.MAIN);
    startButton.setEnabled(false);
    startButton.addClickCommands(
        ignored -> {
          try {
            attemptStart();
          } catch (SQLException e) {
            logger.fatal("Error al iniciar la partida: ", e);
          }
        });
    Vector3f pref = startButton.getPreferredSize();
    startButton.setLocalTranslation((cam.getWidth() - pref.x) / 2f, 90, 0);
    guiNode.attachChild(startButton);

    errorLabel = new Label("", UI_STYLE);
    errorLabel.setColor(new ColorRGBA(1f, 0.4f, 0.4f, 1f));
    Vector3f ep = errorLabel.getPreferredSize();
    errorLabel.setLocalTranslation((cam.getWidth() - ep.x) / 2f, 60, 0);
    guiNode.attachChild(errorLabel);

    // Validación inmediata si jugadores > personajes
    if (playerCount > CHARACTER_NAMES.length) {
      errorLabel.setText(ERR_SIN_SUFFICIENTES);
    }
  }

  private void computeBillDimensions() {
    try {
      String path = BILL_SPRITES[0];
      TextureKey key = new TextureKey(path, true);
      key.setGenerateMips(false);
      Texture2D tex = (Texture2D) app.getAssetManager().loadTexture(key);
      int w = tex.getImage().getWidth();
      int h = tex.getImage().getHeight();
      if (h > 0) {
        billHeight = TARGET_BILL_HEIGHT;
        billWidth = (float) w / (float) h * billHeight;
        // Evitar valores extremos accidentalmente grandes o pequeños
        if (billWidth < 200f) billWidth = 200f; // ancho mínimo razonable (ligeramente menor)
        if (billWidth > 420f) billWidth = 420f; // ancho máximo para no deformar layout
        panelTotalWidth = billWidth + ARROWS_WIDTH_SUM + (ARROW_GAP * 2f);
      }
    } catch (Exception e) {
      // Si falla, usar valores ajustados más estilizados que antes
      billHeight = TARGET_BILL_HEIGHT;
      billWidth = 360f;
      panelTotalWidth = billWidth + ARROWS_WIDTH_SUM + (ARROW_GAP * 2f);
    }
  }

  private void createPlayerPanel(int idx) {
    Container panel = new Container(UI_STYLE);
    panel.setBackground(null);

    float headerHeight = 34f;
    Container headerWrapper = new Container(UI_STYLE);
    headerWrapper.setPreferredSize(new Vector3f(panelTotalWidth, headerHeight + 14f, 0));
    headerWrapper.setInsets(new Insets3f(3, 3, 8, 3));

    TextField headerField = new TextField("", UI_STYLE);
    headerField.setFontSize(22);
    headerField.setPreferredSize(new Vector3f(panelTotalWidth - 20f, headerHeight, 0));
    headerField.setTextHAlignment(HAlignment.Center);
    headerField.setBackground(new QuadBackgroundComponent(INPUT_BG_INNER));
    headerField.setInsets(new Insets3f(6, 12, 6, 12));
    headerField.setText(placeholderFor(idx));
    headerField.setColor(PLACEHOLDER_COLOR);
    headerWrapper.addChild(headerField);
    headerFields.add(headerField);
    placeholderActive.add(true);
    panel.addChild(headerWrapper);

    // Fila central con flechas y billete usando BorderLayout para alinear horizontalmente
    Container middle = new Container(new BorderLayout(), UI_STYLE);
    middle.setBackground(null);

    // Wrapper izquierda para centrar verticalmente
    Container leftWrap = new Container(new BorderLayout(), UI_STYLE);
    leftWrap.setBackground(null);
    leftWrap.setPreferredSize(new Vector3f(40 + ARROW_GAP, billHeight, 0));
    Button left = createArrowButton(false, idx);
    leftWrap.addChild(
        left, BorderLayout.Position.West); // antes Center, ahora West para gap interno consistente
    middle.addChild(leftWrap, BorderLayout.Position.West);

    // Bill sprite central
    Container bill = new Container(UI_STYLE);
    bill.setBackground(loadBillSprite(idx));
    bill.setPreferredSize(new Vector3f(billWidth, billHeight, 0));
    int initialCharIdx = getFirstFreeCharacter();
    characterIndex.add(initialCharIdx);
    Label charName = new Label(CHARACTER_NAMES[initialCharIdx], UI_STYLE);
    charName.setFontSize(20);
    characterLabels.add(charName);

    Picture charIcon = new Picture("CharacterIcon-" + idx);
    configureCharacterIcon(charIcon, initialCharIdx);
    characterIcons.add(charIcon);

    // Adjuntar icono y label al billete
    bill.attachChild(charIcon);
    Vector3f cs = charName.getPreferredSize();
    charName.setLocalTranslation((billWidth - cs.x) / 2f, (billHeight + cs.y) / 2f, 2f);
    bill.setUserData("charLabel", charName);
    bill.attachChild(charName);
    middle.addChild(bill, BorderLayout.Position.Center);

    // Wrapper derecha para centrar verticalmente
    Container rightWrap = new Container(new BorderLayout(), UI_STYLE);
    rightWrap.setBackground(null);
    rightWrap.setPreferredSize(new Vector3f(40 + ARROW_GAP, billHeight, 0));
    Button right = createArrowButton(true, idx);
    rightWrap.addChild(
        right, BorderLayout.Position.East); // antes Center, ahora East para gap interno consistente
    middle.addChild(rightWrap, BorderLayout.Position.East);
    panel.addChild(middle);

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

  private Texture2D loadCharacterTexture(int charIdx) {
    String path = "graphics/sprites/Fichas/" + CHARACTER_NAMES[charIdx] + ".png";
    Texture2D cached = characterSpriteCache.get(path);
    if (cached != null) {
      return cached;
    }
    try {
      TextureKey key = new TextureKey(path, true);
      key.setGenerateMips(false);
      Texture2D tex = (Texture2D) app.getAssetManager().loadTexture(key);
      tex.setMagFilter(Texture.MagFilter.Bilinear);
      tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
      tex.setWrap(Texture.WrapMode.EdgeClamp);
      characterSpriteCache.put(path, tex);
      return tex;
    } catch (Exception e) {
      logger.warn("No se pudo cargar sprite personaje: {}", path, e);
      return null;
    }
  }

  private Vector2f computeArrowIconSize(String asset) {
    try {
      TextureKey k = new TextureKey(asset, true);
      k.setGenerateMips(false);
      Texture2D tex = (Texture2D) app.getAssetManager().loadTexture(k);
      int w = tex.getImage().getWidth();
      int h = tex.getImage().getHeight();
      if (w <= 0 || h <= 0) return new Vector2f(ARROW_ICON_MAX_DIM, ARROW_ICON_MAX_DIM);
      if (w >= h) {
        float scaledH = ARROW_ICON_MAX_DIM * ((float) h / (float) w);
        return new Vector2f(ARROW_ICON_MAX_DIM, scaledH);
      } else {
        float scaledW = ARROW_ICON_MAX_DIM * ((float) w / (float) h);
        return new Vector2f(scaledW, ARROW_ICON_MAX_DIM);
      }
    } catch (Exception e) {
      return new Vector2f(ARROW_ICON_MAX_DIM, ARROW_ICON_MAX_DIM);
    }
  }

  private float computeArrowShift(String asset, float displayedWidth) {
    Float cached = arrowShiftCache.get(asset);
    if (cached != null) return cached;
    try {
      TextureKey k = new TextureKey(asset, true);
      k.setGenerateMips(false);
      Texture2D tex = (Texture2D) app.getAssetManager().loadTexture(k);
      Image img = tex.getImage();
      if (img == null) return 0f;
      int w = img.getWidth();
      int h = img.getHeight();
      if (w <= 0 || h <= 0) return 0f;
      // Intentar acceder al ByteBuffer (índice 0). Si no existe, salir.
      ByteBuffer buf = img.getData(0);
      if (buf == null) return 0f;
      int left = w;
      int right = -1;
      int stride = w * 4;
      for (int y = 0; y < h; y++) {
        int row = y * stride;
        for (int x = 0; x < w; x++) {
          int base = row + x * 4;
          if (base + 3 >= buf.limit()) break; // seguridad
          int a = buf.get(base + 3) & 0xFF; // alpha asumida en byte 3
          if (a > 10) {
            if (x < left) left = x;
            if (x > right) right = x;
          }
        }
      }
      if (right < left) return 0f; // no se encontró contenido visible
      int padLeft = left;
      int padRight = w - 1 - right;
      int diff = padRight - padLeft;
      float scale = displayedWidth / w;
      float shift = diff * 0.5f * scale;
      arrowShiftCache.put(asset, shift);
      return shift;
    } catch (Exception e) {
      return 0f;
    }
  }

  private Button createArrowButton(boolean forward, int playerIdx) {
    String asset =
        forward
            ? "graphics/sprites/Common/Icons/Icon_Forward_Black.png"
            : "graphics/sprites/Common/Icons/Icon_Back_Black.png";
    // Calcular tamaño proporcional del icono
    Vector2f iconSize = computeArrowIconSize(asset);
    IconComponent icon = new IconComponent(asset);
    icon.setIconSize(iconSize);
    Button b = new Button("", UI_STYLE);
    b.setBackground(null);
    b.setIcon(icon);
    // Mantener área de click cuadrada de 40x40 para consistencia aunque el icono no ocupe todo
    b.setPreferredSize(new Vector3f(ARROW_ICON_MAX_DIM, ARROW_ICON_MAX_DIM, 0));

    // Calcular desplazamiento dinámico para centrar bounding box real
    float dynamicShift = computeArrowShift(asset, iconSize.x);

    b.addClickCommands(
        ignored -> {
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
          lbl.setLocalTranslation((billWidth - size.x) / 2f, (billHeight + size.y) / 2f, 2f);
          Picture iconRef = characterIcons.get(playerIdx);
          configureCharacterIcon(iconRef, next);
          if (!hasDuplicateCharacters()
              && (ERR_DUPLICADOS.equals(errorLabel.getText())
                  || ERR_SIN_SUFFICIENTES.equals(errorLabel.getText()))) {
            errorLabel.setText("");
          }
        });

    // Ajuste visual: combinar compensación estática + dinámica
    float baseStatic = forward ? -ARROW_ADJUST_RIGHT : ARROW_ADJUST_LEFT;
    // Para la flecha derecha, si hay más padding a la izquierda la diferencia (dynamicShift)
    // probablemente será negativa.
    // Queremos aplicar el shift tal cual (ya es hacia la derecha si positivo) pero invertido para
    // la flecha opuesta.
    float appliedDynamic =
        forward ? dynamicShift * -1f : dynamicShift; // invertir para mantener simetría perceptual
    b.setLocalTranslation(baseStatic + appliedDynamic, 0, 0);

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
    float panelW = panelTotalWidth; // usar constante para consistencia
    float panelH = 210; // más compacto tras rediseño
    float hGap = 70f;
    float vGap = 60f;
    float totalH = rows * panelH + (rows - 1) * vGap;
    // Ajuste: antes se restaban 100px. Ahora usamos un offset parametrizable (PANELS_Y_OFFSET).
    float topY = (cam.getHeight() + totalH) / 2f + PANELS_Y_OFFSET;
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

  private void attemptStart() throws java.sql.SQLException {
    errorLabel.setText("");
    if (playerCount > CHARACTER_NAMES.length) {
      errorLabel.setText(ERR_SIN_SUFFICIENTES);
      return;
    }
    if (hasDuplicateCharacters()) {
      errorLabel.setText(ERR_DUPLICADOS);
      return;
    }
    if (hasDuplicateNames()) {
      errorLabel.setText(ERR_NOMBRES_DUP);
      return;
    }
    ArrayList<Jugador> jugadores = new ArrayList<>();
    ArrayList<Integer> personajeIds = new ArrayList<>();
    for (int i = 0; i < playerCount; i++) {
      if (Boolean.TRUE.equals(placeholderActive.get(i))) {
        errorLabel.setText("Faltan nombres");
        return;
      }
      String nombre = headerFields.get(i).getText();
      if (nombre == null || nombre.isBlank()) {
        errorLabel.setText("Faltan nombres");
        return;
      }
      nombre = nombre.trim();
      int charIdx = characterIndex.get(i);
      personajeIds.add(charIdx + 1);
      try {
        jugadores.add(new Jugador(nombre, (byte) (i + 1)));
      } catch (IllegalArgumentException ex) {
        errorLabel.setText(ex.getMessage());
        return;
      }
    }
    actions.startMainGame(playerCount, jugadores, personajeIds);
  }

  private boolean hasDuplicateNames() {
    Set<String> seen = new HashSet<>();
    for (TextField headerField : headerFields) {
      String t = headerField.getText();
      if (t == null || t.trim().isEmpty()) continue;
      t = t.trim();
      String key = t.toLowerCase();
      if (!seen.add(key)) return true;
    }
    return false;
  }

  @Override
  public void update(float tpf) {
    super.update(tpf);
    if (headerFields.size() < playerCount
        || placeholderActive.size() < playerCount
        || characterIndex.size() < playerCount) {
      return;
    }
    boolean ready = true;
    // Gestión de placeholders, recorte de longitud y estado de readiness
    for (int i = 0; i < playerCount; i++) {
      TextField f = headerFields.get(i);
      boolean focused = isFieldFocused(f);
      if (Boolean.TRUE.equals(placeholderActive.get(i))) {
        // Si el usuario hace focus, limpiar placeholder
        if (focused) {
          f.setText("");
          f.setColor(INPUT_COLOR);
          placeholderActive.set(i, false);
        } else {
          ready = false;
        }
      } else {
        String txt = f.getText();
        if (txt == null || txt.isBlank()) {
          if (!focused) { // restaurar placeholder
            f.setText(placeholderFor(i));
            f.setColor(PLACEHOLDER_COLOR);
            placeholderActive.set(i, true);
            ready = false;
          } else {
            ready = false;
          }
        } else {
          // Limitar longitud
          if (txt.length() > MAX_NICK_LENGTH) {
            f.setText(txt.substring(0, MAX_NICK_LENGTH));
          }
        }
      }
      if (Boolean.FALSE.equals(placeholderActive.get(i))) {
        String v = f.getText();
        if (v == null || v.trim().isEmpty()) ready = false;
      }
    }

    boolean duplicateCharacters = hasDuplicateCharacters();
    boolean duplicateNames = hasDuplicateNames();

    // Selección de mensaje de error según prioridad
    if (playerCount > CHARACTER_NAMES.length) {
      errorLabel.setText(ERR_SIN_SUFFICIENTES);
    } else if (duplicateCharacters) {
      errorLabel.setText(ERR_DUPLICADOS);
    } else if (duplicateNames) {
      errorLabel.setText(ERR_NOMBRES_DUP);
    } else if (errorLabel.getText() != null
        && (errorLabel.getText().equals(ERR_DUPLICADOS)
            || errorLabel.getText().equals(ERR_NOMBRES_DUP)
            || errorLabel.getText().equals(ERR_SIN_SUFFICIENTES))) {
      errorLabel.setText("");
    }

    boolean enable =
        ready && !duplicateCharacters && !duplicateNames && playerCount <= CHARACTER_NAMES.length;
    if (ready != lastReadyState || startButton.isEnabled() != enable) {
      lastReadyState = ready;
      if (startButton != null) startButton.setEnabled(enable);
    }
  }

  private String placeholderFor(int i) {
    return "Jugador " + (i + 1);
  }

  private boolean isFieldFocused(TextField f) {
    return GuiGlobals.getInstance().getFocusManagerState().getFocus() == f;
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
    headerFields.clear();
    placeholderActive.clear();
    characterIndex.clear();
    characterLabels.clear();
    playerPanels.clear();
    characterIcons.clear();
    characterSpriteCache.clear();
    characterSpriteSizeCache.clear();
    super.cleanup();
  }

  private void configureCharacterIcon(Picture icon, int charIdx) {
    if (icon == null) {
      return;
    }
    Texture2D tex = loadCharacterTexture(charIdx);
    if (tex == null) {
      icon.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
      return;
    }
    icon.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
    icon.setTexture(app.getAssetManager(), tex, true);
    Vector2f iconSize = computeCharacterIconSize(charIdx);
    icon.setWidth(iconSize.x);
    icon.setHeight(iconSize.y);
    Vector3f translation =
        new Vector3f((billWidth - iconSize.x) / 2f, (billHeight - iconSize.y) / 2f - 140f, 1.5f);
    icon.setLocalTranslation(translation);
    if (logger.isDebugEnabled()) {
      logger.debug(
          "Icon {} -> size {}x{} pos {}",
          CHARACTER_NAMES[charIdx],
          iconSize.x,
          iconSize.y,
          translation);
    }
  }

  private Vector2f computeCharacterIconSize(int charIdx) {
    String path = "graphics/sprites/Fichas/" + CHARACTER_NAMES[charIdx] + ".png";
    Vector2f cached = characterSpriteSizeCache.get(path);
    if (cached != null) {
      return cached.clone();
    }
    try {
      TextureKey key = new TextureKey(path, true);
      key.setGenerateMips(false);
      Texture2D tex = (Texture2D) app.getAssetManager().loadTexture(key);
      int iw = tex.getImage().getWidth();
      int ih = tex.getImage().getHeight();
      float iconH = billHeight * ICON_HEIGHT_FACTOR;
      float iconW = (iw > 0 && ih > 0) ? iconH * ((float) iw / (float) ih) : billWidth;
      if (iconW > billWidth) {
        iconW = billWidth;
        iconH = (ih > 0 && iw > 0) ? iconW * ((float) ih / (float) iw) : billHeight;
      }
      Vector2f size = new Vector2f(iconW, iconH);
      characterSpriteSizeCache.put(path, size.clone());
      return size;
    } catch (Exception e) {
      logger.warn("No se pudo calcular tamaño de sprite: {}", path, e);
      Vector2f fallback = new Vector2f(billWidth, billHeight);
      characterSpriteSizeCache.put(path, fallback.clone());
      return fallback;
    }
  }
}
