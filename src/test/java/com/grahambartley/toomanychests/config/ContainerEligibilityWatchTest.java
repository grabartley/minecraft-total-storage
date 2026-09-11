package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ContainerEligibilityWatchTest {
  private final ContainerEligibilityWatch watch = new ContainerEligibilityWatch();

  @Test
  void aConfigCarryingTheDefaultOverridesHasNotMoved() {
    assertFalse(watch.moved(TooManyChestsConfig.defaults()));
  }

  @Test
  void addingAnOverrideCountsAsAMove() {
    assertTrue(watch.moved(TestConfigs.withOverrides(Map.of("minecraft:furnace", true))));
  }

  @Test
  void theSameOverridesTwiceCountAsOneMove() {
    final TooManyChestsConfig config = TestConfigs.withOverrides(Map.of("minecraft:furnace", true));

    assertTrue(watch.moved(config));
    assertFalse(watch.moved(config));
  }

  @Test
  void flippingAnOverrideCountsAsAMove() {
    watch.moved(TestConfigs.withOverrides(Map.of("minecraft:furnace", true)));

    assertTrue(watch.moved(TestConfigs.withOverrides(Map.of("minecraft:furnace", false))));
  }

  @Test
  void withdrawingAnOverrideCountsAsAMove() {
    watch.moved(TestConfigs.withOverrides(Map.of("minecraft:furnace", true)));

    assertTrue(watch.moved(TooManyChestsConfig.defaults()));
  }

  @Test
  void aChangeToAnotherSettingLeavesTheOverridesWhereTheyWere() {
    assertFalse(watch.moved(TestConfigs.withInts(8, 8, 90, 10, 64)));
  }

  @Test
  void seedingFromAWorldsOverridesLeavesThemWhereTheyWere() {
    final TooManyChestsConfig loaded = TestConfigs.withOverrides(Map.of("minecraft:furnace", true));

    watch.seed(loaded);

    assertFalse(watch.moved(loaded));
  }

  @Test
  void clearingAnOverrideTheWorldLoadedWithCountsAsAMove() {
    watch.seed(TestConfigs.withOverrides(Map.of("minecraft:furnace", true)));

    assertTrue(watch.moved(TooManyChestsConfig.defaults()));
  }
}
