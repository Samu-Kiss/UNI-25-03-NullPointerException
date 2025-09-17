package pontiland.entities;

/**
 * Clase que representa un icono en el juego
 * Un icono tiene un id y un nombre
 */
public class Icono {
    private byte idIcono = -1;
    private String nombreIcono = null;
    //Warning: Posible uso futuro
    //private String spriteIcono = null;

    /**
     * Constructor de la clase Icono
     * @param idIcono
     * @param nombreIcono
     */
    public Icono(byte idIcono, String nombreIcono) {
        if (idIcono < 0) {
            throw new IllegalArgumentException("El id del icono no puede ser negativo");
        }
        if (nombreIcono == null || nombreIcono.isEmpty()) {
            throw new IllegalArgumentException("El nombre del icono no puede ser nulo o vacío");
        }

        this.idIcono = idIcono;
        this.nombreIcono = nombreIcono;
    }
}
