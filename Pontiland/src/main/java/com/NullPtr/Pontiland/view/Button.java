package com.NullPtr.Pontiland.view;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.CursorMotionEvent;
import com.simsilica.lemur.event.DefaultCursorListener;

/**
 * Renderizador de botones con sprite + texto centrado basado en Lemur.
 *
 * <p>- Usa los sprites en graphics/sprites/Common/Buttons/{Base,Accent,Positive,Negative}.png -
 * Ajusta el tamaño preferido al del sprite escalado. - Centra el texto y reduce el tamaño de fuente
 * si es necesario para que quede dentro del botón. - Incluye animación simple de escala al hacer
 * hover.
 *
 * <p>Uso: Button ui = new Button(assets); com.simsilica.lemur.Button play =
 * ui.render(Button.Type.POSITIVE, "Jugar", 0.5f); play.addClickCommands(src ->
 * System.out.println("click"));
 */
public class Button {

  public enum Type {
    BASE("graphics/sprites/Common/Buttons/Base.png"),
    ACCENT("graphics/sprites/Common/Buttons/Accent.png"),
    POSITIVE("graphics/sprites/Common/Buttons/Positive.png"),
    NEGATIVE("graphics/sprites/Common/Buttons/Negative.png");

    private final String path;

    Type(String path) {
      this.path = path;
    }

    public String getPath() {
      return path;
    }
  }

  private final AssetManager assets;

  // Parámetros de comportamiento
  private float hoverScale = 1.06f; // escala al pasar el cursor
  private float defaultFontSize = 18f; // tamaño de fuente base
  private float minFontSize = 10f; // tamaño mínimo al auto-ajustar
  private Insets3f textPadding = new Insets3f(6, 10, 6, 10); // top, left, bottom, right

  public Button(AssetManager assets) {
    this.assets = assets;
  }

  /** Atajo con escala por defecto (0.5f). */
  public com.simsilica.lemur.Button render(Type type, String text) {
    return render(type, text, 0.5f);
  }

  /**
   * Crea y configura un botón Lemur con sprite y texto.
   *
   * @param type uno de los cuatro tipos predefinidos
   * @param text texto a mostrar (centrado). Puede incluir saltos de línea "\n".
   * @param scaleFactor factor de escala del sprite base (1.0 = tamaño completo de la textura)
   * @return instancia de com.simsilica.lemur.Button lista para añadirse al GUI node/containers
   */
  public com.simsilica.lemur.Button render(Type type, String text, float scaleFactor) {
    // Crear control base (Label/Button de Lemur)
    final com.simsilica.lemur.Button b = new com.simsilica.lemur.Button("");

    // Cargar la textura del sprite del botón
    Texture2D tex = loadGuiTexture(type.getPath());

    // Aplicar como fondo del control
    QuadBackgroundComponent bg = new QuadBackgroundComponent(tex);
    b.setBackground(bg);

    // Dimensionar el control al tamaño del sprite escalado
    int w = tex.getImage().getWidth();
    int h = tex.getImage().getHeight();
    Vector3f size = new Vector3f(w * scaleFactor, h * scaleFactor, 0);
    b.setPreferredSize(size);

    // Texto y padding
    b.setText(text != null ? text : "");
    b.setTextHAlignment(com.simsilica.lemur.HAlignment.Center);
    b.setTextVAlignment(com.simsilica.lemur.VAlignment.Center);
    b.setInsets(textPadding);

    // Auto ajuste de fuente para encajar en el área útil
    float maxTextWidth = size.x * 0.88f; // margen lateral de seguridad
    float maxTextHeight = size.y * 0.70f; // margen vertical (evitar tocar bordes redondeados)
    fitTextToButton(b, maxTextWidth, maxTextHeight);

    // Animación simple de hover: escala inmediata (sin lerp) para mantenerlo ligero
    addHoverScale(b, hoverScale);

    return b;
  }

  /** Cambia el factor de escala en hover (por ejemplo 1.05f). */
  public Button setHoverScale(float hoverScale) {
    this.hoverScale = hoverScale;
    return this;
  }

  /** Ajusta el tamaño de fuente base usado antes del auto-ajuste. */
  public Button setDefaultFontSize(float size) {
    this.defaultFontSize = size;
    return this;
  }

  /** Define el tamaño mínimo de fuente permitido por el auto-ajuste. */
  public Button setMinFontSize(float size) {
    this.minFontSize = size;
    return this;
  }

  /** Define el padding interno usado para el texto (top, left, bottom, right). */
  public Button setTextPadding(Insets3f padding) {
    this.textPadding = padding != null ? padding : new Insets3f(0, 0, 0, 0);
    return this;
  }

  // --- Utilidades internas ---

  private Texture2D loadGuiTexture(String path) {
    TextureKey key = new TextureKey(path, true); // flipY=true para GUI
    key.setGenerateMips(false);
    Texture2D tex = (Texture2D) assets.loadTexture(key);
    // Suavizado y clamp para evitar bleeding
    tex.setWrap(Texture.WrapMode.EdgeClamp);
    tex.setMagFilter(Texture.MagFilter.Bilinear);
    tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
    return tex;
  }

  private void fitTextToButton(com.simsilica.lemur.Button b, float maxWidth, float maxHeight) {
    if (maxWidth <= 0 || maxHeight <= 0) {
      b.setFontSize(defaultFontSize);
      return;
    }

    // Medidor temporal con el mismo texto para calcular tamaño preferido
    Label measure = new Label(b.getText());
    float size = defaultFontSize;
    measure.setFontSize(size);

    // Disminuir hasta que quepa por ancho y alto o alcanzamos el mínimo
    for (; size >= minFontSize; size -= 1f) {
      measure.setFontSize(size);
      Vector3f pref = measure.getPreferredSize();
      if (pref.x <= maxWidth && pref.y <= maxHeight) {
        break;
      }
    }
    b.setFontSize(Math.max(size, minFontSize));
  }

  private void addHoverScale(final com.simsilica.lemur.Button b, final float factor) {
    final Vector3f baseLoc = new Vector3f();
    final boolean[] captured = new boolean[] {false};
    CursorEventControl.addListenersToSpatial(
        b,
        new DefaultCursorListener() {
          @Override
          public void cursorEntered(
              CursorMotionEvent event,
              com.jme3.scene.Spatial target,
              com.jme3.scene.Spatial capture) {
            // Capturar la posición base una sola vez
            if (!captured[0]) {
              baseLoc.set(b.getLocalTranslation());
              captured[0] = true;
            }
            // Mantener color blanco (evitar highlight amarillo por estilos)
            b.setColor(ColorRGBA.White);
            // Calcular offset para pivotar en el centro visual (top-left origin en GUI)
            Vector3f size = b.getPreferredSize();
            Vector3f center = new Vector3f(size.x * 0.5f, -size.y * 0.5f, 0);
            Vector3f offset = center.subtract(center.mult(factor)); // (1-s)*center
            b.setLocalScale(factor);
            b.setLocalTranslation(baseLoc.x + offset.x, baseLoc.y + offset.y, baseLoc.z);
          }

          @Override
          public void cursorExited(
              CursorMotionEvent event,
              com.jme3.scene.Spatial target,
              com.jme3.scene.Spatial capture) {
            // Restaurar color y transformaciones
            b.setColor(ColorRGBA.White);
            b.setLocalScale(1f);
            if (captured[0]) {
              b.setLocalTranslation(baseLoc);
            }
          }
        });
  }
}
