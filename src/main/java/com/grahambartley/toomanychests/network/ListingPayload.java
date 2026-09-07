package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.listing.ListingPage;
import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.listing.ListingWindow;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * One page of the listing on its way to the screen that asked for it.
 *
 * <p>A page is bounded by {@link ListingWindow#MAX_LENGTH} on both sides of the wire, so no
 * response is ever larger than one grid's worth of rows however much the network holds. That bound
 * is the reason a terminal reaching thousands of variants costs the same to open as one reaching a
 * handful.
 *
 * <p>The sync id lets a client drop a page meant for a screen it has since closed, and the revision
 * lets it drop one that a newer page has already overtaken.
 */
public record ListingPayload(int syncId, ListingPage<ItemVariant> page) implements CustomPayload {
  public static final CustomPayload.Id<ListingPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "listing"));

  public static final PacketCodec<RegistryByteBuf, ListingPayload> CODEC =
      PacketCodec.of(ListingPayload::write, ListingPayload::read);

  @Override
  public CustomPayload.Id<ListingPayload> getId() {
    return ID;
  }

  private static void write(ListingPayload payload, RegistryByteBuf buf) {
    final ListingPage<ItemVariant> page = payload.page();
    buf.writeVarInt(payload.syncId());
    buf.writeVarLong(page.revision());
    buf.writeVarInt(page.offset());
    buf.writeVarInt(page.totalMatching());
    buf.writeVarInt(page.size());
    for (final ListingRow<ItemVariant> row : page.rows()) {
      ItemVariant.PACKET_CODEC.encode(buf, row.variant());
      buf.writeVarLong(row.count());
    }
  }

  private static ListingPayload read(RegistryByteBuf buf) {
    final int syncId = buf.readVarInt();
    final long revision = buf.readVarLong();
    final int offset = buf.readVarInt();
    final int totalMatching = buf.readVarInt();
    final int rowCount = buf.readVarInt();
    if (rowCount < 0 || rowCount > ListingWindow.MAX_LENGTH) {
      throw new IllegalArgumentException("listing page carried " + rowCount + " rows");
    }
    final List<ListingRow<ItemVariant>> rows = new ArrayList<>(rowCount);
    for (int row = 0; row < rowCount; row++) {
      rows.add(new ListingRow<>(ItemVariant.PACKET_CODEC.decode(buf), buf.readVarLong()));
    }
    return new ListingPayload(syncId, new ListingPage<>(revision, offset, totalMatching, rows));
  }
}
