package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Ficha;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;

import java.sql.SQLException;

public class TurnService implements ITurnService{
    private IJugadorRepository jugadorRepository;
    private IPartidaRepository partidaRepository;
    private DiceService diceService;
    private int playerID = 0; // ID del jugador actual
    private boolean canThrowDice = true;
    private Ficha[] fichas = null;

    public TurnService(IJugadorRepository jugadorRepository, IPartidaRepository partidaRepository, DiceService diceService) {
        this.diceService = diceService;
        this.jugadorRepository = jugadorRepository;
        this.partidaRepository = partidaRepository;
        diceService.setTurnService(this);
    }


    @Override
    public void nextTurn() {
        try {
            jugadorRepository.changeActivePlayer(partidaRepository.numJugadores());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Mover al jugador con respecto al numero que resulta del lanzamiento de dados
    public void movePlayer(int numCasillas) {
      int nuevaPosicion;
      Jugador jugadorActual;

      //Tomar el jugador actual y moverlo
      try {
        jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayerID());
        nuevaPosicion = (jugadorActual.getPosicion() + numCasillas) % 40;
        jugadorActual.setPosicion(nuevaPosicion);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

      //Mostrar en consola el movimiento
      try {
        System.out.println("Movimiendo al jugador " + jugadorRepository.getActivePlayerID() +
                " a la posición = " + jugadorActual.getPosicion());
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      //Simular tiempo de movimiento y otros
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      //Actualizar la posicion del jugador en la base de datos
      try {
        jugadorRepository.updateJugadorByID(jugadorActual);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      //Pasar al siguiente turno
      nextTurn();

      for (Ficha ficha : fichas) {
        System.out.println(ficha.toString());
      }

      canThrowDice = true;
    }

    @Override
    public void update() {
        Byte[] dados = diceService.getResultados();
        if(dados[0] != null && dados[1] != null){
            int movimiento = dados[0] + dados[1];

            System.out.println("Resultados dados: [" + dados[0] + ", " + dados[1] + "]");
            dados[0] = dados[1] = null;
            canThrowDice = false;

            movePlayer(movimiento);
        }
    }

    @Override
    public void buyProperty() {

    }

    @Override
    public void payRent() {

    }

    @Override
    public boolean canThrowDice() {
        return canThrowDice;
    }

    @Override
    public void loadFichas(){
      try {
        fichas = jugadorRepository.getFichas();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
}
