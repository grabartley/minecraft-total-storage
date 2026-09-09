package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.ModBlocks;
import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.listing.ListingPage;
import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.network.ListingRequestPayload;
import com.grahambartley.toomanychests.screenhandler.TerminalListingService;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class TerminalListingServiceGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "terminal-listing";
  private static final int TICK_LIMIT = 60;

  private static final BlockPos TERMINAL = new BlockPos(3, 1, 3);
  private static final BlockPos POWER = new BlockPos(3, 1, 2);
  private static final BlockPos BARREL = new BlockPos(4, 1, 3);
  private static final BlockPos STANDING = new BlockPos(3, 1, 4);

  private final TerminalListingService listings = new TerminalListingService();

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRequestIsAnsweredWithWhatTheNetworkIsHoldingNow(TestContext context) {
    ServerPlayerEntity player = openTerminalWithAStockedBarrel(context);
    TerminalScreenHandler handler = handlerOf(player);

    request(player, handler, 0, 10, "");

    ListingPage<ItemVariant> page = listings.pageFor(player, handler);
    context.assertTrue(
        page.totalMatching() == 4,
        "The barrel holds four kinds of thing, listed " + page.totalMatching());
    context.assertTrue(
        countOf(page, Items.DIAMOND) == 64L,
        "The listing should total the diamonds, got " + countOf(page, Items.DIAMOND));
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aWindowIsAnsweredWithOnlyTheRowsItCovers(TestContext context) {
    ServerPlayerEntity player = openTerminalWithAStockedBarrel(context);
    TerminalScreenHandler handler = handlerOf(player);

    request(player, handler, 1, 2, "");

    ListingPage<ItemVariant> page = listings.pageFor(player, handler);
    context.assertTrue(
        page.size() == 2, "A window of two should carry two rows, got " + page.size());
    context.assertTrue(
        page.offset() == 1, "The page should say where it starts, got " + page.offset());
    context.assertTrue(
        page.totalMatching() == 4,
        "A window narrows the rows without narrowing the total, got " + page.totalMatching());
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void searchNarrowsTheWholeListingRatherThanThePage(TestContext context) {
    ServerPlayerEntity player = openTerminalWithAStockedBarrel(context);
    TerminalScreenHandler handler = handlerOf(player);

    request(player, handler, 0, 10, "diamond");

    ListingPage<ItemVariant> page = listings.pageFor(player, handler);
    context.assertTrue(
        page.totalMatching() == 1,
        "Only the diamonds should match a search for them, matched " + page.totalMatching());
    context.assertTrue(countOf(page, Items.DIAMOND) == 64L, "The one match should be the diamonds");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aTerminalRunningNoNetworkAnswersWithAnEmptyPageRatherThanSilence(
      TestContext context) {
    ServerPlayerEntity player = openTerminalWithAStockedBarrel(context);
    TerminalScreenHandler handler = handlerOf(player);
    request(player, handler, 0, 10, "");
    TerminalNetworks.power().unloaded(context.getWorld(), context.getAbsolutePos(TERMINAL));

    ListingPage<ItemVariant> page = listings.pageFor(player, handler);

    context.assertTrue(page.isEmpty(), "An unpowered terminal should list nothing");
    context.assertTrue(
        page.totalMatching() == 0, "An unpowered terminal has nothing to scroll through");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRequestNamingAScreenThePlayerDoesNotHaveOpenIsRefused(TestContext context) {
    ServerPlayerEntity player = openTerminalWithAStockedBarrel(context);
    TerminalScreenHandler handler = handlerOf(player);

    boolean answered =
        listings.request(
            player,
            new ListingRequestPayload(
                handler.syncId + 1, 0, 10, "", TagCombinationMode.DEFAULT, Set.of()));

    context.assertFalse(answered, "A request naming another screen must not be answered");
    context.assertFalse(
        handler.listing().hasWindow(),
        "A refused request must not become what the screen is showing");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyAnswerIsStampedNewerThanTheOneBeforeIt(TestContext context) {
    ServerPlayerEntity player = openTerminalWithAStockedBarrel(context);
    TerminalScreenHandler handler = handlerOf(player);
    request(player, handler, 0, 10, "");

    long first = listings.pageFor(player, handler).revision();
    long second = listings.pageFor(player, handler).revision();

    context.assertTrue(
        second > first, "Pages should be stamped in order, got " + first + " then " + second);
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aContainerFillingUpChangesWhatTheNextPageSays(TestContext context) {
    ServerPlayerEntity player = openTerminalWithAStockedBarrel(context);
    TerminalScreenHandler handler = handlerOf(player);
    request(player, handler, 0, 10, "");
    context.assertTrue(
        countOf(listings.pageFor(player, handler), Items.DIAMOND) == 64L,
        "The barrel should have started with one stack of diamonds");

    BarrelBlockEntity barrel = context.getBlockEntity(BARREL);
    barrel.setStack(4, new ItemStack(Items.DIAMOND, 32));
    barrel.markDirty();

    context.assertTrue(
        countOf(listings.pageFor(player, handler), Items.DIAMOND) == 96L,
        "A container filling up should show in the next page, got "
            + countOf(listings.pageFor(player, handler), Items.DIAMOND));
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theClosestMatchIsListedFirstHoweverTheIndexOrderedIt(TestContext context) {
    ServerPlayerEntity player =
        openTerminalStockedWith(
            context, Items.CHERRY_CHEST_BOAT, Items.CHEST, Items.CHEST_MINECART, Items.ENDER_CHEST);
    TerminalScreenHandler handler = handlerOf(player);

    request(player, handler, 0, 10, "chest");

    ListingPage<ItemVariant> page = listings.pageFor(player, handler);
    List<Item> listed = itemsOf(page);
    context.assertTrue(
        listed.equals(
            List.of(Items.CHEST, Items.CHEST_MINECART, Items.CHERRY_CHEST_BOAT, Items.ENDER_CHEST)),
        "The exact match should lead, then the prefix, then the rest in index order, got "
            + listed);
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aCustomTagNarrowsTheNetworkToTheItemsFiledUnderIt(TestContext context) {
    ServerPlayerEntity player = openTerminalStockedWith(context, Items.DIAMOND, Items.COBBLESTONE);
    TerminalScreenHandler handler = handlerOf(player);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");

    requestFiltered(player, handler, TagCombinationMode.AND, Set.of("castle"));

    context.assertTrue(
        itemsOf(listings.pageFor(player, handler)).equals(List.of(Items.COBBLESTONE)),
        "A custom tag should narrow the network to what was filed under it, got "
            + itemsOf(listings.pageFor(player, handler)));
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void twoTagsNarrowTogetherAndBroadenApart(TestContext context) {
    ServerPlayerEntity player = openTerminalStockedWith(context, Items.DIAMOND, Items.COBBLESTONE);
    TerminalScreenHandler handler = handlerOf(player);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");
    NetworkOperationFixture.fileUnderCustomTag(context, "shiny", "minecraft:diamond");

    requestFiltered(player, handler, TagCombinationMode.AND, Set.of("castle", "shiny"));
    context.assertTrue(
        listings.pageFor(player, handler).rows().isEmpty(),
        "Nothing carries both tags, so narrowing should find nothing");

    requestFiltered(player, handler, TagCombinationMode.OR, Set.of("castle", "shiny"));
    context.assertTrue(
        itemsOf(listings.pageFor(player, handler)).size() == 2,
        "Broadening should find everything either tag holds");
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aTagAndSomeTypedTextNarrowTogetherRatherThanOverriding(TestContext context) {
    ServerPlayerEntity player =
        openTerminalStockedWith(context, Items.DIAMOND, Items.COBBLESTONE, Items.COBBLESTONE_SLAB);
    TerminalScreenHandler handler = handlerOf(player);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:diamond");

    listings.request(
        player,
        new ListingRequestPayload(
            handler.syncId, 0, 10, "cobblestone", TagCombinationMode.AND, Set.of("castle")));

    context.assertTrue(
        itemsOf(listings.pageFor(player, handler)).equals(List.of(Items.COBBLESTONE)),
        "The search and the tag should both apply, got "
            + itemsOf(listings.pageFor(player, handler)));
    finish(context, player);
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aTagNothingIsFiledUnderFindsNothingRatherThanEverything(TestContext context) {
    ServerPlayerEntity player = openTerminalStockedWith(context, Items.DIAMOND);
    TerminalScreenHandler handler = handlerOf(player);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");

    requestFiltered(player, handler, TagCombinationMode.AND, Set.of("castle"));

    context.assertTrue(
        listings.pageFor(player, handler).rows().isEmpty(),
        "A tag holding nothing here should match nothing");
    finish(context, player);
  }

  private void requestFiltered(
      ServerPlayerEntity player,
      TerminalScreenHandler handler,
      TagCombinationMode mode,
      Set<String> tags) {
    listings.request(player, new ListingRequestPayload(handler.syncId, 0, 10, "", mode, tags));
  }

  private void request(
      ServerPlayerEntity player,
      TerminalScreenHandler handler,
      int offset,
      int length,
      String search) {
    listings.request(
        player,
        new ListingRequestPayload(
            handler.syncId, offset, length, search, TagCombinationMode.DEFAULT, Set.of()));
  }

  private static List<Item> itemsOf(ListingPage<ItemVariant> page) {
    return page.rows().stream().map(row -> row.variant().getItem()).toList();
  }

  private static long countOf(ListingPage<ItemVariant> page, Item item) {
    final List<ListingRow<ItemVariant>> rows = page.rows();
    for (final ListingRow<ItemVariant> row : rows) {
      if (row.variant().getItem() == item) {
        return row.count();
      }
    }
    return 0L;
  }

  private static ServerPlayerEntity openTerminalWithAStockedBarrel(TestContext context) {
    return openTerminalStockedWith(
        context, Items.DIAMOND, Items.EMERALD, Items.GOLD_INGOT, Items.IRON_INGOT);
  }

  private static ServerPlayerEntity openTerminalStockedWith(TestContext context, Item... items) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    BlockPos standing = context.getAbsolutePos(STANDING);
    player.refreshPositionAndAngles(
        standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5, 0.0f, 0.0f);

    context.setBlockState(BARREL, Blocks.BARREL.getDefaultState());
    BarrelBlockEntity barrel = context.getBlockEntity(BARREL);
    for (int slot = 0; slot < items.length; slot++) {
      barrel.setStack(slot, new ItemStack(items[slot], items[slot].getMaxCount()));
    }
    barrel.markDirty();

    context.setBlockState(TERMINAL, ModBlocks.TERMINAL.getDefaultState());
    ModBlocks.TERMINAL.onPlaced(
        context.getWorld(),
        context.getAbsolutePos(TERMINAL),
        context.getBlockState(TERMINAL),
        player,
        ModBlocks.TERMINAL.asItem().getDefaultStack());
    context.<TerminalBlockEntity>getBlockEntity(TERMINAL).updateSettings(s -> s.withRadius(1));
    context.setBlockState(POWER, Blocks.REDSTONE_BLOCK.getDefaultState());

    context.useBlock(TERMINAL, player);
    return player;
  }

  private static TerminalScreenHandler handlerOf(ServerPlayerEntity player) {
    return (TerminalScreenHandler) player.currentScreenHandler;
  }

  private static void finish(TestContext context, ServerPlayerEntity player) {
    player.closeHandledScreen();
    TerminalAccessControl.viewers().closedEverything(player.getUuid());
    context.getWorld().getServer().getPlayerManager().remove(player);
    TerminalNetworks.power().unloaded(context.getWorld(), context.getAbsolutePos(TERMINAL));
    context.complete();
  }
}
