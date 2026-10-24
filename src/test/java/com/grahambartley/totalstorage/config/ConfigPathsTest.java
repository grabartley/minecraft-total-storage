package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ConfigPathsTest {
  private static final Path WORLD = Path.of("saves", "New World");

  @Test
  void theModDirectoryLivesInsideTheWorldSave() {
    assertEquals(WORLD.resolve("total-storage"), new ConfigPaths(WORLD).getModDir());
  }

  @Test
  void theServerConfigLivesInsideTheModDirectory() {
    assertEquals(
        WORLD.resolve("total-storage").resolve("server-config.json"),
        new ConfigPaths(WORLD).getServerConfigPath());
  }

  @Test
  void eachWorldGetsItsOwnConfigPath() {
    Path other = Path.of("saves", "Another World");

    assertFalse(
        new ConfigPaths(WORLD)
            .getServerConfigPath()
            .equals(new ConfigPaths(other).getServerConfigPath()));
  }

  @Test
  void aBrokenBackupSitsBesideTheFileItPreserves() {
    Path original = WORLD.resolve("total-storage").resolve("server-config.json");

    Path backup = ConfigPaths.brokenBackupPath(original, Instant.parse("2026-08-22T17:04:05Z"));

    assertEquals(original.resolveSibling("server-config.broken.2026-08-22T17-04-05Z.json"), backup);
  }

  @Test
  void aBrokenBackupKeepsAnExtensionlessNameIntact() {
    Path original = Path.of("server-config");

    Path backup = ConfigPaths.brokenBackupPath(original, Instant.parse("2026-08-22T17:04:05Z"));

    assertEquals(Path.of("server-config.broken.2026-08-22T17-04-05Z.json"), backup);
  }
}
