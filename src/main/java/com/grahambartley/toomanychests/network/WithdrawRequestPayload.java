package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.lock.LockedSlots;
import com.grahambartley.toomanychests.operation.ItemStackLimits;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WithdrawRequestPayload(
    int syncId, ItemVariant variant, int amount, WithdrawDestination destination)
    implements CustomPayload {

  public static final int MAX_AMOUNT = LockedSlots.SLOT_COUNT * ItemStackLimits.MAX_STACK_SIZE;

  public static final CustomPayload.Id<WithdrawRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "withdraw_request"));

  public static final PacketCodec<RegistryByteBuf, WithdrawRequestPayload> CODEC =
      PacketCodec.of(WithdrawRequestPayload::write, WithdrawRequestPayload::read);

  public WithdrawRequestPayload {
    Objects.requireNonNull(variant, "variant");
    amount = boundedAmount(amount);
    destination = destination == null ? WithdrawDestination.CURSOR : destination;
  }

  public static int boundedAmount(int amount) {
    return Math.max(0, Math.min(MAX_AMOUNT, amount));
  }

  @Override
  public CustomPayload.Id<WithdrawRequestPayload> getId() {
    return ID;
  }

  private static void write(WithdrawRequestPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    ItemVariant.PACKET_CODEC.encode(buf, payload.variant());
    buf.writeVarInt(payload.amount());
    buf.writeEnumConstant(payload.destination());
  }

  private static WithdrawRequestPayload read(RegistryByteBuf buf) {
    return new WithdrawRequestPayload(
        buf.readVarInt(),
        ItemVariant.PACKET_CODEC.decode(buf),
        buf.readVarInt(),
        buf.readEnumConstant(WithdrawDestination.class));
  }
}
