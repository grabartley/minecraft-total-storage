package com.grahambartley.totalstorage.screenhandler;

import com.grahambartley.totalstorage.filter.FuzzySearchAttachment;
import java.util.Objects;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FuzzySearchService {
  private final TerminalListingService listings;
  private final TerminalTagPanelService panels;

  public FuzzySearchService(TerminalListingService listings, TerminalTagPanelService panels) {
    this.listings = Objects.requireNonNull(listings, "listings");
    this.panels = Objects.requireNonNull(panels, "panels");
  }

  public boolean choose(ServerPlayerEntity player, boolean wanted) {
    if (player == null || FuzzySearchAttachment.get(player) == wanted) {
      return false;
    }
    FuzzySearchAttachment.set(player, wanted);
    listings.refresh(player);
    panels.refresh(player);
    return true;
  }
}
