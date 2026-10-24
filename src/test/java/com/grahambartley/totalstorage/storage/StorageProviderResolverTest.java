package com.grahambartley.totalstorage.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StorageProviderResolverTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final RegistryKey<World> NETHER =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "the_nether"));
  private static final BlockPos POS = new BlockPos(12, 70, -3);

  private World world;

  @BeforeEach
  void setUp() {
    world = mock(World.class);
    when(world.getRegistryKey()).thenReturn(OVERWORLD);
  }

  private static StorageProviderResolver resolverFinding(Storage<ItemVariant> storage) {
    return new StorageProviderResolver((w, p) -> storage);
  }

  @Test
  void aPositionWithNoContainerResolvesToNothing() {
    StorageProviderResolver resolver = resolverFinding(null);

    assertTrue(resolver.resolve(world, POS).isEmpty());
  }

  @Test
  void aResolvedContainerCarriesItsWorldPositionAsOrigin() {
    StorageProvider provider =
        resolverFinding(new FakeSlottedStorage(27)).resolve(world, POS).orElseThrow();

    assertEquals(new BlockStorageOrigin(OVERWORLD, POS), provider.origin());
  }

  @Test
  void aResolvedContainerExposesItsSlotCount() {
    StorageProvider provider =
        resolverFinding(new FakeSlottedStorage(27)).resolve(world, POS).orElseThrow();

    assertEquals(OptionalInt.of(27), provider.slotCount());
  }

  @Test
  void aThirdPartyContainerWithoutSlotsStillResolves() {
    FakeModdedStorage modded = new FakeModdedStorage();

    StorageProvider provider = resolverFinding(modded).resolve(world, POS).orElseThrow();

    assertSame(modded, provider.storage());
    assertEquals(OptionalInt.empty(), provider.slotCount());
  }

  @Test
  void resolvingWithoutARoleUsesTheDefault() {
    StorageProvider provider =
        resolverFinding(new FakeSlottedStorage(27)).resolve(world, POS).orElseThrow();

    assertEquals(ContainerRole.DEFAULT, provider.role());
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void resolvingWithARoleKeepsIt(ContainerRole role) {
    StorageProvider provider =
        resolverFinding(new FakeSlottedStorage(27)).resolve(world, POS, role).orElseThrow();

    assertEquals(role, provider.role());
  }

  @Test
  void aMutableScanCursorDoesNotLeakIntoTheOrigin() {
    BlockPos.Mutable cursor = new BlockPos.Mutable(12, 70, -3);
    StorageProvider provider =
        resolverFinding(new FakeSlottedStorage(27)).resolve(world, cursor).orElseThrow();

    cursor.set(0, 0, 0);

    assertEquals(new BlockStorageOrigin(OVERWORLD, POS), provider.origin());
  }

  @Test
  void nullWorldIsRejected() {
    StorageProviderResolver resolver = resolverFinding(new FakeSlottedStorage(27));

    assertThrows(NullPointerException.class, () -> resolver.resolve(null, POS));
  }

  @Test
  void nullPositionIsRejected() {
    StorageProviderResolver resolver = resolverFinding(new FakeSlottedStorage(27));

    assertThrows(NullPointerException.class, () -> resolver.resolve(world, null));
  }

  @Test
  void nullRoleIsRejected() {
    StorageProviderResolver resolver = resolverFinding(new FakeSlottedStorage(27));

    assertThrows(NullPointerException.class, () -> resolver.resolve(world, POS, null));
  }

  @Test
  void nullLookupIsRejected() {
    assertThrows(NullPointerException.class, () -> new StorageProviderResolver(null));
  }

  @Test
  void revalidatingReplacesTheStorageHeldFromAnEarlierTick() {
    FakeSlottedStorage stale = new FakeSlottedStorage(27);
    FakeSlottedStorage fresh = new FakeSlottedStorage(54);
    AtomicReference<Storage<ItemVariant>> next = new AtomicReference<>(stale);
    StorageProviderResolver resolver = new StorageProviderResolver((w, p) -> next.get());
    StorageProvider held = resolver.resolve(world, POS).orElseThrow();

    next.set(fresh);
    StorageProvider revalidated = resolver.revalidate(world, held).orElseThrow();

    assertSame(fresh, revalidated.storage());
  }

  @Test
  void revalidatingKeepsTheRole() {
    StorageProviderResolver resolver = resolverFinding(new FakeSlottedStorage(27));
    StorageProvider held = resolver.resolve(world, POS, ContainerRole.EXCLUDED).orElseThrow();

    assertEquals(ContainerRole.EXCLUDED, resolver.revalidate(world, held).orElseThrow().role());
  }

  @Test
  void revalidatingAContainerThatIsGoneResolvesToNothing() {
    AtomicReference<Storage<ItemVariant>> next = new AtomicReference<>(new FakeSlottedStorage(27));
    StorageProviderResolver resolver = new StorageProviderResolver((w, p) -> next.get());
    StorageProvider held = resolver.resolve(world, POS).orElseThrow();

    next.set(null);

    assertTrue(resolver.revalidate(world, held).isEmpty());
  }

  @Test
  void revalidatingAgainstADifferentWorldResolvesToNothing() {
    StorageProviderResolver resolver = resolverFinding(new FakeSlottedStorage(27));
    StorageProvider held = resolver.resolve(world, POS).orElseThrow();
    World otherWorld = mock(World.class);
    when(otherWorld.getRegistryKey()).thenReturn(NETHER);

    assertTrue(resolver.revalidate(otherWorld, held).isEmpty());
  }

  @Test
  void revalidatingANullProviderIsRejected() {
    StorageProviderResolver resolver = resolverFinding(new FakeSlottedStorage(27));

    assertThrows(NullPointerException.class, () -> resolver.revalidate(world, null));
  }
}
