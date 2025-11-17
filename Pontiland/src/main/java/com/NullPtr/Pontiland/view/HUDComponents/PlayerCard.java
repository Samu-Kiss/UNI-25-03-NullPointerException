package com.NullPtr.Pontiland.view.HUDComponents;

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

  private final Container root;
  private final Container topSpacer;
  private final Container textBox;
  private final Label nameLbl;
  private final Label moneyLbl;
  private final Label jailLbl;
  private final AssetManager assets;

  public PlayerCard(AssetManager assets) {
    this.assets = assets;
    // Root con layout vertical (2 filas): spacer arriba + caja de texto abajo
    root = new Container(new SpringGridLayout());

    topSpacer = root.addChild(new Container());
    topSpacer.setBackground(null);

    textBox = root.addChild(new Container());
    textBox.setBackground(null);
    textBox.setInsets(new Insets3f(6, 10, 6, 10));

    nameLbl = textBox.addChild(new Label("Jugador"));
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

  public void setInfo(String playerName, String moneyText, boolean inJail, int playerIndex) {
    nameLbl.setText(playerName != null ? playerName : "Jugador");
    moneyLbl.setText(moneyText != null ? moneyText : "$0");
    jailLbl.setText(inJail ? "En la cárcel" : "");

    applyBackground(inJail, playerIndex);
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
      topSpacer.setPreferredSize(new Vector3f(ws, topH, 0));
      textBox.setPreferredSize(new Vector3f(ws, hs - topH, 0));
    }
  }
}
