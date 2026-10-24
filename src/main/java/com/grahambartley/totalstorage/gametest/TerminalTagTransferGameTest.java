package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.operation.OperationGuards;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import com.grahambartley.totalstorage.tag.TagSetFile;
import com.grahambartley.totalstorage.tag.TagSetImportMode;
import com.grahambartley.totalstorage.tag.TagTransferResult;
import com.grahambartley.totalstorage.terminal.NetworkTagTransfer;
import com.grahambartley.totalstorage.terminal.OwnedTerminal;
import com.grahambartley.totalstorage.terminal.OwnedTerminals;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class TerminalTagTransferGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-tag-transfer";
  private static final int TICK_LIMIT = 60;
  private static final String EXPORT_NAME = "gametest";

  private final NetworkTagTransfer transfers = new NetworkTagTransfer();

  @BeforeBatch(batchId = BATCH)
  public void clearCooldowns(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @AfterBatch(batchId = BATCH)
  public void removeAnythingExported(ServerWorld world) {
    delete(ServerConfigService.pathsFor(world.getServer()).getTagSetPath(EXPORT_NAME));
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void copyingBringsTheOtherTerminalsCustomTagsAndTheirItems(TestContext context) {
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.openNorthTerminalFor(context, owner);
    fileOnNorth(context, "castle", "minecraft:stone");

    final TagTransferResult result = copy(context, owner, TagSetImportMode.MERGE);

    context.assertTrue(result.isChange(), "The copy reported " + result.outcome());
    context.assertEquals(result.imported(), 1, "One tag should have come across");
    context.assertTrue(
        tagsOn(context, NetworkOperationFixture.TERMINAL)
            .byId("castle")
            .orElseThrow()
            .contains("minecraft:stone"),
        "The tag came across without the items filed under it");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void mergingKeepsTheTagsTheTerminalAlreadyHad(TestContext context) {
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.openNorthTerminalFor(context, owner);
    NetworkOperationFixture.fileUnderCustomTag(context, "mine", "minecraft:coal");
    fileOnNorth(context, "castle", "minecraft:stone");

    copy(context, owner, TagSetImportMode.MERGE);

    context.assertTrue(
        tagsOn(context, NetworkOperationFixture.TERMINAL).contains("mine"),
        "Merging dropped a tag that was already there");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void replacingDropsTheTagsTheTerminalAlreadyHad(TestContext context) {
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.openNorthTerminalFor(context, owner);
    NetworkOperationFixture.fileUnderCustomTag(context, "mine", "minecraft:coal");
    fileOnNorth(context, "castle", "minecraft:stone");

    copy(context, owner, TagSetImportMode.REPLACE);

    final NetworkTagSet after = tagsOn(context, NetworkOperationFixture.TERMINAL);
    context.assertFalse(after.contains("mine"), "Replacing left the old tag behind");
    context.assertTrue(after.contains("castle"), "Replacing did not bring the new tag across");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aTerminalThePlayerCannotAdministerIsNeverLiftedFrom(TestContext context) {
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    final ServerPlayerEntity stranger = NetworkOperationFixture.openNorthTerminal(context);
    fileOnNorth(context, "castle", "minecraft:stone");

    final TagTransferResult result = copy(context, owner, TagSetImportMode.MERGE);

    context.assertFalse(result.isChange(), "A stranger's tag set was lifted");
    context.assertFalse(
        tagsOn(context, NetworkOperationFixture.TERMINAL).contains("castle"),
        "A stranger's tag landed on the terminal");
    NetworkOperationFixture.finish(context, owner, stranger);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aTerminalThePlayerCannotAdministerIsNeverWrittenTo(TestContext context) {
    final ServerPlayerEntity stranger = NetworkOperationFixture.openTerminal(context);
    final ServerPlayerEntity owner = NetworkOperationFixture.openNorthTerminal(context);
    fileOnNorth(context, "castle", "minecraft:stone");

    final TagTransferResult result =
        transfers.copy(
            context.getWorld(),
            context.getAbsolutePos(NetworkOperationFixture.TERMINAL),
            context.getAbsolutePos(NetworkOperationFixture.NORTH_TERMINAL),
            TerminalCaller.of(owner),
            TagSetImportMode.MERGE);

    context.assertFalse(result.isChange(), "A tag set landed on a terminal they do not own");
    context.assertFalse(
        tagsOn(context, NetworkOperationFixture.TERMINAL).contains("castle"),
        "A stranger's terminal gained a tag");
    NetworkOperationFixture.finish(context, owner, stranger);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void exportingThenImportingReproducesTheTagSet(TestContext context) {
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, "castle", "minecraft:stone");
    final Path file = exportPath(context);

    final NetworkTagSet exported =
        transfers
            .read(
                context.getWorld(),
                context.getAbsolutePos(NetworkOperationFixture.TERMINAL),
                TerminalCaller.of(owner))
            .orElseThrow();
    context.assertTrue(TagSetFile.save(file, exported), "The tag set was not written");
    NetworkOperationFixture.openNorthTerminalFor(context, owner);
    final TagTransferResult imported =
        transfers.importInto(
            context.getWorld(),
            context.getAbsolutePos(NetworkOperationFixture.NORTH_TERMINAL),
            TerminalCaller.of(owner),
            TagSetFile.load(file).orElseThrow(),
            TagSetImportMode.REPLACE);

    context.assertTrue(imported.isChange(), "The import reported " + imported.outcome());
    context.assertTrue(
        tagsOn(context, NetworkOperationFixture.NORTH_TERMINAL)
            .byId("castle")
            .orElseThrow()
            .contains("minecraft:stone"),
        "The imported tag lost the items filed under it");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theTerminalBeingLookedAtIsNeverOfferedAsASourceOfItsOwnTags(TestContext context) {
    final ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.openNorthTerminalFor(context, owner);

    final List<OwnedTerminal> sources =
        OwnedTerminals.copyableBy(
            context.getWorld(),
            TerminalCaller.of(owner),
            context.getAbsolutePos(NetworkOperationFixture.TERMINAL));

    context.assertTrue(
        sources.stream()
            .noneMatch(
                source ->
                    source.pos().equals(context.getAbsolutePos(NetworkOperationFixture.TERMINAL))),
        "A terminal offered itself as somewhere to copy from");
    context.assertTrue(
        sources.stream()
            .anyMatch(
                source ->
                    source
                        .pos()
                        .equals(context.getAbsolutePos(NetworkOperationFixture.NORTH_TERMINAL))),
        "The other terminal they own was not offered");
    NetworkOperationFixture.finish(context, owner);
  }

  private TagTransferResult copy(
      TestContext context, ServerPlayerEntity owner, TagSetImportMode mode) {
    return transfers.copy(
        context.getWorld(),
        context.getAbsolutePos(NetworkOperationFixture.TERMINAL),
        context.getAbsolutePos(NetworkOperationFixture.NORTH_TERMINAL),
        TerminalCaller.of(owner),
        mode);
  }

  private static void fileOnNorth(TestContext context, String tag, String itemId) {
    TerminalLookup.at(
            context.getWorld(), context.getAbsolutePos(NetworkOperationFixture.NORTH_TERMINAL))
        .orElseThrow()
        .updateSettings(
            settings ->
                settings.withTags(settings.tags().withCustomTag(tag).withItem(tag, itemId)));
  }

  private static NetworkTagSet tagsOn(TestContext context, BlockPos terminal) {
    return TerminalLookup.settingsAt(context.getWorld(), context.getAbsolutePos(terminal))
        .orElseThrow()
        .tags();
  }

  private static Path exportPath(TestContext context) {
    return ServerConfigService.pathsFor(context.getWorld().getServer()).getTagSetPath(EXPORT_NAME);
  }

  private static void delete(Path file) {
    try {
      Files.deleteIfExists(file);
    } catch (IOException leftBehind) {
      throw new IllegalStateException("could not clean up " + file, leftBehind);
    }
  }
}
