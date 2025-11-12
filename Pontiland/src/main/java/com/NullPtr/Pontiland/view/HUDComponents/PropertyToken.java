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
import com.simsilica.lemur.Axis;

/** Zona inferior de tokens de propiedades del jugador activo con sprite de fondo. */
public class PropertyToken {

  private static final float SPRITE_SCALE = 0.55f;

  private final Container root;
  private final AssetManager assets;

  public PropertyToken(AssetManager assets) {
    this.assets = assets;
    // Layout horizontal: recorre columnas primero (Axis.X) y luego filas (Axis.Y)
    root = new Container(new SpringGridLayout(Axis.X, Axis.Y));
    root.setInsets(new Insets3f(10, 14, 10, 14));

    // default placeholder
    Label placeholder = root.addChild(new Label("Tokens: -"));
    placeholder.setName("__propertytoken_placeholder");

    // initial background
    root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.08f, 0.10f, 0.13f, 0.85f)));
  }

  public Container getRoot() {
    return root;
  }

  public Vector3f getPreferredSize() {
    return root.getPreferredSize();
  }

  /**
   * Tokens expected encoded as "propId|nivel|grupo" for each element. If group is missing uses 1.
   * The label will show first line = propId and second line = nivel.
   */
  public void setTokens(String[] tokens) {
    // clear existing children but preserve insets
    root.detachAllChildren();
    root.setInsets(new Insets3f(10, 14, 10, 14));

    if (tokens == null || tokens.length == 0) {
      Label placeholder = root.addChild(new Label("Tokens: -"));
      placeholder.setName("__propertytoken_placeholder");
      root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.08f, 0.10f, 0.13f, 0.85f)));
      return;
    }

    boolean anyAdded = false;
    for (int i = 0; i < tokens.length; i++) {
      String tok = tokens[i];
      if (tok == null || tok.isBlank()) continue;

      String[] parts = tok.split("\\|");
      String propNum = parts.length > 0 ? parts[0] : "?";
      String nivel = parts.length > 1 ? parts[1] : "?";
      int groupIdx = 1;
      try {
        if (parts.length > 2) groupIdx = Integer.parseInt(parts[2]);
      } catch (NumberFormatException e) {
        groupIdx = 1;
      }

      Container tokenBox = new Container();
      tokenBox.setInsets(new Insets3f(6, 10, 6, 10));

      // Try to apply group background (image). If fails, use colored background.
      String path =
          "graphics/sprites/HUD/Propery_Tokens/Group_"
              + Math.max(1, Math.min(8, groupIdx))
              + ".png";
      try {
        TextureKey key = new TextureKey(path, true);
        key.setGenerateMips(false);
        Texture2D tex = (Texture2D) assets.loadTexture(key);
        tex.setWrap(Texture.WrapMode.EdgeClamp);
        tex.setMagFilter(Texture.MagFilter.Bilinear);
        tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

        tokenBox.setBackground(new QuadBackgroundComponent(tex));

        int w = tex.getImage().getWidth();
        int h = tex.getImage().getHeight();
        tokenBox.setPreferredSize(new Vector3f(w * SPRITE_SCALE, h * SPRITE_SCALE, 0));
      } catch (Exception e) {
        // fallback color background
        tokenBox.setBackground(
            new QuadBackgroundComponent(new ColorRGBA(0.12f, 0.14f, 0.18f, 0.95f)));
      }

      Label lbl = tokenBox.addChild(new Label("#" + propNum + "\nNv. " + nivel));
      lbl.setTextHAlignment(HAlignment.Center);

      root.addChild(tokenBox);

      if (i < tokens.length - 1) {
        Container spacer = new Container();
        spacer.setBackground(null);
        spacer.setPreferredSize(new Vector3f(8f, tokenBox.getPreferredSize().y, 0f));
        root.addChild(spacer);
      }

      anyAdded = true;
    }

    if (!anyAdded) {
      Label placeholder = root.addChild(new Label("Tokens: -"));
      placeholder.setName("__propertytoken_placeholder");
      root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.08f, 0.10f, 0.13f, 0.85f)));
    }
  }

  /** Backwards-compatible method: set a single background group for the whole tokens container. */
  public void setGroup(int groupIndex) {
    int g = Math.max(1, Math.min(8, groupIndex));
    String path = "graphics/sprites/HUD/Propery_Tokens/Group_" + g + ".png";
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
