package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.tag.TagNameOutcome;
import com.grahambartley.totalstorage.tag.TagNameRule;
import net.minecraft.text.Text;

public final class TagNameRefusalText {
  private TagNameRefusalText() {}

  public static Text of(TagNameOutcome outcome) {
    return switch (outcome) {
      case BLANK, OK -> TerminalScreenText.of("tags.name.refused.blank");
      case TOO_LONG -> TerminalScreenText.of("tags.name.refused.too_long", TagNameRule.MAX_LENGTH);
      case ILLEGAL_CHARACTER -> TerminalScreenText.of("tags.name.refused.illegal");
      case DUPLICATE -> TerminalScreenText.of("tags.name.refused.duplicate");
      case PRESET_COLLISION -> TerminalScreenText.of("tags.name.refused.preset");
    };
  }
}
