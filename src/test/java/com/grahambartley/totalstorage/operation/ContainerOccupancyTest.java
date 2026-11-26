package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.totalstorage.index.NetworkIndex;
import com.grahambartley.totalstorage.index.NetworkIndexBuilder;
import com.grahambartley.totalstorage.index.NetworkIndexOrdering;
import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.StorageOrigin;
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

class ContainerOccupancyTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final StorageOrigin COBBLE_CHEST = at(1);
  private static final StorageOrigin MIXED_CHEST = at(2);
  private static final StorageOrigin EMPTY_CHEST = at(3);

  private final ContainerOccupancy<String> occupancy = new ContainerOccupancy<>(index());

  static Stream<Arguments> indexedContents() {
    return Stream.of(
        Arguments.of(COBBLE_CHEST, "cobblestone", ContainerContents.HOLDS_ITEM),
        Arguments.of(MIXED_CHEST, "cobblestone", ContainerContents.HOLDS_ITEM),
        Arguments.of(MIXED_CHEST, "torch", ContainerContents.HOLDS_ITEM),
        Arguments.of(COBBLE_CHEST, "diamond", ContainerContents.OTHER_ITEMS),
        Arguments.of(MIXED_CHEST, "diamond", ContainerContents.OTHER_ITEMS),
        Arguments.of(EMPTY_CHEST, "diamond", ContainerContents.EMPTY),
        Arguments.of(EMPTY_CHEST, "cobblestone", ContainerContents.EMPTY));
  }

  @ParameterizedTest(name = "{0} for {1} is {2}")
  @MethodSource("indexedContents")
  void aContainerReadsAsTheIndexSawIt(
      StorageOrigin container, String variant, ContainerContents expected) {
    assertEquals(expected, occupancy.contentsOf(container, variant));
  }

  static Stream<Arguments> afterStoringDiamondsInTheEmptyChest() {
    return Stream.of(
        Arguments.of(EMPTY_CHEST, "diamond", ContainerContents.HOLDS_ITEM),
        Arguments.of(EMPTY_CHEST, "emerald", ContainerContents.OTHER_ITEMS),
        Arguments.of(EMPTY_CHEST, "cobblestone", ContainerContents.OTHER_ITEMS),
        Arguments.of(COBBLE_CHEST, "diamond", ContainerContents.OTHER_ITEMS));
  }

  @ParameterizedTest(name = "{0} for {1} is {2}")
  @MethodSource("afterStoringDiamondsInTheEmptyChest")
  void storingIntoAContainerCountsForEveryLaterItemInTheSameOperation(
      StorageOrigin container, String variant, ContainerContents expected) {
    occupancy.contentsOf(EMPTY_CHEST, "diamond");

    occupancy.stored(EMPTY_CHEST, "diamond");

    assertEquals(expected, occupancy.contentsOf(container, variant));
  }

  private static NetworkIndex<String> index() {
    return new NetworkIndexBuilder<String>()
        .add("cobblestone", 64L, COBBLE_CHEST)
        .add("cobblestone", 32L, MIXED_CHEST)
        .add("torch", 8L, MIXED_CHEST)
        .build(NetworkIndexOrdering.byKey(Function.identity()));
  }

  private static StorageOrigin at(int x) {
    return new BlockStorageOrigin(OVERWORLD, new BlockPos(x, 64, 0));
  }
}
