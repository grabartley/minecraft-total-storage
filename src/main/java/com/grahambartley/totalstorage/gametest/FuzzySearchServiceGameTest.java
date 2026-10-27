package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.filter.FuzzySearchAttachment;
import com.grahambartley.totalstorage.filter.TagCombinationMode;
import com.grahambartley.totalstorage.network.ListingRequestPayload;
import com.grahambartley.totalstorage.network.ModNetworking;
import com.grahambartley.totalstorage.network.TerminalSettingsNetworking;
import com.grahambartley.totalstorage.screenhandler.FuzzySearchService;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class FuzzySearchServiceGameTest implements FabricGameTest {
  private static final String BATCH = "fuzzy-search-service";
  private static final int TICK_LIMIT = 60;

  private final FuzzySearchService service =
      new FuzzySearchService(ModNetworking.listings(), TerminalSettingsNetworking.tagPanels());

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNewChoiceIsStoredAndTheOpenListingIsSentAgain(TestContext context) {
    ServerPlayerEntity player = searching(context);
    long before = handler(player).listing().revision();

    boolean changed = service.choose(player, true);

    context.assertTrue(changed, "Turning fuzzy search on should count as a change");
    context.assertTrue(FuzzySearchAttachment.get(player), "The choice should be stored");
    context.assertTrue(
        handler(player).listing().revision() > before,
        "The open listing should have been sent again with the new matching");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void choosingWhatIsAlreadyStoredSendsNothing(TestContext context) {
    ServerPlayerEntity player = searching(context);
    long before = handler(player).listing().revision();

    boolean changed = service.choose(player, false);

    context.assertFalse(changed, "Choosing off when it is already off should change nothing");
    context.assertTrue(
        handler(player).listing().revision() == before,
        "An unchanged choice should not rebuild the listing");
    NetworkOperationFixture.finish(context, player);
  }

  private static ServerPlayerEntity searching(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 8);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    ModNetworking.listings()
        .request(
            player,
            new ListingRequestPayload(
                handler(player).syncId, 0, 10, "dmnd", TagCombinationMode.DEFAULT, Set.of()));
    return player;
  }

  private static TerminalScreenHandler handler(ServerPlayerEntity player) {
    return NetworkOperationFixture.handlerOf(player);
  }
}
