package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.StorageProvider;
import com.grahambartley.totalstorage.storage.StorageProviderResolver;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.registry.Registries;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class StorageProviderResolverGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "total-storage:container_pad";
  private static final int SINGLE_CONTAINER_SLOTS = 27;
  private static final int DOUBLE_CHEST_SLOTS = 54;
  private static final int TICK_LIMIT = 20;

  private static final List<Block> SINGLE_BLOCK_CONTAINERS =
      List.of(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.SHULKER_BOX);

  private static final BlockPos CONTAINER = new BlockPos(1, 1, 1);
  private static final BlockPos CHEST_LEFT = new BlockPos(1, 1, 3);
  private static final BlockPos CHEST_RIGHT = new BlockPos(2, 1, 3);
  private static final BlockPos NOTHING = new BlockPos(5, 1, 5);

  private final StorageProviderResolver resolver = new StorageProviderResolver();

  @CustomTestProvider
  public Collection<TestFunction> singleBlockContainersResolveToOneProvider() {
    return SINGLE_BLOCK_CONTAINERS.stream()
        .map(
            block ->
                new TestFunction(
                    "defaultBatch",
                    "storageproviderresolver.resolves." + Registries.BLOCK.getId(block).getPath(),
                    CONTAINER_PAD,
                    TICK_LIMIT,
                    0L,
                    true,
                    context -> resolvesWithSingleContainerSlots(context, block)))
        .toList();
  }

  private void resolvesWithSingleContainerSlots(TestContext context, Block block) {
    context.setBlockState(CONTAINER, block.getDefaultState());

    OptionalInt slots = resolveAt(context, CONTAINER).orElseThrow().slotCount();

    context.assertTrue(
        slots.equals(OptionalInt.of(SINGLE_CONTAINER_SLOTS)),
        Registries.BLOCK.getId(block)
            + " should resolve to "
            + SINGLE_CONTAINER_SLOTS
            + " slots but was "
            + slots);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, tickLimit = TICK_LIMIT)
  public void aDoubleChestResolvesToOneProviderWithBothHalvesSlots(TestContext context) {
    placeDoubleChest(context);

    OptionalInt slots = resolveAt(context, CHEST_LEFT).orElseThrow().slotCount();

    context.assertTrue(
        slots.equals(OptionalInt.of(DOUBLE_CHEST_SLOTS)),
        "A double chest should resolve to one provider with "
            + DOUBLE_CHEST_SLOTS
            + " slots but was "
            + slots);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, tickLimit = TICK_LIMIT)
  public void bothHalvesOfADoubleChestResolveToTheSameStorage(TestContext context) {
    placeDoubleChest(context);

    OptionalInt fromLeft = resolveAt(context, CHEST_LEFT).orElseThrow().slotCount();
    OptionalInt fromRight = resolveAt(context, CHEST_RIGHT).orElseThrow().slotCount();

    context.assertTrue(
        fromLeft.equals(OptionalInt.of(DOUBLE_CHEST_SLOTS))
            && fromRight.equals(OptionalInt.of(DOUBLE_CHEST_SLOTS)),
        "Either half of a double chest should see the whole chest, saw "
            + fromLeft
            + " and "
            + fromRight);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, tickLimit = TICK_LIMIT)
  public void aPositionWithNoContainerResolvesToNothing(TestContext context) {
    context.assertTrue(
        resolveAt(context, NOTHING).isEmpty(), "Empty air should resolve to no provider");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, tickLimit = TICK_LIMIT)
  public void aResolvedProviderCarriesThePositionItCameFrom(TestContext context) {
    context.setBlockState(CONTAINER, Blocks.BARREL.getDefaultState());
    World world = context.getWorld();
    BlockPos absolute = context.getAbsolutePos(CONTAINER);

    StorageProvider provider = resolveAt(context, CONTAINER).orElseThrow();

    context.assertTrue(
        provider.origin().equals(BlockStorageOrigin.of(world, absolute)),
        "Provider origin should be "
            + BlockStorageOrigin.of(world, absolute).describe()
            + " but was "
            + provider.origin().describe());
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, tickLimit = TICK_LIMIT)
  public void revalidatingAProviderWhoseContainerIsGoneResolvesToNothing(TestContext context) {
    context.setBlockState(CONTAINER, Blocks.BARREL.getDefaultState());
    StorageProvider held = resolveAt(context, CONTAINER).orElseThrow();

    context.setBlockState(CONTAINER, Blocks.AIR.getDefaultState());

    context.assertTrue(
        resolver.revalidate(context.getWorld(), held).isEmpty(),
        "A provider held past the removal of its container should not revalidate");
    context.complete();
  }

  private Optional<StorageProvider> resolveAt(TestContext context, BlockPos relativePos) {
    return resolver.resolve(context.getWorld(), context.getAbsolutePos(relativePos));
  }

  private static void placeDoubleChest(TestContext context) {
    BlockState left =
        Blocks.CHEST
            .getDefaultState()
            .with(ChestBlock.FACING, Direction.NORTH)
            .with(ChestBlock.CHEST_TYPE, ChestType.LEFT);
    BlockState right =
        Blocks.CHEST
            .getDefaultState()
            .with(ChestBlock.FACING, Direction.NORTH)
            .with(ChestBlock.CHEST_TYPE, ChestType.RIGHT);
    context.setBlockState(CHEST_LEFT, left);
    context.setBlockState(CHEST_RIGHT, right);
  }
}
