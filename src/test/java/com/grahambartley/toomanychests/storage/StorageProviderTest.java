package com.grahambartley.toomanychests.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class StorageProviderTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final StorageOrigin ORIGIN =
      new BlockStorageOrigin(OVERWORLD, new BlockPos(0, 64, 0));

  private static StorageProvider providerOver(Storage<ItemVariant> storage) {
    return new StorageProvider(ORIGIN, storage, ContainerRole.DEFAULT);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 9, 27, 54})
  void slotCountMatchesTheBackingSlottedStorage(int slots) {
    StorageProvider provider = providerOver(new FakeSlottedStorage(slots));

    assertEquals(OptionalInt.of(slots), provider.slotCount());
    assertTrue(provider.isSlotted());
  }

  @Test
  void aCombinedStorageIsOneProviderReportingBothHalves() {
    CombinedSlottedStorage<ItemVariant, FakeSlottedStorage> doubleChest =
        new CombinedSlottedStorage<>(
            List.of(new FakeSlottedStorage(27), new FakeSlottedStorage(27)));

    StorageProvider provider = providerOver(doubleChest);

    assertEquals(OptionalInt.of(54), provider.slotCount());
  }

  @Test
  void slotCountIsEmptyForAStorageWithoutSlots() {
    StorageProvider provider = providerOver(new FakeModdedStorage());

    assertEquals(OptionalInt.empty(), provider.slotCount());
    assertFalse(provider.isSlotted());
  }

  @Test
  void slotReturnsTheRequestedSlotOfTheBackingStorage() {
    FakeSlottedStorage storage = new FakeSlottedStorage(27);
    StorageProvider provider = providerOver(storage);

    assertSame(storage.getSlot(13), provider.slot(13).orElseThrow());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 27, 100})
  void slotIsEmptyOutsideTheStorageBounds(int index) {
    StorageProvider provider = providerOver(new FakeSlottedStorage(27));

    assertTrue(provider.slot(index).isEmpty());
  }

  @Test
  void slotIsEmptyForAStorageWithoutSlots() {
    StorageProvider provider = providerOver(new FakeModdedStorage());

    assertTrue(provider.slot(0).isEmpty());
  }

  @Test
  void ofAppliesTheDefaultRole() {
    StorageProvider provider = StorageProvider.of(ORIGIN, new FakeSlottedStorage(27));

    assertEquals(ContainerRole.DEFAULT, provider.role());
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void withRoleKeepsOriginAndStorage(ContainerRole role) {
    FakeSlottedStorage storage = new FakeSlottedStorage(27);
    StorageProvider provider = StorageProvider.of(ORIGIN, storage).withRole(role);

    assertEquals(role, provider.role());
    assertEquals(ORIGIN, provider.origin());
    assertSame(storage, provider.storage());
  }

  @Test
  void withRoleReturnsTheSameProviderWhenTheRoleIsUnchanged() {
    StorageProvider provider = StorageProvider.of(ORIGIN, new FakeSlottedStorage(27));

    assertSame(provider, provider.withRole(ContainerRole.DEFAULT));
  }

  @Test
  void nullOriginIsRejected() {
    FakeSlottedStorage storage = new FakeSlottedStorage(1);

    assertThrows(
        NullPointerException.class,
        () -> new StorageProvider(null, storage, ContainerRole.GENERAL));
  }

  @Test
  void nullStorageIsRejected() {
    assertThrows(
        NullPointerException.class, () -> new StorageProvider(ORIGIN, null, ContainerRole.GENERAL));
  }

  @Test
  void nullRoleIsRejected() {
    FakeSlottedStorage storage = new FakeSlottedStorage(1);

    assertThrows(NullPointerException.class, () -> new StorageProvider(ORIGIN, storage, null));
  }
}
