package com.NullPtr.Pontiland.controllers;

import com.NullPtr.Pontiland.entities.Dado;
import com.jme3.scene.Spatial;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class DiceController{
    Dado[] dados;

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
                dado.moverDado();
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

    public void lanzamientoDadosBloqueante(AtomicReferenceArray<Byte> resultados) {
        lanzarDados();
        //final CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            boolean dadosEnMovimiento;
            do {
                dadosEnMovimiento = false;
                for (Dado dado : dados) {
                    if (dado != null && dado.estaMoviendose()) {
                        dadosEnMovimiento = true;
                        break;
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (dadosEnMovimiento);
            System.out.println(leerDados()[0]);
            System.out.println(Arrays.toString(leerDados()));
            resultados.getAndSet(0, leerDados()[0]);
            resultados.getAndSet(1, leerDados()[1]);

            //latch.countDown();
        }).start();
    }

}
