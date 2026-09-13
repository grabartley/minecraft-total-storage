package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.config.ServerConfigService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MiddleClickSort {
  private static final ContainerSortService SORTING = new ContainerSortService();

  private MiddleClickSort() {}

  public static boolean handle(
      ScreenHandler handler, SlotActionType actionType, PlayerEntity player) {
    if (!(player instanceof ServerPlayerEntity serverPlayer) || handler == null) {
      return false;
    }
    if (!MiddleClickSortGesture.isSortGesture(
        actionType,
        serverPlayer.getAbilities().creativeMode,
        ServerConfigService.current().middleClickSortEnabled())) {
      return false;
    }
    final SortOutcome outcome = SORTING.sort(serverPlayer, handler.syncId);
    if (outcome.fallsThroughToVanilla()) {
      return false;
    }
    if (outcome != SortOutcome.ALREADY_SORTED && outcome != SortOutcome.NOTHING_TO_SORT) {
      SortFeedback.report(serverPlayer, outcome);
    }
    return true;
  }
}
