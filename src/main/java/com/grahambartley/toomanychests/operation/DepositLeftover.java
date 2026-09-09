package com.grahambartley.toomanychests.operation;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

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
