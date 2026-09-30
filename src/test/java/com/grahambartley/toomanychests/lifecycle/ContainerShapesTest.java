package com.grahambartley.toomanychests.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

class ContainerShapesTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final StorageOrigin CHEST =
      new BlockStorageOrigin(OVERWORLD, new BlockPos(0, 64, 0));
  private static final StorageOrigin BARREL =
      new BlockStorageOrigin(OVERWORLD, new BlockPos(4, 64, 0));

  private final ContainerShapes shapes = new ContainerShapes();

  @Test
  void theFirstLookReportsNothingAsReshaped() {
    assertTrue(shapes.reshaped(Map.of(CHEST, OptionalInt.of(27))).isEmpty());
  }

  @Test
  void aSingleChestBecomingADoubleChestIsReportedAsReshaped() {
    shapes.reshaped(Map.of(CHEST, OptionalInt.of(27), BARREL, OptionalInt.of(27)));

    List<StorageOrigin> changed =
        shapes.reshaped(Map.of(CHEST, OptionalInt.of(54), BARREL, OptionalInt.of(27)));

    assertEquals(List.of(CHEST), changed);
  }

  @Test
  void aContainerKeepingItsShapeIsNotReported() {
    shapes.reshaped(Map.of(BARREL, OptionalInt.of(27)));

    assertTrue(shapes.reshaped(Map.of(BARREL, OptionalInt.of(27))).isEmpty());
  }

  @Test
  void aContainerSeenForTheFirstTimeIsNotReshaped() {
    shapes.reshaped(Map.of(BARREL, OptionalInt.of(27)));

    assertTrue(
        shapes.reshaped(Map.of(BARREL, OptionalInt.of(27), CHEST, OptionalInt.of(54))).isEmpty());
  }

  @Test
  void aContainerThatGainsOrLosesSlotsAtAllIsReshaped() {
    shapes.reshaped(Map.of(CHEST, OptionalInt.empty()));

    assertEquals(List.of(CHEST), shapes.reshaped(Map.of(CHEST, OptionalInt.of(27))));
  }

  @Test
  void onlyTheLatestLookIsRemembered() {
    shapes.reshaped(Map.of(CHEST, OptionalInt.of(27)));
    shapes.reshaped(Map.of());

    assertTrue(shapes.reshaped(Map.of(CHEST, OptionalInt.of(54))).isEmpty());
  }

  @Test
  void clearingForgetsEveryShape() {
    shapes.reshaped(Map.of(CHEST, OptionalInt.of(27)));

    shapes.clear();

    assertTrue(shapes.reshaped(Map.of(CHEST, OptionalInt.of(54))).isEmpty());
  }
}
