package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.lock.LockToggleService;
import com.grahambartley.toomanychests.operation.DepositService;
import com.grahambartley.toomanychests.operation.WithdrawService;
import com.grahambartley.toomanychests.screenhandler.TerminalListingService;
import com.grahambartley.toomanychests.sort.ContainerSortService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Registers the mod's packets and the server-side handlers that resolve them.
 *
 * <p>Payload types register here rather than in the client initialiser because both sides must
 * agree on the codec before a channel is usable, and a client running this same entrypoint gets the
 * registration for free.
 */
public final class ModNetworking {
  private static final ContainerSortService SORTING = new ContainerSortService();
  private static final TerminalListingService LISTINGS = new TerminalListingService();
  private static final WithdrawService WITHDRAWALS = new WithdrawService();
  private static final DepositService DEPOSITS = new DepositService();

  /**
   * The one listing service, so whatever pushes pages answers with the same one that resolves
   * requests.
   */
  public static TerminalListingService listings() {
    return LISTINGS;
  }

  private ModNetworking() {}

  public static void register() {
    PayloadTypeRegistry.playC2S().register(LockTogglePayload.ID, LockTogglePayload.CODEC);
    PayloadTypeRegistry.playC2S().register(ConfigEditPayload.ID, ConfigEditPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(SortRequestPayload.ID, SortRequestPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(ListingRequestPayload.ID, ListingRequestPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(WithdrawRequestPayload.ID, WithdrawRequestPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(DepositRequestPayload.ID, DepositRequestPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(DepositAllRequestPayload.ID, DepositAllRequestPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(ListingPayload.ID, ListingPayload.CODEC);

    ServerPlayNetworking.registerGlobalReceiver(
        LockTogglePayload.ID,
        (payload, context) -> LockToggleService.toggle(context.player(), payload.slot()));
    ServerPlayNetworking.registerGlobalReceiver(
        SortRequestPayload.ID,
        (payload, context) ->
            context.server().execute(() -> SORTING.sort(context.player(), payload.syncId())));
    ServerPlayNetworking.registerGlobalReceiver(
        ConfigEditPayload.ID,
        (payload, context) ->
            context
                .server()
                .execute(() -> ConfigSync.handleEdit(context.player(), payload.config())));

    ServerPlayNetworking.registerGlobalReceiver(
        ListingRequestPayload.ID,
        (payload, context) ->
            context.server().execute(() -> LISTINGS.request(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        WithdrawRequestPayload.ID,
        (payload, context) ->
            context.server().execute(() -> WITHDRAWALS.withdraw(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        DepositRequestPayload.ID,
        (payload, context) ->
            context.server().execute(() -> DEPOSITS.deposit(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        DepositAllRequestPayload.ID,
        (payload, context) ->
            context
                .server()
                .execute(() -> DEPOSITS.depositAll(context.player(), payload.syncId())));

    TerminalSettingsNetworking.register();
    ConfigSync.register();
  }
}
