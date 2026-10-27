package com.grahambartley.totalstorage.screenhandler;

import com.grahambartley.totalstorage.filter.FilterCandidateFactory;
import com.grahambartley.totalstorage.filter.FuzzySearchAttachment;
import com.grahambartley.totalstorage.filter.ItemFilter;
import com.grahambartley.totalstorage.filter.NetworkFilterCandidates;
import com.grahambartley.totalstorage.filter.SearchQueryParser;
import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.listing.ListingPage;
import com.grahambartley.totalstorage.listing.ListingRequestOutcome;
import com.grahambartley.totalstorage.listing.NetworkListing;
import com.grahambartley.totalstorage.listing.TerminalListingState;
import com.grahambartley.totalstorage.network.ListingPayload;
import com.grahambartley.totalstorage.network.ListingRequestPayload;
import com.grahambartley.totalstorage.tag.NetworkTagLookup;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class TerminalListingService {
  private final FilterCandidateFactory candidates;

  public TerminalListingService() {
    this(new FilterCandidateFactory());
  }

  public TerminalListingService(FilterCandidateFactory candidates) {
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  public ListingRequestOutcome request(ServerPlayerEntity player, ListingRequestPayload payload) {
    if (player == null || payload == null) {
      return ListingRequestOutcome.IGNORED;
    }
    final TerminalScreenHandler handler = handlerOf(player, payload.syncId());
    if (handler == null) {
      return ListingRequestOutcome.IGNORED;
    }
    final boolean searchMoved = handler.listing().requested(payload.window(), filterOf(payload));
    send(player, handler);
    return searchMoved
        ? ListingRequestOutcome.ANSWERED_ON_A_NEW_SEARCH
        : ListingRequestOutcome.ANSWERED;
  }

  public boolean refresh(ServerPlayerEntity player) {
    if (player == null
        || !(player.currentScreenHandler instanceof TerminalScreenHandler handler)
        || !handler.listing().hasWindow()) {
      return false;
    }
    send(player, handler);
    return true;
  }

  public ListingPage<ItemVariant> pageFor(
      ServerPlayerEntity player, TerminalScreenHandler handler) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(handler, "handler");
    final TerminalListingState state = handler.listing();
    final long revision = state.nextRevision();
    if (!(player.getWorld() instanceof ServerWorld world)) {
      return ListingPage.offline(revision);
    }
    final NetworkMembership network =
        TerminalNetworks.power().network(world, handler.terminal()).orElse(null);
    if (network == null) {
      return ListingPage.offline(revision);
    }
    return NetworkListing.page(
        network.index(),
        new NetworkFilterCandidates(candidates, new NetworkTagLookup(tagsOf(world, handler))),
        FuzzySearchAttachment.applyTo(player, state.filter()),
        state.window(),
        revision);
  }

  private static NetworkTagSet tagsOf(ServerWorld world, TerminalScreenHandler handler) {
    return TerminalLookup.settingsAt(world, handler.terminal())
        .map(settings -> settings.tags())
        .orElseGet(NetworkTagSet::defaults);
  }

  private void send(ServerPlayerEntity player, TerminalScreenHandler handler) {
    ServerPlayNetworking.send(player, new ListingPayload(handler.syncId, pageFor(player, handler)));
  }

  private static ItemFilter filterOf(ListingRequestPayload payload) {
    return new ItemFilter(
        payload.tags(), payload.mode(), SearchQueryParser.parse(payload.search()));
  }

  private static TerminalScreenHandler handlerOf(ServerPlayerEntity player, int syncId) {
    return player.currentScreenHandler instanceof TerminalScreenHandler handler
            && handler.syncId == syncId
        ? handler
        : null;
  }
}
