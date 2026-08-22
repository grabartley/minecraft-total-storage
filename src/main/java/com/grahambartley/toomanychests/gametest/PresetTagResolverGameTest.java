package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.tag.PresetTag;
import com.grahambartley.toomanychests.tag.PresetTagResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;

/**
 * Preset membership is decided by datapack tags, which only exist on a running server, so the
 * classification is proven here. The decision made once the matches are known is covered off-game
 * by {@code PresetTagRulesTest}.
 */
public final class PresetTagResolverGameTest implements FabricGameTest {
  private static final String BATCH = "preset-tags";
  private static final int TICK_LIMIT = 100;

  private static final List<Expectation> EXPECTATIONS =
      List.of(
          new Expectation(Items.OAK_PLANKS, PresetTag.WOOD),
          new Expectation(Items.OAK_PLANKS, PresetTag.BUILDING_BLOCKS),
          new Expectation(Items.OAK_LOG, PresetTag.WOOD),
          new Expectation(Items.STONE, PresetTag.STONE),
          new Expectation(Items.STONE, PresetTag.BUILDING_BLOCKS),
          new Expectation(Items.DEEPSLATE_BRICKS, PresetTag.STONE),
          new Expectation(Items.IRON_INGOT, PresetTag.ORES_AND_MINERALS),
          new Expectation(Items.DIAMOND, PresetTag.ORES_AND_MINERALS),
          new Expectation(Items.RAW_GOLD, PresetTag.ORES_AND_MINERALS),
          new Expectation(Items.DIAMOND_PICKAXE, PresetTag.TOOLS),
          new Expectation(Items.SHEARS, PresetTag.TOOLS),
          new Expectation(Items.DIAMOND_SWORD, PresetTag.WEAPONS_AND_ARMOUR),
          new Expectation(Items.BOW, PresetTag.WEAPONS_AND_ARMOUR),
          new Expectation(Items.IRON_CHESTPLATE, PresetTag.WEAPONS_AND_ARMOUR),
          new Expectation(Items.COOKED_BEEF, PresetTag.FOOD),
          new Expectation(Items.GOLDEN_APPLE, PresetTag.FOOD),
          new Expectation(Items.REDSTONE, PresetTag.REDSTONE),
          new Expectation(Items.PISTON, PresetTag.REDSTONE),
          new Expectation(Items.POWERED_RAIL, PresetTag.REDSTONE),
          new Expectation(Items.WHITE_WOOL, PresetTag.DECORATION),
          new Expectation(Items.WHITE_BED, PresetTag.DECORATION),
          new Expectation(Items.PAINTING, PresetTag.DECORATION),
          new Expectation(Items.OAK_SAPLING, PresetTag.NATURE),
          new Expectation(Items.WHEAT_SEEDS, PresetTag.NATURE),
          new Expectation(Items.OAK_LEAVES, PresetTag.NATURE),
          new Expectation(Items.BONE, PresetTag.MOB_DROPS),
          new Expectation(Items.GUNPOWDER, PresetTag.MOB_DROPS),
          new Expectation(Items.NETHER_WART, PresetTag.BREWING),
          new Expectation(Items.POTION, PresetTag.BREWING),
          new Expectation(Items.GLASS_BOTTLE, PresetTag.BREWING));

  /**
   * Items no vanilla tag covers, so reaching the right preset is only possible through the
   * convention tags the presets are composed from. These stand in for the modded items that reach
   * the same presets the same way.
   */
  private static final List<Expectation> CONVENTION_ONLY =
      List.of(
          new Expectation(Items.COPPER_INGOT, PresetTag.ORES_AND_MINERALS),
          new Expectation(Items.GOLD_NUGGET, PresetTag.ORES_AND_MINERALS),
          new Expectation(Items.AMETHYST_CLUSTER, PresetTag.ORES_AND_MINERALS),
          new Expectation(Items.STRING, PresetTag.MOB_DROPS),
          new Expectation(Items.LEATHER, PresetTag.MOB_DROPS),
          new Expectation(Items.SLIME_BALL, PresetTag.MOB_DROPS),
          new Expectation(Items.BLAZE_ROD, PresetTag.MOB_DROPS),
          new Expectation(Items.WHEAT, PresetTag.NATURE),
          new Expectation(Items.RED_DYE, PresetTag.DECORATION),
          new Expectation(Items.PURPLE_STAINED_GLASS, PresetTag.DECORATION));

  private final PresetTagResolver resolver = new PresetTagResolver();

  private record Expectation(Item item, PresetTag preset) {}

  @CustomTestProvider
  public Collection<TestFunction> everyNamedItemIsFiledUnderTheExpectedPreset() {
    return EXPECTATIONS.stream().map(expectation -> filingCase("files", expectation)).toList();
  }

  @CustomTestProvider
  public Collection<TestFunction> conventionTagsCarryItemsNoVanillaTagCovers() {
    return CONVENTION_ONLY.stream()
        .map(expectation -> filingCase("convention", expectation))
        .toList();
  }

  private TestFunction filingCase(String behaviour, Expectation expectation) {
    return new TestFunction(
        BATCH,
        "presettagresolver."
            + behaviour
            + "."
            + Registries.ITEM.getId(expectation.item()).getPath()
            + "."
            + expectation.preset().id(),
        FabricGameTest.EMPTY_STRUCTURE,
        TICK_LIMIT,
        0L,
        true,
        context -> assertFiledUnder(context, expectation));
  }

  private void assertFiledUnder(TestContext context, Expectation expectation) {
    Set<PresetTag> presets = resolver.resolve(expectation.item());

    context.assertTrue(
        presets.contains(expectation.preset()),
        Registries.ITEM.getId(expectation.item())
            + " should be filed under "
            + expectation.preset().id()
            + " but was filed under "
            + presets);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyItemInTheRegistryIsFiledUnderAtLeastOnePreset(TestContext context) {
    List<Item> unfiled = new ArrayList<>();
    for (Item item : Registries.ITEM) {
      if (resolver.resolve(item).isEmpty()) {
        unfiled.add(item);
      }
    }

    context.assertTrue(unfiled.isEmpty(), "No preset accepted " + describe(unfiled));
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theFallbackIsNeverCombinedWithARealPreset(TestContext context) {
    List<Item> doubleFiled = new ArrayList<>();
    for (Item item : Registries.ITEM) {
      Set<PresetTag> presets = resolver.resolve(item);
      if (presets.contains(PresetTag.FALLBACK) && presets.size() > 1) {
        doubleFiled.add(item);
      }
    }

    context.assertTrue(
        doubleFiled.isEmpty(),
        "The fallback should only ever stand alone, but it was shared by " + describe(doubleFiled));
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyEdibleItemIsFiledUnderFoodWhetherOrNotATagListsIt(TestContext context) {
    List<Item> missed = new ArrayList<>();
    for (Item item : Registries.ITEM) {
      if (item.getComponents().contains(DataComponentTypes.FOOD)
          && !resolver.resolve(item).contains(PresetTag.FOOD)) {
        missed.add(item);
      }
    }

    context.assertTrue(missed.isEmpty(), "Food was not filed under Food: " + describe(missed));
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyTagBackedPresetHoldsSomething(TestContext context) {
    List<PresetTag> empty = new ArrayList<>(PresetTag.all());
    empty.remove(PresetTag.FALLBACK);
    for (Item item : Registries.ITEM) {
      empty.removeAll(resolver.resolve(item));
      if (empty.isEmpty()) {
        break;
      }
    }

    context.assertTrue(empty.isEmpty(), "These presets matched no item at all: " + empty);
    context.complete();
  }

  private static String describe(List<Item> items) {
    return items.stream().map(item -> Registries.ITEM.getId(item).toString()).toList().toString();
  }
}
