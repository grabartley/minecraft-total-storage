package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.filter.FuzzySearchAttachment;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class FuzzySearchAttachmentGameTest implements FabricGameTest {
  private static final String BATCH = "fuzzy-search";
  private static final int TICK_LIMIT = 20;

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aFreshPlayerHasFuzzySearchOff(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    context.assertFalse(
        FuzzySearchAttachment.get(player), "A player who never chose should have it off");
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theChoiceSurvivesTheNbtRoundTripThatSavesAndReloadsAWorld(TestContext context) {
    ServerPlayerEntity saved = context.createMockCreativeServerPlayerInWorld();
    ServerPlayerEntity reloaded = context.createMockCreativeServerPlayerInWorld();
    FuzzySearchAttachment.set(saved, true);

    reloaded.readNbt(saved.writeNbt(new NbtCompound()));

    context.assertTrue(
        FuzzySearchAttachment.get(reloaded), "Fuzzy search should still be on after a reload");
    disconnectAndComplete(context, saved, reloaded);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theChoiceFollowsThePlayerThroughDeath(TestContext context) {
    ServerPlayerEntity died = context.createMockCreativeServerPlayerInWorld();
    ServerPlayerEntity respawned = context.createMockCreativeServerPlayerInWorld();
    FuzzySearchAttachment.set(died, true);

    ServerPlayerEvents.AFTER_RESPAWN.invoker().afterRespawn(died, respawned, false);

    context.assertTrue(
        FuzzySearchAttachment.get(respawned), "Fuzzy search should still be on after respawning");
    disconnectAndComplete(context, died, respawned);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void onePlayersChoiceDoesNotLeakOntoAnother(TestContext context) {
    ServerPlayerEntity first = context.createMockCreativeServerPlayerInWorld();
    ServerPlayerEntity second = context.createMockCreativeServerPlayerInWorld();

    FuzzySearchAttachment.set(first, true);

    context.assertFalse(
        FuzzySearchAttachment.get(second),
        "Turning fuzzy search on for one player should leave every other player untouched");
    disconnectAndComplete(context, first, second);
  }

  private static void disconnectAndComplete(TestContext context, ServerPlayerEntity... players) {
    PlayerManager manager = context.getWorld().getServer().getPlayerManager();
    for (ServerPlayerEntity player : players) {
      manager.remove(player);
    }
    context.complete();
  }
}
