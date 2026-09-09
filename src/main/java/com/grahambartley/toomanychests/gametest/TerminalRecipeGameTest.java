package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.ModItems;
import com.grahambartley.toomanychests.TooManyChests;
import java.util.Arrays;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

public final class TerminalRecipeGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-recipe";

  private static final Identifier RECIPE = Identifier.of(TooManyChests.MOD_ID, "terminal");
  private static final Identifier RECIPE_ADVANCEMENT =
      Identifier.of(TooManyChests.MOD_ID, "recipes/redstone/terminal");

  private static final Item I = Items.IRON_INGOT;
  private static final Item R = Items.REDSTONE;
  private static final Item C = Items.CHEST;
  private static final Item E = Items.ENDER_PEARL;

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void theServerLoadsTheTerminalRecipe(TestContext context) {
    context.assertTrue(
        recipeManager(context).get(RECIPE).isPresent(),
        "The server should load a recipe called " + RECIPE);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void theIntendedGridCraftsOneTerminal(TestContext context) {
    CraftingRecipeInput grid = gridOf(I, E, I, R, C, R, I, R, I);
    Optional<RecipeEntry<CraftingRecipe>> matched = firstMatch(context, grid);

    context.assertTrue(matched.isPresent(), "The intended grid should match a crafting recipe");
    context.assertTrue(
        matched.get().id().equals(RECIPE),
        "The intended grid should match " + RECIPE + ", not " + matched.get().id());

    ItemStack crafted = matched.get().value().craft(grid, context.getWorld().getRegistryManager());
    context.assertTrue(
        crafted.isOf(ModItems.TERMINAL), "The intended grid should craft a terminal");
    context.assertEquals(crafted.getCount(), 1, "The intended grid should craft exactly one");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aGridWithoutTheEnderPearlDoesNotCraftATerminal(TestContext context) {
    Optional<RecipeEntry<CraftingRecipe>> matched =
        firstMatch(context, gridOf(I, I, I, R, C, R, I, R, I));

    context.assertFalse(
        matched.isPresent() && matched.get().id().equals(RECIPE),
        "Dropping the ender pearl should stop the grid crafting a terminal");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aGridWithoutTheChestDoesNotCraftATerminal(TestContext context) {
    Optional<RecipeEntry<CraftingRecipe>> matched =
        firstMatch(context, gridOf(I, E, I, R, I, R, I, R, I));

    context.assertFalse(
        matched.isPresent() && matched.get().id().equals(RECIPE),
        "Dropping the chest should stop the grid crafting a terminal");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aPlayerWithNoEnderPearlHasNotUnlockedTheRecipe(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

    context.assertFalse(
        hasRecipeAdvancement(context, player),
        "A player who has never held an ender pearl should not have the recipe yet");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void holdingAnEnderPearlUnlocksTheRecipe(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    ItemStack pearl = new ItemStack(Items.ENDER_PEARL);
    player.getInventory().insertStack(pearl.copy());

    Criteria.INVENTORY_CHANGED.trigger(player, player.getInventory(), pearl);

    context.assertTrue(
        hasRecipeAdvancement(context, player),
        "Picking up an ender pearl should award the terminal recipe advancement");
    context.assertTrue(
        player.getRecipeBook().contains(RECIPE),
        "The terminal recipe should be in the recipe book once its advancement is awarded");
    context.complete();
  }

  private static boolean hasRecipeAdvancement(TestContext context, ServerPlayerEntity player) {
    AdvancementEntry advancement = server(context).getAdvancementLoader().get(RECIPE_ADVANCEMENT);
    context.assertTrue(
        advancement != null, "The server should load an advancement called " + RECIPE_ADVANCEMENT);
    return player.getAdvancementTracker().getProgress(advancement).isDone();
  }

  private static Optional<RecipeEntry<CraftingRecipe>> firstMatch(
      TestContext context, CraftingRecipeInput grid) {
    return recipeManager(context).getFirstMatch(RecipeType.CRAFTING, grid, context.getWorld());
  }

  private static RecipeManager recipeManager(TestContext context) {
    return server(context).getRecipeManager();
  }

  private static MinecraftServer server(TestContext context) {
    return context.getWorld().getServer();
  }

  private static CraftingRecipeInput gridOf(Item... items) {
    return CraftingRecipeInput.create(3, 3, Arrays.stream(items).map(ItemStack::new).toList());
  }
}
