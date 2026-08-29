package com.grahambartley.toomanychests.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ContainerRoleTest {
  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void everyRoleRoundTripsThroughItsId(ContainerRole role) {
    assertEquals(Optional.of(role), ContainerRole.fromId(role.id()));
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void idsAreLowercase(ContainerRole role) {
    assertEquals(role.id().toLowerCase(Locale.ROOT), role.id());
  }

  @ParameterizedTest
  @ValueSource(strings = {"EXCLUDED", " excluded ", "Excluded"})
  void parsingIgnoresCaseAndSurroundingSpace(String id) {
    assertEquals(Optional.of(ContainerRole.EXCLUDED), ContainerRole.fromId(id));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "readonly", "general-purpose"})
  void anUnrecognisedIdIsEmpty(String id) {
    assertTrue(ContainerRole.fromId(id).isEmpty());
  }

  @Test
  void aNullIdIsEmpty() {
    assertTrue(ContainerRole.fromId(null).isEmpty());
  }

  @Test
  void theDefaultRoleIsTheOneThatChangesNothing() {
    assertEquals(ContainerRole.GENERAL, ContainerRole.DEFAULT);
  }
}
