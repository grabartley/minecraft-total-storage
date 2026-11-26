package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.totalstorage.filter.FilterCandidate;
import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.ContainerAssignment;
import com.grahambartley.totalstorage.storage.ContainerFilter;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.storage.StorageProvider;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class QuickStackTargetTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));

  static Stream<Arguments> assignments() {
    return Stream.of(
        Arguments.of(ContainerAssignment.DEFAULT, "minecraft:diamond", true),
        Arguments.of(ContainerAssignment.of(ContainerRole.EXCLUDED), "minecraft:diamond", false),
        Arguments.of(
            ContainerAssignment.filtered(ContainerFilter.ofItems("minecraft:diamond")),
            "minecraft:diamond",
            true),
        Arguments.of(
            ContainerAssignment.filtered(ContainerFilter.ofItems("minecraft:diamond")),
            "minecraft:coal",
            false));
  }

  @ParameterizedTest(name = "{0} accepts {1}: {2}")
  @MethodSource("assignments")
  void aTargetAcceptsWhatItsAssignmentAccepts(
      ContainerAssignment assignment, String itemId, boolean expected) {
    QuickStackTarget target =
        QuickStackTarget.of(providerHolding(List.of()), assignment, NetworkTagSet.defaults());

    assertEquals(expected, target.accepts(new FilterCandidate(itemId, itemId, Set.of())));
  }

  @SuppressWarnings("unchecked")
  private static StorageProvider providerHolding(List<StorageView<ItemVariant>> views) {
    Storage<ItemVariant> storage = mock(Storage.class);
    when(storage.nonEmptyViews()).thenReturn(views);
    return StorageProvider.of(new BlockStorageOrigin(OVERWORLD, new BlockPos(1, 64, 1)), storage);
  }
}
