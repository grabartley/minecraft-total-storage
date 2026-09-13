package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClientPreferencesFileTest {
  private static final ClientPreferences EVERYTHING_CHANGED =
      new ClientPreferences(false, ScreenButtonAnchor.OUTSIDE_BOTTOM_RIGHT, false);

  @TempDir Path configDir;

  private Path preferencesPath() {
    return configDir.resolve("too-many-chests").resolve("client.json");
  }

  private List<Path> brokenBackups() throws IOException {
    try (var entries = Files.list(preferencesPath().getParent())) {
      return entries.filter(path -> path.getFileName().toString().contains(".broken.")).toList();
    }
  }

  @Test
  void aClientWithNoPreferencesYetLoadsDefaults() {
    assertEquals(ClientPreferences.defaults(), ClientPreferencesFile.load(preferencesPath()));
  }

  @Test
  void aClientWithNoPreferencesYetHasDefaultsWrittenOut() {
    ClientPreferencesFile.loadOrCreate(preferencesPath());

    assertTrue(Files.exists(preferencesPath()));
    assertEquals(ClientPreferences.defaults(), ClientPreferencesFile.load(preferencesPath()));
  }

  @Test
  void existingPreferencesAreNotOverwrittenOnLoad() {
    ClientPreferencesFile.save(preferencesPath(), EVERYTHING_CHANGED);

    assertEquals(EVERYTHING_CHANGED, ClientPreferencesFile.loadOrCreate(preferencesPath()));
  }

  @Test
  void savedPreferencesSurviveTheRoundTripThroughDisk() {
    assertTrue(ClientPreferencesFile.save(preferencesPath(), EVERYTHING_CHANGED));

    assertEquals(EVERYTHING_CHANGED, ClientPreferencesFile.load(preferencesPath()));
  }

  @Test
  void savingCreatesTheModDirectoryWhenItIsMissing() {
    assertFalse(Files.exists(preferencesPath().getParent()));

    assertTrue(ClientPreferencesFile.save(preferencesPath(), ClientPreferences.defaults()));
  }

  @Test
  void aHandEditedFileThatIsNoLongerJsonIsKeptRatherThanLost() throws IOException {
    Files.createDirectories(preferencesPath().getParent());
    Files.writeString(preferencesPath(), "{ not json at all", StandardCharsets.UTF_8);

    assertEquals(ClientPreferences.defaults(), ClientPreferencesFile.load(preferencesPath()));
    assertEquals(1, brokenBackups().size());
  }

  @Test
  void nothingIsWrittenWhenThereIsNowhereToWriteIt() {
    assertFalse(ClientPreferencesFile.save(null, ClientPreferences.defaults()));
    assertFalse(ClientPreferencesFile.save(preferencesPath(), null));
  }
}
