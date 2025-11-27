package com.NullPtr.Pontiland.controllers;

import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.services.DiceService;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.event.KeyInputEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Clase de pruebas unitarias para {@link LanzamientoDadosController}.
 *
 * <p>Estas pruebas verifican que el controlador de lanzamiento de dados responda correctamente a
 * las entradas del teclado (especialmente la tecla 'Y') y que invoque los métodos adecuados del
 * servicio {@link DiceService}.
 *
 * <p>Se utiliza Mockito para simular el comportamiento del servicio y validar interacciones.
 */
class LanzamientoDadosControllerTest {

  /** Servicio simulado encargado de la lógica de dados. */
  private DiceService diceService;

  /** Controlador bajo prueba. */
  private LanzamientoDadosController controller;

  /** Se ejecuta antes de cada prueba para inicializar el controlador con un mock del servicio. */
  @BeforeEach
  void setUp() {
    diceService = mock(DiceService.class);
    controller = new LanzamientoDadosController(diceService);
  }

  /**
   * Verifica que {@code lanzamientoDados()} se invoque cuando se presiona la tecla 'Y' y el jugador
   * tiene permitido lanzar los dados.
   *
   * <p>Se simula un evento de teclado (KeyInputEvent) con la tecla 'Y' presionada. Se invoca el
   * listener privado mediante reflexión. Finalmente, se verifica que el método del servicio se
   * llame exactamente una vez con {@code verify(..., times(1))}.
   */
  @Test
  void testThrowsDiceWhenPressingY() {
    when(diceService.getCanThrowDice()).thenReturn(true); // Simula que se puede lanzar el dado

    KeyInputEvent event = new KeyInputEvent(KeyInput.KEY_Y, 'Y', true, false);

    try {
      var field = LanzamientoDadosController.class.getDeclaredField("rawKeys");
      field.setAccessible(true);
      var listener = field.get(controller);
      var method = listener.getClass().getMethod("onKeyEvent", KeyInputEvent.class);
      method.invoke(listener, event); // Simula la pulsación de 'Y'
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Verifica que lanzamientoDados() se haya llamado exactamente una vez
    verify(diceService, times(1)).lanzamientoDados();
  }

  /**
   * Verifica que NO se lance el dado si el jugador no tiene permiso, incluso si presiona la tecla
   * 'Y'.
   *
   * <p>El evento de teclado se simula igual que antes, pero se fuerza que getCanThrowDice()
   * devuelva false. Se usa {@code verify(..., never())} para asegurar que el método no se ejecutó.
   */
  @Test
  void testDoesNotThrowIfNotAllowed() throws Exception {
    when(diceService.getCanThrowDice()).thenReturn(false);

    KeyInputEvent event = new KeyInputEvent(KeyInput.KEY_Y, 'Y', true, false);

    var field = LanzamientoDadosController.class.getDeclaredField("rawKeys");
    field.setAccessible(true);
    var listener = field.get(controller);
    var method = listener.getClass().getMethod("onKeyEvent", KeyInputEvent.class);
    method.invoke(listener, event);

    // Verifica que lanzamientoDados() nunca se llame
    verify(diceService, never()).lanzamientoDados();
  }

  /**
   * Verifica que presionar una tecla distinta a 'Y' no lance los dados, incluso si está permitido
   * hacerlo.
   *
   * <p>Se simula la tecla 'A' y se espera que el servicio no reciba ninguna llamada a
   * lanzamientoDados().
   */
  @Test
  void testDoesNotThrowWithOtherKey() throws Exception {
    when(diceService.getCanThrowDice()).thenReturn(true);

    KeyInputEvent event = new KeyInputEvent(KeyInput.KEY_A, 'A', true, false);

    var field = LanzamientoDadosController.class.getDeclaredField("rawKeys");
    field.setAccessible(true);
    var listener = field.get(controller);
    var method = listener.getClass().getMethod("onKeyEvent", KeyInputEvent.class);
    method.invoke(listener, event);

    // La tecla no es 'Y', por lo tanto no se debe lanzar dado
    verify(diceService, never()).lanzamientoDados();
  }

  /**
   * Verifica que {@code registerInputs()} registre correctamente el listener en el {@link
   * InputManager}.
   *
   * <p>Se simula un InputManager y se comprueba que addRawInputListener se invoque una vez.
   */
  @Test
  void testRegisterInputsAddsListener() {
    InputManager inputManager = mock(InputManager.class);
    controller.registerInputs(inputManager);

    // Verifica que el listener se haya registrado exactamente una vez
    verify(inputManager, times(1)).addRawInputListener(any());
  }

  /**
   * Verifica que {@code enableThrow()} invoque la habilitación de interacción en el servicio.
   *
   * <p>Se comprueba que enableInteract() del servicio reciba el valor correcto.
   */
  @Test
  void testEnableThrowCallsService() {
    controller.enableThrow(true);

    verify(diceService).enableInteract(true); // Debe invocarse con true
  }

  /**
   * Verifica que el controlador notifique al servicio la creación de los dados (spatials).
   *
   * <p>Se simulan dos spatials y se verifica que setDados() se llame con ellos.
   */
  @Test
  void testOnDadosCreadosCallsSetDados() {
    var dado1 = mock(com.jme3.scene.Spatial.class);
    var dado2 = mock(com.jme3.scene.Spatial.class);

    controller.onDadosCreados(dado1, dado2);

    verify(diceService).setDados(dado1, dado2);
  }

  /**
   * Verifica que {@code update()} invoque al método correspondiente en el servicio.
   *
   * <p>Se comprueba que update() del servicio se llame exactamente una vez.
   */
  @Test
  void testUpdateCallsServiceUpdate() {
    controller.update();

    verify(diceService, times(1)).update();
  }

  /**
   * Verifica que pasar un {@code null} a registerInputs() no genere errores ni registre listeners.
   *
   * <p>Si se pasa null, el método no debe interactuar con el servicio ni provocar excepciones.
   */
  @Test
  void testRegisterInputsWithNullDoesNothing() {
    controller.registerInputs(null);

    // Verifica que enableInteract() nunca se llame
    verify(diceService, never()).enableInteract(anyBoolean());
  }

  /**
   * Verifica que soltar la tecla 'Y' no provoque un lanzamiento de dados.
   *
   * <p>Se simula un KeyInputEvent con isPressed=false y se asegura que lanzamientoDados() no se
   * invoque.
   */
  @Test
  void testKeyReleasedDoesNotTriggerThrow() throws Exception {
    when(diceService.getCanThrowDice()).thenReturn(true);

    KeyInputEvent event = new KeyInputEvent(KeyInput.KEY_Y, 'Y', false, false);

    var field = LanzamientoDadosController.class.getDeclaredField("rawKeys");
    field.setAccessible(true);
    var listener = field.get(controller);
    var method = listener.getClass().getMethod("onKeyEvent", KeyInputEvent.class);
    method.invoke(listener, event);

    verify(diceService, never()).lanzamientoDados();
  }

  /**
   * Verifica que un evento con isPressed() = false sea ignorado.
   *
   * <p>Incluso si la tecla es 'Y' y el jugador puede lanzar dados, si el evento indica que la tecla
   * fue soltada, no debe invocarse lanzamientoDados().
   */
  @Test
  void testKeyEventNotPressedIsIgnored() throws Exception {
    when(diceService.getCanThrowDice()).thenReturn(true);

    KeyInputEvent event = new KeyInputEvent(KeyInput.KEY_Y, 'Y', false, false);

    var field = LanzamientoDadosController.class.getDeclaredField("rawKeys");
    field.setAccessible(true);
    var listener = field.get(controller);
    var method = listener.getClass().getMethod("onKeyEvent", KeyInputEvent.class);
    method.invoke(listener, event);

    verify(diceService, never()).lanzamientoDados();
  }
}
