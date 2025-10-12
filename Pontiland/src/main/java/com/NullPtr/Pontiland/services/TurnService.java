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
    private int playerID = 0;
    private boolean canThrowDice = true;
    private Ficha[] fichas = null;

    public TurnService(IJugadorRepository jugadorRepository, IPartidaRepository partidaRepository, DiceService diceService) {
        this.diceService = diceService;
        this.jugadorRepository = jugadorRepository;
        this.partidaRepository = partidaRepository;

    }


    @Override
    public void nextTurn() {
        try {
            playerID = jugadorRepository.getPlayerIdByNumJugador((jugadorRepository.getActivePlayer()%partidaRepository.getNumJugadores()) + 1);
            jugadorRepository.changeActivePlayer(playerID);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void movePlayer(int numCasillas) {
      int nuevaPosicion;
      Jugador jugadorActual;

      try {
        jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
        nuevaPosicion = (jugadorActual.getPosicion() + numCasillas) % 40;
        jugadorActual.setPosicion(nuevaPosicion);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

      try {
        System.out.println("Movimiendo al jugador " + jugadorRepository.getActivePlayer() +
                " a la posición = " + jugadorActual.getPosicion());
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      try {
        jugadorRepository.updateJugador(jugadorActual);
      } catch (SQLException e) {
        throw new RuntimeException(e);
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
            nextTurn();
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

}
