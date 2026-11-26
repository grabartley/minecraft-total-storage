package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ClientPreferencesStoreTest {
  @TempDir Path configDir;

  private Path preferencesPath() {
    return configDir.resolve("total-storage").resolve("client.json");
  }

  @BeforeEach
  void startFromDefaults() {
    ClientPreferencesStore.load(preferencesPath());
  }

  static Stream<ClientPreferences> changes() {
    return Stream.of(
        new ClientPreferences(false, ScreenButtonAnchor.INSIDE_TOP_RIGHT, false),
        new ClientPreferences(true, ScreenButtonAnchor.OUTSIDE_BOTTOM_RIGHT, false),
        new ClientPreferences(true, ScreenButtonAnchor.INSIDE_TOP_RIGHT, true));
  }

  @ParameterizedTest
  @MethodSource("changes")
  void aSavedChangeTakesEffectAtOnce(ClientPreferences changed) {
    assertTrue(ClientPreferencesStore.save(preferencesPath(), changed));

    assertEquals(changed, ClientPreferencesStore.current());
  }

  @ParameterizedTest
  @MethodSource("changes")
  void aSavedChangeSurvivesARestart(ClientPreferences changed) {
    ClientPreferencesStore.save(preferencesPath(), changed);
    ClientPreferencesStore.load(configDir.resolve("elsewhere.json"));

    ClientPreferencesStore.load(preferencesPath());

    assertEquals(changed, ClientPreferencesStore.current());
  }

  @Test
  void aChangeThatCannotBeWrittenIsNotApplied() throws IOException {
    final Path blocked = configDir.resolve("blocked");
    Files.writeString(blocked, "a file where the folder should be");
    final ClientPreferences changed =
        new ClientPreferences(false, ScreenButtonAnchor.OUTSIDE_TOP_RIGHT, true);

    assertFalse(ClientPreferencesStore.save(blocked.resolve("client.json"), changed));

    assertEquals(ClientPreferences.defaults(), ClientPreferencesStore.current());
  }
}
