package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.filter.FilterCandidate;
import com.grahambartley.toomanychests.filter.FilterCandidateFactory;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * What an item variant looks like once it has been flattened for the filter engine.
 *
 * <p>The matching rules are proven in JUnit against hand-written candidates. What needs a running
 * world is that a real variant flattens into the identifier, name and tags those rules expect,
 * since every one of those comes from a registry that only exists inside the game.
 */
public final class FilterCandidateFactoryGameTest implements FabricGameTest {
  private static final String BATCH = "filter-candidates";

  private final FilterCandidateFactory factory = new FilterCandidateFactory();

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aVariantCarriesTheNamespacedIdentifierSearchFiltersOn(TestContext context) {
    FilterCandidate candidate = factory.of(ItemVariant.of(Items.COBBLESTONE));

    context.assertTrue(
        "minecraft:cobblestone".equals(candidate.identifier()),
        "Cobblestone should flatten to its namespaced id, got " + candidate.identifier());
    context.assertTrue(
        "minecraft".equals(candidate.modId()),
        "Cobblestone should read as a vanilla item, got " + candidate.modId());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aVariantCarriesTheNameAPlayerWouldType(TestContext context) {
    FilterCandidate candidate = factory.of(ItemVariant.of(Items.DIAMOND_PICKAXE));

    context.assertTrue(
        "diamond pickaxe".equals(candidate.displayName()),
        "A pickaxe should flatten to its display name, got " + candidate.displayName());
    context.complete();
  }

  /**
   * The tags are what the filter column narrows by, so a variant that arrived without them would
   * quietly vanish from every filtered view while still being in the network.
   */
  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aVariantCarriesThePresetTagsItIsFiledUnder(TestContext context) {
    FilterCandidate candidate = factory.of(ItemVariant.of(Items.COOKED_BEEF));

    context.assertFalse(
        candidate.tags().isEmpty(), "Steak should be filed under at least one preset tag");
    context.assertTrue(
        candidate.hasTag("food"), "Steak should be filed under food, got " + candidate.tags());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void anItemUnderNoPresetStillFlattensRatherThanFailing(TestContext context) {
    FilterCandidate candidate = factory.of(ItemVariant.of(Items.BEDROCK));

    context.assertTrue(
        "minecraft:bedrock".equals(candidate.identifier()),
        "Every item should flatten, got " + candidate.identifier());
    context.complete();
  }
}
