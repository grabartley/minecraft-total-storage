package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.listing.ListingStatus;
import java.util.Optional;
import net.minecraft.text.Text;

public record ListingPlaceholder(TerminalSprite sprite, Text message) {
  public static final int SPRITE_SIZE = 32;

  public static final int MESSAGE_GAP = 6;

  public static Optional<ListingPlaceholder> forStatus(ListingStatus status) {
    return switch (status) {
      case EMPTY_NETWORK -> Optional.of(of(TerminalSprite.EMPTY_NETWORK, "empty.network"));
      case NO_RESULTS -> Optional.of(of(TerminalSprite.NO_RESULTS, "empty.no_results"));
      case OFFLINE -> Optional.of(of(TerminalSprite.EMPTY_NETWORK, "offline"));
      case PENDING, LIVE -> Optional.empty();
    };
  }

  private static ListingPlaceholder of(TerminalSprite sprite, String messageKey) {
    return new ListingPlaceholder(sprite, TerminalScreenText.of(messageKey));
  }
}
