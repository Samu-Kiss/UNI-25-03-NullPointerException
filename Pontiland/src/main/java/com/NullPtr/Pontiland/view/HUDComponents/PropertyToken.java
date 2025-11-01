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

/** Zona inferior de tokens de propiedades del jugador activo con sprite de fondo. */
public class PropertyToken {

  private static final float SPRITE_SCALE = 0.55f;

  private final Container root;
  private final Label tokensLbl;
  private final AssetManager assets;
  private int groupIndex = 1;

  public PropertyToken(AssetManager assets) {
    this.assets = assets;
    root = new Container();
    root.setInsets(new Insets3f(10, 14, 10, 14));

    tokensLbl = root.addChild(new Label("Tokens: -"));

    applyBackground();
  }

  public Container getRoot() {
    return root;
  }

  public Vector3f getPreferredSize() {
    return root.getPreferredSize();
  }

  public void setTokens(String[] tokens) {
    if (tokens == null || tokens.length == 0) {
      tokensLbl.setText("Tokens: -");
      return;
    }
    StringBuilder sb = new StringBuilder("Tokens: ");
    for (int i = 0; i < tokens.length; i++) {
      if (i > 0) sb.append(" | ");
      sb.append(tokens[i]);
    }
    tokensLbl.setText(sb.toString());
  }

  public void setGroup(int groupIndex) {
    this.groupIndex = Math.max(1, Math.min(8, groupIndex));
    applyBackground();
  }

  private void applyBackground() {
    // Nota: carpeta con tipo ortográfico "Propery_Tokens"
    String path = "graphics/sprites/HUD/Propery_Tokens/Group_" + this.groupIndex + ".png";
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
      root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.08f, 0.10f, 0.13f, 0.85f)));
    }
  }
}
