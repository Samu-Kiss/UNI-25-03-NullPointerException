
package view.panel;

/* ======================================= Headers ===================================== */
import com.NullPtr.Pontiland.view.IPanelCompra;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.NullPtr.Pontiland.controllers.HudController;


/**
 * Clase: PanelCompra
 * Propósito: Vista presentacional del panel de compra. No contiene lógica de juego
 *            y no llama repos/servicios. El Servicio la invoca mediante IPanelCompraView.
 */
public class PanelCompra implements IPanelCompra {

    /* ---- GUI raíz y contenedor principal ---- */
    private final Node rootGui;
    private final Container panel;

    /* ---- Widgets ---- */
    private Label  lblTitulo;
    private Label  lblPrecio;
    private Button btnComprar;
    private Button btnCancelar;
    private Button btnSubastar;

    /* ---- Referencia al controlador (solo para reenviar acciones de usuario) ---- */
    private HudController controller;

    /**
     * Firma: PanelCompra(Node rootGui)
     * Propósito: Construir el panel en estado oculto.
     * Parámetros:
     *   - rootGui: nodo GUI raíz (guiNode)
     * Retorno: N/A
     */
    public PanelCompra(Node rootGui) {
        this.rootGui = rootGui;
        this.panel   = new Container();
        construirUI();
        ocultarCompra();
    }

    /**
     * Firma: setController(HudController controller)
     * Propósito: Inyectar el controlador a quien se reportan los clicks del usuario.
     * Parámetros:
     *   - controller: controlador HUD
     * Retorno: void
     */
    public void setController(HudController controller) {
        this.controller = controller;
    }

    /* =============================== IPanelCompraView =============================== */

    @Override
    public void mostrarCompra(int casillaId, String nombre, int precio, Integer duenioId) {
        lblTitulo.setText(nombre != null ? nombre : "Propiedad");
        lblPrecio.setText("$ " + precio);

        if (panel.getParent() == null) {
            rootGui.attachChild(panel);
        }

        panel.setCullHint(Spatial.CullHint.Inherit);
        // UiFx.show(panel, 220);
    }

    @Override
    public void ocultarCompra() {
        panel.setCullHint(Spatial.CullHint.Always);
        // UiFx.hide(panel, 180);
    }

    @Override
    public boolean estaVisible() {
        return panel.getCullHint() != Spatial.CullHint.Always;
    }

    /* ================================ Construcción UI ================================ */

    /**
     * Firma: construirUI()
     * Propósito: Crear y organizar widgets Lemur. Reenviar eventos → controlador.
     * Parámetros: N/A
     * Retorno: void
     */
    private void construirUI() {
        lblTitulo  = panel.addChild(new Label("Propiedad"));
        lblPrecio  = panel.addChild(new Label("$ 0"));
        btnComprar = panel.addChild(new Button("COMPRAR"));
        btnCancelar= panel.addChild(new Button("CANCELAR"));
        btnSubastar= panel.addChild(new Button("SUBASTAR"));

        btnComprar.addClickCommands(b -> {
            if (controller != null) controller.onComprarDesdeVista();
        });
        btnCancelar.addClickCommands(b -> {
            if (controller != null) controller.onCancelarCompraDesdeVista();
        });
        btnSubastar.addClickCommands(b -> {
            if (controller != null) controller.onSubastarDesdeVista();
        });
    }

    public Container getContainer() { return panel; }
}
