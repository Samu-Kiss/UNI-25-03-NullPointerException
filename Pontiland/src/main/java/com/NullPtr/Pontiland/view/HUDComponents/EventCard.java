package com.NullPtr.Pontiland.view.HUDComponents;

import com.NullPtr.Pontiland.view.Button;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;

/** Componente simple para mostrar una carta de evento (bueno o malo) con un botón de cerrar. */
public class EventCard {

  private static final float SPRITE_SCALE = 0.80f;

  public enum Type {
    POSITIVE("graphics/sprites/HUD/Event_Cards/Positive.png"),
    NEGATIVE("graphics/sprites/HUD/Event_Cards/Negative.png");

    private final String path;

    Type(String path) {
      this.path = path;
    }

    public String getPath() {
      return path;
    }
  }

  private final Container root;
  private final Label title;
  private final Label description;
  private final com.simsilica.lemur.Button closeBtn;
  private final AssetManager assets;

  // TODO Revisar tamaños y fuentes
  // (El boton está muy grande)
  public EventCard(AssetManager assets) {
    this.assets = assets;
    root = new Container();
    root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0f, 0f, 0f, 0.7f)));

    // Header: título centrado
    Container header = root.addChild(new Container());
    header.setBackground(null);
    title = header.addChild(new Label(""));
    title.setLocalTranslation(0, -2, 0);
    title.setFontSize(24f);
    title.setColor(ColorRGBA.Black);
    title.setTextHAlignment(com.simsilica.lemur.HAlignment.Center);

    // Body: descripción
    Container body = root.addChild(new Container());
    body.setBackground(null);
    description = body.addChild(new Label(""));
    description.setFontSize(26f);
    description.setColor(ColorRGBA.Black);
    description.setTextHAlignment(com.simsilica.lemur.HAlignment.Center);

    com.NullPtr.Pontiland.view.Button renderer =
        new com.NullPtr.Pontiland.view.Button(assets).setDefaultFontSize(24f).setHoverScale(1.04f);

    // Footer: botón de cerrado, centrado abajo
    Container footer = root.addChild(new Container());
    footer.setBackground(null);
    closeBtn = renderer.render(Button.Type.ACCENT, "Cerrar", 1f, Button.Variant.MAIN);
    footer.addChild(closeBtn);

    // Layout spacing (valor por defecto si no se cargan sprites)
    root.setPreferredSize(new Vector3f(100f, 120f, 0f));
  }

  public Container getRoot() {
    return root;
  }

  public void setInfo(String t, String desc) {
    title.setText(t == null ? "" : t);
    description.setText(desc == null ? "" : desc);
  }

  // Cerrar la ventana
  public void setCloseCommand(Runnable action) {
    if (closeBtn != null && action != null) {
      closeBtn.addClickCommands(src -> action.run());
    }
  }

  /*
   * Establece el tipo de carta (POSITIVE/NEGATIVE) y carga el sprite correspondiente desde
   * resources. Si falla, mantiene el fondo de color.
   */
  public void setType(Type type) {
    if (type == null || assets == null) return;
    String path = type.getPath();
    try {
      TextureKey key = new TextureKey(path, true);
      key.setGenerateMips(false);
      Texture2D tex = (Texture2D) assets.loadTexture(key);
      tex.setWrap(com.jme3.texture.Texture.WrapMode.EdgeClamp);
      tex.setMagFilter(com.jme3.texture.Texture.MagFilter.Bilinear);
      tex.setMinFilter(com.jme3.texture.Texture.MinFilter.BilinearNoMipMaps);

      root.setBackground(new QuadBackgroundComponent(tex));

      int w = tex.getImage().getWidth();
      int h = tex.getImage().getHeight();
      root.setPreferredSize(new Vector3f(w * SPRITE_SCALE, h * SPRITE_SCALE, 0f));
    } catch (Exception ex) {
      // Si falla la carga de la textura, dejar el fondo semitransparente por defecto
      root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0f, 0f, 0f, 0.7f)));
    }
  }
}
