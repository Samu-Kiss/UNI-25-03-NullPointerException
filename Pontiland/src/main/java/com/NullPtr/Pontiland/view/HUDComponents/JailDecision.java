package com.NullPtr.Pontiland.view.HUDComponents;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;

/**
 * Componente HUD para la decisión de cárcel con dos botones: Pagar y Lanzar. Se modela similar a
 * Auction pero minimalista.
 */
public class JailDecision {
  private final Node root;
  private final Container overlay;
  private final Container box;
  private IHUDcontroller hudController;

  public JailDecision(AssetManager assets) {
    root = new Node("JailDecisionRoot");

    overlay = new Container();
    overlay.setBackground(new QuadBackgroundComponent(new ColorRGBA(0, 0, 0, 0.25f)));

    box = overlay.addChild(new Container());
    box.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.05f, 0.05f, 0.05f, 0.9f)));
    box.setInsets(new com.simsilica.lemur.Insets3f(12, 12, 12, 12));

    com.NullPtr.Pontiland.view.Button renderer =
        new com.NullPtr.Pontiland.view.Button(assets).setDefaultFontSize(16f).setHoverScale(1.06f);

    // Fila horizontal para los dos botones siguiendo el estilo de Auction
    Container row =
        new Container(new SpringGridLayout(com.simsilica.lemur.Axis.X, com.simsilica.lemur.Axis.Y));
    row.setBackground(null);

    // Botón pagar (usar ACCENT para destacar acción de salida) y variante MAIN
    com.simsilica.lemur.Button payBtn =
        renderer.render(
            com.NullPtr.Pontiland.view.Button.Type.ACCENT,
            "Pagar y salir",
            0.55f,
            com.NullPtr.Pontiland.view.Button.Variant.MAIN);
    row.addChild(payBtn);
    payBtn.addClickCommands(
        src -> {
          if (hudController != null) hudController.chooseJailPay();
        });

    // Separador horizontal
    Container hspace = new Container();
    hspace.setBackground(null);
    hspace.setPreferredSize(new Vector3f(12f, 0f, 0));
    row.addChild(hspace);

    // Botón lanzar (usar POSITIVE) variante MAIN
    com.simsilica.lemur.Button rollBtn =
        renderer.render(
            com.NullPtr.Pontiland.view.Button.Type.POSITIVE,
            "Lanzar dados",
            0.55f,
            com.NullPtr.Pontiland.view.Button.Variant.MAIN);
    row.addChild(rollBtn);
    rollBtn.addClickCommands(
        src -> {
          if (hudController != null) hudController.chooseJailRoll();
        });

    box.addChild(row);

    // Por defecto oculto
    overlay.setCullHint(com.jme3.scene.Spatial.CullHint.Always);

    // Añadir al root
    root.attachChild(overlay);
  }

  public void setHudController(IHUDcontroller hudController) {
    this.hudController = hudController;
  }

  public Node getRoot() {
    return root;
  }

  public Vector3f getPreferredSize() {
    return overlay.getPreferredSize();
  }

  public void setVisible(boolean visible) {
    overlay.setCullHint(
        visible ? com.jme3.scene.Spatial.CullHint.Inherit : com.jme3.scene.Spatial.CullHint.Always);
  }

  public void setLocalTranslation(float x, float y, float z) {
    overlay.setLocalTranslation(x, y, z);
  }
}
