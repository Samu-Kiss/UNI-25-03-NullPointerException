package com.NullPtr.Pontiland.controllers;

import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.services.DiceService;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.event.KeyInputEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
   */
  @Test
  void testThrowsDiceWhenPressingY() throws Exception {
    when(diceService.getCanThrowDice()).thenReturn(true);

    KeyInputEvent event = new KeyInputEvent(KeyInput.KEY_Y, 'Y', true, false);
    invokeKeyListener(event);

    verify(diceService, times(1)).lanzamientoDados();
  }

  /**
   * Parámetros para el test parametrizado: casos donde el dado **no debe lanzarse**.
   *
   * <p>Cada argumento contiene: canThrowDice, isPressed
   */
  private static Stream<Arguments> keyEventParameters() {
    return Stream.of(
        Arguments.of(false, true), // No permitido lanzar
        Arguments.of(true, false), // Tecla liberada
        Arguments.of(true, false) // Evento no presionado (similar al anterior)
        );
  }

  /**
   * Test parametrizado que verifica que distintos eventos de teclado no disparen el lanzamiento de
   * dados.
   */
  @ParameterizedTest
  @MethodSource("keyEventParameters")
  void testKeyEventsDoNotTriggerThrow(boolean canThrowDice, boolean isPressed) throws Exception {
    when(diceService.getCanThrowDice()).thenReturn(canThrowDice);

    KeyInputEvent event = new KeyInputEvent(KeyInput.KEY_Y, 'Y', isPressed, false);
    invokeKeyListener(event);

    verify(diceService, never()).lanzamientoDados();
  }

  /**
   * Verifica que presionar una tecla distinta a 'Y' no lance los dados, incluso si está permitido
   * hacerlo.
   */
  @Test
  void testDoesNotThrowWithOtherKey() throws Exception {
    when(diceService.getCanThrowDice()).thenReturn(true);

    KeyInputEvent event = new KeyInputEvent(KeyInput.KEY_A, 'A', true, false);
    invokeKeyListener(event);

    verify(diceService, never()).lanzamientoDados();
  }

  /**
   * Verifica que {@code registerInputs()} registre correctamente el listener en el {@link
   * InputManager}.
   */
  @Test
  void testRegisterInputsAddsListener() {
    InputManager inputManager = mock(InputManager.class);
    controller.registerInputs(inputManager);

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

    verify(diceService).enableInteract(true);
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
   */
  @Test
  void testRegisterInputsWithNullDoesNothing() {
    controller.registerInputs(null);

    verify(diceService, never()).enableInteract(anyBoolean());
  }

  /**
   * Método auxiliar para invocar el listener privado del controlador de forma reflejada.
   *
   * @param event evento de teclado a simular
   * @throws Exception si ocurre un error de reflexión
   */
  private void invokeKeyListener(KeyInputEvent event) throws Exception {
    Field field = LanzamientoDadosController.class.getDeclaredField("rawKeys");
    field.setAccessible(true);
    var listener = field.get(controller);
    Method method = listener.getClass().getMethod("onKeyEvent", KeyInputEvent.class);
    method.invoke(listener, event);
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

    invokeKeyListener(event);

    verify(diceService, never()).lanzamientoDados();
  }
}
