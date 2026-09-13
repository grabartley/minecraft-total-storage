package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.lock.LockToggleService;
import com.grahambartley.toomanychests.operation.DepositService;
import com.grahambartley.toomanychests.operation.QuickStackFeedback;
import com.grahambartley.toomanychests.operation.QuickStackService;
import com.grahambartley.toomanychests.operation.WithdrawService;
import com.grahambartley.toomanychests.screenhandler.TerminalListingService;
import com.grahambartley.toomanychests.sort.ContainerSortService;
import com.grahambartley.toomanychests.sort.SortFeedback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
  private static final ContainerSortService SORTING = new ContainerSortService();
  private static final TerminalListingService LISTINGS = new TerminalListingService();
  private static final WithdrawService WITHDRAWALS = new WithdrawService();
  private static final DepositService DEPOSITS = new DepositService();
  private static final QuickStackService QUICK_STACKS = new QuickStackService();

  public static QuickStackService quickStacks() {
    return QUICK_STACKS;
  }

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
    PayloadTypeRegistry.playC2S()
        .register(QuickStackRequestPayload.ID, QuickStackRequestPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(ListingPayload.ID, ListingPayload.CODEC);

    ServerPlayNetworking.registerGlobalReceiver(
        LockTogglePayload.ID,
        (payload, context) -> LockToggleService.toggle(context.player(), payload.slot()));
    ServerPlayNetworking.registerGlobalReceiver(
        SortRequestPayload.ID,
        (payload, context) ->
            context
                .server()
                .execute(
                    () ->
                        SortFeedback.report(
                            context.player(),
                            SORTING.sortFromButton(context.player(), payload.syncId()))));
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

    ServerPlayNetworking.registerGlobalReceiver(
        QuickStackRequestPayload.ID,
        (payload, context) ->
            context
                .server()
                .execute(
                    () ->
                        QuickStackFeedback.report(
                            context.player(), QUICK_STACKS.quickStack(context.player()))));

    TerminalSettingsNetworking.register();
    ConfigSync.register();
  }
}
