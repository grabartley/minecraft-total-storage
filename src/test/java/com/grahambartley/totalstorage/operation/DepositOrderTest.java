package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.totalstorage.filter.FilterCandidate;
import com.grahambartley.totalstorage.index.NetworkIndexBuilder;
import com.grahambartley.totalstorage.index.NetworkIndexOrdering;
import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.ContainerAssignment;
import com.grahambartley.totalstorage.storage.ContainerFilter;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DepositOrderTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final BlockPos TERMINAL = new BlockPos(0, 64, 0);
  private static final BlockPos COBBLE_CHEST = new BlockPos(1, 64, 0);
  private static final BlockPos EMPTY_NEAR = new BlockPos(2, 64, 0);
  private static final BlockPos EMPTY_FAR = new BlockPos(3, 64, 0);
  private static final BlockPos FILTERED_EMPTY = new BlockPos(4, 64, 0);
  private static final BlockPos EXCLUDED_EMPTY = new BlockPos(-1, 64, 0);
  private static final String COBBLESTONE = "minecraft:cobblestone";
  private static final String DIAMOND = "minecraft:diamond";
  private static final String EMERALD = "minecraft:emerald";

  static Stream<Arguments> firstChoices() {
    return Stream.of(
        Arguments.of("a held item tops up its chest", COBBLESTONE, COBBLE_CHEST),
        Arguments.of("a new item takes the nearest empty chest", DIAMOND, EMPTY_NEAR),
        Arguments.of(
            "a filtered chest that wants it comes before any general one",
            EMERALD,
            FILTERED_EMPTY));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("firstChoices")
  void theFirstChoiceFollowsTheRoutingOrder(String rule, String item, BlockPos expected) {
    assertEquals(origin(expected), order().forVariant(item).get(0));
  }

  static Stream<Arguments> fullOrders() {
    return Stream.of(
        Arguments.of(DIAMOND, List.of(EMPTY_NEAR, EMPTY_FAR, COBBLE_CHEST)),
        Arguments.of(COBBLESTONE, List.of(COBBLE_CHEST, EMPTY_NEAR, EMPTY_FAR)),
        Arguments.of(EMERALD, List.of(FILTERED_EMPTY, EMPTY_NEAR, EMPTY_FAR, COBBLE_CHEST)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fullOrders")
  void anExcludedChestIsNeverOfferedAndEveryOtherIsInRungOrder(
      String item, List<BlockPos> expected) {
    assertEquals(
        expected.stream().map(DepositOrderTest::origin).toList(), order().forVariant(item));
  }

  @ParameterizedTest(name = "{0} stores")
  @ValueSource(ints = {1, 2, 5})
  void toppingUpAChestThatAlreadyHoldsTheItemKeepsTheWorkedOutOrder(int stores) {
    DepositOrder<String> order = order();
    List<StorageOrigin> before = order.forVariant(DIAMOND);

    for (int store = 0; store < stores; store++) {
      order.stored(origin(COBBLE_CHEST), COBBLESTONE);
    }

    assertSame(before, order.forVariant(DIAMOND));
  }

  static Stream<Arguments> storesThatChangeTheOrder() {
    return Stream.of(
        Arguments.of(
            DIAMOND, EMERALD, List.of(FILTERED_EMPTY, EMPTY_FAR, COBBLE_CHEST, EMPTY_NEAR)),
        Arguments.of(DIAMOND, DIAMOND, List.of(EMPTY_NEAR, EMPTY_FAR, COBBLE_CHEST)),
        Arguments.of(COBBLESTONE, DIAMOND, List.of(EMPTY_FAR, COBBLE_CHEST, EMPTY_NEAR)));
  }

  @ParameterizedTest(name = "after {0} goes into the nearest empty chest, {1} goes {2}")
  @MethodSource("storesThatChangeTheOrder")
  void aChestFilledDuringTheDepositIsNoLongerOfferedAsEmpty(
      String stored, String next, List<BlockPos> expected) {
    DepositOrder<String> order = order();
    List<StorageOrigin> before = order.forVariant(next);

    order.stored(origin(EMPTY_NEAR), stored);

    List<StorageOrigin> after = order.forVariant(next);
    assertNotSame(before, after);
    assertEquals(expected.stream().map(DepositOrderTest::origin).toList(), after);
  }

  private static DepositOrder<String> order() {
    Map<BlockPos, StorageOrigin> containers = new LinkedHashMap<>();
    for (BlockPos pos :
        List.of(COBBLE_CHEST, EMPTY_NEAR, EMPTY_FAR, FILTERED_EMPTY, EXCLUDED_EMPTY)) {
      containers.put(pos, origin(pos));
    }
    TerminalSettings settings =
        TerminalSettings.defaults()
            .withContainers(
                TerminalSettings.defaults()
                    .containers()
                    .withRole(EXCLUDED_EMPTY, ContainerRole.EXCLUDED)
                    .with(
                        FILTERED_EMPTY,
                        ContainerAssignment.filtered(ContainerFilter.ofItems(EMERALD))));
    return new DepositOrder<>(
        TERMINAL,
        containers,
        settings,
        new ContainerOccupancy<>(
            new NetworkIndexBuilder<String>()
                .add(COBBLESTONE, 64L, origin(COBBLE_CHEST))
                .build(NetworkIndexOrdering.byKey(Function.identity()))),
        item -> new FilterCandidate(item, item, Set.of()));
  }

  private static StorageOrigin origin(BlockPos pos) {
    return new BlockStorageOrigin(OVERWORLD, pos);
  }
}
