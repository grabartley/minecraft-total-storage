package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.operation.DepositSource;
import com.grahambartley.toomanychests.operation.ItemStackLimits;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A player pushing one stack of their own into the network.
 *
 * <p>The item is not named. What is deposited is whatever the named source is really holding when
 * the server looks, which is what stops a client claiming to be depositing diamonds while its
 * cursor holds dirt. The slot index names the player's own inventory rather than the network, so
 * ADR 0009 still holds; it is validated against the lockable range before anything is read from it.
 *
 * <p>An index outside that range is carried through as it arrived rather than clamped into it. A
 * clamp would turn a request that should be refused into one acting on the first hotbar slot, which
 * is a forged packet quietly reaching a real slot instead of reaching none.
 *
 * <p>The amount is what the gesture asked for and is clamped to what the source really holds. A
 * whole-stack deposit sends the ceiling rather than a count read from a client-side stack, so a
 * stack that grew between the click and the packet is deposited whole.
 */
public record DepositRequestPayload(int syncId, DepositSource source, int slot, int amount)
    implements CustomPayload {

  /** Both sources hold one stack at most, so no honest request asks for more than a stack. */
  public static final int MAX_AMOUNT = ItemStackLimits.MAX_STACK_SIZE;

  public static final CustomPayload.Id<DepositRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "deposit_request"));

  public static final PacketCodec<RegistryByteBuf, DepositRequestPayload> CODEC =
      PacketCodec.of(DepositRequestPayload::write, DepositRequestPayload::read);

  public DepositRequestPayload {
    source = source == null ? DepositSource.CURSOR : source;
    amount = Math.max(0, Math.min(MAX_AMOUNT, amount));
  }

  /** Everything the source holds, whatever that turns out to be. */
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
