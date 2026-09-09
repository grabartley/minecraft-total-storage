package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.storage.NetworkEligibility;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class NetworkEligibilityGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String CLASSIFY_BATCH = "eligibility-classify";
  private static final String CONFIG_BATCH = "eligibility-config";
  private static final int TICK_LIMIT = 20;

  private static final BlockPos CONTAINER = new BlockPos(1, 1, 1);
  private static final BlockPos CHEST_LEFT = new BlockPos(1, 1, 3);
  private static final BlockPos CHEST_RIGHT = new BlockPos(2, 1, 3);

  private static final List<Block> ELIGIBLE =
      List.of(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.SHULKER_BOX);

  private static final List<Block> INELIGIBLE =
      List.of(
          Blocks.FURNACE,
          Blocks.BLAST_FURNACE,
          Blocks.SMOKER,
          Blocks.BREWING_STAND,
          Blocks.HOPPER,
          Blocks.DISPENSER,
          Blocks.DROPPER,
          Blocks.CRAFTER,
          Blocks.ENDER_CHEST,
          Blocks.CHISELED_BOOKSHELF,
          Blocks.JUKEBOX,
          Blocks.DECORATED_POT);

  private final NetworkEligibility eligibility = new NetworkEligibility();

  @CustomTestProvider
  public Collection<TestFunction> everyNamedBlockIsClassifiedCorrectly() {
    return java.util.stream.Stream.concat(
            ELIGIBLE.stream().map(block -> classificationCase(block, true)),
            INELIGIBLE.stream().map(block -> classificationCase(block, false)))
        .toList();
  }

  private TestFunction classificationCase(Block block, boolean expected) {
    return new TestFunction(
        CLASSIFY_BATCH,
        "networkeligibility."
            + (expected ? "eligible." : "ineligible.")
            + Registries.BLOCK.getId(block).getPath(),
        CONTAINER_PAD,
        TICK_LIMIT,
        0L,
        true,
        context -> assertClassification(context, block, expected));
  }

  private void assertClassification(TestContext context, Block block, boolean expected) {
    context.setBlockState(CONTAINER, block.getDefaultState());

    boolean actual = eligibility.isEligible(context.getWorld(), context.getAbsolutePos(CONTAINER));

    context.assertTrue(
        actual == expected,
        Registries.BLOCK.getId(block)
            + " should be "
            + (expected ? "eligible" : "ineligible")
            + " but was not");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = CLASSIFY_BATCH, tickLimit = TICK_LIMIT)
  public void aDoubleChestIsEligibleFromEitherHalf(TestContext context) {
    placeDoubleChest(context);

    boolean left = eligibility.isEligible(context.getWorld(), context.getAbsolutePos(CHEST_LEFT));
    boolean right = eligibility.isEligible(context.getWorld(), context.getAbsolutePos(CHEST_RIGHT));

    context.assertTrue(left && right, "Both halves of a double chest should be eligible");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = CLASSIFY_BATCH, tickLimit = TICK_LIMIT)
  public void aPositionHoldingNoContainerIsNotEligible(TestContext context) {
    context.setBlockState(CONTAINER, Blocks.STONE.getDefaultState());

    context.assertTrue(
        !eligibility.isEligible(context.getWorld(), context.getAbsolutePos(CONTAINER)),
        "A block with no item storage should never be eligible");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = CONFIG_BATCH, tickLimit = TICK_LIMIT)
  public void anOperatorCanAdmitABlockTheDefaultsDeny(TestContext context) {
    MinecraftServer server = context.getWorld().getServer();
    TooManyChestsConfig original = ServerConfigService.current();
    context.setBlockState(CONTAINER, Blocks.FURNACE.getDefaultState());

    boolean before = isEligibleHere(context);
    ServerConfigService.update(server, override(original, "minecraft:furnace", true));
    boolean after = isEligibleHere(context);
    ServerConfigService.update(server, original);

    context.assertTrue(!before, "A furnace should be ineligible by default");
    context.assertTrue(after, "An operator override should admit the furnace with no restart");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = CONFIG_BATCH, tickLimit = TICK_LIMIT)
  public void anOperatorCanExcludeABlockTheDefaultsAdmit(TestContext context) {
    MinecraftServer server = context.getWorld().getServer();
    TooManyChestsConfig original = ServerConfigService.current();
    context.setBlockState(CONTAINER, Blocks.BARREL.getDefaultState());

    boolean before = isEligibleHere(context);
    ServerConfigService.update(server, override(original, "minecraft:barrel", false));
    boolean after = isEligibleHere(context);
    ServerConfigService.update(server, original);

    context.assertTrue(before, "A barrel should be eligible by default");
    context.assertTrue(!after, "An operator override should exclude the barrel with no restart");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = CONFIG_BATCH, tickLimit = TICK_LIMIT)
  public void withdrawingAnOverrideRestoresTheDefaultAnswerImmediately(TestContext context) {
    MinecraftServer server = context.getWorld().getServer();
    TooManyChestsConfig original = ServerConfigService.current();
    context.setBlockState(CONTAINER, Blocks.BARREL.getDefaultState());

    ServerConfigService.update(server, override(original, "minecraft:barrel", false));
    boolean whileExcluded = isEligibleHere(context);
    ServerConfigService.update(server, original);
    boolean afterWithdrawal = isEligibleHere(context);

    context.assertTrue(!whileExcluded, "The override should have excluded the barrel");
    context.assertTrue(
        afterWithdrawal,
        "Eligibility is read fresh, so withdrawing an override should take effect at once");
    context.complete();
  }

  private boolean isEligibleHere(TestContext context) {
    return eligibility.isEligible(context.getWorld(), context.getAbsolutePos(CONTAINER));
  }

  private static TooManyChestsConfig override(
      TooManyChestsConfig base, String blockId, boolean eligible) {
    return base.withContainerEligibilityOverrides(Map.of(blockId, eligible));
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
