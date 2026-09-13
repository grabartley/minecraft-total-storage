package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ConfigSectionTest {
  @Test
  void everySettingTheServerHasIsOnExactlyOneSection() {
    final List<String> grouped = new ArrayList<>();
    for (final ConfigSection section : ConfigSection.values()) {
      section.settings().forEach(setting -> grouped.add(setting.name()));
    }

    assertEquals(ConfigSettings.names().size(), grouped.size());
    assertEquals(new HashSet<>(ConfigSettings.names()), new HashSet<>(grouped));
  }

  @ParameterizedTest
  @EnumSource(ConfigSection.class)
  void aSectionHoldsSomethingToShow(ConfigSection section) {
    assertFalse(section.settings().isEmpty());
  }

  @ParameterizedTest
  @EnumSource(ConfigSection.class)
  void aSectionIsAddressableByALowerCaseId(ConfigSection section) {
    assertEquals(section.name().toLowerCase(java.util.Locale.ROOT), section.id());
  }

  @Test
  void noTwoSectionsShareAnId() {
    final Set<String> ids = new HashSet<>();
    for (final ConfigSection section : ConfigSection.values()) {
      assertTrue(ids.add(section.id()));
    }
  }
}
