package com.NullPtr.Pontiland.view.HUDComponents;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;

/**
 * Tarjeta de propiedad con sprite de fondo por grupo. Nota: el archivo se llama ProperyCard por
 * tipografía en el repo.
 */
public class ProperyCard {

  private static final float SPRITE_SCALE = 0.60f;

  private final Container root;
  private final Label nameLbl;
  private final Label priceLbl;
  private final Label rentsLbl;
  private final AssetManager assets;

  private int groupIndex = 1;

  public ProperyCard(AssetManager assets) {
    this.assets = assets;
    root = new Container();
    root.setInsets(new Insets3f(14, 16, 14, 16));

    nameLbl = root.addChild(new Label("Propiedad"));
    nameLbl.setFontSize(18);
    priceLbl = root.addChild(new Label("Precio: $0"));
    rentsLbl = root.addChild(new Label("Rentas: -"));

    applyBackground();
  }

  public Container getRoot() {
    return root;
  }

  public Vector3f getPreferredSize() {
    return root.getPreferredSize();
  }

  public void setInfo(String name, String priceText, String[] rentsText) {
    nameLbl.setText(name != null ? name : "Propiedad");
    priceLbl.setText(priceText != null ? priceText : "Precio: $0");
    if (rentsText == null || rentsText.length == 0) {
      rentsLbl.setText("Rentas: -");
    } else {
      StringBuilder sb = new StringBuilder("Rentas: ");
      for (int i = 0; i < rentsText.length; i++) {
        if (i > 0) sb.append(", ");
        sb.append(rentsText[i]);
      }
      rentsLbl.setText(sb.toString());
    }
  }

  public void setGroup(int groupIndex) {
    this.groupIndex = Math.max(1, Math.min(8, groupIndex));
    applyBackground();
  }

  private void applyBackground() {
    String path = "graphics/sprites/HUD/Property_Cards/Group_" + this.groupIndex + ".png";
    try {
      TextureKey key = new TextureKey(path, true);
      key.setGenerateMips(false);
      Texture2D tex = (Texture2D) assets.loadTexture(key);
      tex.setWrap(Texture.WrapMode.EdgeClamp);
      tex.setMagFilter(Texture.MagFilter.Bilinear);
      tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

      root.setBackground(new QuadBackgroundComponent(tex));

      int w = tex.getImage().getWidth();
      int h = tex.getImage().getHeight();
      root.setPreferredSize(new Vector3f(w * SPRITE_SCALE, h * SPRITE_SCALE, 0));
    } catch (Exception e) {
      root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.10f, 0.12f, 0.16f, 0.85f)));
    }
  }
}
