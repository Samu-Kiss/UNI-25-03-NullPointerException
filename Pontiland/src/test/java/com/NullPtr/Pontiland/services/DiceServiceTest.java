package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.NullPtr.Pontiland.entities.Dado;
import com.jme3.scene.Spatial;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para la clase {@link DiceService}.
 *
 * <p>Esta clase valida el comportamiento del servicio de dados utilizando mocks de la clase {@link
 * Dado} para evitar depender de la implementación real de jMonkeyEngine.
 *
 * <p>Se comprueba:
 *
 * <ul>
 *   <li>El estado inicial del servicio.
 *   <li>La habilitación y deshabilitación de la interacción.
 *   <li>El flujo de actualización de estados durante un lanzamiento de dados.
 *   <li>Que los métodos de los dados sean invocados correctamente.
 * </ul>
 */
public class DiceServiceTest {

  /** Instancia del servicio de dados a probar */
  private DiceService diceService;

  /** Mocks de los dados */
  private Dado dadoMock1;

  private Dado dadoMock2;

  /** Campo de resultados para inyección por reflexión */
  private Field resultadosField;

  /**
   * Configuración inicial de las pruebas.
   *
   * <p>Se crean los mocks de los dados y se inyectan en la instancia de {@link DiceService}.
   */
  @BeforeEach
  void setUp() {
    diceService = new DiceService();

    // Creamos los mocks para los dados
    dadoMock1 = mock(Dado.class);
    dadoMock2 = mock(Dado.class);

    // Llamamos a setDados para inicializar el array 'dados' en el servicio.
    // Usamos mocks para los Spatial.
    diceService.setDados(mock(Spatial.class), mock(Spatial.class));

    // Usamos reflexión para reemplazar los objetos Dado reales (creados en setDados)
    // por nuestros mocks, lo que permite controlar su comportamiento.
    try {
      var field = DiceService.class.getDeclaredField("dados");
      field.setAccessible(true);
      field.set(diceService, new Dado[] {dadoMock1, dadoMock2});

      // Inicializar el campo de reflexión para 'resultados'
      resultadosField = DiceService.class.getDeclaredField("resultados");
      resultadosField.setAccessible(true);

    } catch (Exception e) {
      // En un entorno de prueba real, esto indicaría un problema en la configuración del mock.
      throw new RuntimeException("Error al inyectar mocks de Dado.", e);
    }
  }

  /**
   * Verifica el estado inicial del servicio de dados.
   *
   * <p>Debe permitir lanzar dados, permitir interacción y tener resultados iniciales nulos.
   */
  @Test
  void testInitialState() {
    assertTrue(diceService.getCanThrowDice(), "Debe poder lanzar dados inicialmente.");
    assertTrue(diceService.getCanInteract(), "La interacción debe estar habilitada inicialmente.");
    assertArrayEquals(
        new Byte[] {null, null},
        diceService.getResultados(),
        "Los resultados deben ser nulos inicialmente.");
  }

  /** Verifica la funcionalidad de habilitar y deshabilitar la interacción con los dados. */
  @Test
  void testEnableInteract() {
    // Deshabilitar
    diceService.enableInteract(false);
    assertFalse(diceService.getCanInteract(), "La interacción debe estar deshabilitada.");
    assertFalse(
        diceService.getCanThrowDice(),
        "No se debe poder lanzar si la interacción está deshabilitada.");

    // Habilitar
    diceService.enableInteract(true);
    assertTrue(diceService.getCanInteract(), "La interacción debe estar habilitada.");
    assertTrue(
        diceService.getCanThrowDice(),
        "Se debe poder lanzar si la interacción está habilitada y en IDLE.");
  }

  /**
   * Simula un lanzamiento de dados utilizando mocks y valida la secuencia de estados.
   *
   * <p>Se verifica que los resultados se asignen correctamente y luego se reinicien.
   */
  @Test
  void testLaunchAndUpdateWithMocks() {
    // Configuración de los mocks para simular dados detenidos y con resultados
    when(dadoMock1.enMovimiento()).thenReturn(false);
    when(dadoMock2.enMovimiento()).thenReturn(false);
    when(dadoMock1.getCaraSuperior()).thenReturn((byte) 4);
    when(dadoMock2.getCaraSuperior()).thenReturn((byte) 6);

    // Iniciar secuencia de estados
    diceService.lanzamientoDados(); // IDLE -> LAUNCH
    diceService.update(); // LAUNCH -> CHECK_MOVIMIENTO (lanza los dados)

    // Simular que el movimiento ha terminado
    diceService.update(); // CHECK_MOVIMIENTO -> READ (chequea el movimiento y pasa a leer)

    diceService.update(); // READ -> DONE (lee los resultados)

    // Los resultados deben ser leídos
    Byte[] resultados = diceService.getResultados();
    assertEquals(4, resultados[0].intValue(), "El resultado del dado 1 debe ser 4.");
    assertEquals(6, resultados[1].intValue(), "El resultado del dado 2 debe ser 6.");

    // Pasar al estado IDLE
    diceService.update(); // DONE -> IDLE (limpia los resultados)

    // Los resultados deben ser reseteados
    resultados = diceService.getResultados();
    assertNull(resultados[0], "El resultado del dado 1 debe ser nulo después del reinicio.");
    assertNull(resultados[1], "El resultado del dado 2 debe ser nulo después del reinicio.");
  }

  /**
   * Verifica que durante el estado CHECK_MOVIMIENTO, si algún dado está en movimiento, no se puede
   * lanzar nuevamente.
   */
  @Test
  void testGetCanThrowDiceOnMotion() {
    // Simular que el dado 1 está en movimiento
    when(dadoMock1.enMovimiento()).thenReturn(true);
    when(dadoMock2.enMovimiento()).thenReturn(false);

    // Iniciar la secuencia y avanzar al chequeo de movimiento
    diceService.lanzamientoDados(); // IDLE -> LAUNCH
    diceService.update(); // LAUNCH -> CHECK_MOVIMIENTO

    // No debe permitir lanzar dados
    assertFalse(
        diceService.getCanThrowDice(), "No se debe poder lanzar dados en CHECK_MOVIMIENTO.");

    // Simular que el dado 1 se detiene
    when(dadoMock1.enMovimiento()).thenReturn(false);

    // Avanzar la secuencia (saldrá de CHECK_MOVIMIENTO)
    diceService.update(); // CHECK_MOVIMIENTO -> READ
    diceService.update(); // READ -> DONE
    diceService.update(); // DONE -> IDLE

    // Debe permitir lanzar dados de nuevo
    assertTrue(diceService.getCanThrowDice(), "Se debe poder lanzar dados de nuevo en IDLE.");
  }

  /** Verifica que al lanzar los dados, el método {@code lanzar()} se invoque en ambos dados. */
  @Test
  void ThrowDiceInvokesMethod() {

    diceService.lanzarDados();
    verify(dadoMock1, times(1)).lanzar();
    verify(dadoMock2, times(1)).lanzar();
  }

  /**
   * Verifica que el método lanzarDados() maneje correctamente un elemento nulo en el array 'dados',
   * cubriendo el 'else' del 'if (dado != null)'.
   */
  @Test
  void testLanzarDados_HandlesNullDice() {
    // Usamos reflexión para inyectar un array donde el primer dado es null.
    try {
      var field = DiceService.class.getDeclaredField("dados");
      field.setAccessible(true);
      field.set(diceService, new Dado[] {null, dadoMock2});
    } catch (Exception e) {
      fail("Fallo al inyectar dados nulos para la prueba: " + e.getMessage());
    }

    // Lanzar los dados. Esto debe ejecutar la rama 'dado == null'.
    // El método no debe lanzar una NullPointerException.
    assertDoesNotThrow(
        () -> diceService.lanzarDados(),
        "LanzarDados no debe fallar con NullPointerException al encontrar un dado nulo.");

    // Verificar que solo el dado no nulo recibió la llamada a lanzar().
    verify(dadoMock2, times(1)).lanzar();
    verify(dadoMock1, never()).lanzar(); // dardoMock1 es reemplazado por null para este test
  }

  /**
   * Verifica que el método leerDados() maneje correctamente los casos en los que uno o ambos dados
   * son nulos, cubriendo ambos 'if (dados[i] != null)'.
   */
  @Test
  void testLeerDados_HandlesNullDice() {
    // Caso 1: dados[0] es nulo
    try {
      var field = DiceService.class.getDeclaredField("dados");
      field.setAccessible(true);
      field.set(diceService, new Dado[] {null, dadoMock2});
    } catch (Exception e) {
      fail("Fallo al inyectar dados nulos para la prueba: " + e.getMessage());
    }

    when(dadoMock2.getCaraSuperior()).thenReturn((byte) 5);
    Byte[] resultados1 = diceService.leerDados();
    assertNull(resultados1[0], "El resultado del dado 0 debe ser nulo si el dado es nulo.");
    assertEquals(
        5,
        resultados1[1].intValue(),
        "El resultado del dado 1 debe ser correcto si el dado no es nulo.");

    // Caso 2: dados[1] es nulo
    try {
      var field = DiceService.class.getDeclaredField("dados");
      field.setAccessible(true);
      field.set(diceService, new Dado[] {dadoMock1, null});
    } catch (Exception e) {
      fail("Fallo al inyectar dados nulos para la prueba: " + e.getMessage());
    }

    when(dadoMock1.getCaraSuperior()).thenReturn((byte) 3);
    Byte[] resultados2 = diceService.leerDados();
    assertEquals(
        3,
        resultados2[0].intValue(),
        "El resultado del dado 0 debe ser correcto si el dado no es nulo.");
    assertNull(resultados2[1], "El resultado del dado 1 debe ser nulo si el dado es nulo.");

    // Caso 3: Ambos dados son nulos
    try {
      var field = DiceService.class.getDeclaredField("dados");
      field.setAccessible(true);
      field.set(diceService, new Dado[] {null, null});
    } catch (Exception e) {
      fail("Fallo al inyectar dados nulos para la prueba: " + e.getMessage());
    }

    Byte[] resultados3 = diceService.leerDados();
    assertNull(resultados3[0], "El resultado del dado 0 debe ser nulo si el dado es nulo.");
    assertNull(resultados3[1], "El resultado del dado 1 debe ser nulo si el dado es nulo.");

    // Verificar que getCaraSuperior nunca fue llamado en el mock nulo
    verify(dadoMock1, times(1)).getCaraSuperior(); // Sólo llamado en el Caso 2
    verify(dadoMock2, times(1)).getCaraSuperior(); // Sólo llamado en el Caso 1
  }

  /**
   * Verifica que el método getResultados() devuelva {null, null} si uno o ambos resultados internos
   * (resultados[0] o resultados[1]) son nulos.
   */
  @Test
  void testGetResultados_HandlesNullValues() throws IllegalAccessException {
    // Caso 1: resultados[0] es nulo
    resultadosField.set(diceService, new Byte[] {null, (byte) 6});
    assertArrayEquals(
        new Byte[] {null, null},
        diceService.getResultados(),
        "Debe devolver {null, null} si resultados[0] es nulo.");

    // Caso 2: resultados[1] es nulo
    resultadosField.set(diceService, new Byte[] {(byte) 4, null});
    assertArrayEquals(
        new Byte[] {null, null},
        diceService.getResultados(),
        "Debe devolver {null, null} si resultados[1] es nulo.");

    // Caso 3: Ambos son nulos
    resultadosField.set(diceService, new Byte[] {null, null});
    assertArrayEquals(
        new Byte[] {null, null},
        diceService.getResultados(),
        "Debe devolver {null, null} si ambos son nulos.");

    // Caso 4: Ambos son válidos (Verificación de la rama cubierta en otro test, pero para
    // completitud)
    resultadosField.set(diceService, new Byte[] {(byte) 4, (byte) 6});
    assertArrayEquals(
        new Byte[] {(byte) 4, (byte) 6},
        diceService.getResultados(),
        "Debe devolver los resultados si ambos son válidos.");
  }
}
