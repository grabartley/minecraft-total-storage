package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.NetworkOperationPhase;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NetworkOperationStatusPayload(
    int syncId, ExpensiveOperation operation, NetworkOperationPhase phase, int percentDone)
    implements CustomPayload {
  public static final CustomPayload.Id<NetworkOperationStatusPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TotalStorage.MOD_ID, "network_operation_status"));

  public static final PacketCodec<RegistryByteBuf, NetworkOperationStatusPayload> CODEC =
      PacketCodec.of(NetworkOperationStatusPayload::write, NetworkOperationStatusPayload::read);

  public NetworkOperationStatusPayload {
    Objects.requireNonNull(operation, "operation");
    Objects.requireNonNull(phase, "phase");
    percentDone = Math.max(0, Math.min(100, percentDone));
  }

  @Override
  public CustomPayload.Id<NetworkOperationStatusPayload> getId() {
    return ID;
  }

  private static void write(NetworkOperationStatusPayload payload, RegistryByteBuf buf) {
    buf.writeVarInt(payload.syncId());
    buf.writeEnumConstant(payload.operation());
    buf.writeEnumConstant(payload.phase());
    buf.writeVarInt(payload.percentDone());
  }

  private static NetworkOperationStatusPayload read(RegistryByteBuf buf) {
    return new NetworkOperationStatusPayload(
        buf.readVarInt(),
        buf.readEnumConstant(ExpensiveOperation.class),
        buf.readEnumConstant(NetworkOperationPhase.class),
        buf.readVarInt());
  }
}
