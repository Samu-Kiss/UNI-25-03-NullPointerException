package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.controllers.MenuController;
import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Aplicación principal de JMonkeyEngine 3 para el juego Pontiland.
 *
 * <p>Esta clase extiende SimpleApplication y maneja la inicialización completa del motor gráfico
 * 3D, incluyendo: - Configuración de ventana y renderizado - Pantalla de inicio para selección de
 * jugadores - Carga de modelos 3D del tablero - Configuración del entorno visual (cielo HDRi) -
 * Sistema de iluminación - Posicionamiento y configuración de cámara
 *
 * <p>La aplicación abre una ventana con el 75% del tamaño de la pantalla principal y está
 * optimizada para mostrar el tablero de juego con enfoque completo.
 *
 * @author Equipo de desarrollo Pontiland
 * @version 1.0
 * @since 2025
 */
public class GameApplication extends SimpleApplication {

  private MenuController menuController;

  /**
   * Método principal que inicia la aplicación del juego.
   *
   * <p>Configura los ajustes de la ventana incluyendo: - Resolución automática basada en el 75% de
   * la pantalla - Configuración de VSync y corrección gamma - Título de la ventana - Modo ventana
   * (no pantalla completa)
   *
   * @param args Argumentos de línea de comandos (no utilizados actualmente)
   */
  public static void main(String[] args) {
    GameApplication app = new GameApplication();
    AppSettings settings = new AppSettings(true);

    // Determinar el 75% del tamaño de pantalla (monitor principal)
    try {
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      int width = (int) (screen.width * 0.75);
      int height = (int) (screen.height * 0.75);
      settings.setResolution(width, height);
    } catch (HeadlessException e) {
      // Respaldo si AWT está en modo headless (ej. entorno CI)
      settings.setResolution(1280, 720);
    }

    settings.setTitle("Pontiland");
    settings.setVSync(true);
    settings.setFullscreen(false);
    settings.setGammaCorrection(true);
    app.setShowSettings(false); // Omitir diálogo de configuración predeterminado
    app.setSettings(settings);
    app.start();
  }

  /**
   * Método de inicialización principal de la aplicación JMonkeyEngine.
   *
   * <p>Este método se ejecuta automáticamente cuando la aplicación inicia y muestra primero la
   * pantalla de selección de jugadores. Una vez seleccionado el número de jugadores, se procede a
   * cargar la escena 3D del juego.
   */
  @Override
  public void simpleInitApp() {
    // Inicializar controlador de menús y mostrar pantalla de inicio
    this.menuController = new MenuController(this);
    // Inicializar la pantalla de inicio
    menuController.showStartScreen();
  }

  // ========== Accesores de controlador ==========
  public MenuController getMenuController() {
    return menuController;
  }

  // ========== Delegaciones para compatibilidad con vistas ==========
  /** Muestra la pantalla de inicio para selección de jugadores. */
  private void showStartScreen() {
    if (menuController != null) menuController.showStartScreen();
  }

  /** Inicia el juego principal con el número de jugadores seleccionado. */
  public void startMainGame(int playerCount) {
    if (menuController != null) menuController.startMainGame(playerCount);
  }

  /** Obtiene el número de jugadores seleccionado. */
  public int getSelectedPlayerCount() {
    return menuController != null ? menuController.getSelectedPlayerCount() : 0;
  }

  /** Verifica si el juego ya ha sido iniciado. */
  public boolean isGameStarted() {
    return menuController != null && menuController.isGameStarted();
  }

  /** Carga una partida guardada abriendo el menú de carga. */
  public void loadSavedGame() {
    if (menuController != null) menuController.loadSavedGame();
  }

  /** Muestra el menú de carga con la lista de partidas y un callback al seleccionar. */
  public void showLoadMenu(List<MenuCarga.SavedGame> saves, Consumer<String> onSelect) {
    if (menuController != null) menuController.showLoadMenu(saves, onSelect);
  }

  /** Inicia la selección de jugadores. */
  public void startPlayerSelection() {
    if (menuController != null) menuController.startPlayerSelection();
  }

  /** Muestra la pantalla de créditos. */
  public void showCredits() {
    if (menuController != null) menuController.showCredits();
  }

  /** Muestra la pantalla de créditos con repo. */
  public void showCredits(String owner, String repo, int limit) {
    if (menuController != null) menuController.showCredits(owner, repo, limit);
  }

  /** Vuelve al menú principal desde cualquier estado. */
  public void goToMainMenu() {
    if (menuController != null) menuController.goToMainMenu();
  }

  // ========== Inicialización de escena 3D ==========
  /**
   * Inicializa la escena 3D del juego.
   *
   * <p>Secuencia de inicialización: 1. Carga del modelo 3D del tablero 2. Configuración del entorno
   * y cielo 3. Configuración del sistema de iluminación 4. Posicionamiento y configuración de la
   * cámara
   */
  public void initializeGame3D() {
    loadBoardModel();
    setupSkyEnvironment();
    setupLighting();
    setupCamera();
  }

  /**
   * Carga el modelo 3D del tablero de juego desde los recursos.
   *
   * <p>Intenta cargar el archivo Board.glb desde la ruta de recursos. Si la carga falla (archivo no
   * encontrado, formato incorrecto, etc.), crea automáticamente un cubo rojo como objeto de
   * respaldo para asegurar que la escena no esté vacía durante el desarrollo.
   *
   * <p>Ruta del archivo: graphics/models/Board.glb (relativa a src/main/resources)
   */
  private void loadBoardModel() {
    // Cargar modelo del tablero (GLB). La ruta es relativa al classpath (src/main/resources)
    // Ajustar la ruta si posteriormente se reorganizan los assets.
    try {
      Spatial board = assetManager.loadModel("graphics/models/Board.glb");
      rootNode.attachChild(board);
    } catch (Exception ex) {
      // Cubo de prueba de respaldo si el modelo falla al cargar para que la escena no esté vacía.
      Box b = new Box(1, 1, 1);
      Geometry geom = new Geometry("FallbackBox", b);
      Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      mat.setColor("Color", ColorRGBA.Red);
      geom.setMaterial(mat);
      rootNode.attachChild(geom);
    }
  }

  /**
   * Configura el entorno visual y cielo de la escena usando textura HDRi.
   *
   * <p>Carga una textura HDR en formato equirectangular para crear un cielo realista con
   * iluminación ambiental. La textura HDRi proporciona: - Fondo visual realista para la escena -
   * Iluminación ambiental natural - Reflexiones ambientales en materiales
   *
   * <p>Si la carga de la textura HDR falla, la escena continuará sin cielo (fondo negro) pero sin
   * errores críticos.
   *
   * <p>Archivo utilizado: kloofendal_48d_partly_cloudy_puresky_4k.hdr
   */
  private void setupSkyEnvironment() {
    // Cargar textura del entorno HDRi y crear un cielo (mapa equirectangular)
    try {
      Texture hdr =
          assetManager.loadTexture("graphics/HDRi/kloofendal_48d_partly_cloudy_puresky_4k.hdr");
      // Crear cielo desde HDR equirectangular (JME auto-detecta en 3.8 via EnvMapType)
      rootNode.attachChild(
          SkyFactory.createSky(assetManager, hdr, SkyFactory.EnvMapType.EquirectMap));
    } catch (Exception ex) {
      // Si HDR falla, dejar cielo vacío; no se necesita fallo crítico aquí.
      System.err.println("Failed to load HDRi sky: " + ex.getMessage());
    }
  }

  /**
   * Configura el sistema de iluminación básico de la escena.
   *
   * <p>Establece dos tipos de luces esenciales:
   *
   * <p>1. Luz direccional (sol): Simula la luz solar con dirección específica - Color: Blanco puro
   * - Dirección: (-1, -2, -1) normalizada (desde arriba-derecha-atrás)
   *
   * <p>2. Luz ambiental: Proporciona iluminación suave uniforme - Color: Blanco al 25% de
   * intensidad - Evita sombras completamente negras
   *
   * <p>Esta combinación asegura que todos los objetos sean visibles con buena definición de formas
   * y profundidad.
   */
  private void setupLighting() {
    // Configuración básica de iluminación
    DirectionalLight sun = new DirectionalLight();
    sun.setColor(ColorRGBA.White);
    sun.setDirection(new Vector3f(-1f, -2f, -1f).normalizeLocal());
    rootNode.addLight(sun);

    AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White.mult(0.25f));
    rootNode.addLight(ambient);
  }

  /**
   * Posiciona y configura la cámara para visualización óptima del tablero.
   *
   * <p>Configuración de la cámara: - Posición: (6, 6, 10) - Vista isométrica desde arriba-atrás -
   * Objetivo: Centro de la escena (0, 0, 0) - Vector up: Eje Y hacia arriba - Velocidad de
   * movimiento: 20 unidades/segundo
   *
   * <p>Configuración del frustum (campo de visión): - Campo de visión: 60 grados (más amplio para
   * mejor cobertura) - Plano cercano: 0.01 (objetos muy cercanos siguen visibles) - Plano lejano:
   * 500 (rango optimizado para el tamaño de la escena)
   *
   * <p>Estos ajustes aseguran que todos los objetos permanezcan enfocados simultáneamente, evitando
   * problemas de enfoque por distancia.
   */
  private void setupCamera() {
    // Posicionar la cámara para ver el tablero
    cam.setLocation(new Vector3f(6, 6, 10));
    cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    flyCam.setMoveSpeed(20f);

    // Configuraciones mejoradas de recorte de cámara para mejor rango de enfoque
    // Campo de visión más amplio y planos cercano/lejano optimizados para mantener todo enfocado
    cam.setFrustumPerspective(60f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 500f);
  }
}
