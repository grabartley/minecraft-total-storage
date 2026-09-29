package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.EnglishLang;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class SortOrderingTest {
  @ParameterizedTest
  @EnumSource(SortOrdering.class)
  void everyOrderingRoundTripsThroughItsId(SortOrdering ordering) {
    assertEquals(Optional.of(ordering), SortOrdering.fromId(ordering.id()));
  }

  @ParameterizedTest
  @EnumSource(SortOrdering.class)
  void idsAreLowercase(SortOrdering ordering) {
    assertEquals(ordering.id().toLowerCase(Locale.ROOT), ordering.id());
  }

  @ParameterizedTest
  @ValueSource(strings = {"REGISTRY_ID", " registry_id ", "Registry_Id"})
  void parsingIgnoresCaseAndSurroundingSpace(String id) {
    assertEquals(Optional.of(SortOrdering.REGISTRY_ID), SortOrdering.fromId(id));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "alphabetical", "registry id"})
  void anUnrecognisedIdIsEmpty(String id) {
    assertTrue(SortOrdering.fromId(id).isEmpty());
  }

  @Test
  void aNullIdIsEmpty() {
    assertTrue(SortOrdering.fromId(null).isEmpty());
  }

  @Test
  void theOrderingsReadAsAPlayerWouldSayThem() {
    assertEquals("Registry order", EnglishLang.of(keyOf(SortOrdering.REGISTRY_ID)));
    assertEquals("Display name", EnglishLang.of(keyOf(SortOrdering.DISPLAY_NAME)));
  }

  private static String keyOf(SortOrdering ordering) {
    return ((TranslatableTextContent) ordering.displayName().getContent()).getKey();
  }
}
