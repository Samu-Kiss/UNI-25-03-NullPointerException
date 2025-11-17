package com.NullPtr.Pontiland.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GitHubContributorsService {
  public static class Contributor {
    public final String login;
    public final String avatarUrl;
    public final String htmlUrl;

    public Contributor(String login, String avatarUrl, String htmlUrl) {
      this.login = login;
      this.avatarUrl = avatarUrl;
      this.htmlUrl = htmlUrl;
    }
  }

  private final HttpClient client;
  private final ObjectMapper mapper;

  public GitHubContributorsService() {
    this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    this.mapper = new ObjectMapper();
  }

  /**
   * Obtiene los nombres de usuario (login) de los contribuidores de un repositorio público.
   * Preferir {@link #fetchContributorsDetailed(String, String, int)} si necesitas avatar/URL.
   *
   * @param owner propietario del repositorio
   * @param repo nombre del repositorio
   * @param limit número máximo de contribuidores a devolver
   * @return lista con los logins de los contribuidores
   * @throws IOException si ocurre un problema al comunicarse con la API de GitHub
   * @throws InterruptedException si la operación HTTP es interrumpida
   */
  public List<String> fetchContributors(String owner, String repo, int limit)
      throws IOException, InterruptedException {
    List<Contributor> detailed = fetchContributorsDetailed(owner, repo, limit);
    List<String> out = new ArrayList<>(detailed.size());
    for (Contributor c : detailed) out.add(c.login);
    return out;
  }

  /**
   * Obtiene contribuidores con datos detallados (login, avatar_url, html_url).
   *
   * @param owner propietario del repositorio
   * @param repo nombre del repositorio
   * @param limit número máximo de contribuidores a devolver
   * @return lista con los contribuidores recuperados de la API de GitHub
   * @throws IOException si ocurre un problema al comunicarse con la API de GitHub
   * @throws InterruptedException si la operación HTTP es interrumpida
   */
  public List<Contributor> fetchContributorsDetailed(String owner, String repo, int limit)
      throws IOException, InterruptedException {
    int pageSize = Math.clamp(limit, 1, 100);
    String url =
        String.format(
            "https://api.github.com/repos/%s/%s/contributors?per_page=%d", owner, repo, pageSize);

    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Pontiland/1.0");

    // Token opcional para evitar rate limiting (no requerido)
    String token = System.getenv("GITHUB_TOKEN");
    if (token != null && !token.isBlank()) {
      builder.header("Authorization", "Bearer " + token.trim());
    }

    HttpRequest request = builder.GET().build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      return parseContributors(response.body(), limit);
    } else {
      throw new IOException(
          "GitHub API error: status=" + response.statusCode() + ", body=" + response.body());
    }
  }

  private List<Contributor> parseContributors(String json, int limit) throws IOException {
    List<Contributor> out = new ArrayList<>();
    JsonNode root = mapper.readTree(json);
    if (root.isArray()) {
      for (JsonNode n : root) {
        String login = n.path("login").asText(null);
        String avatar = n.path("avatar_url").asText(null);
        String html = n.path("html_url").asText(null);
        if (login != null) {
          out.add(new Contributor(login, avatar, html));
          if (out.size() >= limit) break;
        }
      }
    }
    return out;
  }
}
