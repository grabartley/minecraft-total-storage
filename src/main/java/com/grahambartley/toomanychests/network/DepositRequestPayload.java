package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.operation.DepositSource;
import com.grahambartley.toomanychests.operation.ItemStackLimits;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DepositRequestPayload(int syncId, DepositSource source, int slot, int amount)
    implements CustomPayload {

  public static final int MAX_AMOUNT = ItemStackLimits.MAX_STACK_SIZE;

  public static final CustomPayload.Id<DepositRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "deposit_request"));

  public static final PacketCodec<RegistryByteBuf, DepositRequestPayload> CODEC =
      PacketCodec.of(DepositRequestPayload::write, DepositRequestPayload::read);

  public DepositRequestPayload {
    source = source == null ? DepositSource.CURSOR : source;
    amount = Math.max(0, Math.min(MAX_AMOUNT, amount));
  }

  public static DepositRequestPayload wholeCursor(int syncId) {
    return new DepositRequestPayload(syncId, DepositSource.CURSOR, 0, MAX_AMOUNT);
  }

  public static DepositRequestPayload oneFromCursor(int syncId) {
    return new DepositRequestPayload(syncId, DepositSource.CURSOR, 0, 1);
  }

  public static DepositRequestPayload wholeSlot(int syncId, int slot) {
    return new DepositRequestPayload(syncId, DepositSource.SLOT, slot, MAX_AMOUNT);
  }

  @Override
  public CustomPayload.Id<DepositRequestPayload> getId() {
    return ID;
  }

  private static void write(DepositRequestPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeEnumConstant(payload.source());
    buf.writeVarInt(payload.slot());
    buf.writeVarInt(payload.amount());
  }

  private static DepositRequestPayload read(RegistryByteBuf buf) {
    return new DepositRequestPayload(
        buf.readVarInt(),
        buf.readEnumConstant(DepositSource.class),
        buf.readVarInt(),
        buf.readVarInt());
  }
}
