package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.operation.DepositDestinations;
import com.grahambartley.totalstorage.storage.ContainerAssignment;
import com.grahambartley.totalstorage.storage.ContainerFilter;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DepositDestinationsGameTest implements FabricGameTest {
  private static final String BATCH = "deposit-destinations";
  private static final int TICK_LIMIT = 60;

  private static final String CUSTOM_TAG = "shinies";

  private final DepositDestinations destinations = new DepositDestinations();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void everyWritableContainerIsOfferedInRoutingOrder(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    List<Storage<ItemVariant>> offered = routeFor(context, Items.DIAMOND);

    context.assertTrue(offered.size() == 2, "Offered " + offered.size() + " of two containers");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anExcludedContainerIsOfferedToNothing(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context, NetworkOperationFixture.EAST_BARREL, ContainerRole.EXCLUDED);

    context.assertTrue(
        routeFor(context, Items.DIAMOND).isEmpty(), "An excluded container was offered");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFilterNamingAPresetMatchesWhatTheGameFilesUnderIt(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context,
        NetworkOperationFixture.EAST_BARREL,
        ContainerAssignment.filtered(ContainerFilter.ofTags("ores_and_minerals")));

    context.assertTrue(
        routeFor(context, Items.DIAMOND).size() == 1,
        "A preset tag did not reach the item the game files under it");
    context.assertTrue(
        routeFor(context, Items.OAK_LOG).isEmpty(),
        "A preset tag matched an item that is not filed under it");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFilterNamingACustomTagMatchesWhatTheTerminalFiledUnderIt(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.fileUnderCustomTag(context, CUSTOM_TAG, "minecraft:diamond");
    NetworkOperationFixture.assign(
        context,
        NetworkOperationFixture.EAST_BARREL,
        ContainerAssignment.filtered(ContainerFilter.ofTags(CUSTOM_TAG)));

    context.assertTrue(
        routeFor(context, Items.DIAMOND).size() == 1,
        "A custom tag did not reach the item the terminal filed under it");
    context.assertTrue(
        routeFor(context, Items.OAK_LOG).isEmpty(),
        "A custom tag matched an item nobody filed under it");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void severalVariantsAreAnsweredInOnePassOverTheNetwork(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    Map<ItemVariant, List<Storage<ItemVariant>>> routed =
        destinations.forVariants(
            context.getWorld(),
            terminal(context),
            network(context),
            List.of(ItemVariant.of(Items.DIAMOND), ItemVariant.of(Items.OAK_LOG)));

    context.assertTrue(routed.size() == 2, "Answered " + routed.size() + " of two variants");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void askingAboutNothingWalksTheNetworkAtAll(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    context.assertTrue(
        destinations
            .forVariants(context.getWorld(), terminal(context), network(context), List.of())
            .isEmpty(),
        "Asking about no variants answered something");
    NetworkOperationFixture.finish(context, player);
  }

  private List<Storage<ItemVariant>> routeFor(TestContext context, net.minecraft.item.Item item) {
    final ItemVariant variant = ItemVariant.of(item);
    return destinations
        .forVariants(context.getWorld(), terminal(context), network(context), List.of(variant))
        .getOrDefault(variant, List.of());
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }

  private static NetworkMembership network(TestContext context) {
    return TerminalNetworks.power().network(context.getWorld(), terminal(context)).orElseThrow();
  }
}
