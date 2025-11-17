package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.Launcher;
import com.NullPtr.Pontiland.controllers.IMenuActions;
import com.NullPtr.Pontiland.services.GitHubContributorsService;
import com.NullPtr.Pontiland.services.GitHubContributorsService.Contributor;
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
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.Styles;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Pantalla de créditos con layout consistente al menú principal: título a la izquierda y contenido
 * a la derecha.
 */
public class MenuCreditos extends AbstractAppState {

  private final Runnable onClose;
  private final String fetchOwner;
  private final String fetchRepo;
  private final int fetchLimit;
  private IMenuActions actions;

  // Referencias JME/Lemur
  private LegacyApplication app;
  private Node guiNode;
  private InputManager inputManager;
  private Camera camera;

  // Contenedores UI
  private Container backdrop;
  private Container leftPane;
  private Container rightPane;
  private Container dynamicSection; // para insertar contribuidores
  private Container backBar;
  private static final int AVATAR_SIZE = 36; // px

  private static Logger logger = LogManager.getLogger(MenuCreditos.class);

  public MenuCreditos(Runnable onClose) {
    this(onClose, null, null, 0);
  }

  public MenuCreditos(Runnable onClose, String owner, String repo, int limit) {
    this.onClose = onClose;
    this.fetchOwner = owner;
    this.fetchRepo = repo;
    this.fetchLimit = limit <= 0 ? 10 : limit;
  }

  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    Launcher launcher = (Launcher) app;
    this.app = launcher;
    this.guiNode = launcher.getGuiNode();
    this.inputManager = launcher.getInputManager();
    this.camera = launcher.getCamera();

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

    // Backdrop
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

    // Panel derecho: créditos
    rightPane = new Container("pontiland");

    Label subtitle = new Label("Créditos", "pontiland");
    subtitle.setFontSize(22);
    subtitle.setInsets(new com.simsilica.lemur.Insets3f(0, 0, 16, 0));
    subtitle.setColor(new ColorRGBA(0.85f, 0.86f, 0.92f, 1f));
    subtitle.setTextHAlignment(com.simsilica.lemur.HAlignment.Center);
    rightPane.addChild(subtitle);

    Container content = rightPane.addChild(new Container("pontiland"));
    content.setBackground(null);
    content.setInsets(new com.simsilica.lemur.Insets3f(8, 12, 8, 12));

    // Equipo con roles y nombres (sin descripciones)
    addSection(
        content,
        "Equipo NullPtr",
        new String[] {
          "PM: Maria Alejandra García ✨",
          "QA / Testing: Maria Alejandra García ✨",
          "Backend Developer: Thomas Leal 🧩, Nicolas Torres 🏢",
          "Data Base Administrator: Juan David Ortiz ☕",
          "Frontend Developer: Samuel Pico ✒️",
          "Escritor Técnico UML: Nicolas Torres 🏢, Santiago Mendez 💀",
          "Diseñador: Samuel Pico ✒️",
        });

    addSection(content, "Tecnologías", new String[] {"JMonkeyEngine 3", "Lemur UI", "Java 24"});

    // Sección dinámica para contribuidores
    dynamicSection = rightPane.addChild(new Container("pontiland"));
    dynamicSection.setBackground(null);
    dynamicSection.setInsets(new com.simsilica.lemur.Insets3f(10, 12, 0, 12));

    if (fetchOwner != null && fetchRepo != null) {
      Label loading =
          new Label(
              "Cargando contribuidores de " + fetchOwner + "/" + fetchRepo + "…", "pontiland");
      loading.setColor(new ColorRGBA(0.80f, 0.82f, 0.88f, 1f));
      dynamicSection.addChild(loading);
      startFetchContributors();
    }

    // Centrar panel derecho sin fijar tamaño preferido (dejar que Lemur lo calcule)
    Vector3f rightPref = rightPane.getPreferredSize();
    float rightX = halfW + (halfW - rightPref.x) / 2f;
    float rightY = (camera.getHeight() + rightPref.y) / 2f;
    rightPane.setLocalTranslation(rightX, rightY, 0);

    guiNode.attachChild(leftPane);
    guiNode.attachChild(rightPane);

    // Overlay: botón volver arriba-izquierda con icono + texto
    backBar = new Container("pontiland");
    backBar.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.16f, 0.18f, 0.22f, 0.85f)));
    backBar.setInsets(new com.simsilica.lemur.Insets3f(6, 10, 6, 10));

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
    iconBtn.setInsets(new com.simsilica.lemur.Insets3f(0, 0, 0, 6));
    iconBtn.addClickCommands(src -> onBack());

    Button textBtn = new Button("Volver", "pontiland");
    textBtn.setFontSize(16);
    textBtn.setInsets(new com.simsilica.lemur.Insets3f(2, 6, 2, 6));
    textBtn.addClickCommands(src -> onBack());

    row.addChild(iconBtn, BorderLayout.Position.West);
    row.addChild(textBtn, BorderLayout.Position.Center);
    backBar.addChild(row);

    backBar.setLocalTranslation(10, camera.getHeight() - 10f, 1);
    guiNode.attachChild(backBar);
  }

  private void startFetchContributors() {
    Thread t =
        new Thread(
            () -> {
              try {
                List<Contributor> contributors =
                    new GitHubContributorsService()
                        .fetchContributorsDetailed(fetchOwner, fetchRepo, fetchLimit);
                // Descargar avatares (BufferedImage) en background
                List<Object[]> data = new ArrayList<>(); // [login, htmlUrl, bufferedImage]
                for (Contributor c : contributors) {
                  BufferedImage img = null;
                  try {
                    img = downloadAvatar(c.avatarUrl, AVATAR_SIZE);
                    img = makeCircular(img, AVATAR_SIZE);
                  } catch (Exception ex) {
                    img = makePlaceholderCircle(AVATAR_SIZE);

                    logger.error("Failed to download or process avatar for {}: ", c.login, ex);
                  }
                  data.add(new Object[] {c.login, c.htmlUrl, img});
                }

                if (app != null) {
                  List<Object[]> finalData = data;
                  app.enqueue(
                      () -> {
                        showContributorsWithAvatars(finalData);
                        recenterRightPane();
                        return null;
                      });
                }
              } catch (Exception ex) {
                if (app != null) {
                  app.enqueue(
                      () -> {
                        showContributorsError(ex.getMessage());
                        recenterRightPane();
                        return null;
                      });
                }
                logger.error("Failed to fetch contributors from GitHub: ", ex);
                // TODO revisar si es correcto interrumpir el hilo aquí (Sonarqube molesta)
                if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
              }
            },
            "github-contrib-fetch");
    t.setDaemon(true);
    t.start();
  }

  private BufferedImage downloadAvatar(String avatarUrl, int size) {
    if (avatarUrl == null || avatarUrl.isBlank()) return null;
    String sizedUrl = avatarUrl + (avatarUrl.contains("?") ? "&" : "?") + "s=" + size;
    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    HttpRequest req =
        HttpRequest.newBuilder()
            .uri(URI.create(sizedUrl))
            .timeout(Duration.ofSeconds(15))
            .header("User-Agent", "Pontiland/1.0")
            .GET()
            .build();

    try {
      HttpResponse<java.io.InputStream> resp =
          client.send(req, HttpResponse.BodyHandlers.ofInputStream());
      if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
        return javax.imageio.ImageIO.read(resp.body());
      }
    } catch (Exception ex) {
      logger.error("Failed to download avatar from {}: ", sizedUrl, ex);
      // TODO revisar si es correcto interrumpir el hilo aquí (Sonarqube molesta)
      if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
    }
    return null;
  }

  private BufferedImage makeCircular(BufferedImage src, int size) {
    if (src == null) return makePlaceholderCircle(size);
    BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = out.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Fondo transparente
    g.setComposite(java.awt.AlphaComposite.Clear);
    g.fillRect(0, 0, size, size);
    g.setComposite(java.awt.AlphaComposite.SrcOver);

    // Clip circular
    g.setClip(new Ellipse2D.Float(0, 0, size, size));

    // Escalar manteniendo aspecto para cubrir el círculo
    double scale = Math.max((double) size / src.getWidth(), (double) size / src.getHeight());
    int drawW = (int) Math.round(src.getWidth() * scale);
    int drawH = (int) Math.round(src.getHeight() * scale);
    int offsetX = (size - drawW) / 2;
    int offsetY = (size - drawH) / 2;
    g.drawImage(src, offsetX, offsetY, drawW, drawH, null);

    g.dispose();
    return out;
  }

  private BufferedImage makePlaceholderCircle(int size) {
    BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = out.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(new Color(80, 82, 90));
    g.fill(new Ellipse2D.Float(0, 0, size, size));
    g.dispose();
    return out;
  }

  private Texture2D toTexture(BufferedImage img) {
    if (img == null) return null;
    AWTLoader loader = new AWTLoader();
    Image jmeImage = loader.load(img, true);
    return new Texture2D(jmeImage);
  }

  private void showContributorsWithAvatars(List<Object[]> items) {
    dynamicSection.clearChildren();

    if (items == null || items.isEmpty()) {
      Label none = new Label("Contribuidores: (vacío)", "pontiland");
      none.setColor(new ColorRGBA(0.80f, 0.82f, 0.88f, 1f));
      dynamicSection.addChild(none);
      return;
    }

    addSection(dynamicSection, "Contribuidores (GitHub)", new String[] {});

    for (Object[] it : items) {
      String login = (String) it[0];
      BufferedImage img = (BufferedImage) it[2];

      Container row = new Container(new BorderLayout(), "pontiland");
      row.setBackground(null);
      row.setInsets(new com.simsilica.lemur.Insets3f(4, 6, 4, 6));

      // Avatar fijo en el lado izquierdo (west)
      Container avatarBox = new Container("pontiland");
      avatarBox.setBackground(null);
      Texture2D tex = toTexture(img);
      if (tex != null) {
        avatarBox.setBackground(new QuadBackgroundComponent(tex));
      } else {
        avatarBox.setBackground(
            new QuadBackgroundComponent(new ColorRGBA(0.30f, 0.32f, 0.37f, 1f)));
      }
      avatarBox.setPreferredSize(new Vector3f(AVATAR_SIZE, AVATAR_SIZE, 0));
      row.addChild(avatarBox, BorderLayout.Position.West);

      // Nombre en el centro
      Label name = new Label(login, "pontiland");
      name.setFontSize(16);
      name.setColor(new ColorRGBA(0.90f, 0.91f, 0.96f, 1f));
      name.setInsets(new com.simsilica.lemur.Insets3f(8, 10, 8, 10));
      row.addChild(name, BorderLayout.Position.Center);

      dynamicSection.addChild(row);
    }
  }

  private void recenterRightPane() {
    if (rightPane == null) return;
    Vector3f rightPref = rightPane.getPreferredSize();
    float halfW = camera.getWidth() / 2f;
    float rightX = halfW + (halfW - rightPref.x) / 2f;
    float rightY = (camera.getHeight() + rightPref.y) / 2f;
    rightPane.setLocalTranslation(rightX, rightY, 0);
  }

  // TODO revisar si es necesario (Sonarqube molesta)
  private void showContributors(List<String> logins) {
    dynamicSection.clearChildren();

    if (logins == null || logins.isEmpty()) {
      Label none = new Label("Contribuidores: (vacío)", "pontiland");
      none.setColor(new ColorRGBA(0.80f, 0.82f, 0.88f, 1f));
      dynamicSection.addChild(none);
      return;
    }

    addSection(dynamicSection, "Contribuidores (GitHub)", logins.toArray(new String[0]));
  }

  private void showContributorsError(String message) {
    dynamicSection.clearChildren();
    Label err = new Label("Error al cargar contribuidores: " + message, "pontiland");
    err.setColor(new ColorRGBA(1f, 0.55f, 0.55f, 1f));
    dynamicSection.addChild(err);
  }

  private void addSection(Container parent, String title, String[] lines) {
    Label t = new Label(title, "pontiland");
    t.setFontSize(18);
    t.setColor(new ColorRGBA(0.90f, 0.91f, 0.96f, 1f));
    t.setInsets(new com.simsilica.lemur.Insets3f(8, 0, 6, 0));
    parent.addChild(t);

    for (String l : lines) {
      Label item = new Label(l, "pontiland");
      item.setFontSize(16);
      item.setColor(new ColorRGBA(0.82f, 0.84f, 0.90f, 1f));
      item.setInsets(new com.simsilica.lemur.Insets3f(2, 8, 2, 8));
      parent.addChild(item);
    }
  }

  private void onBack() {
    try {
      if (actions != null) {
        actions.goToMainMenu();
      } else if (onClose != null) {
        onClose.run();
      }
    } finally {
      cleanup();
      if (app != null) app.getStateManager().detach(this);
    }
  }

  @Override
  public void cleanup() {
    if (leftPane != null && leftPane.getParent() != null) leftPane.removeFromParent();
    if (rightPane != null && rightPane.getParent() != null) rightPane.removeFromParent();
    if (backdrop != null && backdrop.getParent() != null) backdrop.removeFromParent();
    if (backBar != null && backBar.getParent() != null) backBar.removeFromParent();
    super.cleanup();
  }
}
