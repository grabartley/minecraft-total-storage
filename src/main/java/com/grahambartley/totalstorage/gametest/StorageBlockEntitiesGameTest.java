package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.ModBlocks;
import com.grahambartley.totalstorage.storage.StorageBlockEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class StorageBlockEntitiesGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "total-storage:container_pad";
  private static final String BATCH = "storage-block-entities";
  private static final BlockPos SPOT = new BlockPos(3, 1, 3);

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 20)
  public void aChestMayHoldItems(TestContext context) {
    assertMayHoldItems(context, Blocks.CHEST, true);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 20)
  public void aBarrelMayHoldItems(TestContext context) {
    assertMayHoldItems(context, Blocks.BARREL, true);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 20)
  public void aShulkerBoxMayHoldItems(TestContext context) {
    assertMayHoldItems(context, Blocks.SHULKER_BOX, true);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 20)
  public void aSignHoldsNoItems(TestContext context) {
    assertMayHoldItems(context, Blocks.OAK_SIGN, false);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 20)
  public void aMovingPistonHoldsNoItems(TestContext context) {
    BlockEntity moving =
        PistonExtensionBlock.createBlockEntityPiston(
            context.getAbsolutePos(SPOT),
            Blocks.MOVING_PISTON.getDefaultState(),
            Blocks.STONE.getDefaultState(),
            Direction.UP,
            true,
            false);
    context.assertFalse(
        StorageBlockEntities.mayHoldItems(moving), "A piston push should not count as storage");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 20)
  public void theTerminalHoldsNoItems(TestContext context) {
    assertMayHoldItems(context, ModBlocks.TERMINAL, false);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = 20)
  public void nothingAtAllHoldsNoItems(TestContext context) {
    context.assertFalse(StorageBlockEntities.mayHoldItems(null), "No block entity holds items");
    context.complete();
  }

  private static void assertMayHoldItems(TestContext context, Block block, boolean expected) {
    context.setBlockState(SPOT, block.getDefaultState());
    BlockEntity blockEntity = context.getBlockEntity(SPOT);
    context.assertTrue(blockEntity != null, "The test needs a block entity at the spot");
    context.assertTrue(
        StorageBlockEntities.mayHoldItems(blockEntity) == expected,
        block + " should " + (expected ? "" : "not ") + "count as somewhere items may be held");
    context.complete();
  }
}
