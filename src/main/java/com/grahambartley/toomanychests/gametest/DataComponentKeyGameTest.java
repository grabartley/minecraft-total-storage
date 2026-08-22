package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.sort.DataComponentKey;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

/**
 * Component changes only exist on a real stack, so the encoding is proven here. What matters is
 * that two stacks a player would call identical always key identically, because an unstable key
 * makes the sort shuffle variants of one item on every press.
 */
public final class DataComponentKeyGameTest implements FabricGameTest {
  private static final String BATCH = "sort-candidates";
  private static final int TICK_LIMIT = 20;

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aStackWithNoComponentChangesKeysEmpty(TestContext context) {
    String key = DataComponentKey.of(new ItemStack(Items.DIAMOND_SWORD).getComponentChanges());

    context.assertTrue(key.isEmpty(), "A plain sword should key empty, found " + key);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void absentChangesKeyEmpty(TestContext context) {
    context.assertTrue(DataComponentKey.of(null).isEmpty(), "Absent changes should key empty");
    context.assertTrue(
        DataComponentKey.of(ComponentChanges.EMPTY).isEmpty(), "No changes should key empty");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void stacksDifferingByAComponentValueKeyDifferently(TestContext context) {
    String sting = DataComponentKey.of(sword("Sting", 1).getComponentChanges());
    String glamdring = DataComponentKey.of(sword("Glamdring", 1).getComponentChanges());

    context.assertFalse(
        sting.equals(glamdring), "Two differently named swords should not share a key: " + sting);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theKeyIgnoresTheOrderTheComponentsWereSetIn(TestContext context) {
    ItemStack nameFirst = new ItemStack(Items.DIAMOND_SWORD);
    nameFirst.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Sting"));
    nameFirst.set(DataComponentTypes.DAMAGE, 12);
    ItemStack damageFirst = new ItemStack(Items.DIAMOND_SWORD);
    damageFirst.set(DataComponentTypes.DAMAGE, 12);
    damageFirst.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Sting"));

    String first = DataComponentKey.of(nameFirst.getComponentChanges());
    String second = DataComponentKey.of(damageFirst.getComponentChanges());

    context.assertTrue(
        first.equals(second),
        "The same components should key identically, found " + first + " and " + second);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theSameStackKeysTheSameWayEveryTime(TestContext context) {
    ItemStack stack = sword("Sting", 12);

    context.assertTrue(
        DataComponentKey.of(stack.getComponentChanges())
            .equals(DataComponentKey.of(stack.getComponentChanges())),
        "Keying is not deterministic");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRemovedComponentKeysApartFromOneThatWasNeverSet(TestContext context) {
    ItemStack removed = new ItemStack(Items.DIAMOND_SWORD);
    removed.remove(DataComponentTypes.MAX_DAMAGE);

    String key = DataComponentKey.of(removed.getComponentChanges());

    context.assertFalse(key.isEmpty(), "A removed component is still a difference worth keying");
    context.complete();
  }

  private static ItemStack sword(String name, int damage) {
    ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
    stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
    stack.set(DataComponentTypes.DAMAGE, damage);
    return stack;
  }
}
