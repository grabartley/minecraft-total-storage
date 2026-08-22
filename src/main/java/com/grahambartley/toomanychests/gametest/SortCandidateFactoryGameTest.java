package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.SortOrdering;
import com.grahambartley.toomanychests.sort.SortCandidate;
import com.grahambartley.toomanychests.sort.SortCandidateFactory;
import com.grahambartley.toomanychests.sort.SortComparator;
import com.grahambartley.toomanychests.tag.PresetTag;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

/**
 * Flattening a variant reads the item registry, the live tag data, and the stack's components, none
 * of which exist outside a running server. The ordering those fields feed is proven off-game by
 * {@code SortComparatorTest}; what is proven here is that the fields arrive correct.
 */
public final class SortCandidateFactoryGameTest implements FabricGameTest {
  private static final String BATCH = "sort-candidates";
  private static final int TICK_LIMIT = 20;

  private final SortCandidateFactory factory = new SortCandidateFactory();

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theCandidateCarriesTheItemsRegistryIdentifier(TestContext context) {
    SortCandidate candidate = factory.of(ItemVariant.of(Items.COBBLESTONE), 64L);

    context.assertTrue(
        "minecraft:cobblestone".equals(candidate.identifier()),
        "Expected the registry identifier, found " + candidate.identifier());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theCandidateCarriesTheCountItWasGiven(TestContext context) {
    SortCandidate candidate = factory.of(ItemVariant.of(Items.COBBLESTONE), 300L);

    context.assertTrue(
        candidate.count() == 300L, "Expected the given count, found " + candidate.count());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theCandidateCarriesTheItemsOwnStackLimit(TestContext context) {
    assertStackLimit(context, Items.COBBLESTONE, 64);
    assertStackLimit(context, Items.ENDER_PEARL, 16);
    assertStackLimit(context, Items.DIAMOND_SWORD, 1);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void fullnessFollowsTheItemsOwnStackLimitRatherThanAFixedSixtyFour(TestContext context) {
    SortCandidate pearls = factory.of(ItemVariant.of(Items.ENDER_PEARL), 16L);
    SortCandidate cobblestone = factory.of(ItemVariant.of(Items.COBBLESTONE), 16L);

    context.assertTrue(pearls.isFullStack(), "Sixteen ender pearls is a full stack");
    context.assertFalse(cobblestone.isFullStack(), "Sixteen cobblestone is not a full stack");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theCandidateIsFiledUnderTheItemsLivePresets(TestContext context) {
    SortCandidate candidate = factory.of(ItemVariant.of(Items.COBBLESTONE), 64L);

    context.assertTrue(
        candidate.categories().contains(PresetTag.STONE),
        "Cobblestone should be filed under stone, found " + candidate.categories());
    context.assertTrue(
        candidate.primaryCategory() == PresetTag.BUILDING_BLOCKS,
        "Building blocks comes first in filter column order, found " + candidate.primaryCategory());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theCandidateCarriesATranslatedDisplayName(TestContext context) {
    SortCandidate candidate = factory.of(ItemVariant.of(Items.COOKED_BEEF), 1L);

    context.assertTrue(
        "steak".equals(candidate.displayName()),
        "Expected the translated name, found " + candidate.displayName());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theCandidateCarriesTheVariantsComponentKey(TestContext context) {
    SortCandidate plain = factory.of(ItemVariant.of(Items.DIAMOND_SWORD), 1L);
    SortCandidate named = factory.of(ItemVariant.of(sword("Sting", 0)), 1L);

    context.assertTrue(plain.componentsKey().isEmpty(), "A plain sword carries no component key");
    context.assertFalse(
        named.componentsKey().isEmpty(), "A renamed sword should carry its component key through");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void realHoldingsSortByCategoryThenRegistryIdentifier(TestContext context) {
    List<SortCandidate> holdings =
        new ArrayList<>(
            List.of(
                factory.of(ItemVariant.of(Items.COOKED_BEEF), 8L),
                factory.of(ItemVariant.of(Items.OAK_LOG), 64L),
                factory.of(ItemVariant.of(Items.COBBLESTONE), 64L),
                factory.of(ItemVariant.of(Items.ANDESITE), 64L)));
    holdings.sort(SortComparator.forOrdering(SortOrdering.REGISTRY_ID));

    List<String> ordered = holdings.stream().map(SortCandidate::identifier).toList();
    context.assertTrue(
        ordered.equals(
            List.of(
                "minecraft:andesite",
                "minecraft:cobblestone",
                "minecraft:oak_log",
                "minecraft:cooked_beef")),
        "Expected building blocks then wood then food, found " + ordered);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyItemInTheRegistryFlattensIntoAUsableCandidate(TestContext context) {
    List<String> unusable = new ArrayList<>();
    for (SortCandidate candidate : everyItemAsACandidate()) {
      if (candidate.categories().isEmpty() || candidate.maxCount() < 1) {
        unusable.add(candidate.identifier());
      }
    }

    context.assertTrue(unusable.isEmpty(), "These items flattened unusably: " + unusable);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void sortingEveryItemInTheRegistryTwiceProducesZeroMoves(TestContext context) {
    Comparator<SortCandidate> ordering = SortComparator.forOrdering(SortOrdering.REGISTRY_ID);
    List<SortCandidate> once = new ArrayList<>(everyItemAsACandidate());
    once.sort(ordering);
    List<SortCandidate> twice = new ArrayList<>(once);
    twice.sort(ordering);

    for (int slot = 0; slot < once.size(); slot++) {
      context.assertTrue(
          once.get(slot) == twice.get(slot),
          "Holding moved out of slot " + slot + ": " + once.get(slot).identifier());
    }
    context.complete();
  }

  private List<SortCandidate> everyItemAsACandidate() {
    List<SortCandidate> candidates = new ArrayList<>();
    for (Item item : Registries.ITEM) {
      if (item != Items.AIR) {
        candidates.add(factory.of(ItemVariant.of(item), 1L));
      }
    }
    return candidates;
  }

  private void assertStackLimit(TestContext context, Item item, int expected) {
    SortCandidate candidate = factory.of(ItemVariant.of(item), 1L);

    context.assertTrue(
        candidate.maxCount() == expected,
        "Expected a stack limit of " + expected + ", found " + candidate.maxCount());
  }

  private static ItemStack sword(String name, int damage) {
    ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
    stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
    stack.set(DataComponentTypes.DAMAGE, damage);
    return stack;
  }
}
