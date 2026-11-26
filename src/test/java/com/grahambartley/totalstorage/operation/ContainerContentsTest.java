package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ContainerContentsTest {
  static Stream<Arguments> preferredOverEachOther() {
    return Stream.of(
        Arguments.of(ContainerContents.HOLDS_ITEM, ContainerContents.EMPTY),
        Arguments.of(ContainerContents.EMPTY, ContainerContents.OTHER_ITEMS),
        Arguments.of(ContainerContents.HOLDS_ITEM, ContainerContents.OTHER_ITEMS));
  }

  @ParameterizedTest(name = "{0} before {1}")
  @MethodSource("preferredOverEachOther")
  void aContainerAlreadyHoldingTheItemComesFirstAndOneOfOtherItemsLast(
      ContainerContents preferred, ContainerContents later) {
    assertTrue(preferred.preference() < later.preference());
  }
}
