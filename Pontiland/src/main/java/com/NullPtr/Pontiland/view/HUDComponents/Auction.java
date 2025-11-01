package com.NullPtr.Pontiland.view.HUDComponents;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;

/** Overlay de subastas con sprites. */
public class Auction {

  private static final float SPRITE_SCALE = 0.62f;

  private final Container root;
  private final Label titleLbl;
  private final Label priceLbl;
  private final Button bidBtn;
  private final Button passBtn;
  private final AssetManager assets;

  public Auction(AssetManager assets) {
    this.assets = assets;
    root = new Container();
    root.setInsets(new Insets3f(16, 20, 16, 20));

    titleLbl = root.addChild(new Label("Subasta"));
    titleLbl.setFontSize(20);
    priceLbl = root.addChild(new Label("Precio actual: $0"));

    Container row = root.addChild(new Container());
    row.setBackground(null);
    bidBtn = row.addChild(new Button("Pujar"));
    passBtn = row.addChild(new Button("Pasar"));

    applyBackground();
    styleButtons();
  }

  public Container getRoot() {
    return root;
  }

  public Vector3f getPreferredSize() {
    return root.getPreferredSize();
  }

  public void setInfo(String propertyName, String currentPriceText) {
    titleLbl.setText(propertyName != null ? propertyName : "Subasta");
    priceLbl.setText(currentPriceText != null ? currentPriceText : "Precio actual: $0");
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
      // fallback silencioso
      root.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.05f, 0.06f, 0.08f, 0.95f)));
    }
  }

  private void styleButtons() {
    styleButton(bidBtn, "graphics/sprites/Common/Buttons/Positive.png");
    styleButton(passBtn, "graphics/sprites/Common/Buttons/Negative.png");
  }

  private void styleButton(Button b, String path) {
    try {
      TextureKey key = new TextureKey(path, true);
      key.setGenerateMips(false);
      Texture2D tex = (Texture2D) assets.loadTexture(key);
      tex.setWrap(Texture.WrapMode.EdgeClamp);
      tex.setMagFilter(Texture.MagFilter.Bilinear);
      tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
      b.setBackground(new QuadBackgroundComponent(tex));
      int w = tex.getImage().getWidth();
      int h = tex.getImage().getHeight();
      b.setPreferredSize(new Vector3f(w * 0.45f, h * 0.45f, 0));
    } catch (Exception e) {
      // fallback silencioso
    }
  }
}
