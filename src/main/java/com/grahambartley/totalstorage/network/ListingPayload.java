package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.listing.ListingPage;
import com.grahambartley.totalstorage.listing.ListingRow;
import com.grahambartley.totalstorage.listing.ListingStatus;
import com.grahambartley.totalstorage.listing.ListingWindow;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ListingPayload(int syncId, ListingPage<ItemVariant> page) implements CustomPayload {
  public static final CustomPayload.Id<ListingPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "listing"));

  public static final PacketCodec<RegistryByteBuf, ListingPayload> CODEC =
      PacketCodec.of(ListingPayload::write, ListingPayload::read);

  public ListingPayload {
    Objects.requireNonNull(page, "page");
  }

  @Override
  public CustomPayload.Id<ListingPayload> getId() {
    return ID;
  }

  private static void write(ListingPayload payload, RegistryByteBuf buf) {
    final ListingPage<ItemVariant> page = payload.page();
    buf.writeVarInt(payload.syncId());
    buf.writeVarLong(page.revision());
    buf.writeEnumConstant(page.status());
    buf.writeVarInt(page.offset());
    buf.writeVarInt(page.totalMatching());
    buf.writeVarInt(page.size());
    for (final ListingRow<ItemVariant> row : page.rows()) {
      ItemVariant.PACKET_CODEC.encode(buf, row.variant());
      buf.writeVarLong(row.count());
      buf.writeVarInt(row.providerCount());
    }
  }

  private static ListingPayload read(RegistryByteBuf buf) {
    final int syncId = buf.readVarInt();
    final long revision = buf.readVarLong();
    final ListingStatus status = buf.readEnumConstant(ListingStatus.class);
    final int offset = buf.readVarInt();
    final int totalMatching = buf.readVarInt();
    final int rowCount = buf.readVarInt();
    if (rowCount < 0 || rowCount > ListingWindow.MAX_LENGTH) {
      throw new IllegalArgumentException("listing page carried " + rowCount + " rows");
    }
    final List<ListingRow<ItemVariant>> rows = new ArrayList<>(rowCount);
    for (int row = 0; row < rowCount; row++) {
      rows.add(
          new ListingRow<>(
              ItemVariant.PACKET_CODEC.decode(buf), buf.readVarLong(), buf.readVarInt()));
    }
    return new ListingPayload(
        syncId, new ListingPage<>(revision, status, offset, totalMatching, rows));
  }
}
