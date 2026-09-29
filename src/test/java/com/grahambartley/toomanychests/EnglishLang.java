package com.grahambartley.toomanychests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class EnglishLang {
  private EnglishLang() {}

  public static String of(String key) {
    try (InputStream lang =
        EnglishLang.class.getResourceAsStream("/assets/too-many-chests/lang/en_us.json")) {
      assertTrue(lang != null, "the language file this mod ships was not on the classpath");
      final JsonObject entries =
          JsonParser.parseReader(new InputStreamReader(lang, StandardCharsets.UTF_8))
              .getAsJsonObject();
      assertTrue(entries.has(key), key + " has no English text");
      return entries.get(key).getAsString();
    } catch (IOException failure) {
      throw new AssertionError("the language file this mod ships could not be read", failure);
    }
  }
}
