package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.listing.ListingStatus;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record ListingPlaceholder(Identifier sprite, Text message) {
  public static final int SPRITE_SIZE = 32;

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
