package com.NullPtr.Pontiland.view.HUDComponents;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;

/** Overlay de subastas con sprites. */
public class Auction {

  private static final float SPRITE_SCALE = 0.62f;

  private final Container root;
  private final Label priceTitleLbl;
  private final Label priceValueLbl;
  private final Button plus20Btn;
  private final Button plus50Btn;
  private final Button plus100Btn;
  private final Button exitBtn;
  private final AssetManager assets;

  public Auction(AssetManager assets) {
    this.assets = assets;
    root = new Container();

    // Bloque superior para labels con padding extra que los baja visualmente
    Container priceBlock = root.addChild(new Container());
    priceBlock.setBackground(null);
    priceBlock.setInsets(new Insets3f(96f, 12f, 12f, 12f));

    // Linea 1: "Precio Actual:" centrado y negro
    priceTitleLbl = priceBlock.addChild(new Label("Precio Actual:"));
    priceTitleLbl.setColor(ColorRGBA.Black);
    priceTitleLbl.setTextHAlignment(HAlignment.Center);
    priceTitleLbl.setInsets(new Insets3f(0f, 0f, 2f, 0f));

    // Linea 2: valor actual centrado y negro
    priceValueLbl = priceBlock.addChild(new Label("$0"));
    priceValueLbl.setColor(ColorRGBA.Black);
    priceValueLbl.setTextHAlignment(HAlignment.Center);
    priceValueLbl.setFontSize(28f);
    priceValueLbl.setInsets(new Insets3f(0f, 0f, 6f, 0f));

    // Separador vertical pequeño
    Container vspace1 = new Container();
    vspace1.setBackground(null);
    vspace1.setPreferredSize(new Vector3f(0, 8f, 0));
    root.addChild(vspace1);

    // Fila de tres botones de incremento, uno al lado del otro
    Container row =
        root.addChild(
            new Container(
                new SpringGridLayout(com.simsilica.lemur.Axis.X, com.simsilica.lemur.Axis.Y)));
    row.setBackground(null);

    // Usar el renderizador compartido para mantener proporcion y hover desde el centro
    com.NullPtr.Pontiland.view.Button renderer =
        new com.NullPtr.Pontiland.view.Button(assets).setDefaultFontSize(16f).setHoverScale(1.06f);

    plus20Btn = renderer.render(com.NullPtr.Pontiland.view.Button.Type.ACCENT, "+20", 0.42f);
    row.addChild(plus20Btn);
    Container hspace1 = new Container();
    hspace1.setBackground(null);
    hspace1.setPreferredSize(new Vector3f(10f, 0, 0));
    row.addChild(hspace1);

    plus50Btn = renderer.render(com.NullPtr.Pontiland.view.Button.Type.BASE, "+50", 0.42f);
    row.addChild(plus50Btn);
    Container hspace2 = new Container();
    hspace2.setBackground(null);
    hspace2.setPreferredSize(new Vector3f(10f, 0, 0));
    row.addChild(hspace2);

    plus100Btn = renderer.render(com.NullPtr.Pontiland.view.Button.Type.POSITIVE, "+100", 0.42f);
    row.addChild(plus100Btn);

    // Separador vertical antes de salir
    Container vspace2 = new Container();
    vspace2.setBackground(null);
    vspace2.setPreferredSize(new Vector3f(0, 2f, 0));
    root.addChild(vspace2);

    // Fila del boton de salida, centrado
    Container exitRow = root.addChild(new Container());
    exitRow.setBackground(null);
    exitBtn = renderer.render(com.NullPtr.Pontiland.view.Button.Type.NEGATIVE, "Salir", 0.55f);
    exitRow.addChild(exitBtn);

    applyBackground();

    // Ajustar anchos para que los labels se centren visualmente
    Vector3f pref = root.getPreferredSize();
    if (pref != null) {
      priceTitleLbl.setPreferredSize(new Vector3f(pref.x, priceTitleLbl.getPreferredSize().y, 0));
      priceValueLbl.setPreferredSize(new Vector3f(pref.x, priceValueLbl.getPreferredSize().y, 0));
      // Ajustar a ancho completo y reducir ligeramente la altura visual
      Vector3f exitPref = exitBtn.getPreferredSize();
      float reducedHeight = exitPref.y * 0.82f;
      exitBtn.setPreferredSize(new Vector3f(pref.x, reducedHeight, 0));
      exitRow.setPreferredSize(new Vector3f(pref.x, reducedHeight, 0));
    }
  }

  public Container getRoot() {
    return root;
  }

  public Vector3f getPreferredSize() {
    return root.getPreferredSize();
  }

  public void setInfo(String propertyName, String currentPriceText) {
    // Solo mostramos el precio como segunda linea
    String value =
        (currentPriceText != null && !currentPriceText.isEmpty()) ? currentPriceText : "$0";
    if (!value.startsWith("$")) {
      value = "$" + value;
    }
    priceValueLbl.setText(value);
  }

  private void applyBackground() {
    String path = "graphics/sprites/HUD/Auction.png";
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
      root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.05f, 0.06f, 0.08f, 0.95f)));
    }
  }

  // Getters para enganchar acciones desde fuera si se necesita
  public Button getPlus20Btn() {
    return plus20Btn;
  }

  public Button getPlus50Btn() {
    return plus50Btn;
  }

  public Button getPlus100Btn() {
    return plus100Btn;
  }

  public Button getExitBtn() {
    return exitBtn;
  }
}
