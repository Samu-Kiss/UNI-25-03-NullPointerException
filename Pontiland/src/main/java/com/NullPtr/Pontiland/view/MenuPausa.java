package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.controllers.IMenuPausaActions;
import com.jme3.app.Application;
import com.jme3.app.LegacyApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.TextureKey;
import com.jme3.input.InputManager;
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
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.Styles;

/**
 * Menú de pausa superpuesto sobre la escena principal. Utiliza Lemur para construir un panel
 * semitransparente que aplica un efecto visual de desenfoque mediante un overlay oscuro y permite
 * reanudar el juego o guardarlo y salir.
 */
public class MenuPausa extends AbstractAppState {

  private static final float LOGO_SCALE = 0.55f;
  private static final float PANEL_WIDTH = 520f;
  private static final float PANEL_PADDING_X = 38f;
  private static final float PANEL_PADDING_Y = 32f;

  private final IMenuPausaActions actions;

  private LegacyApplication app;
  private Node guiNode;
  private InputManager inputManager;
  private Camera camera;

  private Container blurLayer;
  private Container backBar;
  private Container dialog;
  private Container logoContainer;

  public MenuPausa() {
    this(null);
  }

  public MenuPausa(IMenuPausaActions actions) {
    this.actions = actions;
  }

  @Override
  public void initialize(AppStateManager stateManager, Application application) {
    super.initialize(stateManager, application);

    Launcher launcher = (Launcher) application;
    this.app = launcher;
    this.guiNode = launcher.getGuiNode();
    this.inputManager = launcher.getInputManager();
    this.camera = launcher.getCamera();

    if (GuiGlobals.getInstance() == null) {
      GuiGlobals.initialize(application);
    }

    inputManager.setCursorVisible(true);
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

    float width = camera.getWidth();
    float height = camera.getHeight();

    blurLayer = new Container("pontiland");
    blurLayer.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.04f, 0.05f, 0.07f, 0.74f)));
    blurLayer.setPreferredSize(new Vector3f(width, height, 0));
    blurLayer.setLocalTranslation(0, height, -1f);
    guiNode.attachChild(blurLayer);

    backBar = buildBackBar();
    guiNode.attachChild(backBar);

    logoContainer = buildLogoContainer();
    if (logoContainer != null) {
      guiNode.attachChild(logoContainer);
    }

    dialog = buildDialogPanel();
    guiNode.attachChild(dialog);
  }

  private Container buildBackBar() {
    Container bar = new Container("pontiland");
    bar.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.18f, 0.20f, 0.25f, 0.9f)));
    bar.setInsets(new Insets3f(6, 10, 6, 10));

    Container row = new Container(new BorderLayout(), "pontiland");

    TextureKey key = new TextureKey("graphics/sprites/Common/Icons/Icon_Back_White.png", true);
    key.setGenerateMips(false);
    Texture2D iconTex = (Texture2D) this.app.getAssetManager().loadTexture(key);
    iconTex.setWrap(Texture.WrapMode.EdgeClamp);
    iconTex.setMagFilter(Texture.MagFilter.Bilinear);
    iconTex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

    Button iconBtn = new Button("", "pontiland");
    iconBtn.setBackground(new QuadBackgroundComponent(iconTex));
    iconBtn.setPreferredSize(new Vector3f(24, 24, 0));
    iconBtn.setInsets(new Insets3f(0, 0, 0, 6));
    iconBtn.addClickCommands(source -> resume());

    Button textBtn = new Button("Volver", "pontiland");
    textBtn.setFontSize(16);
    textBtn.setInsets(new Insets3f(2, 6, 2, 6));
    textBtn.addClickCommands(source -> resume());

    row.addChild(iconBtn, BorderLayout.Position.West);
    row.addChild(textBtn, BorderLayout.Position.Center);

    bar.addChild(row);

    bar.setLocalTranslation(10f, camera.getHeight() - 10f, 2f);
    return bar;
  }

  private Container buildLogoContainer() {
    TextureKey logoKey = new TextureKey("graphics/sprites/Common/Logo_White_Arch.png", true);
    logoKey.setGenerateMips(false);
    Texture2D logoTex;
    try {
      logoTex = (Texture2D) this.app.getAssetManager().loadTexture(logoKey);
    } catch (Exception ex) {
      return null;
    }

    logoTex.setWrap(Texture.WrapMode.EdgeClamp);
    logoTex.setMagFilter(Texture.MagFilter.Bilinear);
    logoTex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

    int w = logoTex.getImage().getWidth();
    int h = logoTex.getImage().getHeight();
    Vector3f size = new Vector3f(w * LOGO_SCALE, h * LOGO_SCALE, 0);

    Container logoPanel = new Container("pontiland");
    logoPanel.setBackground(new QuadBackgroundComponent(logoTex));
    logoPanel.setPreferredSize(size);

    float x = (camera.getWidth() - size.x) / 2f;
    float y = camera.getHeight() - size.y - 70f;
    logoPanel.setLocalTranslation(x, y, 3f);

    return logoPanel;
  }

  private Container buildDialogPanel() {
    Container panel = new Container("pontiland");
    panel.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.13f, 0.14f, 0.18f, 0.92f)));

    Container content = panel.addChild(new Container("pontiland"));
    content.setBackground(null);
    content.setInsets(
        new Insets3f(PANEL_PADDING_Y, PANEL_PADDING_X, PANEL_PADDING_Y, PANEL_PADDING_X));

    Container titleRow = new Container(new BorderLayout(), "pontiland");
    titleRow.setBackground(null);
    Label title = new Label("Juego en pausa", "pontiland");
    title.setFontSize(28f);
    title.setTextHAlignment(HAlignment.Center);
    titleRow.setPreferredSize(
        new Vector3f(
            Math.max(0f, PANEL_WIDTH - PANEL_PADDING_X * 2f), title.getPreferredSize().y, 0));
    titleRow.addChild(title, BorderLayout.Position.Center);
    content.addChild(titleRow);

    content.addChild(createSpacer(0f, 12f));

    Container infoRow = new Container(new BorderLayout(), "pontiland");
    infoRow.setBackground(null);
    Label info = new Label("Puedes guardar el progreso antes de salir.", "pontiland");
    info.setTextHAlignment(HAlignment.Center);
    info.setColor(new ColorRGBA(0.82f, 0.83f, 0.89f, 1f));
    infoRow.setPreferredSize(
        new Vector3f(
            Math.max(0f, PANEL_WIDTH - PANEL_PADDING_X * 2f), info.getPreferredSize().y, 0));
    infoRow.addChild(info, BorderLayout.Position.Center);
    content.addChild(infoRow);

    content.addChild(createSpacer(0f, 24f));

    com.NullPtr.Pontiland.view.Button renderer =
        new com.NullPtr.Pontiland.view.Button(this.app.getAssetManager())
            .setDefaultFontSize(22f)
            .setHoverScale(1.05f);

    com.simsilica.lemur.Button saveAndExitButton =
        renderer.render(
            com.NullPtr.Pontiland.view.Button.Type.ACCENT,
            "Guardar y salir",
            0.58f,
            com.NullPtr.Pontiland.view.Button.Variant.LONG);
    saveAndExitButton.addClickCommands(source -> saveAndExit());

    Container buttonRow = new Container(new BorderLayout(), "pontiland");
    buttonRow.setBackground(null);
    buttonRow.setPreferredSize(
        new Vector3f(
            Math.max(PANEL_WIDTH - PANEL_PADDING_X * 2f, saveAndExitButton.getPreferredSize().x),
            saveAndExitButton.getPreferredSize().y,
            0));
    buttonRow.addChild(saveAndExitButton, BorderLayout.Position.Center);
    content.addChild(buttonRow);

    Vector3f desiredSize = content.getPreferredSize().clone();
    desiredSize.x = Math.max(desiredSize.x, PANEL_WIDTH);
    content.setPreferredSize(desiredSize);
    panel.setPreferredSize(new Vector3f(desiredSize.x, desiredSize.y, 0));

    float x = (camera.getWidth() - desiredSize.x) / 2f;
    float y = (camera.getHeight() + desiredSize.y) / 2f - 140f;
    panel.setLocalTranslation(x, y, 4f);
    return panel;
  }

  private Container createSpacer(float width, float height) {
    Container spacer = new Container("pontiland");
    spacer.setBackground(null);
    spacer.setPreferredSize(new Vector3f(width, height, 0));
    return spacer;
  }

  private void resume() {
    if (actions != null) {
      actions.resumeGame();
    } else if (app != null) {
      app.getStateManager().detach(this);
    }
  }

  private void saveAndExit() {
    if (actions != null) {
      actions.saveAndExit();
    } else if (app != null) {
      app.getStateManager().detach(this);
    }
  }

  @Override
  public void cleanup() {
    super.cleanup();

    if (blurLayer != null && blurLayer.getParent() != null) {
      blurLayer.removeFromParent();
    }
    if (backBar != null && backBar.getParent() != null) {
      backBar.removeFromParent();
    }
    if (dialog != null && dialog.getParent() != null) {
      dialog.removeFromParent();
    }
    if (logoContainer != null && logoContainer.getParent() != null) {
      logoContainer.removeFromParent();
    }
  }
}
