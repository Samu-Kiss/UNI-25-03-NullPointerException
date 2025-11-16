package com.NullPtr.Pontiland.view.HUDComponents;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;

/** Zona inferior de tokens de propiedades del jugador activo con sprite de fondo. */
public class PropertyToken {

  private static final float SPRITE_SCALE = 0.55f;
  // extracted constants to avoid duplicated-literal warnings
  private static final String PLACEHOLDER_TEXT = "Tokens: -";
  private static final String PLACEHOLDER_NAME = "__propertytoken_placeholder";

  private final Container root;
  private final AssetManager assets;
  // current global group used for the container background / placeholder contrast
  private int currentGroup = 1;

  public PropertyToken(AssetManager assets) {
    this.assets = assets;
    // Layout horizontal: recorre columnas primero (Axis.X) y luego filas (Axis.Y)
    root = new Container(new SpringGridLayout(Axis.X, Axis.Y));
    root.setInsets(new Insets3f(10, 14, 10, 14));

    // default placeholder
    Label placeholder = root.addChild(new Label(PLACEHOLDER_TEXT));
    placeholder.setName(PLACEHOLDER_NAME);
    // placeholder contrast color based on initial group
    placeholder.setColor(com.NullPtr.Pontiland.view.HUD.getContrastingColorForGroup(currentGroup));

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
   *
   * @param tokens array de tokens en formato "propId|nivel|grupo"; si es {@code null} o vacío se
   *     mostrará un placeholder
   */
  public void setTokens(String[] tokens) {
    // clear existing children but preserve insets
    root.detachAllChildren();
    root.setInsets(new Insets3f(10, 14, 10, 14));

    if (tokens == null || tokens.length == 0) {
      Label placeholder = root.addChild(new Label(PLACEHOLDER_TEXT));
      placeholder.setName(PLACEHOLDER_NAME);
      // use currentGroup to choose placeholder color for proper contrast
      placeholder.setColor(
          com.NullPtr.Pontiland.view.HUD.getContrastingColorForGroup(currentGroup));
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
        if (parts.length > 2) groupIdx = Integer.parseInt(parts[2]);

      Container tokenBox = new Container();
      tokenBox.setInsets(new Insets3f(6, 10, 6, 10));

      // Try to apply group background (image). If fails, use colored background.
      String path =
          "graphics/sprites/HUD/Propery_Tokens/Group_"
              + Math.max(1, Math.min(8, groupIdx))
              + ".png";

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


      // Create label and set contrast color based on the token's group
      Label lbl = tokenBox.addChild(new Label("\n#" + propNum + "\nNv. " + nivel));
      lbl.setTextHAlignment(HAlignment.Center);
      lbl.setColor(com.NullPtr.Pontiland.view.HUD.getContrastingColorForGroup(groupIdx));

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
      Label placeholder = root.addChild(new Label(PLACEHOLDER_TEXT));
      placeholder.setName(PLACEHOLDER_NAME);
      placeholder.setColor(
          com.NullPtr.Pontiland.view.HUD.getContrastingColorForGroup(currentGroup));
      root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.08f, 0.10f, 0.13f, 0.85f)));
    }
  }

  /**
   * Establece el grupo de fondo que se usará para el contenedor de tokens.
   *
   * @param groupIndex índice del grupo (1-8), valores fuera de rango son normalizados.
   */
  public void setGroup(int groupIndex) {
    int g = Math.max(1, Math.min(8, groupIndex));
    this.currentGroup = g;
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
      // update placeholder color (if present) to match new group contrast
      if (root.getChild(0) != null && root.getChild(0) instanceof Label) {
        Label l = (Label) root.getChild(0);
        if (PLACEHOLDER_NAME.equals(l.getName())) {
          l.setColor(com.NullPtr.Pontiland.view.HUD.getContrastingColorForGroup(g));
        }
      }
    } catch (Exception e) {
      root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.08f, 0.10f, 0.13f, 0.85f)));
    }
  }
}
