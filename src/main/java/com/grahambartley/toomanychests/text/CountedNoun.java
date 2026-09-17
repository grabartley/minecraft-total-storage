package com.grahambartley.toomanychests.text;

import com.grahambartley.toomanychests.TooManyChests;
import java.util.Objects;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public enum CountedNoun {
  ITEMS("items", "1 item", "%s items"),
  KINDS("kinds", "1 kind", "%s kinds"),
  STACKS("stacks", "1 stack", "%s stacks"),
  CONTAINERS("containers", "1 container", "%s containers"),
  EXCLUDED_CONTAINERS("excluded-containers", "1 excluded container", "%s excluded containers"),
  NEARBY_CONTAINERS("nearby-containers", "1 nearby container", "%s nearby containers"),
  TAGS("tags", "1 tag", "%s tags"),
  CUSTOM_TAGS("custom-tags", "1 custom tag", "%s custom tags"),
  BLOCKS("blocks", "1 block", "%s blocks"),
  CHARACTERS("characters", "1 character", "%s characters");

  private static final String PREFIX = "count." + TooManyChests.MOD_ID + ".";

  private static final String SINGULAR_SUFFIX = ".one";

  private final String id;
  private final String singular;
  private final String plural;

  CountedNoun(String id, String singular, String plural) {
    this.id = id;
    this.singular = singular;
    this.plural = plural;
  }

  public String id() {
    return id;
  }

  public MutableText of(long count) {
    return count == 1L ? one() : Text.translatableWithFallback(PREFIX + id, plural, count);
  }

  public MutableText ofFormatted(String amount, long count) {
    Objects.requireNonNull(amount, "amount");
    return count == 1L ? one() : Text.translatableWithFallback(PREFIX + id, plural, amount);
  }

  private MutableText one() {
    return Text.translatableWithFallback(PREFIX + id + SINGULAR_SUFFIX, singular);
  }
}
