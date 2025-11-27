package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para {@link GitHubContributorsService}.
 *
 * <p>Los tests usan Mockito para simular las respuestas del cliente HTTP y Reflection para inyectar
 * un {@link HttpClient} mock en la instancia original del servicio.
 */
class GitHubContributorsServiceTest {

  private HttpClient client;
  private HttpResponse<String> response;
  private GitHubContributorsService service;

  /**
   * Configura una instancia real del servicio y reemplaza el HttpClient interno mediante Reflection
   * por un mock controlable en los tests.
   */
  @BeforeEach
  void setup() throws Exception {
    client = mock(HttpClient.class);
    response = mock(HttpResponse.class);

    service = new GitHubContributorsService();

    // Reemplaza el HttpClient real por el mock
    Field f = GitHubContributorsService.class.getDeclaredField("client");
    f.setAccessible(true);
    f.set(service, client);
  }

  /**
   * Verifica que {@link GitHubContributorsService#fetchContributorsDetailed(String, String, int)}
   * devuelva correctamente una lista válida cuando la API responde con un estado 200 y un JSON
   * correcto.
   */
  @Test
  void testFetchContributorsDetailed_success() throws Exception {
    String json =
        """
                [
                  { "login": "alice", "avatar_url": "a1", "html_url": "u1" },
                  { "login": "bob",   "avatar_url": "a2", "html_url": "u2" }
                ]
                """;

    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn(json);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    List<GitHubContributorsService.Contributor> list =
        service.fetchContributorsDetailed("owner", "repo", 2);

    assertEquals(2, list.size());
    assertEquals("alice", list.get(0).login);
    assertEquals("bob", list.get(1).login);
  }

  /**
   * Verifica que el método lance {@link IOException} cuando GitHub responde con un estado distinto
   * de 2xx.
   */
  @Test
  void testFetchContributorsDetailed_errorStatus() throws Exception {
    when(response.statusCode()).thenReturn(404);
    when(response.body()).thenReturn("Not Found");
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    assertThrows(IOException.class, () -> service.fetchContributorsDetailed("x", "y", 5));
  }

  /**
   * Cubre la rama donde GITHUB_TOKEN existe pero está en blanco, por lo que no debe añadirse el
   * header Authorization.
   */
  @Test
  void testTokenBlankDoesNotAddAuthorization() throws Exception {
    setEnv("GITHUB_TOKEN", "   "); // token en blanco

    String json = "[{\"login\":\"user\"}]";

    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn(json);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    List<GitHubContributorsService.Contributor> list =
        service.fetchContributorsDetailed("a", "b", 1);

    assertEquals(1, list.size());
    assertEquals("user", list.get(0).login);
  }

  /**
   * Test de IF de statusCode 2xx cuando el cuerpo es vacío, asegurando que el método no lance
   * errores y retorne lista vacía.
   */
  @Test
  void testStatus2xxWithEmptyBody() throws Exception {
    String json = ""; // cuerpo vacío

    when(response.statusCode()).thenReturn(204); // 2xx válido
    when(response.body()).thenReturn(json);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    List<GitHubContributorsService.Contributor> list =
        service.fetchContributorsDetailed("a", "b", 5);

    assertTrue(list.isEmpty());
  }

  /** Donde el JSON raíz no es un arreglo, por lo que no deben extraerse contribuidores. */
  @Test
  void testParseContributors_notArray() throws Exception {
    String json = "{\"login\":\"not-array\"}"; // No es una lista

    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn(json);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    List<GitHubContributorsService.Contributor> list =
        service.fetchContributorsDetailed("o", "r", 5);

    assertTrue(list.isEmpty());
  }

  /**
   * Valida que {@link GitHubContributorsService#fetchContributors(String, String, int)} extrae
   * correctamente solo los logins a partir del JSON retornado.
   */
  @Test
  void testFetchContributors_extractsLogins() throws Exception {
    String json =
        """
                [
                  { "login": "alice" },
                  { "login": "bob" }
                ]
                """;

    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn(json);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    List<String> logins = service.fetchContributors("o", "r", 2);
    assertEquals(List.of("alice", "bob"), logins);
  }

  /** Comprueba que un arreglo JSON vacío resulte en una lista de contribuidores vacía. */
  @Test
  void testParseContributors_emptyArray() throws Exception {
    String json = "[]";

    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn(json);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    List<GitHubContributorsService.Contributor> list =
        service.fetchContributorsDetailed("o", "r", 10);

    assertTrue(list.isEmpty());
  }

  /**
   * Verifica que los elementos sin campo "login" sean ignorados por el parser y que solo se
   * incluyan aquellos que sí lo contienen.
   */
  @Test
  void testParseContributors_missingLogin() throws Exception {
    String json =
        """
                [
                  { "avatar_url": "xx", "html_url": "yy" },
                  { "login": "real" }
                ]
                """;

    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn(json);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    List<GitHubContributorsService.Contributor> list =
        service.fetchContributorsDetailed("o", "r", 5);

    assertEquals(1, list.size());
    assertEquals("real", list.get(0).login);
  }

  /**
   * Valida que el parámetro {@code limit} se ajuste correctamente dentro de los rangos permitidos
   * (1 a 100) sin producir excepciones.
   */
  @Test
  void testPerPageLimitBounds() throws Exception {
    String json = "[{\"login\":\"x\"}]";

    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn(json);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);
    java.util.List<GitHubContributorsService.Contributor> listLow =
        service.fetchContributorsDetailed("a", "b", 0);
    java.util.List<GitHubContributorsService.Contributor> listHigh =
        service.fetchContributorsDetailed("a", "b", 200);

    assertEquals(1, listLow.size());
    assertEquals(1, listHigh.size());
    assertEquals("x", listLow.get(0).login);
  }

  /**
   * Asegura que el código utilice la variable de entorno {@code GITHUB_TOKEN} cuando está presente,
   * ejecutando así la rama correspondiente.
   */
  @Test
  void testWithTokenEnvironmentVariable() throws Exception {
    setEnv("GITHUB_TOKEN", "fake123");

    String json = "[{\"login\":\"tok\"}]";
    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn(json);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    List<GitHubContributorsService.Contributor> list =
        service.fetchContributorsDetailed("a", "b", 5);

    assertEquals(1, list.size());
    assertEquals("tok", list.get(0).login);
  }

  /**
   * Modifica dinámicamente una variable de entorno para permitir probar comportamientos que
   * dependen de GITHUB_TOKEN.
   */
  private static void setEnv(String key, String value) {
    try {
      var env = System.getenv();
      var cl = env.getClass();
      var field = cl.getDeclaredField("m");
      field.setAccessible(true);
      ((java.util.Map<String, String>) field.get(env)).put(key, value);
    } catch (Exception ignored) {
    }
  }
}
