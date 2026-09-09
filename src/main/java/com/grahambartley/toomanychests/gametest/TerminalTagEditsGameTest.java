package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.network.ListingRequestPayload;
import com.grahambartley.toomanychests.network.ModNetworking;
import com.grahambartley.toomanychests.network.TagEditPayload;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.screenhandler.TerminalListingService;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.screenhandler.TerminalTagEdits;
import com.grahambartley.toomanychests.screenhandler.TerminalTagPanelService;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import com.grahambartley.toomanychests.tag.PresetTag;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class TerminalTagEditsGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-tag-edits";
  private static final int TICK_LIMIT = 60;

  @BeforeBatch(batchId = BATCH)
  public void clearCooldowns(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  private final TerminalTagPanelService panels = new TerminalTagPanelService();
  private final TerminalTagEdits edits = new TerminalTagEdits(panels, new TerminalListingService());

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theOwnerCanCreateATagAndItLandsOnTheBlock(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    final boolean applied = edits.edit(owner, TagEditPayload.create(syncId(owner), "Base Build"));

    context.assertTrue(applied, "The owner could not create a tag");
    context.assertTrue(tagsOf(context).contains("base build"), "The tag is not on the terminal");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aGuestCannotCreateATagByPacket(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    final ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);

    final boolean applied = edits.edit(guest, TagEditPayload.create(syncId(guest), "Theirs"));

    context.assertFalse(applied, "A guest created a tag on someone else's network");
    context.assertFalse(tagsOf(context).contains("theirs"), "The guest's tag was stored");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aGuestCannotDeleteATagByPacket(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");
    final ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);

    final boolean applied = edits.edit(guest, TagEditPayload.delete(syncId(guest), "castle"));

    context.assertFalse(applied, "A guest deleted someone else's tag");
    context.assertTrue(tagsOf(context).contains("castle"), "The tag was deleted anyway");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aGuestCannotAssignATagByPacket(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");
    final ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);

    final boolean applied =
        edits.edit(
            guest,
            TagEditPayload.assign(syncId(guest), "castle", ItemVariant.of(Items.DIAMOND), true));

    context.assertFalse(applied, "A guest tagged an item on someone else's network");
    context.assertFalse(
        tagsOf(context).tagIdsFor("minecraft:diamond").contains("castle"),
        "The guest's assignment was stored");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void renamingATagKeepsEveryItemItAlreadyHeld(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");

    edits.edit(owner, TagEditPayload.rename(syncId(owner), "castle", "The Keep"));

    context.assertTrue(tagsOf(context).contains("the keep"), "The tag was not renamed");
    context.assertFalse(tagsOf(context).contains("castle"), "The old name is still there");
    context.assertTrue(
        tagsOf(context).tagIdsFor("minecraft:cobblestone").contains("the keep"),
        "Renaming lost the items the tag held");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void deletingATagTakesItOffEveryItemThatCarriedIt(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");

    edits.edit(owner, TagEditPayload.delete(syncId(owner), "castle"));

    context.assertFalse(tagsOf(context).contains("castle"), "The tag survived its deletion");
    context.assertTrue(
        tagsOf(context).tagIdsFor("minecraft:cobblestone").isEmpty(),
        "The item still carries a tag nobody has");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPresetTagCannotBeAssignedToBecauseItIsWorkedOut(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    final boolean applied =
        edits.edit(
            owner,
            TagEditPayload.assign(
                syncId(owner), PresetTag.WOOD.id(), ItemVariant.of(Items.DIAMOND), true));

    context.assertFalse(applied, "A preset tag took an assignment");
    context.assertFalse(
        tagsOf(context).tagIdsFor("minecraft:diamond").contains(PresetTag.WOOD.id()),
        "A diamond was filed under wood by hand");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNameAlreadyTakenIsRefusedRatherThanReplacingTheTag(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:cobblestone");

    final boolean applied = edits.edit(owner, TagEditPayload.create(syncId(owner), "CASTLE"));

    context.assertFalse(applied, "A duplicate name was accepted");
    context.assertTrue(
        tagsOf(context).custom().size() == 1, "The network grew a second castle tag");
    context.assertTrue(
        tagsOf(context).tagIdsFor("minecraft:cobblestone").contains("castle"),
        "The original tag lost its items");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNameCollidingWithAPresetIsRefused(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    final boolean applied =
        edits.edit(owner, TagEditPayload.create(syncId(owner), PresetTag.WOOD.id()));

    context.assertFalse(applied, "A preset name was taken by a custom tag");
    context.assertTrue(tagsOf(context).custom().isEmpty(), "A colliding tag was stored");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anItemCanBeAssignedAndUnassignedAgain(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 8);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    edits.edit(owner, TagEditPayload.create(syncId(owner), "castle"));

    edits.edit(
        owner, TagEditPayload.assign(syncId(owner), "castle", ItemVariant.of(Items.DIAMOND), true));
    context.assertTrue(
        tagsOf(context).tagIdsFor("minecraft:diamond").contains("castle"),
        "The item was not tagged");

    edits.edit(
        owner,
        TagEditPayload.assign(syncId(owner), "castle", ItemVariant.of(Items.DIAMOND), false));
    context.assertFalse(
        tagsOf(context).tagIdsFor("minecraft:diamond").contains("castle"),
        "The tag stuck to the item after being taken off");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void assigningEverythingMatchingTagsTheWholeNarrowedSet(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 8);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, Items.COBBLESTONE, 8);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    edits.edit(owner, TagEditPayload.create(syncId(owner), "castle"));
    NetworkOperationFixture.watching(owner);

    edits.edit(owner, TagEditPayload.assignMatching(syncId(owner), "castle"));

    context.assertTrue(
        tagsOf(context).tagIdsFor("minecraft:diamond").contains("castle"),
        "The diamond in the network was not tagged");
    context.assertTrue(
        tagsOf(context).tagIdsFor("minecraft:cobblestone").contains("castle"),
        "The cobblestone in the network was not tagged");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void assigningEverythingMatchingLeavesWhatTheFilterExcludedAlone(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 8);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, Items.COBBLESTONE, 8);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    edits.edit(owner, TagEditPayload.create(syncId(owner), "castle"));
    narrowTo(owner, "cobblestone");

    edits.edit(owner, TagEditPayload.assignMatching(syncId(owner), "castle"));

    context.assertTrue(
        tagsOf(context).tagIdsFor("minecraft:cobblestone").contains("castle"),
        "The item the filter matched was not tagged");
    context.assertFalse(
        tagsOf(context).tagIdsFor("minecraft:diamond").contains("castle"),
        "An item the filter excluded was tagged anyway");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void assigningEverythingMatchingIsPacedLikeEveryOtherWholeNetworkWalk(
      TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 8);
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    edits.edit(owner, TagEditPayload.create(syncId(owner), "castle"));
    edits.edit(owner, TagEditPayload.create(syncId(owner), "keep"));
    NetworkOperationFixture.watching(owner);

    edits.edit(owner, TagEditPayload.assignMatching(syncId(owner), "castle"));
    edits.edit(owner, TagEditPayload.assignMatching(syncId(owner), "keep"));

    context.assertTrue(
        tagsOf(context).tagIdsFor("minecraft:diamond").contains("castle"),
        "The first tagging should have gone through");
    context.assertFalse(
        tagsOf(context).tagIdsFor("minecraft:diamond").contains("keep"),
        "A second whole-network tagging ran inside the cooldown");
    NetworkOperationFixture.finish(context, owner);
  }

  private static void narrowTo(ServerPlayerEntity player, String search) {
    ModNetworking.listings()
        .request(
            player,
            new ListingRequestPayload(
                syncId(player), 0, 10, search, TagCombinationMode.DEFAULT, Set.of()));
  }

  private static NetworkTagSet tagsOf(TestContext context) {
    return TerminalLookup.settingsAt(
            context.getWorld(), context.getAbsolutePos(NetworkOperationFixture.TERMINAL))
        .orElseThrow()
        .tags();
  }

  private static int syncId(ServerPlayerEntity player) {
    return ((TerminalScreenHandler) player.currentScreenHandler).syncId;
  }
}
