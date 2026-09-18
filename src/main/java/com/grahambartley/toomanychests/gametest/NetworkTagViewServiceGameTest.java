package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.filter.ItemFilter;
import com.grahambartley.toomanychests.tag.PresetTag;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import com.grahambartley.toomanychests.terminal.NetworkTagViewService;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class NetworkTagViewServiceGameTest implements FabricGameTest {
  private static final String BATCH = "network-tag-view";
  private static final int TICK_LIMIT = 60;

  private final NetworkTagViewService views = new NetworkTagViewService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void everyPresetIsOfferedBesideTheNetworksOwnTags(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");

    final NetworkTagView view = viewFor(owner, Optional.empty());

    context.assertTrue(
        view.tags().size() == PresetTag.all().size() + 1,
        "The column should list every preset and the one custom tag, got " + view.tags().size());
    context.assertTrue(view.custom().size() == 1, "Only the custom tag should be editable");
    context.assertTrue(
        view.custom().get(0).id().equals("castle"), "The custom tag is not the one made");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aTagCountsTheItemsTheNetworkActuallyHoldsUnderIt(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 8);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");

    final NetworkTagEntry castle = customTag(viewFor(owner, Optional.empty()), "castle");

    context.assertTrue(
        castle.matchingItems() == 1,
        "The tag should count the one item held here, got " + castle.matchingItems());
    context.assertTrue(
        castle.assignedItems() == 1,
        "The tag should report the one item filed under it, got " + castle.assignedItems());
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aTagHoldingNothingHereReadsAsEmptyRatherThanMissing(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 8);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");

    final NetworkTagEntry castle = customTag(viewFor(owner, Optional.empty()), "castle");

    context.assertTrue(castle.isEmptyHere(), "A tag matching nothing here should read as empty");
    context.assertTrue(
        castle.assignedItems() == 1, "The tag still holds the item it was given elsewhere");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anItemReportsWhichTagsAlreadyCarryIt(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 8);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");

    final NetworkTagView view = viewFor(owner, Optional.of(ItemVariant.of(Items.COBBLESTONE)));

    context.assertTrue(view.subject().isPresent(), "The view forgot which item was asked about");
    context.assertTrue(view.subjectCarries("castle"), "The item's own tag was not reported");
    context.assertFalse(view.subjectCarries("nonexistent"), "The item carries a tag nobody made");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aGuestIsToldTheyMayNotEditTheseTags(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    final ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);

    context.assertTrue(
        viewFor(owner, Optional.empty()).mayAdminister(), "The owner may edit their own tags");
    context.assertFalse(
        viewFor(guest, Optional.empty()).mayAdminister(), "A guest was offered tag editing");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anItemReportsThePresetsItFallsUnderAsWellAsTheTagsItWasFiledIn(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 8);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");

    final NetworkTagView view = viewFor(owner, Optional.of(ItemVariant.of(Items.COBBLESTONE)));

    context.assertTrue(view.subjectCarries("castle"), "The item's own tag was not reported");
    context.assertTrue(
        view.subjectCarries(PresetTag.STONE.id()),
        "The preset the item falls under was not reported, so the prompt cannot show it");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aSearchNarrowsTheCountsSoAtagSaysWhatClickingItWouldGive(TestContext context) {
    final BarrelBlockEntity barrel =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 8);
    barrel.setStack(1, new ItemStack(Items.DIAMOND, 8));
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:diamond");

    final NetworkTagEntry wideOpen = customTag(viewFor(owner, Optional.empty()), "castle");
    final NetworkTagEntry searched =
        customTag(viewFor(owner, Optional.empty(), ItemFilter.ofSearch("diamond")), "castle");

    context.assertTrue(
        wideOpen.matchingItems() == 2,
        "With no search up the tag counts both items, got " + wideOpen.matchingItems());
    context.assertTrue(
        searched.matchingItems() == 1,
        "With a search up the tag should count only what matches, got " + searched.matchingItems());
    context.assertTrue(
        searched.assignedItems() == 2, "A search must not change what is filed under the tag");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aTagMatchingNothingUnderTheSearchStaysInTheListGreyed(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 8);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");

    final NetworkTagView view = viewFor(owner, Optional.empty(), ItemFilter.ofSearch("diamond"));
    final NetworkTagEntry castle = customTag(view, "castle");

    context.assertTrue(castle.isEmptyHere(), "A tag with no matches should read as empty");
    context.assertTrue(
        view.tags().size() == PresetTag.all().size() + 1,
        "No tag should leave the list while a search is up, got " + view.tags().size());
    NetworkOperationFixture.finish(context, owner);
  }

  private NetworkTagView viewFor(ServerPlayerEntity player, Optional<ItemVariant> subject) {
    return views.viewFor(player, NetworkOperationFixture.handlerOf(player).terminal(), subject);
  }

  private NetworkTagView viewFor(
      ServerPlayerEntity player, Optional<ItemVariant> subject, ItemFilter narrowing) {
    return views.viewFor(
        player, NetworkOperationFixture.handlerOf(player).terminal(), subject, narrowing);
  }

  private static NetworkTagEntry customTag(NetworkTagView view, String id) {
    return view.custom().stream()
        .filter(tag -> tag.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no custom tag " + id + " in the view"));
  }
}
