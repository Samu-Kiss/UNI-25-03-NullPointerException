package com.NullPtr.Pontiland.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesReader {
  private static final String RESOURCE_NAME = "ponti.properties";
  private static final Path DEV_FALLBACK = Paths.get("src/main/resources", RESOURCE_NAME);
  private static final Properties properties = new Properties();

  static {
    // 1) Try classpath (works when packaged and in IDE if resource is on classpath)
    try (InputStream is =
        PropertiesReader.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
      if (is != null) {
        properties.load(is);
      }
    } catch (IOException ignored) {
      // continue to fallback
    }

    // 2) Fallback to project file (useful in IDE during development)
    if (Files.exists(DEV_FALLBACK)) {
      try (InputStream fis = Files.newInputStream(DEV_FALLBACK)) {
        properties.load(fis);
      } catch (IOException ignored) {
        // leave properties empty
      }
    }
    // If neither found, properties remains empty (getProperty will return null)
  }

  public static String getProperty(String key) {
    return properties.getProperty(key);
  }
}
