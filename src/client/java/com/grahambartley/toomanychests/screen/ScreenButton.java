package com.grahambartley.toomanychests.screen;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import net.minecraft.text.Text;

public enum ScreenButton {
  QUICK_STACK(
      IconButtonSprites.QUICK_STACK,
      "quick_stack",
      QuickStackRequests::isOffered,
      QuickStackRequests::isAvailable,
      syncId -> QuickStackRequests.send()),
  SORT(
      IconButtonSprites.SORT,
      "sort",
      SortRequests::isOffered,
      SortRequests::isAvailable,
      SortRequests::send);

  private final IconButtonSprites sprites;
  private final String translationSuffix;
  private final BooleanSupplier offered;
  private final BooleanSupplier available;
  private final IntConsumer press;

  ScreenButton(
      IconButtonSprites sprites,
      String translationSuffix,
      BooleanSupplier offered,
      BooleanSupplier available,
      IntConsumer press) {
    this.sprites = sprites;
    this.translationSuffix = translationSuffix;
    this.offered = offered;
    this.available = available;
    this.press = press;
  }

  public IconButtonSprites sprites() {
    return sprites;
  }

  public Text label() {
    return ScreenButtonText.of(translationSuffix);
  }

  public boolean isOffered() {
    return offered.getAsBoolean();
  }

  public boolean isAvailable() {
    return available.getAsBoolean();
  }

  public void press(int syncId) {
    press.accept(syncId);
  }
}
