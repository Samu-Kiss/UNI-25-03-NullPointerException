package pontiland.entities;

enum Accion {
    bajarNivel, aumentarNivel, CobroAJugador, AbonoAJugador
}

public class TarjetaEvento {
    private String nombre = null;
    private String descripcion = null;
    private Accion accion;

}
