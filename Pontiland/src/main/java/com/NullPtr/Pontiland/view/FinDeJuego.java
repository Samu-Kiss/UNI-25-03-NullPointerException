package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.controllers.IMenuActions;
import com.NullPtr.Pontiland.repository.PartidaRepository;
import com.jme3.app.Application;
import com.jme3.app.LegacyApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.style.Styles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Pantalla de fin de juego que muestra los resultados finales de la partida.
 *
 * <p>Muestra una tabla con los jugadores ordenados por patrimonio total (dinero + valor de
 * propiedades), con el ganador destacado en color dorado. Incluye un botón para volver al menú
 * principal.
 */
public class FinDeJuego extends AbstractAppState {

  /** Nombres de los iconos disponibles (1-indexed: iconoId 1 = "Kiwi", etc.). */
  private static final String[] ICON_NAMES = {
    "Kiwi", "Balon", "Maleta", "Pescadito", "Ignacito", "Nave"
  };

  /** Color dorado para destacar al ganador. */
  private static final ColorRGBA GOLD_COLOR = new ColorRGBA(1f, 0.84f, 0f, 1f);

  /** Color blanco para los demás jugadores. */
  private static final ColorRGBA WHITE_COLOR = new ColorRGBA(0.95f, 0.96f, 1f, 1f);

  /** Color de subtítulo/texto secundario. */
  private static final ColorRGBA SUBTITLE_COLOR = new ColorRGBA(0.85f, 0.86f, 0.92f, 1f);

  private static Logger logger = LogManager.getLogger(FinDeJuego.class);

  // Entradas externas
  private final Map<String, Map.Entry<Integer, Long>> resultados;
  private final IMenuActions actions;
  private final PartidaRepository partidaRepository;

  // Referencias JME/Lemur
  private LegacyApplication app;
  private Node guiNode;
  private Camera camera;

  // Contenedores UI
  private Container backdrop;
  private Container leftPane;
  private Container rightPane;
  private Container backBar;

  /**
   * Crea una nueva pantalla de fin de juego.
   *
   * @param resultados mapa con los resultados: nombreJugador -> (iconoId, patrimonioTotal)
   * @param actions referencia a las acciones del menú para navegación
   * @param partidaRepository repositorio para actualizar el estado de la partida
   */
  public FinDeJuego(
      Map<String, Map.Entry<Integer, Long>> resultados,
      IMenuActions actions,
      PartidaRepository partidaRepository) {
    this.resultados = resultados;
    this.actions = actions;
    this.partidaRepository = partidaRepository;
  }

  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    Launcher launcher = (Launcher) app;
    this.app = launcher;
    this.guiNode = launcher.getGuiNode();
    this.camera = launcher.getCamera();

    launcher.getInputManager().setCursorVisible(true);
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
    leftPane.setBackground(null);

    Label title = new Label("RESULTADOS", "pontiland");
    title.setFontSize(54);
    title.setInsets(new Insets3f(20, 30, 10, 30));
    title.setColor(WHITE_COLOR);
    leftPane.addChild(title);

    Label subtitle = new Label("Fin de la partida", "pontiland");
    subtitle.setFontSize(22);
    subtitle.setInsets(new Insets3f(0, 30, 0, 30));
    subtitle.setColor(SUBTITLE_COLOR);
    leftPane.addChild(subtitle);

    Vector3f leftPref = leftPane.getPreferredSize();
    leftPane.setPreferredSize(leftPref);
    float leftX = (halfW - leftPref.x) / 2f;
    float leftY = (camera.getHeight() + leftPref.y) / 2f;
    leftPane.setLocalTranslation(leftX, leftY, 0);

    // Panel derecho: tabla de resultados
    rightPane = new Container("pontiland");
    rightPane.setBackground(null);

    Label tableTitle = new Label("Clasificación Final", "pontiland");
    tableTitle.setFontSize(26);
    tableTitle.setInsets(new Insets3f(0, 0, 20, 0));
    tableTitle.setColor(WHITE_COLOR);
    tableTitle.setTextHAlignment(HAlignment.Center);
    rightPane.addChild(tableTitle);

    // Contenedor de la tabla
    Container tableContainer = new Container("pontiland");
    tableContainer.setBackground(
        new QuadBackgroundComponent(new ColorRGBA(0.12f, 0.13f, 0.16f, 0.9f)));
    tableContainer.setInsets(new Insets3f(15, 20, 15, 20));

    // Ordenar resultados por patrimonio descendente
    List<Map.Entry<String, Map.Entry<Integer, Long>>> sortedResults =
        new ArrayList<>(resultados.entrySet());
    sortedResults.sort(
        Comparator.comparingLong(
                (Map.Entry<String, Map.Entry<Integer, Long>> e) -> e.getValue().getValue())
            .reversed());

    // Crear filas para cada jugador
    int rank = 1;
    for (Map.Entry<String, Map.Entry<Integer, Long>> entry : sortedResults) {
      String nombre = entry.getKey();
      int iconoId = entry.getValue().getKey();
      long patrimonio = entry.getValue().getValue();

      Container row = createPlayerRow(rank, nombre, iconoId, patrimonio, rank == 1);
      tableContainer.addChild(row);

      // Espaciador entre filas (excepto la última)
      if (rank < sortedResults.size()) {
        Container spacer = new Container();
        spacer.setBackground(null);
        spacer.setPreferredSize(new Vector3f(0, 8, 0));
        tableContainer.addChild(spacer);
      }
      rank++;
    }

    rightPane.addChild(tableContainer);

    // Fijar ancho preferido
    float prefWidth = Math.max(450f, halfW * 0.85f);
    Vector3f tablePref = tableContainer.getPreferredSize().clone();
    tablePref.x = prefWidth;
    tableContainer.setPreferredSize(tablePref);

    // Centrar el panel derecho
    Vector3f rightPref = rightPane.getPreferredSize();
    rightPane.setPreferredSize(rightPref);
    float rightX = halfW + (halfW - rightPref.x) / 2f;
    float rightY = (camera.getHeight() + rightPref.y) / 2f;
    rightPane.setLocalTranslation(rightX, rightY, 0);

    guiNode.attachChild(leftPane);
    guiNode.attachChild(rightPane);

    // Barra inferior con botón "Volver al menú"
    backBar = new Container("pontiland");
    backBar.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.16f, 0.18f, 0.22f, 0.85f)));
    backBar.setInsets(new Insets3f(10, 20, 10, 20));

    Button backButton = new Button("Volver al menú", "pontiland");
    backButton.setFontSize(18);
    backButton.setColor(WHITE_COLOR);
    backButton.setTextHAlignment(HAlignment.Center);
    backButton.addClickCommands(
        source -> {
          try {
            partidaRepository.updatePartidaActiveStatus();
            logger.info("Partida marcada como inactiva");
          } catch (SQLException e) {
            logger.error("Error al marcar la partida como inactiva", e);
          }
          actions.goToMainMenu();
        });
    backBar.addChild(backButton);

    Vector3f barPref = backBar.getPreferredSize();
    float barX = (camera.getWidth() - barPref.x) / 2f;
    float barY = barPref.y + 30;
    backBar.setLocalTranslation(barX, barY, 0);
    guiNode.attachChild(backBar);
  }

  /**
   * Crea una fila de la tabla con la información del jugador.
   *
   * @param rank posición en el ranking (1 = primero)
   * @param nombre nombre del jugador
   * @param iconoId ID del icono (1-indexed)
   * @param patrimonio patrimonio total del jugador
   * @param isWinner true si es el ganador (primer lugar)
   * @return contenedor con la fila
   */
  private Container createPlayerRow(
      int rank, String nombre, int iconoId, long patrimonio, boolean isWinner) {
    Container row = new Container(new SpringGridLayout(), "pontiland");
    row.setBackground(null);

    ColorRGBA textColor = isWinner ? GOLD_COLOR : WHITE_COLOR;

    // Posición/Rank
    Label rankLabel = new Label("#" + rank, "pontiland");
    rankLabel.setFontSize(20);
    rankLabel.setColor(textColor);
    rankLabel.setInsets(new Insets3f(5, 10, 5, 15));
    row.addChild(rankLabel);

    // Icono del jugador
    Container iconContainer = new Container("pontiland");
    iconContainer.setBackground(null);
    if (iconoId >= 1 && iconoId <= ICON_NAMES.length) {
      String iconName = ICON_NAMES[iconoId - 1];
      String path = "graphics/sprites/Fichas/" + iconName + ".png";
      try {
        TextureKey key = new TextureKey(path, true);
        key.setGenerateMips(false);
        Texture2D tex = (Texture2D) this.app.getAssetManager().loadTexture(key);
        tex.setWrap(Texture.WrapMode.EdgeClamp);
        tex.setMagFilter(Texture.MagFilter.Bilinear);
        tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        iconContainer.setBackground(new QuadBackgroundComponent(tex));
      } catch (Exception e) {
        logger.warn("No se pudo cargar el icono: {}", path);
      }
    }
    iconContainer.setPreferredSize(new Vector3f(40, 40, 0));
    iconContainer.setInsets(new Insets3f(5, 5, 5, 15));
    row.addChild(iconContainer);

    // Nombre del jugador
    Label nameLabel = new Label(nombre, "pontiland");
    nameLabel.setFontSize(20);
    nameLabel.setColor(textColor);
    nameLabel.setInsets(new Insets3f(5, 10, 5, 20));
    nameLabel.setPreferredSize(new Vector3f(150, 30, 0));
    row.addChild(nameLabel);

    // Patrimonio
    Label moneyLabel = new Label("$" + patrimonio, "pontiland");
    moneyLabel.setFontSize(20);
    moneyLabel.setColor(textColor);
    moneyLabel.setTextHAlignment(HAlignment.Right);
    moneyLabel.setInsets(new Insets3f(5, 10, 5, 10));
    row.addChild(moneyLabel);

    // Indicador de ganador
    if (isWinner) {
      Label winnerLabel = new Label("👑 GANADOR", "pontiland");
      winnerLabel.setFontSize(16);
      winnerLabel.setColor(GOLD_COLOR);
      winnerLabel.setInsets(new Insets3f(5, 15, 5, 5));
      row.addChild(winnerLabel);
    }

    return row;
  }

  @Override
  public void cleanup() {
    super.cleanup();
    if (backdrop != null) {
      backdrop.removeFromParent();
      backdrop = null;
    }
    if (leftPane != null) {
      leftPane.removeFromParent();
      leftPane = null;
    }
    if (rightPane != null) {
      rightPane.removeFromParent();
      rightPane = null;
    }
    if (backBar != null) {
      backBar.removeFromParent();
      backBar = null;
    }
  }
}
