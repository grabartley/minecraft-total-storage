package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.network.NetworkSettingsPayload;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.NetworkCapacity;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class NetworkSettingsPayloadGameTest implements FabricGameTest {
  private static final String BATCH = "network-settings-payload";
  private static final int SYNC_ID = 6;

  private static final BlockPos WHERE = new BlockPos(31, 70, -4);

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void theIconARowLeadsWithSurvivesTheWire(TestContext context) {
    ContainerRoleEntry received =
        roundTrip(context, row(Optional.of(ItemVariant.of(Items.COBBLESTONE))))
            .view()
            .containers()
            .get(0);

    context.assertTrue(received.icon().isPresent(), "The row arrived with no icon at all");
    context.assertTrue(
        received.icon().get().isOf(Items.COBBLESTONE),
        "The row should still be iconed with cobblestone, got " + received.icon().get());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aRowWithNothingToShowArrivesWithNoIcon(TestContext context) {
    ContainerRoleEntry received =
        roundTrip(context, row(Optional.empty())).view().containers().get(0);

    context.assertTrue(
        received.icon().isEmpty(),
        "An empty container should arrive with no icon, got " + received);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void anIconedRowStillCarriesEverythingElseItSays(TestContext context) {
    ContainerRoleEntry received =
        roundTrip(context, row(Optional.of(ItemVariant.of(Items.DIAMOND))))
            .view()
            .containers()
            .get(0);

    context.assertTrue(
        WHERE.equals(received.container()), "Where it is, got " + received.container());
    context.assertTrue(
        received.role() == ContainerRole.EXCLUDED, "Its role, got " + received.role());
    context.assertTrue(received.variantCount() == 4, "Its kinds, got " + received.variantCount());
    context.assertTrue(received.itemCount() == 900L, "Its total, got " + received.itemCount());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void anIconedPayloadIsReadBackWithNothingLeftOver(TestContext context) {
    RegistryByteBuf buffer = buffer(context);

    NetworkSettingsPayload.CODEC.encode(
        buffer, payload(row(Optional.of(ItemVariant.of(Items.OAK_LOG)))));
    NetworkSettingsPayload.CODEC.decode(buffer);

    context.assertTrue(
        buffer.readableBytes() == 0,
        "A settings answer should read back exactly, left " + buffer.readableBytes() + " bytes");
    context.complete();
  }

  private static ContainerRoleEntry row(Optional<ItemVariant> icon) {
    return new ContainerRoleEntry(WHERE, ContainerRole.EXCLUDED, icon, 4, 900L, 6, 27, 12.0);
  }

  private static NetworkSettingsPayload payload(ContainerRoleEntry row) {
    return new NetworkSettingsPayload(
        SYNC_ID,
        new NetworkSettingsView(
            16,
            16,
            32,
            TerminalAccessMode.TRUSTED,
            true,
            true,
            List.of("Alice"),
            new NetworkCapacity(9, 27),
            0,
            1,
            List.of(row)));
  }

  private static NetworkSettingsPayload roundTrip(TestContext context, ContainerRoleEntry row) {
    RegistryByteBuf buffer = buffer(context);
    NetworkSettingsPayload.CODEC.encode(buffer, payload(row));
    return NetworkSettingsPayload.CODEC.decode(buffer);
  }

  private static RegistryByteBuf buffer(TestContext context) {
    return new RegistryByteBuf(Unpooled.buffer(), context.getWorld().getRegistryManager());
  }
}
