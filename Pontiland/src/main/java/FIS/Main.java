package FIS;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.OBJLoader;
import com.jme3.material.Material;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.ToneMapFilter;

/**
 * Aplicación principal de jMonkeyEngine que:
 * Carga un modelo OBJ (tablero) desde los recursos del proyecto.
 * Configura iluminación direccional y ambiental básica.
 * Configura un cielo HDRI y aplica mapeo de tonos para rango dinámico alto.
 * Mejora la calidad de texturas (anisotrópico y filtrado trilineal).
 * Ajusta la cámara para navegación con flyCam.
 * <p>
 * Pensado como punto de partida para un entorno 3D con iluminación física moderada.
 */
public class Main extends SimpleApplication {
    /**
     * Referencia al modelo principal (tablero) cargado en la escena.
     */
    private Spatial board;

    /**
     * Punto de entrada de la aplicación.
     * <p>
     * Configura:
     * <p>
     * Resolución (mitad de la pantalla con mínimos razonables).
     * Título de la ventana.
     * VSync y corrección gamma para flujo HDR.
     * Evita el diálogo de configuración inicial.
     *
     * @param args argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        Main app = new Main();
        // Obtener dimensiones de pantalla y aplicar límites mínimos
        java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.max(640, screen.width / 2); // mínimo sensato
        int height = Math.max(360, screen.height / 2);
        AppSettings settings = new AppSettings(true);
        settings.setResolution(width, height);
        settings.setTitle("Pontiland");
        settings.setVSync(true);
        // Activar framebuffer sRGB para corrección gamma (crucial con HDR + tone mapping)
        settings.setGammaCorrection(true);
        settings.setFullscreen(false);
        app.setShowSettings(false); // omitir diálogo estándar
        app.setSettings(settings);
        app.start();
    }

    /**
     * Inicialización principal de la escena.
     * Se ejecuta una vez al iniciar el contexto gráfico.
     * Realiza:
     * <p>
     * Registro de loaders para OBJ y HDR.
     * Carga y centra el modelo del tablero.
     * Ajuste de calidad de texturas.
     * Configuración de luces (direccional + ambiental).
     * Creación de un cielo HDRI con mapeo equirectangular.
     * Inserción de un filtro de tone mapping para comprimir rango dinámico.
     * Ajustes de cámara y planos de recorte.
     */
    @Override
    public void simpleInitApp() {
        // Asegurar que el loader de OBJ esté registrado (normalmente ya lo está con jme3-plugins)
        assetManager.registerLoader(OBJLoader.class, "obj");
        // Registrar loader para imágenes HDR (.hdr de formato Radiance)
        assetManager.registerLoader(com.jme3.texture.plugins.HDRLoader.class, "hdr");

        // Cargar modelo desde el classpath (ruta en resources)
        board = assetManager.loadModel("assets/3DModels/board/Tablero.obj");

        rootNode.attachChild(board);

        // Opcional: escalar/posicionar si fuese necesario
        // board.scale(2f);
        board.center(); // centra el modelo para facilitar la orientación inicial de la cámara

        // Mejorar calidad de texturas (filtro anisotrópico + trilineal)
        applyTextureQualitySettings(rootNode);

        // Iluminación direccional (sol) con intensidad moderada por HDR
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1f, -2f, -1f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(0.6f));
        rootNode.addLight(sun);

        // Luz ambiental suave para levantar sombras demasiado oscuras
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.15f));
        rootNode.addLight(ambient);

        // Cargar cielo HDRI (preferible .hdr convertido previamente)
        try {
            com.jme3.texture.Texture hdr = assetManager.loadTexture("assets/HDRI/citrus_orchard_road_puresky_4k.hdr");
            Spatial sky = SkyFactory.createSky(assetManager, hdr, SkyFactory.EnvMapType.EquirectMap);
            rootNode.attachChild(sky);
        } catch (Exception e) {
            System.err.println("Error al cargar el cielo HDR: " + e.getMessage());
            viewPort.setBackgroundColor(ColorRGBA.DarkGray); // degradado simple si falla
        }

        // Post-procesado: mapeo de tonos para adaptar HDR a LDR
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        ToneMapFilter tone = new ToneMapFilter(); // Se puede ajustar el punto blanco si hay sobreexposición
        fpp.addFilter(tone);
        viewPort.addProcessor(fpp);

        // Configuración de la cámara: posición y frustum adaptado (near plane reducido)
        cam.setLocation(new Vector3f(0, 4f, 10f));
        cam.lookAt(board.getWorldTranslation(), Vector3f.UNIT_Y);
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.05f, 2000f);
        flyCam.setMoveSpeed(15f); // velocidad de navegación
    }

    /**
     * Recorre el grafo de escena y mejora la calidad de todas las texturas encontradas.
     * Acciones aplicadas:
     * <p>
     * Establece filtrado anisotrópico (valor configurable).
     * Activa filtrado trilineal para minificación.
     * Usa filtrado bilineal para magnificación.
     * Emite trazas en consola para depuración de cada textura ajustada.
     *
     * @param root nodo raíz (o subárbol) desde el cual iniciar la búsqueda.
     */
    private void applyTextureQualitySettings(Spatial root) {
        root.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Geometry geom) {
                Material mat = geom.getMaterial();
                if (mat == null) return;
                for (MatParam param : mat.getParams()) {
                    if (param instanceof MatParamTexture) {
                        Texture tex = ((MatParamTexture) param).getTextureValue();
                        if (tex != null) {
                            // Aumentar filtrado anisotrópico para mantener detalle en ángulos rasantes
                            tex.setAnisotropicFilter(8); // subir a 16 si el hardware lo permite
                            // Filtrado trilineal (suaviza transiciones entre niveles de mipmap)
                            tex.setMinFilter(Texture.MinFilter.Trilinear);
                            // Filtrado bilineal para magnificación (suficiente en la mayoría de casos)
                            tex.setMagFilter(Texture.MagFilter.Bilinear);
                            // Salida de depuración (nombre y dimensiones)
                            System.out.println("Textura ajustada: " + tex.getName() + " size=" + tex.getImage().getWidth() + "x" + tex.getImage().getHeight() + " aniso=" + tex.getAnisotropicFilter());
                        }
                    }
                }
            }
        });
    }
}