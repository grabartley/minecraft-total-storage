package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.filter.FilterCandidateFactory;
import com.grahambartley.toomanychests.filter.ItemFilter;
import com.grahambartley.toomanychests.filter.SearchQueryParser;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.listing.ListingPage;
import com.grahambartley.toomanychests.listing.NetworkListing;
import com.grahambartley.toomanychests.listing.TerminalListingState;
import com.grahambartley.toomanychests.network.ListingPayload;
import com.grahambartley.toomanychests.network.ListingRequestPayload;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Answers a screen's request for a page, and builds the same page again on demand.
 *
 * <p>Every answer is built from the live index rather than from anything the client sent back, so a
 * page reflects the network as it stands at the moment it is written. Nothing here trusts the sync
 * id it was handed: the handler is the one the server already holds for that player, and the
 * request is dropped if it names a different screen.
 *
 * <p>A terminal running no network answers with an empty page rather than with silence. A screen
 * that heard nothing could not tell an unpowered terminal from a lost packet, and would sit waiting
 * on a reply that was never coming.
 *
 * <p>Deciding when a page is owed unprompted is not this class's job; that belongs to {@link
 * TerminalListingBroadcast}, which calls {@link #refresh} once a network has moved.
 */
public final class TerminalListingService {
  private final FilterCandidateFactory candidates;

  public TerminalListingService() {
    this(new FilterCandidateFactory());
  }

  public TerminalListingService(FilterCandidateFactory candidates) {
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  /** Records what the screen asked for and answers it, or drops a request naming another screen. */
  public boolean request(ServerPlayerEntity player, ListingRequestPayload payload) {
    if (player == null || payload == null) {
      return false;
    }
    final TerminalScreenHandler handler = handlerOf(player, payload.syncId());
    if (handler == null) {
      return false;
    }
    handler.listing().requested(payload.window(), filterOf(payload));
    send(player, handler);
    return true;
  }

  /**
   * Sends the page the screen is already asking for, which is how a change to the network reaches a
   * player who is not touching anything. A screen that has asked for nothing yet is left alone.
   */
  public boolean refresh(ServerPlayerEntity player) {
    if (player == null
        || !(player.currentScreenHandler instanceof TerminalScreenHandler handler)
        || !handler.listing().hasWindow()) {
      return false;
    }
    send(player, handler);
    return true;
  }

  /**
   * The page this screen is currently asking for, stamped as the newest one it has been sent.
   *
   * <p>Separate from sending it so the page a request produces can be read directly, which is what
   * lets the listing be proven against a real network rather than against a captured packet.
   */
  public ListingPage<ItemVariant> pageFor(
      ServerPlayerEntity player, TerminalScreenHandler handler) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(handler, "handler");
    final TerminalListingState state = handler.listing();
    final long revision = state.nextRevision();
    if (!(player.getWorld() instanceof ServerWorld world)) {
      return ListingPage.empty(revision);
    }
    final NetworkMembership network =
        TerminalNetworks.power().network(world, handler.terminal()).orElse(null);
    if (network == null) {
      return ListingPage.empty(revision);
    }
    return NetworkListing.page(
        network.index(), candidates::of, state.filter(), state.window(), revision);
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
