package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.filter.FilterCandidate;
import com.grahambartley.totalstorage.filter.NetworkFilterCandidates;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import com.grahambartley.totalstorage.tag.PresetTag;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class NetworkFilterCandidatesGameTest implements FabricGameTest {
  private static final String BATCH = "network-filter-candidates";

  private static final String COBBLESTONE = "minecraft:cobblestone";

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void anItemCarriesTheNetworksOwnTagsBesideItsPresets(TestContext context) {
    final NetworkTagSet tags =
        NetworkTagSet.defaults().withCustomTag("castle").withItem("castle", COBBLESTONE);

    final FilterCandidate candidate =
        new NetworkFilterCandidates(tags).of(ItemVariant.of(Items.COBBLESTONE));

    context.assertTrue(
        candidate.hasTag("castle"), "The network's own tag never reached the filter engine");
    context.assertTrue(
        candidate.hasTag(PresetTag.STONE.id()),
        "The preset tags should survive alongside the custom ones, got " + candidate.tags());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void anItemNobodyFiledCarriesItsPresetsAlone(TestContext context) {
    final NetworkTagSet tags =
        NetworkTagSet.defaults().withCustomTag("castle").withItem("castle", COBBLESTONE);

    final FilterCandidate candidate =
        new NetworkFilterCandidates(tags).of(ItemVariant.of(Items.DIAMOND));

    context.assertFalse(
        candidate.hasTag("castle"), "A diamond nobody filed came back filed under castle");
    context.assertFalse(candidate.tags().isEmpty(), "A diamond should still carry its presets");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aNetworkWithNoCustomTagsChangesNothingAboutAnItem(TestContext context) {
    final FilterCandidate withoutCustom =
        new NetworkFilterCandidates(NetworkTagSet.defaults()).of(ItemVariant.of(Items.COBBLESTONE));

    context.assertTrue(
        withoutCustom.hasTag(PresetTag.STONE.id()),
        "Cobblestone should still be filed under stone, got " + withoutCustom.tags());
    context.assertTrue(
        COBBLESTONE.equals(withoutCustom.identifier()),
        "The item should keep its identifier, got " + withoutCustom.identifier());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void anItemFiledUnderSeveralTagsCarriesAllOfThem(TestContext context) {
    final NetworkTagSet tags =
        NetworkTagSet.defaults()
            .withCustomTag("castle")
            .withItem("castle", COBBLESTONE)
            .withCustomTag("keep")
            .withItem("keep", COBBLESTONE);

    final FilterCandidate candidate =
        new NetworkFilterCandidates(tags).of(ItemVariant.of(Items.COBBLESTONE));

    context.assertTrue(candidate.hasTag("castle"), "The first tag went missing");
    context.assertTrue(candidate.hasTag("keep"), "The second tag went missing");
    context.complete();
  }
}
