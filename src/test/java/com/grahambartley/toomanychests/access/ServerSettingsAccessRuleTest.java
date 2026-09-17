package com.grahambartley.toomanychests.access;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ServerSettingsAccessRuleTest {
  private static final boolean OPERATOR = true;
  private static final boolean ORDINARY = false;
  private static final boolean DEDICATED = true;
  private static final boolean INTEGRATED = false;
  private static final boolean HOST = true;
  private static final boolean GUEST = false;

  static Stream<Arguments> theCallers() {
    return Stream.of(
        Arguments.of(
            "an operator on a dedicated server writes settings", OPERATOR, DEDICATED, GUEST, true),
        Arguments.of(
            "an ordinary player on a dedicated server does not", ORDINARY, DEDICATED, GUEST, false),
        Arguments.of(
            "the owner of a singleplayer world writes its settings with no cheats",
            ORDINARY,
            INTEGRATED,
            HOST,
            true),
        Arguments.of(
            "somebody who joined that world over LAN does not", ORDINARY, INTEGRATED, GUEST, false),
        Arguments.of(
            "a guest on a LAN world who was given operator still writes",
            OPERATOR,
            INTEGRATED,
            GUEST,
            true),
        Arguments.of(
            "a dedicated server never treats anyone as its host",
            ORDINARY,
            DEDICATED,
            HOST,
            false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("theCallers")
  void onlyAnOperatorOrTheOwnerOfTheWorldWrites(
      String reason, boolean operator, boolean dedicated, boolean host, boolean mayEdit) {
    assertEquals(mayEdit, ServerSettingsAccessRule.mayEdit(operator, dedicated, host), reason);
  }
}
