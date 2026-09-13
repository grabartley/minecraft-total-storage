package com.grahambartley.toomanychests.compat;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class TooManyChestsModMenuTest {
  @Test
  void modMenuIsHandedAFactoryThatBuildsTheServerSettingsScreen() {
    assertNotNull(new TooManyChestsModMenu().getModConfigScreenFactory());
  }
}
