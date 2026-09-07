package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.listing.ListingStatus;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * What a grid with no rows shows instead of them.
 *
 * <p>An empty network and an empty search get their own silhouette as well as their own words, per
 * the sprite reference: a crate against a magnifier. A player who cannot read the line still learns
 * which of the two they are looking at.
 *
 * <p>A terminal with no network borrows the crate, and is told apart from an empty one by the line
 * alone. Both are a network holding nothing the player can reach, so the same picture is honest;
 * what differs is whether there is a network at all, and that is a sentence rather than a shape.
 *
 * <p>{@link ListingStatus#PENDING} draws nothing. It lasts until the first page lands, and a
 * message that flashes up for a tick every time a terminal opens is noise rather than feedback.
 *
 * @param sprite the 32x32 illustration, centred in the grid
 * @param message the line under it
 */
public record ListingPlaceholder(Identifier sprite, Text message) {
  /** The illustrations are square and drawn at their native size. */
  public static final int SPRITE_SIZE = 32;

  /** The gap between the illustration and the line under it. */
  public static final int MESSAGE_GAP = 6;

  public static Optional<ListingPlaceholder> forStatus(ListingStatus status) {
    return switch (status) {
      case EMPTY_NETWORK -> Optional.of(of("empty/network", "empty.network"));
      case NO_RESULTS -> Optional.of(of("empty/no_results", "empty.no_results"));
      case OFFLINE -> Optional.of(of("empty/network", "offline"));
      case PENDING, LIVE -> Optional.empty();
    };
  }

  private static ListingPlaceholder of(String sprite, String messageKey) {
    return new ListingPlaceholder(
        Identifier.of(TooManyChests.MOD_ID, sprite), TerminalScreenText.of(messageKey));
  }
}
