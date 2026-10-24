package com.grahambartley.totalstorage.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TerminalAddressTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final RegistryKey<World> NETHER =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "the_nether"));

  @Test
  void sameWorldAndPositionAreEqualAndShareHashCode() {
    TerminalAddress first = new TerminalAddress(OVERWORLD, new BlockPos(8, 70, -3));
    TerminalAddress second = new TerminalAddress(OVERWORLD, new BlockPos(8, 70, -3));

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @ParameterizedTest
  @CsvSource({"9, 70, -3", "8, 71, -3", "8, 70, -2"})
  void differentPositionsAreNotEqual(int x, int y, int z) {
    TerminalAddress address = new TerminalAddress(OVERWORLD, new BlockPos(8, 70, -3));

    assertNotEquals(address, new TerminalAddress(OVERWORLD, new BlockPos(x, y, z)));
  }

  @Test
  void theSamePositionInAnotherWorldIsAnotherTerminal() {
    TerminalAddress overworld = new TerminalAddress(OVERWORLD, new BlockPos(8, 70, -3));
    TerminalAddress nether = new TerminalAddress(NETHER, new BlockPos(8, 70, -3));

    assertNotEquals(overworld, nether);
  }

  @Test
  void mutatingThePositionItWasBuiltFromDoesNotMoveTheAddress() {
    BlockPos.Mutable moving = new BlockPos.Mutable(8, 70, -3);
    TerminalAddress address = new TerminalAddress(OVERWORLD, moving);
    Map<TerminalAddress, String> networks = new HashMap<>();
    networks.put(address, "network");

    moving.set(100, 100, 100);

    assertEquals("network", networks.get(new TerminalAddress(OVERWORLD, new BlockPos(8, 70, -3))));
  }

  @Test
  void ofTakesTheWorldsOwnKey() {
    World world = mock(World.class);
    when(world.getRegistryKey()).thenReturn(NETHER);

    assertEquals(
        new TerminalAddress(NETHER, new BlockPos(1, 2, 3)),
        TerminalAddress.of(world, new BlockPos(1, 2, 3)));
  }

  @Test
  void describeNamesTheWorldAndThePosition() {
    TerminalAddress address = new TerminalAddress(OVERWORLD, new BlockPos(8, 70, -3));

    assertEquals("minecraft:overworld [8, 70, -3]", address.describe());
  }

  @Test
  void anAddressNeedsBothAWorldAndAPosition() {
    assertThrows(
        NullPointerException.class, () -> new TerminalAddress(null, new BlockPos(1, 2, 3)));
    assertThrows(NullPointerException.class, () -> new TerminalAddress(OVERWORLD, null));
    assertThrows(NullPointerException.class, () -> TerminalAddress.of(null, new BlockPos(1, 2, 3)));
  }
}
