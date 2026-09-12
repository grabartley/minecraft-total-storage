package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.TooManyChests;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record QuickStackRequestPayload() implements CustomPayload {
  public static final CustomPayload.Id<QuickStackRequestPayload> ID =
      new CustomPayload.Id<>(Identifier.of(TooManyChests.MOD_ID, "quick_stack_request"));

  public static final PacketCodec<RegistryByteBuf, QuickStackRequestPayload> CODEC =
      PacketCodec.unit(new QuickStackRequestPayload());

  @Override
  public CustomPayload.Id<QuickStackRequestPayload> getId() {
    return ID;
  }
}
