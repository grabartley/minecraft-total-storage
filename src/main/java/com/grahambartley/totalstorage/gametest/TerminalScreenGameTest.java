package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.ModBlocks;
import com.grahambartley.totalstorage.access.TerminalAccessControl;
import com.grahambartley.totalstorage.access.TerminalViewers;
import com.grahambartley.totalstorage.block.entity.TerminalBlockEntity;
import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.screenhandler.TerminalOpenData;
import com.grahambartley.totalstorage.screenhandler.TerminalOpenDataCodec;
import com.grahambartley.totalstorage.screenhandler.TerminalPermission;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenFactory;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import com.grahambartley.totalstorage.screenhandler.TerminalViewerSync;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class TerminalScreenGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "total-storage:container_pad";
  private static final String BATCH = "terminal-screen";
  private static final int TICK_LIMIT = 60;

  private static final BlockPos TERMINAL = new BlockPos(3, 1, 3);
  private static final BlockPos POWER = new BlockPos(3, 1, 2);
  private static final BlockPos BARREL = new BlockPos(4, 1, 3);
  private static final BlockPos STANDING = new BlockPos(3, 1, 4);

  private static final int OVERSIZED_RADIUS = 64;

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPoweredTerminalOpensForItsOwner(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    poweredTerminalOwnedBy(context, owner);

    context.useBlock(TERMINAL, owner);

    context.assertTrue(
        owner.currentScreenHandler instanceof TerminalScreenHandler,
        "Right-clicking a powered terminal should open the terminal screen");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anUnpoweredTerminalOpensNothing(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    terminalOwnedBy(context, owner);

    context.useBlock(TERMINAL, owner);

    context.assertTrue(
        owner.currentScreenHandler == owner.playerScreenHandler,
        "An unpowered terminal must leave the player holding no screen");
    context.assertTrue(
        viewers().viewing(address(context)).isEmpty(),
        "A terminal that did not open must record no viewer");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aTerminalSomebodyElseOwnsOpensNothing(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    ServerPlayerEntity stranger = playerAtTheTerminal(context);
    poweredTerminalOwnedBy(context, owner);

    context.useBlock(TERMINAL, stranger);

    context.assertTrue(
        stranger.currentScreenHandler == stranger.playerScreenHandler,
        "An owner-locked terminal must leave a stranger holding no screen");
    context.assertTrue(
        viewers().viewing(address(context)).isEmpty(),
        "A refused player must not be recorded as viewing");
    finish(context, owner, stranger);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void openingRecordsThePlayerAsAViewer(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    poweredTerminalOwnedBy(context, owner);

    context.useBlock(TERMINAL, owner);

    context.assertTrue(
        viewers().isViewing(address(context), owner.getUuid()),
        "A player who opened the terminal should be recorded as viewing it");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void closingTheScreenStopsThePlayerBeingAViewer(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    poweredTerminalOwnedBy(context, owner);
    context.useBlock(TERMINAL, owner);

    owner.closeHandledScreen();

    context.assertFalse(
        viewers().isViewing(address(context), owner.getUuid()),
        "Closing the screen should leave no viewer entry behind");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void twoPlayersCanHaveTheSameTerminalOpenAtOnce(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    ServerPlayerEntity friend = playerAtTheTerminal(context);
    poweredTerminalOwnedBy(context, owner);
    openTo(context, TerminalAccessMode.OPEN);

    context.useBlock(TERMINAL, owner);
    context.useBlock(TERMINAL, friend);

    context.assertTrue(
        owner.currentScreenHandler instanceof TerminalScreenHandler
            && friend.currentScreenHandler instanceof TerminalScreenHandler,
        "Both players should be holding a terminal screen");
    context.assertTrue(
        owner.currentScreenHandler != friend.currentScreenHandler,
        "Each player should hold their own handler rather than sharing one");
    context.assertEquals(
        2,
        viewers().viewing(address(context)).size(),
        "Both players should be recorded as viewers");
    finish(context, owner, friend);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aResyncReachesEveryScreenShowingTheTerminal(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    ServerPlayerEntity friend = playerAtTheTerminal(context);
    poweredTerminalOwnedBy(context, owner);
    openTo(context, TerminalAccessMode.OPEN);
    context.useBlock(TERMINAL, owner);
    context.useBlock(TERMINAL, friend);

    int resynced = TerminalViewerSync.resync(server(context), address(context));

    context.assertEquals(2, resynced, "A resync should reach both open screens");
    finish(context, owner, friend);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPlayerWhoHasClosedTheirScreenIsNotResynced(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    ServerPlayerEntity friend = playerAtTheTerminal(context);
    poweredTerminalOwnedBy(context, owner);
    openTo(context, TerminalAccessMode.OPEN);
    context.useBlock(TERMINAL, owner);
    context.useBlock(TERMINAL, friend);

    friend.closeHandledScreen();
    int resynced = TerminalViewerSync.resync(server(context), address(context));

    context.assertEquals(1, resynced, "Only the screen still open should be resynced");
    finish(context, owner, friend);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void dyingClosesTheTerminalAndLeavesNoViewer(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    poweredTerminalOwnedBy(context, owner);
    context.useBlock(TERMINAL, owner);

    owner.kill();

    context.assertTrue(
        owner.currentScreenHandler == owner.playerScreenHandler,
        "A player who died should not still be holding a terminal screen");
    context.assertFalse(
        viewers().isViewing(address(context), owner.getUuid()),
        "A player who died should leave no viewer entry behind");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void cuttingPowerClosesTheScreenItLeftOpen(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    poweredTerminalOwnedBy(context, owner);
    context.useBlock(TERMINAL, owner);
    context.assertTrue(
        owner.currentScreenHandler instanceof TerminalScreenHandler,
        "The terminal should really have opened before power is cut");

    context.setBlockState(POWER, Blocks.AIR.getDefaultState());

    context.waitAndRun(
        5,
        () -> {
          context.assertTrue(
              owner.currentScreenHandler == owner.playerScreenHandler,
              "A terminal that lost power should close the screen it was showing");
          context.assertFalse(
              viewers().isViewing(address(context), owner.getUuid()),
              "A screen closed by power loss should leave no viewer entry");
          finish(context, owner);
        });
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theOpenPayloadDoesNotGrowWithWhatTheNetworkHolds(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    context.setBlockState(BARREL, Blocks.BARREL.getDefaultState());
    poweredTerminalOwnedBy(context, owner);
    int empty = openPayloadSize(context, owner);

    stockBarrel(context);
    TerminalNetworks.power().unloaded(context.getWorld(), absolute(context));
    TerminalNetworks.power()
        .loaded(context.getWorld(), absolute(context), context.getBlockState(TERMINAL));

    context.assertTrue(
        TerminalNetworks.power()
                .network(context.getWorld(), absolute(context))
                .orElseThrow()
                .index()
                .countOf(ItemVariant.of(Items.DIAMOND))
            > 0L,
        "The test needs the network to really be holding something");
    int stocked = openPayloadSize(context, owner);
    context.assertEquals(
        empty, stocked, "The open payload grew with the network's contents, which it must not");
    finish(context, owner);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theOwnerMayAdministerAndEverybodyElseMayNot(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    ServerPlayerEntity friend = playerAtTheTerminal(context);
    poweredTerminalOwnedBy(context, owner);
    openTo(context, TerminalAccessMode.OPEN);

    context.assertTrue(
        openDataFor(context, owner).permission() == TerminalPermission.ADMINISTER,
        "The owner should be given the permission that draws the access controls");
    context.assertTrue(
        openDataFor(context, friend).permission() == TerminalPermission.USE,
        "A player let in by an open terminal may use it without being able to lock it again");
    finish(context, owner, friend);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theScreenIsToldTheReachTheTerminalActuallyHas(TestContext context) {
    ServerPlayerEntity owner = playerAtTheTerminal(context);
    poweredTerminalOwnedBy(context, owner);
    TerminalBlockEntity terminal = context.getBlockEntity(TERMINAL);
    terminal.updateSettings(settings -> settings.withRadius(OVERSIZED_RADIUS));

    int reported = openDataFor(context, owner).radius();

    context.assertEquals(
        ServerConfigService.current().maxTerminalRadius(),
        reported,
        "The screen should be told the clamped reach rather than the configured one");
    context.assertEquals(
        OVERSIZED_RADIUS,
        terminal.settings().radius(),
        "Reporting the clamp must not rewrite what the terminal was configured with");
    finish(context, owner);
  }

  private static TerminalOpenData openDataFor(TestContext context, ServerPlayerEntity player) {
    return new TerminalScreenFactory(context.getWorld(), absolute(context))
        .getScreenOpeningData(player);
  }

  private static int openPayloadSize(TestContext context, ServerPlayerEntity player) {
    RegistryByteBuf buffer =
        new RegistryByteBuf(Unpooled.buffer(), context.getWorld().getRegistryManager());
    TerminalOpenDataCodec.INSTANCE.encode(buffer, openDataFor(context, player));
    return buffer.readableBytes();
  }

  private static ServerPlayerEntity playerAtTheTerminal(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    BlockPos standing = context.getAbsolutePos(STANDING);
    player.refreshPositionAndAngles(
        standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5, 0.0f, 0.0f);
    return player;
  }

  private static void terminalOwnedBy(TestContext context, ServerPlayerEntity owner) {
    context.setBlockState(TERMINAL, ModBlocks.TERMINAL.getDefaultState());
    ModBlocks.TERMINAL.onPlaced(
        context.getWorld(),
        absolute(context),
        context.getBlockState(TERMINAL),
        owner,
        ModBlocks.TERMINAL.asItem().getDefaultStack());
    context.<TerminalBlockEntity>getBlockEntity(TERMINAL).updateSettings(s -> s.withRadius(1));
  }

  private static void poweredTerminalOwnedBy(TestContext context, ServerPlayerEntity owner) {
    terminalOwnedBy(context, owner);
    context.setBlockState(POWER, Blocks.REDSTONE_BLOCK.getDefaultState());
  }

  private static void openTo(TestContext context, TerminalAccessMode mode) {
    TerminalAccessControl.trust().setAccessMode(context.getWorld(), absolute(context), mode);
  }

  private static void stockBarrel(TestContext context) {
    BarrelBlockEntity barrel = context.getBlockEntity(BARREL);
    barrel.setStack(0, new ItemStack(Items.DIAMOND, 64));
    barrel.setStack(1, new ItemStack(Items.EMERALD, 64));
    barrel.setStack(2, new ItemStack(Items.GOLD_INGOT, 64));
    barrel.setStack(3, new ItemStack(Items.IRON_INGOT, 64));
    barrel.markDirty();
  }

  private static BlockPos absolute(TestContext context) {
    return context.getAbsolutePos(TERMINAL);
  }

  private static TerminalAddress address(TestContext context) {
    return TerminalAddress.of(context.getWorld(), absolute(context));
  }

  private static MinecraftServer server(TestContext context) {
    return context.getWorld().getServer();
  }

  private static TerminalViewers viewers() {
    return TerminalAccessControl.viewers();
  }

  private static void finish(TestContext context, ServerPlayerEntity... players) {
    for (final ServerPlayerEntity player : players) {
      player.closeHandledScreen();
      viewers().closedEverything(player.getUuid());
      server(context).getPlayerManager().remove(player);
    }
    TerminalNetworks.power().unloaded(context.getWorld(), absolute(context));
    context.complete();
  }
}
