package com.NullPtr.Pontiland.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesReader {
  private static final String PROPERTIES_PATH =
      "src/main/java/com/NullPtr/Pontiland/ponti.properties";
  private static Properties properties = null;

  static {
    properties = new Properties();
    try (FileInputStream fis = new FileInputStream(PROPERTIES_PATH)) {
      properties.load(fis);
    } catch (IOException e) {
      // Log or handle error: file not found or cannot be read
      properties = null;
    }
  }

  public static String getProperty(String key) {
    if (properties == null) {
      return null;
    }
    return properties.getProperty(key);
  }
}
