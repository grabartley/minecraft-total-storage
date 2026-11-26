package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.storage.ContainerFilter;
import com.grahambartley.totalstorage.storage.ContainerRole;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DepositRoutingTest {
  private static final BlockPos TERMINAL = new BlockPos(0, 64, 0);
  private static final String COBBLESTONE = "minecraft:cobblestone";
  private static final String DIAMOND = "minecraft:diamond";
  private static final Set<String> NO_TAGS = Set.of();

  private static final ContainerContents HOLDS = ContainerContents.HOLDS_ITEM;
  private static final ContainerContents EMPTY = ContainerContents.EMPTY;
  private static final ContainerContents OTHER = ContainerContents.OTHER_ITEMS;

  private static DepositTarget target(
      int x, ContainerRole role, ContainerFilter filter, ContainerContents contents) {
    return new DepositTarget(new BlockPos(x, 64, 0), role, filter, contents);
  }

  private static DepositTarget general(int x, ContainerContents contents) {
    return target(x, ContainerRole.GENERAL, ContainerFilter.NONE, contents);
  }

  private static DepositTarget filtered(int x, ContainerContents contents) {
    return target(x, ContainerRole.FILTERED, ContainerFilter.ofItems(COBBLESTONE), contents);
  }

  private static DepositTarget excluded(int x) {
    return target(x, ContainerRole.EXCLUDED, ContainerFilter.NONE, HOLDS);
  }

  private static List<Integer> order(List<DepositTarget> candidates) {
    return DepositRouting.order(candidates, TERMINAL, COBBLESTONE, NO_TAGS).stream()
        .map(routed -> routed.container().getX())
        .toList();
  }

  static Stream<Arguments> theRoutingOrder() {
    return Stream.of(
        Arguments.of(
            "a filtered container already holding it beats every other kind",
            List.of(general(1, HOLDS), filtered(2, EMPTY), filtered(3, HOLDS)),
            3),
        Arguments.of(
            "an empty filtered container still beats a general one holding it",
            List.of(general(1, HOLDS), general(2, EMPTY), filtered(3, EMPTY)),
            3),
        Arguments.of(
            "a filtered container of other items still beats a general one holding it",
            List.of(general(1, HOLDS), filtered(3, OTHER)),
            3),
        Arguments.of(
            "an empty filtered container beats a nearer one of other items",
            List.of(filtered(1, OTHER), filtered(2, EMPTY)),
            2),
        Arguments.of(
            "a general container holding it beats an empty general one",
            List.of(general(1, EMPTY), general(2, HOLDS)),
            2),
        Arguments.of(
            "an empty general container beats a nearer one of other items",
            List.of(general(1, OTHER), general(2, EMPTY)),
            2),
        Arguments.of(
            "a general container of other items is used when nothing emptier is left",
            List.of(excluded(1), general(2, OTHER)),
            2),
        Arguments.of(
            "a general container is used when nothing better will take it",
            List.of(excluded(1), general(2, EMPTY)),
            2));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("theRoutingOrder")
  void theBestContainerIsChosenFirst(String rung, List<DepositTarget> candidates, int expectedX) {
    assertEquals(expectedX, order(candidates).get(0), rung);
  }

  @Test
  void everyRungIsOfferedInOrderRatherThanOnlyTheWinner() {
    List<DepositTarget> candidates =
        List.of(
            general(1, OTHER),
            general(2, EMPTY),
            general(3, HOLDS),
            filtered(4, OTHER),
            filtered(5, EMPTY),
            filtered(6, HOLDS));

    assertEquals(List.of(6, 5, 4, 3, 2, 1), order(candidates));
  }

  @Test
  void anExcludedContainerAppearsAtNoRung() {
    List<DepositTarget> candidates = List.of(excluded(1), excluded(2), general(3, EMPTY));

    assertEquals(List.of(3), order(candidates));
  }

  @Test
  void aFilteredContainerThatDoesNotWantTheItemIsPassedOver() {
    List<DepositTarget> candidates = List.of(filtered(1, HOLDS), general(2, EMPTY));

    assertEquals(
        List.of(2),
        DepositRouting.order(candidates, TERMINAL, DIAMOND, NO_TAGS).stream()
            .map(routed -> routed.container().getX())
            .toList());
  }

  @Test
  void aFilteredContainerNamingATagTakesWhateverTheNetworkFilesUnderIt() {
    DepositTarget byTag =
        target(1, ContainerRole.FILTERED, ContainerFilter.ofTags("ores_and_minerals"), EMPTY);

    assertEquals(
        List.of(1),
        DepositRouting.order(List.of(byTag), TERMINAL, DIAMOND, Set.of("ores_and_minerals"))
            .stream()
            .map(routed -> routed.container().getX())
            .toList());
  }

  @Test
  void containersOnTheSameRungAreOrderedNearestToTheTerminalFirst() {
    List<DepositTarget> candidates =
        List.of(general(9, EMPTY), general(2, EMPTY), general(5, EMPTY));

    assertEquals(List.of(2, 5, 9), order(candidates));
  }

  @Test
  void containersTheSameDistanceAwayAreOrderedByPositionSoTheAnswerIsStable() {
    DepositTarget east =
        new DepositTarget(
            new BlockPos(3, 64, 0), ContainerRole.GENERAL, ContainerFilter.NONE, EMPTY);
    DepositTarget west =
        new DepositTarget(
            new BlockPos(-3, 64, 0), ContainerRole.GENERAL, ContainerFilter.NONE, EMPTY);

    List<DepositTarget> forwards =
        DepositRouting.order(List.of(east, west), TERMINAL, COBBLESTONE, NO_TAGS);
    List<DepositTarget> backwards =
        DepositRouting.order(List.of(west, east), TERMINAL, COBBLESTONE, NO_TAGS);

    assertEquals(forwards, backwards);
  }

  @Test
  void aNetworkWithNowhereToPutItRoutesToNothingRatherThanAnywhere() {
    assertTrue(order(List.of(excluded(1))).isEmpty());
    assertTrue(order(List.of()).isEmpty());
    assertTrue(DepositRouting.order(null, TERMINAL, COBBLESTONE, NO_TAGS).isEmpty());
  }

  @Test
  void anItemFiledUnderNothingIsStillRoutedToGeneralContainers() {
    assertEquals(
        List.of(1),
        DepositRouting.order(List.of(general(1, EMPTY)), TERMINAL, DIAMOND, null).stream()
            .map(routed -> routed.container().getX())
            .toList());
  }
}
