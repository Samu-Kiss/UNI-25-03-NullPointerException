package pontiland.entities;

/**
 * Clase que representa un icono en el juego
 * Un icono tiene un id y un nombre
 */
public class Icono {
    private String nombreIcono = null;
    //Warning: Posible uso futuro
    //private String spriteIcono = null;

    /**
     * Constructor de la clase Icono
     * @param nombreIcono
     */
    public Icono(String nombreIcono) {
        if (nombreIcono == null || nombreIcono.isEmpty()) {
            throw new IllegalArgumentException("El nombre del icono no puede ser nulo o vacío");
        }

        this.nombreIcono = nombreIcono;
    }
}
