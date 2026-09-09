package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.Collection;
import java.util.List;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class TerminalsHoldingTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final RegistryKey<World> NETHER =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "the_nether"));

  private static final StorageOrigin SHARED = originAt(OVERWORLD, 0);
  private static final StorageOrigin NORTH_ONLY = originAt(OVERWORLD, 1);
  private static final StorageOrigin SOUTH_ONLY = originAt(OVERWORLD, 2);
  private static final StorageOrigin UNRELATED = originAt(OVERWORLD, 9);

  private static final TerminalAddress NORTH =
      new TerminalAddress(OVERWORLD, new BlockPos(0, 64, 0));
  private static final TerminalAddress SOUTH =
      new TerminalAddress(OVERWORLD, new BlockPos(0, 64, 8));
  private static final TerminalAddress ELSEWHERE =
      new TerminalAddress(NETHER, new BlockPos(0, 64, 0));

  private final TerminalNetworkRegistry<List<StorageOrigin>> registry =
      new TerminalNetworkRegistry<>();
  private final TerminalsHolding<List<StorageOrigin>> holding =
      new TerminalsHolding<>(registry, network -> network);

  @Test
  void aTerminalHoldingTheChangedContainerIsNamed() {
    registry.activate(NORTH, List.of(SHARED, NORTH_ONLY));

    assertEquals(List.of(NORTH), holding.any(List.of(SHARED)));
  }

  @Test
  void bothTerminalsWhoseRadiiOverlapOnAContainerAreNamed() {
    registry.activate(NORTH, List.of(SHARED, NORTH_ONLY));
    registry.activate(SOUTH, List.of(SHARED, SOUTH_ONLY));

    assertEquals(List.of(NORTH, SOUTH), holding.any(List.of(SHARED)));
  }

  @Test
  void aTerminalHoldingNoneOfThemIsLeftOut() {
    registry.activate(NORTH, List.of(NORTH_ONLY));
    registry.activate(SOUTH, List.of(SOUTH_ONLY));

    assertEquals(List.of(SOUTH), holding.any(List.of(SOUTH_ONLY)));
  }

  @Test
  void aContainerNoNetworkHoldsNamesNoTerminal() {
    registry.activate(NORTH, List.of(NORTH_ONLY));

    assertEquals(List.of(), holding.any(List.of(UNRELATED)));
  }

  @Test
  void aTerminalIsNamedOnceHoweverManyOfItsContainersChanged() {
    registry.activate(NORTH, List.of(SHARED, NORTH_ONLY));

    assertEquals(List.of(NORTH), holding.any(List.of(SHARED, NORTH_ONLY)));
  }

  @Test
  void aTerminalInAnotherWorldIsNotNamedByAPositionThatMatchesInThisOne() {
    registry.activate(ELSEWHERE, List.of(originAt(NETHER, 0)));

    assertEquals(List.of(), holding.any(List.of(SHARED)));
  }

  @Test
  void anUnpoweredTerminalIsNotNamedBecauseItIsNotInTheRegistry() {
    registry.activate(NORTH, List.of(SHARED));
    registry.deactivate(NORTH);

    assertTrue(holding.any(List.of(SHARED)).isEmpty());
  }

  @Test
  void anEmptyChangeSetNamesNobody() {
    registry.activate(NORTH, List.of(SHARED));

    assertEquals(List.of(), holding.any(List.of()));
    assertEquals(List.of(), holding.any(null));
  }

  @Test
  void aNetworkThatNamesNoContainersIsSkippedRatherThanThrowing() {
    final TerminalNetworkRegistry<String> silent = new TerminalNetworkRegistry<>();
    silent.activate(NORTH, "a network still starting up");

    assertEquals(List.of(), new TerminalsHolding<>(silent, network -> null).any(List.of(SHARED)));
  }

  @Test
  void aRegistryAndAWayToReadItAreBothRequired() {
    assertThrows(
        NullPointerException.class,
        () -> new TerminalsHolding<Collection<StorageOrigin>>(null, network -> network));
    assertThrows(NullPointerException.class, () -> new TerminalsHolding<>(registry, null));
  }

  private static StorageOrigin originAt(RegistryKey<World> worldKey, int x) {
    return new BlockStorageOrigin(worldKey, new BlockPos(x, 64, 0));
  }
}
