package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Dado;
import com.jme3.scene.Spatial;

import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Servicio encargado de gestionar el lanzamiento de dados.
 *
 * Este servicio encapsula toda la lógica relacionada con los dados, de forma
 * que el controlador sólo invoque sus métodos sin conocer detalles de la
 * implementación interna. Incluye una máquina de estados para permitir
 * lanzamientos no bloqueantes en jMonkeyEngine.
 */
public class DiceService {
    private final Dado[] dados = new Dado[2];

    /**
     * Máquina de estados interna para el lanzamiento no bloqueante.
     */
    private enum Estado {IDLE, INIT, LAUNCH, CHECK_MOVIMIENTO, READ, DONE}

    private Estado estado = Estado.IDLE;

    /**
     * Referencia donde se almacenarán los resultados del lanzamiento no bloqueante.
     */
    private AtomicReferenceArray<Byte> resultadosRef;

    /**
     * Inyecta ambos dados en el servicio.
     *
     * @param dado1 Spatial que representa el modelo 3D del primer dado
     * @param dado2 Spatial que representa el modelo 3D del segundo dado
     */
    public void setDados(Spatial dado1, Spatial dado2) {

        dados[0] = new Dado(dado1);
        dados[1] = new Dado(dado2);

    }

    /**
     * Realiza el lanzamiento simultáneo de todos los dados cargados.
     */
    public void lanzarDados() {
        for (Dado dado : dados) {
            if (dado != null) {
                dado.lanzar();
            }
        }
    }

    /**
     * Devuelve el valor superior de cada dado cargado.
     *
     * @return Un array de dos bytes con los valores de los dados, o null si el dado no está definido
     */
    public Byte[] leerDados() {
        Byte[] resultados = new Byte[2];
        resultados[0] = (dados[0] != null) ? dados[0].getCaraSuperior() : null;
        resultados[1] = (dados[1] != null) ? dados[1].getCaraSuperior() : null;
        return resultados;
    }

    /**
     * Inicia la secuencia de lanzamiento no bloqueante.
     *
     * @param resultados Objeto atómico donde se escribirán los resultados al finalizar
     */
    public void lanzamientoDadosNoBloqueante(AtomicReferenceArray<Byte> resultados) {
        this.resultadosRef = resultados;
        this.estado = Estado.INIT;
    }

    /**
     * Actualiza la máquina de estados del lanzamiento no bloqueante.
     * Este método debe llamarse en cada frame para que el lanzamiento avance.
     */
    public void update() {
        switch (estado) {
            case IDLE:
                break;
            case INIT:
                estado = Estado.LAUNCH;
                break;
            case LAUNCH:
                lanzarDados();
                estado = Estado.CHECK_MOVIMIENTO;
                break;
            case CHECK_MOVIMIENTO:
                boolean enMovimiento = false;
                for (Dado dado : dados) {
                    if (dado != null && dado.enMovimiento()) {
                        enMovimiento = true;
                        break;
                    }
                }
                if (!enMovimiento) {
                    estado = Estado.READ;
                }
                break;
            case READ:
                Byte[] valores = leerDados();
                if (resultadosRef != null) {
                    resultadosRef.set(0, valores[0]);
                    resultadosRef.set(1, valores[1]);
                }
                // Terminar ciclo
                estado = Estado.DONE;
                break;
            case DONE:
                // Resetear al estado inicial y limpiar referencia a resultados
                estado = Estado.IDLE;
                resultadosRef = null;
                break;
        }
    }
}