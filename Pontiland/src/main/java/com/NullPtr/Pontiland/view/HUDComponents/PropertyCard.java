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
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;

/**
 * Tarjeta de propiedad con sprite de fondo por grupo. Nota: el archivo se llama ProperyCard por
 * tipografía en el repo.
 */
public class PropertyCard {

  private static final float SPRITE_SCALE = 0.60f;

  private final Container root;
  private final Container header; // encabezado superior
  private final Container content; // contenido central
  private final Label nameLbl;
  private final Container rentsContainer; // lista de rentas
  private final AssetManager assets;

  // Layout de rentas configurable
  private float rentsTopMargin = 12f;
  private float rentsRightPadding = 24f; // despegar del borde derecho
  private float rentLineSpacing = 0f; // espacio mínimo entre líneas

  // Control fino por nivel (offset superior adicional por etiqueta)
  public float nivel1YPos = 25f;
  public float nivel2YPos = 0f;
  public float nivel3YPos = 0f;
  public float nivel4YPos = -3f;
  public float nivel5YPos = -5f;

  private String[] lastRents;
  private int groupIndex = 1;

  public PropertyCard(AssetManager assets) {
    this.assets = assets;
    root = new Container(new BorderLayout());
    root.setInsets(new Insets3f(14, 16, 14, 16));

    nameLbl = new Label("Propiedad");
    nameLbl.setFontSize(18);
    nameLbl.setTextHAlignment(HAlignment.Center);
    nameLbl.setColor(ColorRGBA.Black);

    header = new Container();
    header.setInsets(new Insets3f(12, 0, 6, 0));
    header.addChild(nameLbl);
    root.addChild(header, BorderLayout.Position.North);

    content = new Container(new BorderLayout());

    rentsContainer = new Container();
    rentsContainer.setInsets(new Insets3f(rentsTopMargin, 4, 0, rentsRightPadding));
    content.addChild(rentsContainer, BorderLayout.Position.East);

    root.addChild(content, BorderLayout.Position.Center);

    applyBackground();
  }

  public Container getRoot() {
    return root;
  }

  public Vector3f getPreferredSize() {
    return root.getPreferredSize();
  }

  public void setInfo(String name, String priceText, String[] rentsText) {
    if (name != null) {
      nameLbl.setText(name);
    }
    if (rentsText != null) {
      this.lastRents = rentsText;
      renderRents();
    }
  }

  public void setRentsLayout(float topMargin, float rightPadding, float lineSpacing) {
    this.rentsTopMargin = Math.max(0f, topMargin);
    this.rentsRightPadding = Math.max(0f, rightPadding);
    this.rentLineSpacing = Math.max(0f, lineSpacing);
    rentsContainer.setInsets(new Insets3f(rentsTopMargin, 4, 0, rentsRightPadding));
    renderRents();
  }

  private void renderRents() {
    rentsContainer.detachAllChildren();
    if (lastRents == null || lastRents.length == 0) {
      Label dash = rentsContainer.addChild(new Label("-"));
      dash.setColor(ColorRGBA.Black);
      dash.setTextHAlignment(HAlignment.Right);
      return;
    }

    for (int i = 0; i < lastRents.length && i < 5; i++) {
      String text = lastRents[i];
      Label line = rentsContainer.addChild(new Label(text != null ? text : "-"));
      line.setColor(ColorRGBA.Black);
      line.setTextHAlignment(HAlignment.Right);

      float manual;
      switch (i) {
        case 0:
          manual = nivel1YPos;
          break;
        case 1:
          manual = nivel2YPos;
          break;
        case 2:
          manual = nivel3YPos;
          break;
        case 3:
          manual = nivel4YPos;
          break;
        case 4:
          manual = nivel5YPos;
          break;
        default:
          manual = 0f;
      }

      line.setInsets(new Insets3f((i == 0 ? 0f : rentLineSpacing) + manual, 0, 0, 0));
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
