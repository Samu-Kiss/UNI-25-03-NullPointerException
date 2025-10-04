package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.entities.Dado;
import com.jme3.scene.Spatial;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class DiceController{
    Dado[] dados;

    // FSM de lanzamiento no bloqueante
    private enum Estado { IDLE, INIT, LAUNCH, CHECK_MOVIMIENTO, READ, DONE }
    private Estado estado = Estado.IDLE;
    private AtomicReferenceArray<Byte> resultadosRef;

    public DiceController(){
        dados = new Dado[2];
        dados[1] = null;
        dados[0] = null;
    }

    public void setDado1(Spatial spatial){
        dados[0] = new Dado(spatial);
    }

    public void setDado2(Spatial spatial){
        dados[1] = new Dado(spatial);
    }


    public void lanzarDados() {
        for (Dado dado : dados) {
            if (dado != null){
                dado.lanzar();
            }
        }
    }

    public Byte[] leerDados(){
        Byte[] resultados = new Byte[2];
        if (dados[0] != null){
            resultados[0] = dados[0].getCaraSuperior();
        } else {
            resultados[0] = null;
        }
        if (dados[1] != null){
            resultados[1] = dados[1].getCaraSuperior();
        } else {
            resultados[1] = null;
        }
        return resultados;
    }

    // Inicia el proceso no bloqueante basado en FSM. Conserva el nombre del método para compatibilidad.
    public void lanzamientoDadosBloqueante(AtomicReferenceArray<Byte> resultados) {
        // Guardar referencia donde escribir los resultados al finalizar
        this.resultadosRef = resultados;
        // Arrancar la FSM sin bloquear
        this.estado = Estado.INIT;
    }

    // Llamar en cada frame desde simpleUpdate(tpf)
    public void update(float tpf) {
        switch (estado) {
            case IDLE:
                // No hay lanzamiento en curso
                break;
            case INIT:
                // Preparar e ir a LAUNCH
                // System.out.println("Lanzando dados...");
                estado = Estado.LAUNCH;
                break;
            case LAUNCH:
                // Solo se ejecuta una vez por lanzamiento
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
                    // Escribir de forma atómica en las posiciones 0 y 1
                    resultadosRef.set(0, valores[0]);
                    resultadosRef.set(1, valores[1]);
                }


                break;
            case DONE:
                // Fin del ciclo de lanzamiento. Volver a IDLE para permitir nuevos lanzamientos.
                estado = Estado.IDLE;
                break;
        }
    }

    // Utilidades opcionales
    public boolean lanzamientoEnCurso() {
        return estado != Estado.IDLE && estado != Estado.DONE; // DONE dura un frame
    }

    public void cancelarLanzamiento() {
        estado = Estado.IDLE;
        resultadosRef = null;
    }
}
