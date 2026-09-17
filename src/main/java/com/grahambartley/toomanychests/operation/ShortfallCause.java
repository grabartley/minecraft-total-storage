package com.grahambartley.toomanychests.operation;

public enum ShortfallCause {
  NO_STOCK(WithdrawResult.NOTHING_AVAILABLE, WithdrawResult.PARTIAL_NO_STOCK),
  NO_ROOM(WithdrawResult.NO_ROOM, WithdrawResult.PARTIAL_NO_ROOM);

  private final WithdrawResult emptyHanded;
  private final WithdrawResult partial;

  ShortfallCause(WithdrawResult emptyHanded, WithdrawResult partial) {
    this.emptyHanded = emptyHanded;
    this.partial = partial;
  }

  public WithdrawResult emptyHanded() {
    return emptyHanded;
  }

  public WithdrawResult partial() {
    return partial;
  }
}
