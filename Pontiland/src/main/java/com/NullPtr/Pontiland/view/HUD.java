package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.view.HUDComponents.*;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HUD extends AbstractAppState {

  private Launcher app;
  private Node guiNode;
  private Camera camera;
  // TODO: Comprobar que realmente se necesita y revisar su eso en la linea 89
  // private InputManager input;

  private Container leftPane;
  private Container rightPane;
  private Container bottomPane;
  private Container overlayPane;

  private Container playersBox; // contendrá varias PlayerCard apiladas
  private Container playerActionsBox; // contendrá botones de acciones del jugador
  private com.simsilica.lemur.Button rollDiceBtn;
  private com.simsilica.lemur.Button payBailBtn;

  private List<PlayerCard> playerCards = new ArrayList<>();
  private PropertyCard propertyCard;
  private PropertyToken propertyToken;
  private Auction auction;
  private EventCard eventCard;

  private IHUDcontroller hudController;

  // Jugadores pendientes si se llaman antes de construir playersBox
  private List<Jugador> pendingPlayers;

  // Animación indicador de turno: offset X del jugador activo
  private static final float ACTIVE_PLAYER_OFFSET_X = 30f;
  private static final float ANIMATION_SPEED = 8f;
  private int activePlayerIndex = 1; // índice del jugador activo (1-based)
  private List<Float> cardCurrentOffsets = new ArrayList<>();

  // Tokens pendientes si se reciben antes de construir la vista
  private String[] pendingTokens;

  // Grupo de tokens pendiente (si se setea antes de crear el PropertyToken)
  private Integer pendingTokensGroup = null;

  // Visibilidad de tokens según si hay propiedades
  private boolean hasTokens = false;

  // Visibilidad de la tarjeta de propiedad
  private boolean showPropertyCard = false;

  // TODO: Revisar si se requiere y ver su uso en la linea 284
  // private boolean visible = false;

  private Container actionBar; // barra de acciones bajo la property card
  private com.simsilica.lemur.Button buyBtn;
  private com.simsilica.lemur.Button auctionBtn;
  private String currentPriceDigits = "0";

  // TODO: Revisar si se requiere su uso y verificar las lineas comentadas 360 y 380
  // private String currentPropertyName = null;

  // Logger
  private static Logger logger = LogManager.getLogger(HUD.class);

  // Nueva referencia a JailDecision
  private JailDecision jailDecision;

  public HUD() {
    // No necesita implemetacion (Creo)
    // Comentario para evitar warning de sonar
  }

  @Override
  public void initialize(AppStateManager stateManager, Application application) {
    super.initialize(stateManager, application);
    this.app = (Launcher) application;

    if (GuiGlobals.getInstance() == null) {
      GuiGlobals.initialize(application);
    }

    this.guiNode = app.getGuiNode();
    this.camera = app.getCamera();
    // this.input = app.getInputManager();

    buildPanes();
    layoutComponents();
    setVisible(true);
  }

  public void setHudController(IHUDcontroller hudController) {
    this.hudController = hudController;
  }

  private void buildPanes() {
    // Crear subcomponentes con AssetManager para cargar sprites
    this.propertyCard = new PropertyCard(app.getAssetManager());
    this.propertyToken = new PropertyToken(app.getAssetManager());
    this.auction = new Auction(app.getAssetManager());
    this.auction.setHudController(hudController);
    if (pendingTokensGroup != null) {
      try {
        propertyToken.setGroup(pendingTokensGroup);
      } catch (Exception ex) {
        logger.error("No se pudo aplicar pendingTokensGroup", ex);
      }
      pendingTokensGroup = null;
    }
    if (pendingTokens != null) {
      try {
        propertyToken.setTokens(pendingTokens);
        // marcar tokens como presentes
        hasTokens = pendingTokens.length > 0;
      } catch (Exception ex) {
        logger.error("No se pudo aplicar pendingTokens", ex);
      }
      pendingTokens = null;
    }

    // Left (PlayerCards stacked)
    leftPane = new Container();
    leftPane.setBackground(new QuadBackgroundComponent(new ColorRGBA(0, 0, 0, 0)));
    playersBox = leftPane.addChild(new Container());
    playersBox.setBackground(null);

    // Si había jugadores pendientes, poblar ahora
    if (pendingPlayers != null) {
      populatePlayers(pendingPlayers);
      pendingPlayers = null;
    }

    // Right (PropertyCard)
    rightPane = new Container();
    rightPane.setBackground(new QuadBackgroundComponent(new ColorRGBA(0, 0, 0, 0)));
    rightPane.addChild(propertyCard.getRoot());
    // Ocultar por defecto hasta que el juego la solicite explícitamente
    rightPane.setCullHint(com.jme3.scene.Spatial.CullHint.Always);

    // Bottom (PropertyToken)
    bottomPane = new Container();
    bottomPane.setBackground(new QuadBackgroundComponent(new ColorRGBA(0, 0, 0, 0)));
    bottomPane.addChild(propertyToken.getRoot());
    // Ocultar tokens por defecto hasta que haya propiedades
    bottomPane.setCullHint(com.jme3.scene.Spatial.CullHint.Always);

    // Overlay (Auction)
    overlayPane = new Container();
    overlayPane.setBackground(new QuadBackgroundComponent(new ColorRGBA(0, 0, 0, 0.25f)));
    overlayPane.addChild(auction.getRoot());
    overlayPane.setCullHint(com.jme3.scene.Spatial.CullHint.Always);

    // Jail decision component
    jailDecision = new JailDecision(app.getAssetManager());
    jailDecision.setHudController(hudController);
    guiNode.attachChild(jailDecision.getRoot());

    // Action bar debajo de la tarjeta
    actionBar = new Container();
    actionBar.setBackground(null);
    actionBar.setInsets(new com.simsilica.lemur.Insets3f(8, 0, 0, 0));
    Container actionRow =
        new Container(new SpringGridLayout(com.simsilica.lemur.Axis.X, com.simsilica.lemur.Axis.Y));
    actionBar.addChild(actionRow);

    com.NullPtr.Pontiland.view.Button renderer =
        new com.NullPtr.Pontiland.view.Button(app.getAssetManager())
            .setDefaultFontSize(16f)
            .setHoverScale(1.06f);

    buyBtn =
        renderer.render(
            com.NullPtr.Pontiland.view.Button.Type.POSITIVE,
            "Comprar -$0",
            0.55f,
            com.NullPtr.Pontiland.view.Button.Variant.MAIN);
    buyBtn.addClickCommands(ignored -> hudController.comprarPropiedad());

    auctionBtn =
        renderer.render(
            com.NullPtr.Pontiland.view.Button.Type.NEGATIVE,
            "Subastar",
            0.55f,
            com.NullPtr.Pontiland.view.Button.Variant.MAIN);
    auctionBtn.addClickCommands(ignored -> hudController.iniciarSubasta());

    // Event card (usada para mostrar eventos buenos y malos)
    this.eventCard = new com.NullPtr.Pontiland.view.HUDComponents.EventCard(app.getAssetManager());
    // El botón de la EventCard ocultará la carta al ser pulsado
    eventCard.setCloseCommand(this::hideEventCards);

    actionRow.addChild(buyBtn);
    Container spacer = new Container();
    spacer.setBackground(null);
    spacer.setPreferredSize(new Vector3f(12f, 0f, 0));
    actionRow.addChild(spacer);

    actionRow.addChild(auctionBtn);

    guiNode.attachChild(leftPane);
    guiNode.attachChild(rightPane);
    guiNode.attachChild(bottomPane);
    guiNode.attachChild(overlayPane);
    guiNode.attachChild(actionBar);
    // Event card overlay: añadir pero ocultar por defecto
    guiNode.attachChild(eventCard.getRoot());
    eventCard.getRoot().setCullHint(com.jme3.scene.Spatial.CullHint.Always);
  }

  private void layoutComponents() {
    float w = camera.getWidth();
    float h = camera.getHeight();

    Vector3f leftPref = leftPane.getPreferredSize();
    float leftX = 16f;
    float leftY = h - 16f;
    leftPane.setLocalTranslation(leftX, leftY, 0);
    /*Let Lemur compute preferred size automatically; do not force sizes here
         leftPane.setPreferredSize(leftPref);
    */
    Vector3f rightSize = propertyCard.getPreferredSize();
    // clamp right size to avoid negative values
    if (rightSize == null) rightSize = new Vector3f(0f, 0f, 0f);
    if (rightSize.x < 0f || rightSize.y < 0f) {
      logger.debug("rightSize had negative values, clamping: {}", rightSize);
      rightSize.x = Math.max(0f, rightSize.x);
      rightSize.y = Math.max(0f, rightSize.y);
      rightSize.z = Math.max(0f, rightSize.z);
    }
    float rightX = w - rightSize.x - 16f;
    float rightY = h - 16f;
    rightPane.setLocalTranslation(rightX, rightY, 0);
    // rightPane.setPreferredSize(rightSize);

    // Action bar justo debajo de la tarjeta
    Vector3f actionSize = actionBar.getPreferredSize();
    if (actionSize == null) actionSize = new Vector3f(0f, 0f, 0f);
    if (actionSize.x < 0f || actionSize.y < 0f) {
      logger.debug("actionSize had negative values, clamping: {}", actionSize);
      actionSize.x = Math.max(0f, actionSize.x);
      actionSize.y = Math.max(0f, actionSize.y);
      actionSize.z = Math.max(0f, actionSize.z);
    }
    float actionX = rightX + (rightSize.x - actionSize.x) / 2f;
    float actionY = rightY - rightSize.y - 8f; // 8px debajo
    actionBar.setLocalTranslation(actionX, actionY, 0);
    // Do not force the action bar size; avoids negative inner sizes from insets
    // actionBar.setPreferredSize(actionSize);

    Vector3f bottomSize = propertyToken.getPreferredSize();
    if (bottomSize == null) bottomSize = new Vector3f(0f, 0f, 0f);
    if (bottomSize.x < 0f || bottomSize.y < 0f) {
      logger.debug("bottomSize had negative values, clamping: {}", bottomSize);
      bottomSize.x = Math.max(0f, bottomSize.x);
      bottomSize.y = Math.max(0f, bottomSize.y);
      bottomSize.z = Math.max(0f, bottomSize.z);
    }
    float bottomX = (w - bottomSize.x) / 2f;
    float bottomY = bottomSize.y + 16f;
    bottomPane.setLocalTranslation(bottomX, bottomY, 0);
    // bottomPane.setPreferredSize(bottomSize);

    Vector3f overlaySize = auction.getPreferredSize();
    if (overlaySize == null) overlaySize = new Vector3f(0f, 0f, 0f);
    if (overlaySize.x < 0f || overlaySize.y < 0f) {
      logger.debug("overlaySize had negative values, clamping: {}", overlaySize);
      overlaySize.x = Math.max(0f, overlaySize.x);
      overlaySize.y = Math.max(0f, overlaySize.y);
      overlaySize.z = Math.max(0f, overlaySize.z);
    }
    float ovX = (w - overlaySize.x) / 2f;
    float ovY = (h + overlaySize.y) / 2f;
    overlayPane.setLocalTranslation(ovX, ovY, 10f);
    // overlayPane.setPreferredSize(overlaySize);

    // Center JailDecision like Auction
    if (jailDecision != null) {
      Vector3f size = jailDecision.getPreferredSize();
      if (size == null) size = new Vector3f(0f, 0f, 0f);
      float jx = (camera.getWidth() - size.x) / 2f;
      float jy = (camera.getHeight() + size.y) / 2f;
      jailDecision.setLocalTranslation(jx, jy, 15f);
    }
  }

  @Override
  public void update(float tpf) {
    super.update(tpf);
    layoutComponents();
    animateActivePlayerIndicator(tpf);
  }

  @Override
  public void cleanup() {
    super.cleanup();
    if (leftPane != null && leftPane.getParent() != null) leftPane.removeFromParent();
    if (rightPane != null && rightPane.getParent() != null) rightPane.removeFromParent();
    if (bottomPane != null && bottomPane.getParent() != null) bottomPane.removeFromParent();
    if (overlayPane != null && overlayPane.getParent() != null) overlayPane.removeFromParent();
  }

  public void setVisible(boolean value) {
    // this.visible = value;
    if (leftPane != null)
      leftPane.setCullHint(
          value ? com.jme3.scene.Spatial.CullHint.Inherit : com.jme3.scene.Spatial.CullHint.Always);
    if (rightPane != null)
      rightPane.setCullHint(
          value && showPropertyCard
              ? com.jme3.scene.Spatial.CullHint.Inherit
              : com.jme3.scene.Spatial.CullHint.Always);
    if (actionBar != null)
      actionBar.setCullHint(
          value && showPropertyCard
              ? com.jme3.scene.Spatial.CullHint.Inherit
              : com.jme3.scene.Spatial.CullHint.Always);
    if (bottomPane != null)
      bottomPane.setCullHint(
          value && hasTokens
              ? com.jme3.scene.Spatial.CullHint.Inherit
              : com.jme3.scene.Spatial.CullHint.Always);
  }

  public void showAuction(String propertyName, String currentPriceText) {
    auction.setInfo(propertyName, currentPriceText);
    if (overlayPane != null) overlayPane.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
  }

  // Actualiza el nombre del jugador que participa en la subasta (vista).
  public void setAuctionPlayerName(String playerName) {
    if (auction != null) auction.setPlayerName(playerName);
  }

  // Oculta la UI de subasta
  public void hideAuction() {
    if (overlayPane != null) overlayPane.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
  }

  // Muestra una carta de evento "buena" en el centro con estilo por defecto.
  public void showGoodEvent(String title, String descriptionText) {
    if (eventCard == null) return;
    eventCard.setInfo(title, descriptionText);
    // usar sprite positivo
    eventCard.setType(com.NullPtr.Pontiland.view.HUDComponents.EventCard.Type.POSITIVE);
    // posicionar en el centro de la pantalla
    layoutEventCard();
    eventCard.getRoot().setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
  }

  // Muestra una carta de evento "mala" en el centro con estilo por defecto.
  public void showBadEvent(String title, String descriptionText) {
    if (eventCard == null) return;
    eventCard.setInfo(title, descriptionText);
    // usar sprite negativo
    eventCard.setType(com.NullPtr.Pontiland.view.HUDComponents.EventCard.Type.NEGATIVE);
    layoutEventCard();
    eventCard.getRoot().setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
  }

  private void layoutEventCard() {
    if (eventCard == null || camera == null) return;
    Vector3f pref = eventCard.getRoot().getPreferredSize();
    if (pref == null) pref = new Vector3f(300f, 160f, 0f);
    float cx = (camera.getWidth() - pref.x) / 2f;
    float cy = (camera.getHeight() + pref.y) / 2f;
    eventCard.getRoot().setLocalTranslation(cx, cy, 20f);
  }

  // Oculta cualquier carta de evento visible
  public void hideEventCards() {
    if (eventCard != null) {
      eventCard.getRoot().setCullHint(com.jme3.scene.Spatial.CullHint.Always);
    }
  }

  /**
   * Establece el índice del jugador activo para la animación del indicador de turno.
   *
   * @param playerIndex índice del jugador activo (1-based)
   */
  public void setActivePlayerIndex(int playerIndex) {
    this.activePlayerIndex = Math.max(1, playerIndex);
  }

  /**
   * Anima el desplazamiento de las tarjetas de jugador para indicar el turno activo. El jugador
   * activo se desplaza hacia la derecha.
   *
   * @param tpf tiempo transcurrido desde el último frame
   */
  private void animateActivePlayerIndicator(float tpf) {
    if (playerCards.isEmpty()) return;

    // Asegurar que tenemos la lista de offsets inicializada
    while (cardCurrentOffsets.size() < playerCards.size()) {
      cardCurrentOffsets.add(0f);
    }

    for (int i = 0; i < playerCards.size(); i++) {
      float targetOffset = (i + 1 == activePlayerIndex) ? ACTIVE_PLAYER_OFFSET_X : 0f;
      float currentOffset = cardCurrentOffsets.get(i);

      // Interpolación suave hacia el offset objetivo
      float newOffset = currentOffset + (targetOffset - currentOffset) * ANIMATION_SPEED * tpf;

      // Evitar oscilaciones mínimas
      if (Math.abs(newOffset - targetOffset) < 0.5f) {
        newOffset = targetOffset;
      }

      cardCurrentOffsets.set(i, newOffset);

      // Aplicar el offset usando setLocalTranslation con la posición Y/Z preservada del layout
      Container cardRoot = playerCards.get(i).getRoot();
      Vector3f currentPos = cardRoot.getLocalTranslation();
      // Solo modificar X, preservar Y y Z que Lemur calcula
      cardRoot.setLocalTranslation(newOffset, currentPos.y, currentPos.z);
    }
  }

  /**
   * Actualiza la tarjeta de un jugador específico.
   *
   * @param player entidad Jugador con nombre, dinero, iconoId, etc.
   * @param playerIndex índice del jugador (1-4)
   */
  public void updatePlayerCard(Jugador player, int playerIndex) {
    int idx = Math.max(1, playerIndex) - 1;
    if (idx >= 0 && idx < playerCards.size()) {
      playerCards.get(idx).setInfo(player, playerIndex);
    }
  }

  /**
   * Establece la lista de jugadores para mostrar en las PlayerCards.
   *
   * @param jugadores lista de entidades Jugador con nombre, dinero, iconoId, etc.
   */
  public void setPlayers(List<Jugador> jugadores) {
    if (playersBox == null) {
      pendingPlayers = (jugadores == null) ? null : new ArrayList<>(jugadores);
      return;
    }
    populatePlayers(jugadores);
  }

  private void populatePlayers(List<Jugador> jugadores) {
    playerCards.clear();
    playersBox.detachAllChildren();
    if (jugadores == null) return;

    for (int i = 0; i < jugadores.size(); i++) {
      PlayerCard card = new PlayerCard(app.getAssetManager());
      card.setInfo(jugadores.get(i), i + 1);
      playerCards.add(card);
      playersBox.addChild(card.getRoot());
      // Añadir un pequeño separador
      Container spacer = new Container();
      spacer.setBackground(null);
      spacer.setPreferredSize(new Vector3f(0, 10f, 0));
      playersBox.addChild(spacer);
    }
  }

  public void updatePropertyCard(String name, String priceText, String[] rentsText) {
    propertyCard.setInfo(name, priceText, rentsText);

    // No se usa aun
    /*if (name != null && !name.isBlank()) {
      currentPropertyName = name;
    }*/

    // Actualizar el texto del botón de compra con el precio recibido (si viene)
    if (priceText != null && !priceText.isBlank()) {
      String digits = priceText.replaceAll("\\D", "");
      if (digits.isEmpty()) digits = "0";
      currentPriceDigits = digits;
      if (buyBtn != null) buyBtn.setText("Comprar -$" + currentPriceDigits);
    }
  }

  public void setPropertyGroup(int groupIndex) {
    propertyCard.setGroup(groupIndex);
  }

  // Mostrar/ocultar explícitamente la tarjeta de propiedad
  public void showPropertyCard(String name, String priceText, String[] rentsText, int groupIndex) {
    propertyCard.setGroup(groupIndex);
    propertyCard.setInfo(name, priceText, rentsText);

    /*if (name != null && !name.isBlank()) {
      currentPropertyName = name;
    }*/

    // Precio inicial solo si se recibió
    if (priceText != null && !priceText.isBlank()) {
      String digits = priceText.replaceAll("\\D", "");
      if (digits.isEmpty()) digits = "0";
      currentPriceDigits = digits;
      if (buyBtn != null) buyBtn.setText("Comprar -$" + currentPriceDigits);
    }

    showPropertyCard = true;
    if (rightPane != null) {
      rightPane.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
    }
    if (actionBar != null) {
      actionBar.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
    }
  }

  public void hidePropertyCard() {
    showPropertyCard = false;
    if (rightPane != null) {
      rightPane.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
    }
    if (actionBar != null) {
      actionBar.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
    }
  }

  public void updatePropertyTokens(String[] tokens) {
    // Si el componente no está aún inicializado, guardar para aplicar luego
    if (propertyToken == null) {
      logger.debug(
          "updatePropertyTokens llamado antes de inicializar HUD: almacenando tokens pendientes");
      pendingTokens = (tokens == null) ? null : Arrays.copyOf(tokens, tokens.length);
      hasTokens = pendingTokens != null && pendingTokens.length > 0;
      if (bottomPane != null) {
        bottomPane.setCullHint(
            hasTokens
                ? com.jme3.scene.Spatial.CullHint.Inherit
                : com.jme3.scene.Spatial.CullHint.Always);
      }
      return;
    }

    propertyToken.setTokens(tokens);
    // Alternar visibilidad según si hay tokens
    hasTokens = tokens != null && tokens.length > 0;
    if (bottomPane != null) {
      bottomPane.setCullHint(
          hasTokens
              ? com.jme3.scene.Spatial.CullHint.Inherit
              : com.jme3.scene.Spatial.CullHint.Always);
    }
  }

  public void setPropertyTokensGroup(int groupIndex) {
    if (propertyToken == null) {
      // almacenar el grupo y aplicarlo cuando se cree propertyToken
      pendingTokensGroup = groupIndex;
      logger.debug(
          "setPropertyTokensGroup llamado antes de inicializar HUD: guardando grupo pendiente={}",
          groupIndex);
      return;
    }
    propertyToken.setGroup(groupIndex);
  }

  // Método compartido para obtener el color de texto contrastado por grupo.
  // Se hace público y estático para que los componentes HUDComponent lo reutilicen.
  public static ColorRGBA getContrastingColorForGroup(int group) {
    int g = Math.clamp(group, 1, 8);
    switch (g) {
      case 1:
        return ColorRGBA.White;
      case 2:
        return ColorRGBA.Black;
      case 3:
        return ColorRGBA.White;
      case 4:
        return ColorRGBA.White;
      case 5:
        return ColorRGBA.White;
      case 6:
        return ColorRGBA.Black;
      case 7:
        return ColorRGBA.White;
      case 8:
        return ColorRGBA.White;
      default:
        return ColorRGBA.White;
    }
  }

  public void showJailDecision() {
    if (jailDecision != null) jailDecision.setVisible(true);
  }

  public void hideJailDecision() {
    if (jailDecision != null) {
      jailDecision.setVisible(false);
    }
  }
}
