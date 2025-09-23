package com.NullPtr.Pontiland.view;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

/**
 * Pantalla de inicio del juego Pontiland que permite seleccionar el número de jugadores.
 *
 * <p>Esta clase maneja la interfaz inicial donde los jugadores pueden elegir entre 2, 3 o 4
 * jugadores mediante botones clickeables que muestran los sprites correspondientes.
 *
 * @author Equipo de desarrollo Pontiland
 * @version 1.0
 */
public class StartScreen extends AbstractAppState implements ActionListener {

  private Node startScreenNode;
  private Node guiNode;
  private AssetManager assetManager;
  private InputManager inputManager;
  private Camera camera;
  private GameApplication app;

  // Geometrías de los botones
  private Geometry button2Players;
  private Geometry button3Players;
  private Geometry button4Players;
  private BitmapText titleText;

  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    this.app = (GameApplication) app;
    this.assetManager = app.getAssetManager();
    this.inputManager = app.getInputManager();
    this.camera = app.getCamera();
    this.guiNode = ((GameApplication) app).getGuiNode();

    // Habilitar y mostrar el cursor del mouse
    inputManager.setCursorVisible(true);

    startScreenNode = new Node("StartScreen");
    guiNode.attachChild(startScreenNode);

    setupInputs();
    createStartScreen();
  }

  /** Configura los controles de entrada para detectar clicks del mouse. */
  private void setupInputs() {
    inputManager.addMapping("MouseClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    inputManager.addListener(this, "MouseClick");
  }

  /** Crea todos los elementos visuales de la pantalla de inicio. */
  private void createStartScreen() {
    createBackground();
    createTitle();
    createPlayerButtons();
  }

  /** Crea el fondo blanco de la pantalla. */
  private void createBackground() {
    float screenWidth = camera.getWidth();
    float screenHeight = camera.getHeight();

    // Crear un quad que cubra toda la pantalla
    Quad backgroundQuad = new Quad(screenWidth, screenHeight);
    Geometry background = new Geometry("Background", backgroundQuad);

    // Material blanco
    Material backgroundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    backgroundMat.setColor("Color", ColorRGBA.White);
    background.setMaterial(backgroundMat);

    // Posicionar en (0,0) para cubrir toda la pantalla
    background.setLocalTranslation(0, 0, -1); // Z=-1 para que esté detrás de otros elementos

    startScreenNode.attachChild(background);
  }

  /** Crea el título "PONTILAND" en la pantalla. */
  private void createTitle() {
    // Crear el objeto BitmapText para el título
    BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
    titleText = new BitmapText(font, false);
    titleText.setSize(font.getCharSet().getRenderedSize() * 2); // Tamaño del texto
    titleText.setText("PONTILAND"); // Texto a mostrar
    titleText.setColor(ColorRGBA.Black); // Color negro para el texto

    // Posicionar el título en la parte superior central
    float screenWidth = camera.getWidth();
    float screenHeight = camera.getHeight();
    titleText.setLocalTranslation(
        (screenWidth - titleText.getLineWidth()) / 2, screenHeight - 100, 0);

    startScreenNode.attachChild(titleText);
  }

  /** Crea los botones clickeables para selección de jugadores. */
  private void createPlayerButtons() {
    float screenWidth = camera.getWidth();
    float screenHeight = camera.getHeight();

    // Tamaño máximo de los botones
    float maxButtonWidth = 250;
    float maxButtonHeight = 150;

    // Crear los botones primero para obtener sus tamaños reales
    button2Players = createPlayerButton("2 Jugadores.png", maxButtonWidth, maxButtonHeight);
    button2Players.setName("Button2Players");

    button3Players = createPlayerButton("3 Jugadores.png", maxButtonWidth, maxButtonHeight);
    button3Players.setName("Button3Players");

    button4Players = createPlayerButton("4 Jugadores.png", maxButtonWidth, maxButtonHeight);
    button4Players.setName("Button4Players");

    // Obtener los anchos reales de los botones
    float width2 = ((Quad) button2Players.getMesh()).getWidth();
    float width3 = ((Quad) button3Players.getMesh()).getWidth();
    float width4 = ((Quad) button4Players.getMesh()).getWidth();

    // Espaciado entre botones
    float spacing = 30;
    float totalWidth = width2 + width3 + width4 + (spacing * 2);
    float startX = (screenWidth - totalWidth) / 2;
    float buttonY = (screenHeight - maxButtonHeight) / 2 - 50;

    // Posicionar botones
    button2Players.setLocalTranslation(startX, buttonY, 0);
    startScreenNode.attachChild(button2Players);

    button3Players.setLocalTranslation(startX + width2 + spacing, buttonY, 0);
    startScreenNode.attachChild(button3Players);

    button4Players.setLocalTranslation(startX + width2 + width3 + (spacing * 2), buttonY, 0);
    startScreenNode.attachChild(button4Players);
  }

  /**
   * Crea un botón individual con el sprite correspondiente, manteniendo las proporciones
   * originales.
   *
   * @param spritePath Ruta al sprite del botón
   * @param maxWidth Ancho máximo deseado del botón
   * @param maxHeight Alto máximo deseado del botón
   * @return Geometría del botón creado
   */
  private Geometry createPlayerButton(String spritePath, float maxWidth, float maxHeight) {
    float buttonWidth = maxWidth;
    float buttonHeight = maxHeight;

    try {
      // Cargar la textura primero para obtener sus dimensiones reales
      Texture buttonTexture = assetManager.loadTexture("graphics/sprites/" + spritePath);

      // Obtener las dimensiones reales de la imagen
      int imageWidth = buttonTexture.getImage().getWidth();
      int imageHeight = buttonTexture.getImage().getHeight();

      // Calcular el aspect ratio de la imagen original
      float aspectRatio = (float) imageWidth / (float) imageHeight;

      // Ajustar las dimensiones del botón manteniendo las proporciones
      if (aspectRatio > maxWidth / maxHeight) {
        // La imagen es más ancha, ajustar por ancho
        buttonWidth = maxWidth;
        buttonHeight = maxWidth / aspectRatio;
      } else {
        // La imagen es más alta, ajustar por alto
        buttonHeight = maxHeight;
        buttonWidth = maxHeight * aspectRatio;
      }

      Quad buttonQuad = new Quad(buttonWidth, buttonHeight);
      Geometry button = new Geometry("PlayerButton", buttonQuad);

      Material buttonMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      buttonMat.setTexture("ColorMap", buttonTexture);
      button.setMaterial(buttonMat);

      return button;

    } catch (Exception e) {
      // Si falla la carga del sprite, usar color sólido como respaldo
      System.err.println("Failed to load sprite: " + spritePath + " - " + e.getMessage());

      Quad buttonQuad = new Quad(buttonWidth, buttonHeight);
      Geometry button = new Geometry("PlayerButton", buttonQuad);

      Material buttonMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      buttonMat.setColor("Color", ColorRGBA.Gray);
      button.setMaterial(buttonMat);

      return button;
    }
  }

  @Override
  public void onAction(String name, boolean isPressed, float tpf) {
    if ("MouseClick".equals(name) && !isPressed) {
      Vector2f click2d = inputManager.getCursorPosition();

      // Debug: mostrar posición del click
      System.out.println("Click detectado en posición: " + click2d);
      System.out.println("Resolución de pantalla: " + camera.getWidth() + "x" + camera.getHeight());

      // Debug: mostrar posiciones de los botones
      if (button2Players != null) {
        Vector3f pos2 = button2Players.getLocalTranslation();
        Quad quad2 = (Quad) button2Players.getMesh();
        System.out.println(
            "Botón 2J: pos=" + pos2 + " size=" + quad2.getWidth() + "x" + quad2.getHeight());
      }

      if (button3Players != null) {
        Vector3f pos3 = button3Players.getLocalTranslation();
        Quad quad3 = (Quad) button3Players.getMesh();
        System.out.println(
            "Botón 3J: pos=" + pos3 + " size=" + quad3.getWidth() + "x" + quad3.getHeight());
      }

      if (button4Players != null) {
        Vector3f pos4 = button4Players.getLocalTranslation();
        Quad quad4 = (Quad) button4Players.getMesh();
        System.out.println(
            "Botón 4J: pos=" + pos4 + " size=" + quad4.getWidth() + "x" + quad4.getHeight());
      }

      // Verificar si el click está dentro de algún botón usando coordenadas 2D
      if (isClickInsideButton(click2d, button2Players)) {
        System.out.println("¡ÉXITO! Seleccionados: 2 Jugadores");
        startGame(2);
      } else if (isClickInsideButton(click2d, button3Players)) {
        System.out.println("¡ÉXITO! Seleccionados: 3 Jugadores");
        startGame(3);
      } else if (isClickInsideButton(click2d, button4Players)) {
        System.out.println("¡ÉXITO! Seleccionados: 4 Jugadores");
        startGame(4);
      } else {
        System.out.println("Click fuera de todos los botones");
      }
    }
  }

  /**
   * Verifica si un click está dentro de las coordenadas de un botón.
   *
   * @param clickPos Posición del click en coordenadas de pantalla
   * @param button Geometría del botón a verificar
   * @return true si el click está dentro del botón
   */
  private boolean isClickInsideButton(Vector2f clickPos, Geometry button) {
    if (button == null) {
      System.out.println("Button is null!");
      return false;
    }

    Vector3f buttonPos = button.getLocalTranslation();
    Quad buttonQuad = (Quad) button.getMesh();

    float buttonLeft = buttonPos.x;
    float buttonRight = buttonPos.x + buttonQuad.getWidth();
    float buttonBottom = buttonPos.y;
    float buttonTop = buttonPos.y + buttonQuad.getHeight();

    // JME3 puede usar diferentes sistemas de coordenadas, probar ambos
    float adjustedClickY = camera.getHeight() - clickPos.y;
    float directClickY = clickPos.y;

    // Debug: mostrar cálculos de detección
    System.out.println(
        "Button bounds: left="
            + buttonLeft
            + " right="
            + buttonRight
            + " bottom="
            + buttonBottom
            + " top="
            + buttonTop);
    System.out.println(
        "Click coords: x="
            + clickPos.x
            + " y="
            + clickPos.y
            + " adjustedY="
            + adjustedClickY
            + " directY="
            + directClickY);

    // Probar con coordenadas Y ajustadas
    boolean hitAdjusted =
        clickPos.x >= buttonLeft
            && clickPos.x <= buttonRight
            && adjustedClickY >= buttonBottom
            && adjustedClickY <= buttonTop;

    // Probar con coordenadas Y directas
    boolean hitDirect =
        clickPos.x >= buttonLeft
            && clickPos.x <= buttonRight
            && directClickY >= buttonBottom
            && directClickY <= buttonTop;

    System.out.println("Hit test - adjusted: " + hitAdjusted + ", direct: " + hitDirect);

    return hitAdjusted || hitDirect;
  }

  /**
   * Inicia el juego con el número de jugadores seleccionado.
   *
   * @param playerCount Número de jugadores seleccionados
   */
  private void startGame(int playerCount) {
    // Remover la pantalla de inicio
    cleanup();

    // Iniciar el juego principal
    app.startMainGame(playerCount);
  }

  @Override
  public void cleanup() {
    super.cleanup();
    if (startScreenNode != null) {
      guiNode.detachChild(startScreenNode);
    }
    inputManager.removeListener(this);
    inputManager.deleteMapping("MouseClick");
  }
}
