package com.grahambartley.toomanychests.screenhandler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TagRowRegionTest {
  @ParameterizedTest
  @EnumSource(value = TagRowRegion.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
  void aRegionOverARowIsSomethingToActOn(TagRowRegion region) {
    assertTrue(region.isSomething());
  }

  @Test
  void aPointOverNoRowIsNothingToActOn() {
    assertFalse(TagRowRegion.NONE.isSomething());
  }
}
