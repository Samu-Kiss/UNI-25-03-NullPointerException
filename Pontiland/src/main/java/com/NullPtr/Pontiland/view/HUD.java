package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.view.HUDComponents.Auction;
import com.NullPtr.Pontiland.view.HUDComponents.PlayerCard;
import com.NullPtr.Pontiland.view.HUDComponents.PropertyCard;
import com.NullPtr.Pontiland.view.HUDComponents.PropertyToken;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import java.util.ArrayList;
import java.util.List;

public class HUD extends AbstractAppState {

  private Launcher app;
  private Node guiNode;
  private Camera camera;
  private InputManager input;

  private Container leftPane;
  private Container rightPane;
  private Container bottomPane;
  private Container overlayPane;

  private Container playersBox; // contendrá varias PlayerCard apiladas

  private List<PlayerCard> playerCards = new ArrayList<>();
  private PropertyCard propertyCard;
  private PropertyToken propertyToken;
  private Auction auction;

  private IHUDcontroller hudController;

  // Nombres pendientes si se llaman antes de construir playersBox
  private List<String> pendingPlayerNames;

  // Visibilidad de tokens según si hay propiedades
  private boolean hasTokens = false;

  // Visibilidad de la tarjeta de propiedad
  private boolean showPropertyCard = false;

  private boolean visible = false;

  private Container actionBar; // barra de acciones bajo la property card
  private com.simsilica.lemur.Button buyBtn;
  private com.simsilica.lemur.Button auctionBtn;
  private String currentPriceDigits = "0";
  private String currentPropertyName = null;

  public HUD() {}

  @Override
  public void initialize(AppStateManager stateManager, Application application) {
    super.initialize(stateManager, application);
    this.app = (Launcher) application;

    if (GuiGlobals.getInstance() == null) {
      GuiGlobals.initialize(application);
    }

    this.guiNode = app.getGuiNode();
    this.camera = app.getCamera();
    this.input = app.getInputManager();

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

    // Left (PlayerCards stacked)
    leftPane = new Container();
    leftPane.setBackground(new QuadBackgroundComponent(new ColorRGBA(0, 0, 0, 0)));
    playersBox = leftPane.addChild(new Container());
    playersBox.setBackground(null);

    // Si había nombres pendientes, poblar ahora
    if (pendingPlayerNames != null) {
      populatePlayerNames(pendingPlayerNames);
      pendingPlayerNames = null;
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
    auctionBtn.addClickCommands(
        ignored -> {
          hudController.iniciarSubasta();
        });

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
  }

  private void layoutComponents() {
    float w = camera.getWidth();
    float h = camera.getHeight();

    Vector3f leftPref = leftPane.getPreferredSize();
    float leftX = 16f;
    float leftY = h - 16f;
    leftPane.setLocalTranslation(leftX, leftY, 0);
    leftPane.setPreferredSize(leftPref);

    Vector3f rightSize = propertyCard.getPreferredSize();
    float rightX = w - rightSize.x - 16f;
    float rightY = h - 16f;
    rightPane.setLocalTranslation(rightX, rightY, 0);
    rightPane.setPreferredSize(rightSize);

    // Action bar justo debajo de la tarjeta
    Vector3f actionSize = actionBar.getPreferredSize();
    float actionX = rightX + (rightSize.x - actionSize.x) / 2f;
    float actionY = rightY - rightSize.y - 8f; // 8px debajo
    actionBar.setLocalTranslation(actionX, actionY, 0);
    actionBar.setPreferredSize(actionSize);

    Vector3f bottomSize = propertyToken.getPreferredSize();
    float bottomX = (w - bottomSize.x) / 2f;
    float bottomY = bottomSize.y + 16f;
    bottomPane.setLocalTranslation(bottomX, bottomY, 0);
    bottomPane.setPreferredSize(bottomSize);

    Vector3f overlaySize = auction.getPreferredSize();
    float ovX = (w - overlaySize.x) / 2f;
    float ovY = (h + overlaySize.y) / 2f;
    overlayPane.setLocalTranslation(ovX, ovY, 10f);
    overlayPane.setPreferredSize(overlaySize);
  }

  @Override
  public void update(float tpf) {
    super.update(tpf);
    layoutComponents();
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
    this.visible = value;
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

  public void setAuctionPlayerName(String playerName) {
    auction.setPlayerName(playerName);
  }

  public void hideAuction() {
    if (overlayPane != null) overlayPane.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
  }

  public void updatePlayerCard(
      String playerName, String moneyText, boolean inJail, int playerIndex) {

    int idx = Math.max(1, playerIndex) - 1;
    if (idx >= 0 && idx < playerCards.size()) {
      playerCards.get(idx).setInfo(playerName, moneyText, inJail, playerIndex);
    }
  }

  public void setPlayerNames(List<String> names) {
    if (playersBox == null) {
      pendingPlayerNames = (names == null) ? null : new ArrayList<>(names);
      return;
    }
    populatePlayerNames(names);
  }

  private void populatePlayerNames(List<String> names) {
    playerCards.clear();
    playersBox.detachAllChildren();
    if (names == null) return;

    for (int i = 0; i < names.size(); i++) {
      PlayerCard card = new PlayerCard(app.getAssetManager());
      card.setInfo(names.get(i), "$1500", false, i + 1);
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

    if (name != null && !name.isBlank()) {
      currentPropertyName = name;
    }

    // Actualizar el texto del botón de compra con el precio recibido (si viene)
    if (priceText != null && !priceText.isBlank()) {
      String digits = priceText.replaceAll("[^0-9]", "");
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

    if (name != null && !name.isBlank()) {
      currentPropertyName = name;
    }

    // Precio inicial solo si se recibió
    if (priceText != null && !priceText.isBlank()) {
      String digits = priceText.replaceAll("[^0-9]", "");
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
    propertyToken.setGroup(groupIndex);
  }
}
