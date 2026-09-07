package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.listing.ListingPage;
import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.listing.ListingStatus;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.grahambartley.toomanychests.network.ListingPayload;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * What a page looks like once it has been across the wire.
 *
 * <p>A gametest rather than a unit test because a page carries item variants, and encoding one
 * needs the registries a plain JVM does not have.
 */
public final class ListingPayloadGameTest implements FabricGameTest {
  private static final String BATCH = "listing-payload";
  private static final int SYNC_ID = 4;

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void everyRowSurvivesTheWireUnchanged(TestContext context) {
    ListingPage<ItemVariant> sent =
        new ListingPage<>(
            9L,
            ListingStatus.LIVE,
            72,
            5000,
            List.of(
                new ListingRow<>(ItemVariant.of(Items.DIAMOND), 4096L, 7),
                new ListingRow<>(ItemVariant.of(Items.OAK_LOG), 1L, 1)));

    ListingPage<ItemVariant> received = roundTrip(context, sent).page();

    context.assertTrue(
        received.revision() == 9L, "The stamp should survive, got " + received.revision());
    context.assertTrue(
        received.offset() == 72, "The offset should survive, got " + received.offset());
    context.assertTrue(
        received.totalMatching() == 5000,
        "The total should survive, got " + received.totalMatching());
    context.assertTrue(received.size() == 2, "Both rows should survive, got " + received.size());
    context.assertTrue(
        received.rows().get(0).variant().getItem() == Items.DIAMOND
            && received.rows().get(0).count() == 4096L,
        "The diamonds and their count should survive");
    context.assertTrue(
        received.rows().get(0).providerCount() == 7,
        "How many containers hold them should survive, got "
            + received.rows().get(0).providerCount());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void anEmptyPageIsStillAWholePage(TestContext context) {
    ListingPage<ItemVariant> received = roundTrip(context, ListingPage.offline(3L)).page();

    context.assertTrue(received.isEmpty(), "An empty page should arrive empty");
    context.assertTrue(received.revision() == 3L, "An empty page still carries its stamp");
    context.complete();
  }

  /**
   * The status is the only thing separating the several ways a grid comes up empty, and every one
   * of them travels as a page carrying no rows at all. Losing it on the wire would leave a screen
   * unable to say which of them it is showing.
   */
  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aRowlessPageStillSaysWhyItHasNoRows(TestContext context) {
    ListingPage<ItemVariant> received =
        roundTrip(
                context,
                new ListingPage<ItemVariant>(2L, ListingStatus.NO_RESULTS, 0, 0, List.of()))
            .page();

    context.assertTrue(
        received.status() == ListingStatus.NO_RESULTS,
        "The reason the page is empty should survive, got " + received.status());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void theWholePayloadIsReadBackWithNothingLeftOver(TestContext context) {
    RegistryByteBuf buffer = buffer(context);

    ListingPayload.CODEC.encode(
        buffer,
        new ListingPayload(
            SYNC_ID,
            new ListingPage<>(
                1L,
                ListingStatus.LIVE,
                0,
                1,
                List.of(new ListingRow<>(ItemVariant.of(Items.DIAMOND), 1L, 1)))));
    ListingPayload.CODEC.decode(buffer);

    context.assertTrue(
        buffer.readableBytes() == 0,
        "A page should read back exactly, left " + buffer.readableBytes() + " bytes");
    context.complete();
  }

  /**
   * The cap is what keeps a response bounded however much the network holds, so a packet claiming
   * more rows than a page may carry is refused before any of them are read.
   */
  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aPacketClaimingMoreRowsThanAPageMayCarryIsRefused(TestContext context) {
    RegistryByteBuf buffer = buffer(context);
    buffer.writeVarInt(SYNC_ID);
    buffer.writeVarLong(1L);
    buffer.writeEnumConstant(ListingStatus.LIVE);
    buffer.writeVarInt(0);
    buffer.writeVarInt(0);
    buffer.writeVarInt(ListingWindow.MAX_LENGTH + 1);

    context.assertTrue(
        refusedToDecode(buffer), "A page over the cap should be refused rather than read");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aPacketClaimingANegativeNumberOfRowsIsRefused(TestContext context) {
    RegistryByteBuf buffer = buffer(context);
    buffer.writeVarInt(SYNC_ID);
    buffer.writeVarLong(1L);
    buffer.writeEnumConstant(ListingStatus.LIVE);
    buffer.writeVarInt(0);
    buffer.writeVarInt(0);
    buffer.writeVarInt(-1);

    context.assertTrue(refusedToDecode(buffer), "A page claiming negative rows should be refused");
    context.complete();
  }

  /** A page longer than the cap cannot be built, so one can never be written either. */
  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void aPageOverTheCapCannotBeBuiltInTheFirstPlace(TestContext context) {
    final List<ListingRow<ItemVariant>> tooMany = new ArrayList<>();
    for (int row = 0; row <= ListingWindow.MAX_LENGTH; row++) {
      tooMany.add(new ListingRow<>(ItemVariant.of(Items.DIAMOND), 1L, 1));
    }

    boolean refused;
    try {
      new ListingPage<>(1L, ListingStatus.LIVE, 0, tooMany.size(), tooMany);
      refused = false;
    } catch (IllegalArgumentException expected) {
      refused = true;
    }

    context.assertTrue(refused, "A page over the cap should refuse to exist");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void everyPayloadReportsTheSharedChannelId(TestContext context) {
    context.assertTrue(
        "too-many-chests:listing".equals(ListingPayload.ID.id().toString()),
        "The channel should be the shared one, got " + ListingPayload.ID.id());
    context.complete();
  }

  private static boolean refusedToDecode(RegistryByteBuf buffer) {
    try {
      ListingPayload.CODEC.decode(buffer);
      return false;
    } catch (RuntimeException expected) {
      return true;
    }
  }

  private static ListingPayload roundTrip(TestContext context, ListingPage<ItemVariant> page) {
    RegistryByteBuf buffer = buffer(context);
    ListingPayload.CODEC.encode(buffer, new ListingPayload(SYNC_ID, page));
    return ListingPayload.CODEC.decode(buffer);
  }

  private static RegistryByteBuf buffer(TestContext context) {
    return new RegistryByteBuf(Unpooled.buffer(), context.getWorld().getRegistryManager());
  }
}
