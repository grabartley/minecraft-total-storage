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

class ConfigFileTest {
  @TempDir Path worldDir;

  private Path configPath() {
    return new ConfigPaths(worldDir).getServerConfigPath();
  }

  private List<Path> brokenBackups() throws IOException {
    try (var entries = Files.list(configPath().getParent())) {
      return entries.filter(p -> p.getFileName().toString().contains(".broken.")).toList();
    }
  }

  @Test
  void aWorldWithNoConfigYetLoadsDefaults() {
    assertEquals(TooManyChestsConfig.defaults(), ConfigFile.load(configPath()));
  }

  @Test
  void aWorldWithNoConfigYetHasDefaultsWrittenOut() {
    ConfigFile.loadOrCreate(configPath());

    assertTrue(Files.exists(configPath()));
    assertEquals(TooManyChestsConfig.defaults(), ConfigFile.load(configPath()));
  }

  @Test
  void anExistingConfigIsNotOverwrittenOnLoad() {
    TooManyChestsConfig stored = TestConfigs.everythingNonDefault();
    ConfigFile.save(configPath(), stored);

    assertEquals(stored, ConfigFile.loadOrCreate(configPath()));
  }

  @Test
  void aSavedConfigSurvivesTheRoundTripThroughDisk() {
    TooManyChestsConfig stored = TestConfigs.everythingNonDefault();

    assertTrue(ConfigFile.save(configPath(), stored));
    assertEquals(stored, ConfigFile.load(configPath()));
  }

  @Test
  void savingCreatesTheModDirectoryWhenItIsMissing() {
    assertFalse(Files.exists(configPath().getParent()));

    assertTrue(ConfigFile.save(configPath(), TooManyChestsConfig.defaults()));
    assertTrue(Files.exists(configPath()));
  }

  @Test
  void aMalformedConfigLoadsDefaultsRatherThanFailing() throws IOException {
    Files.createDirectories(configPath().getParent());
    Files.writeString(configPath(), "{ not json at all", StandardCharsets.UTF_8);

    assertEquals(TooManyChestsConfig.defaults(), ConfigFile.load(configPath()));
  }

  @Test
  void aMalformedConfigIsPreservedRatherThanDiscarded() throws IOException {
    Files.createDirectories(configPath().getParent());
    String broken = "{ not json at all";
    Files.writeString(configPath(), broken, StandardCharsets.UTF_8);

    ConfigFile.load(configPath());

    List<Path> backups = brokenBackups();
    assertEquals(1, backups.size());
    assertEquals(broken, Files.readString(backups.get(0), StandardCharsets.UTF_8));
  }

  @Test
  void aMalformedConfigIsReplacedWithDefaultsOnLoadOrCreate() throws IOException {
    Files.createDirectories(configPath().getParent());
    Files.writeString(configPath(), "{ not json at all", StandardCharsets.UTF_8);

    assertEquals(TooManyChestsConfig.defaults(), ConfigFile.loadOrCreate(configPath()));
    assertEquals(1, brokenBackups().size());
  }

  @Test
  void anEmptyConfigFileLoadsDefaults() throws IOException {
    Files.createDirectories(configPath().getParent());
    Files.writeString(configPath(), "", StandardCharsets.UTF_8);

    assertEquals(TooManyChestsConfig.defaults(), ConfigFile.load(configPath()));
  }

  @Test
  void aConfigWrittenByAnOlderVersionKeepsWhatItStillDefines() throws IOException {
    Files.createDirectories(configPath().getParent());
    Files.writeString(configPath(), "{\"maxTerminalRadius\": 24}", StandardCharsets.UTF_8);

    TooManyChestsConfig loaded = ConfigFile.load(configPath());

    assertEquals(24, loaded.maxTerminalRadius());
    assertEquals(TooManyChestsConfig.DEFAULT_QUICK_STACK_RADIUS, loaded.quickStackRadius());
  }

  @Test
  void aConfigWithAnOutOfRangeValueIsClampedOnLoad() throws IOException {
    Files.createDirectories(configPath().getParent());
    Files.writeString(configPath(), "{\"maxTerminalRadius\": 9999}", StandardCharsets.UTF_8);

    assertEquals(
        TooManyChestsConfig.MAX_TERMINAL_RADIUS_MAX,
        ConfigFile.load(configPath()).maxTerminalRadius());
  }

  @Test
  void savingRejectsAMissingPath() {
    assertFalse(ConfigFile.save(null, TooManyChestsConfig.defaults()));
  }

  @Test
  void savingRejectsAMissingConfig() {
    assertFalse(ConfigFile.save(configPath(), null));
  }

  @Test
  void loadingAMissingPathYieldsDefaults() {
    assertEquals(TooManyChestsConfig.defaults(), ConfigFile.load(null));
  }

  @Test
  void savingLeavesNoTemporaryFileBehind() throws IOException {
    ConfigFile.save(configPath(), TooManyChestsConfig.defaults());

    try (var entries = Files.list(configPath().getParent())) {
      assertTrue(entries.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")));
    }
  }
}
