package com.grahambartley.toomanychests.operation;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

/**
 * One kind of item a deposit could not store, and how much of it stayed behind.
 *
 * <p>A total on its own does not answer the question a player asks when a deposit comes up short,
 * which is always "what of it". Sixty items left over reads as a failure; sixty cobblestone left
 * over reads as a filtered chest that filled up, which is something they can act on.
 *
 * @param variant the item that stayed in the player's inventory
 * @param amount how much of it did, which is always at least one
 */
public record DepositLeftover(ItemVariant variant, long amount) {
  public DepositLeftover {
    Objects.requireNonNull(variant, "variant");
    if (variant.isBlank()) {
      throw new IllegalArgumentException("nothing is not a leftover");
    }
    if (amount <= 0L) {
      throw new IllegalArgumentException("a leftover of " + amount + " is not left over");
    }
  }
}
