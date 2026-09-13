package com.grahambartley.toomanychests.sort;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ScreenSortPolicyTest {
  @ParameterizedTest
  @ValueSource(
      strings = {
        "crafting",
        "furnace",
        "blast_furnace",
        "smoker",
        "brewing_stand",
        "enchantment",
        "anvil",
        "smithing",
        "merchant",
        "beacon",
        "grindstone",
        "loom",
        "stonecutter",
        "cartography_table",
        "lectern",
        "crafter_3x3"
      })
  void aScreenWhoseSlotsMeanSomethingIsRefused(String path) {
    assertFalse(ScreenSortPolicy.allows(Identifier.ofVanilla(path)));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "generic_9x1",
        "generic_9x2",
        "generic_9x3",
        "generic_9x4",
        "generic_9x5",
        "generic_9x6",
        "generic_3x3",
        "hopper",
        "shulker_box"
      })
  void aScreenThatIsJustARowOfSlotsIsAllowed(String path) {
    assertTrue(ScreenSortPolicy.allows(Identifier.ofVanilla(path)));
  }

  @Test
  void aScreenWithNoRegisteredTypeIsRefused() {
    assertFalse(ScreenSortPolicy.allows(null));
  }

  @Test
  void anUnknownModdedScreenPassesThisGateAndIsLeftToTheStructuralCheck() {
    assertTrue(ScreenSortPolicy.allows(Identifier.of("someothermod", "crate")));
  }

  @Test
  void aModdedScreenSharingAVanillaPathIsNotCaughtByTheVanillaDenyList() {
    assertTrue(ScreenSortPolicy.allows(Identifier.of("someothermod", "furnace")));
  }

  @Test
  void theModsOwnTerminalIsLeftToItsOwnControls() {
    assertFalse(ScreenSortPolicy.allows(Identifier.of("too-many-chests", "terminal")));
  }

  @Test
  void aScreenWithNoHandlerAtAllIsRefused() {
    assertFalse(ScreenSortPolicy.allowsHandler(null));
    org.junit.jupiter.api.Assertions.assertNull(ScreenSortPolicy.typeIdOf(null));
  }

  @Test
  void theDenyListCannotBeChangedByCallers() {
    assertTrue(ScreenSortPolicy.deniedTypes().contains(Identifier.ofVanilla("anvil")));
    org.junit.jupiter.api.Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> ScreenSortPolicy.deniedTypes().add(Identifier.ofVanilla("generic_9x3")));
  }
}
