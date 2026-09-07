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

/**
 * A player asking the network for some of an item.
 *
 * <p>Per ADR 0009 this is the shape the whole mod's safety rests on: it names <em>what</em> the
 * player wants and never <em>where it is</em>. There is no container position here, no slot index
 * into the network, and no page offset, so a modified client has nothing positional to lie about.
 * The server finds the item against live containers at the moment it extracts.
 *
 * <p>The sync id names which screen is asking and is checked against the handler the server already
 * holds for that player, so the request cannot reach a terminal the sender does not have open.
 *
 * <p>The amount is bounded while decoding rather than after it. It is clamped again server-side
 * against what the destination can really hold; this bound only stops one packet claiming a number
 * that would be absurd to act on at all.
 */
public record WithdrawRequestPayload(
    int syncId, ItemVariant variant, int amount, WithdrawDestination destination)
    implements CustomPayload {

  /** More than the player's own slots could hold of anything, so no honest request is refused. */
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

  /**
   * The bound every decoded amount is held to, before anything acts on it. Public so it can be
   * proven on its own: building a payload needs an {@code ItemVariant}, and that needs a registry.
   */
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
