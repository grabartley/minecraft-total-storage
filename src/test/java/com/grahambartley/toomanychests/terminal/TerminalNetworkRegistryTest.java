package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class TerminalNetworkRegistryTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));

  private static final TerminalAddress FIRST =
      new TerminalAddress(OVERWORLD, new BlockPos(0, 64, 0));
  private static final TerminalAddress SECOND =
      new TerminalAddress(OVERWORLD, new BlockPos(9, 64, 9));

  private final TerminalNetworkRegistry<String> registry = new TerminalNetworkRegistry<>();

  @Test
  void aRegistryStartsWithNothingRunning() {
    assertEquals(0, registry.activeCount());
    assertFalse(registry.isActive(FIRST));
    assertEquals(Optional.empty(), registry.find(FIRST));
  }

  @Test
  void anActivatedTerminalIsFoundByItsAddress() {
    registry.activate(FIRST, "network");

    assertTrue(registry.isActive(FIRST));
    assertEquals(Optional.of("network"), registry.find(FIRST));
    assertEquals(1, registry.activeCount());
  }

  @Test
  void terminalsAreTrackedIndependently() {
    registry.activate(FIRST, "first");
    registry.activate(SECOND, "second");

    registry.deactivate(FIRST);

    assertFalse(registry.isActive(FIRST));
    assertEquals(Optional.of("second"), registry.find(SECOND));
  }

  @Test
  void activatingAnAddressTwiceHandsBackWhatItReplaced() {
    registry.activate(FIRST, "first");

    Optional<String> replaced = registry.activate(FIRST, "second");

    assertEquals(Optional.of("first"), replaced);
    assertEquals(Optional.of("second"), registry.find(FIRST));
    assertEquals(1, registry.activeCount());
  }

  @Test
  void theFirstActivationOfAnAddressReplacesNothing() {
    assertEquals(Optional.empty(), registry.activate(FIRST, "network"));
  }

  @Test
  void deactivatingHandsBackTheNetworkSoItCanBeToldToStop() {
    registry.activate(FIRST, "network");

    assertEquals(Optional.of("network"), registry.deactivate(FIRST));
    assertFalse(registry.isActive(FIRST));
    assertEquals(0, registry.activeCount());
  }

  @Test
  void deactivatingATerminalThatWasNeverOnHandsBackNothing() {
    assertEquals(Optional.empty(), registry.deactivate(FIRST));
  }

  @Test
  void deactivatingTwiceHandsBackTheNetworkOnlyOnce() {
    registry.activate(FIRST, "network");
    registry.deactivate(FIRST);

    assertEquals(Optional.empty(), registry.deactivate(FIRST));
  }

  @Test
  void addressesListEveryRunningTerminal() {
    registry.activate(FIRST, "first");
    registry.activate(SECOND, "second");

    assertEquals(List.of(FIRST, SECOND), registry.addresses());
  }

  @Test
  void clearEmptiesTheRegistryAndHandsBackEveryNetwork() {
    registry.activate(FIRST, "first");
    registry.activate(SECOND, "second");

    List<String> cleared = registry.clear();

    assertEquals(List.of("first", "second"), cleared);
    assertEquals(0, registry.activeCount());
    assertEquals(List.of(), registry.clear());
  }

  @Test
  void aNullAddressIsUnknownRatherThanAFailure() {
    registry.activate(FIRST, "network");

    assertFalse(registry.isActive(null));
    assertEquals(Optional.empty(), registry.find(null));
    assertEquals(Optional.empty(), registry.deactivate(null));
    assertEquals(1, registry.activeCount());
  }

  @Test
  void anActivationNeedsBothAnAddressAndANetwork() {
    assertThrows(NullPointerException.class, () -> registry.activate(null, "network"));
    assertThrows(NullPointerException.class, () -> registry.activate(FIRST, null));
  }
}
