package com.NullPtr.Pontiland.view.HUDComponents;

import com.NullPtr.Pontiland.entities.Jugador;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;

public class PlayerCard {

  // 50% más grande que antes
  private static final float SPRITE_SCALE = 0.825f;
  // Proporción aproximada del área superior (color) del sprite; el resto es gris inferior
  private static final float TOP_AREA_RATIO = 0.60f; // ajustar si hace falta 0.60..0.62

  // Escala del icono del jugador respecto al área superior (topSpacer). Ajustable.
  private static final float ICON_SCALE_FACTOR = 1f;

  // Mapeo de iconoId (1-7) al nombre del sprite en graphics/sprites/Fichas/
  private static final String[] ICON_NAMES = {
    "Kiwi", "Balon", "Maleta", "Pescadito", /*"Carnet",*/ "Ignacito", "Nave"
  };

  // Texto por defecto para el nombre del jugador
  private static final String DEFAULT_PLAYER_NAME = "Jugador";

  private final Container root;
  private final Container topSpacer;
  private final Container iconContainer; // contenedor para el icono del jugador superpuesto
  private final Container textBox;
  private final Label nameLbl;
  private final Label moneyLbl;
  private final Label jailLbl;
  private final AssetManager assets;

  // Dimensiones actuales del topSpacer para posicionar el icono
  private float currentTopW = 0f;
  private float currentTopH = 0f;

  public PlayerCard(AssetManager assets) {
    this.assets = assets;
    // Root con layout vertical (2 filas): spacer arriba + caja de texto abajo
    root = new Container(new SpringGridLayout());

    topSpacer = root.addChild(new Container());
    topSpacer.setBackground(null);

    // Contenedor del icono, se añade como hijo de topSpacer para superposición
    iconContainer = topSpacer.addChild(new Container());
    iconContainer.setBackground(null);

    textBox = root.addChild(new Container());
    textBox.setBackground(null);
    textBox.setInsets(new Insets3f(6, 10, 6, 10));

    nameLbl = textBox.addChild(new Label(DEFAULT_PLAYER_NAME));
    nameLbl.setFontSize(18);
    nameLbl.setColor(ColorRGBA.Black);
    nameLbl.setTextHAlignment(HAlignment.Center);

    moneyLbl = textBox.addChild(new Label("$0"));
    moneyLbl.setFontSize(16);
    moneyLbl.setColor(ColorRGBA.Black);
    moneyLbl.setTextHAlignment(HAlignment.Center);

    jailLbl = textBox.addChild(new Label(""));
    jailLbl.setFontSize(14);
    jailLbl.setColor(ColorRGBA.Black);
    jailLbl.setTextHAlignment(HAlignment.Center);

    applyBackground(false, 1);
  }

  public Container getRoot() {
    return root;
  }

  public Vector3f getPreferredSize() {
    return root.getPreferredSize();
  }

  /**
   * Actualiza la información mostrada en la tarjeta del jugador.
   *
   * @param jugador entidad Jugador con todos los datos (nombre, dinero, enCarcel, iconoId, etc.)
   * @param playerIndex índice del jugador (1-4) para determinar el color de fondo
   */
  public void setInfo(Jugador jugador, int playerIndex) {
    String playerName = jugador != null ? jugador.getNombreJugador() : DEFAULT_PLAYER_NAME;
    String moneyText = jugador != null ? "$" + jugador.getDinero() : "$0";
    boolean inJail = jugador != null && jugador.getEstado();
    int iconoId = jugador != null ? jugador.getIconoId() : -1;

    nameLbl.setText(playerName);
    moneyLbl.setText(moneyText);
    jailLbl.setText(inJail ? "En la cárcel" : "");

    applyBackground(inJail, playerIndex);
    applyPlayerIcon(iconoId);
  }

  /**
   * Aplica el icono del jugador superpuesto y centrado en la zona superior (topSpacer).
   *
   * @param iconoId identificador del icono (1-7), valores fuera de rango no muestran icono
   */
  private void applyPlayerIcon(int iconoId) {
    // Limpiar icono previo
    iconContainer.setBackground(null);

    if (iconoId < 1 || iconoId > ICON_NAMES.length) {
      return;
    }

    String iconName = ICON_NAMES[iconoId - 1];
    String path = "graphics/sprites/Fichas/" + iconName + ".png";

    TextureKey key = new TextureKey(path, true);
    key.setGenerateMips(false);
    Texture2D tex = (Texture2D) assets.loadTexture(key);
    tex.setWrap(Texture.WrapMode.EdgeClamp);
    tex.setMagFilter(Texture.MagFilter.Bilinear);
    tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

    iconContainer.setBackground(new QuadBackgroundComponent(tex));

    // Calcular tamaño del icono escalado al ICON_SCALE_FACTOR del área superior
    int texW = tex.getImage().getWidth();
    int texH = tex.getImage().getHeight();
    float aspectRatio = (float) texW / texH;

    // Escalar para que quepa dentro del área disponible manteniendo aspect ratio
    float maxW = currentTopW * ICON_SCALE_FACTOR;
    float maxH = currentTopH * ICON_SCALE_FACTOR;

    float iconW;
    float iconH;
    if (maxW / aspectRatio <= maxH) {
      iconW = maxW;
      iconH = maxW / aspectRatio;
    } else {
      iconH = maxH;
      iconW = maxH * aspectRatio;
    }

    iconContainer.setPreferredSize(new Vector3f(iconW, iconH, 0));

    // Centrar el icono dentro del topSpacer usando insets
    float padX = (currentTopW - iconW) / 2f;
    float padY = (currentTopH - iconH) / 2f;
    iconContainer.setInsets(new Insets3f(padY, padX, padY, padX));
  }

  private void applyBackground(boolean inJail, int playerIndex) {
    String path;
    if (inJail) {
      path = "graphics/sprites/HUD/Player_Cards/Player_Jail.png";
    } else {
      int p = playerIndex <= 0 ? 1 : playerIndex;
      int idx = ((p - 1) % 4) + 1; // 1..4
      path = "graphics/sprites/HUD/Player_Cards/Player_" + idx + ".png";
    }

    try {
      TextureKey key = new TextureKey(path, true);
      key.setGenerateMips(false);
      Texture2D tex = (Texture2D) assets.loadTexture(key);
      tex.setWrap(Texture.WrapMode.EdgeClamp);
      tex.setMagFilter(Texture.MagFilter.Bilinear);
      tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

      // Fondo exacto al root, sin deformarlo
      root.setBackground(new QuadBackgroundComponent(tex));

      int w = tex.getImage().getWidth();
      int h = tex.getImage().getHeight();
      float ws = w * SPRITE_SCALE;
      float hs = h * SPRITE_SCALE;
      root.setPreferredSize(new Vector3f(ws, hs, 0));

      // Alturas fijas de filas: parte superior (color) y parte inferior (gris)
      float topH = hs * TOP_AREA_RATIO;
      float bottomH = hs - topH;

      // Guardar dimensiones del topSpacer para posicionar el icono
      currentTopW = ws;
      currentTopH = topH;

      topSpacer.setPreferredSize(new Vector3f(ws, topH, 0));
      textBox.setPreferredSize(new Vector3f(ws, bottomH, 0));

      // Ajustar ancho de los labels al ancho disponible (menos padding del textBox)
      float sidePad = 10f; // coincide con textBox.setInsets(6, 10, 6, 10)
      float labelW = Math.max(10f, ws - 2f * sidePad);
      nameLbl.setPreferredSize(new Vector3f(labelW, nameLbl.getPreferredSize().y, 0));
      moneyLbl.setPreferredSize(new Vector3f(labelW, moneyLbl.getPreferredSize().y, 0));
      jailLbl.setPreferredSize(new Vector3f(labelW, jailLbl.getPreferredSize().y, 0));

    } catch (Exception e) {
      root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.12f, 0.14f, 0.18f, 0.85f)));
      // Tamaño fallback razonable
      float ws = 220f;
      float hs = 260f;
      root.setPreferredSize(new Vector3f(ws, hs, 0));
      float topH = hs * TOP_AREA_RATIO;

      // Guardar dimensiones del topSpacer para posicionar el icono
      currentTopW = ws;
      currentTopH = topH;

      topSpacer.setPreferredSize(new Vector3f(ws, topH, 0));
      textBox.setPreferredSize(new Vector3f(ws, hs - topH, 0));
    }
  }
}
