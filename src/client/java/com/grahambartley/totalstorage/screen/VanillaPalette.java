package com.grahambartley.totalstorage.screen;

public final class VanillaPalette {
  public static final int OUTLINE = 0x000000;

  public static final int SUNKEN_SHADOW = 0x373737;

  public static final int BEVEL_SHADOW = 0x555555;

  public static final int SUNKEN_FILL = 0x8B8B8B;

  public static final int PANEL = 0xC6C6C6;

  public static final int BEVEL_HIGHLIGHT = 0xFFFFFF;

  public static final int LABEL = 0x404040;

  public static final int MUTED = BEVEL_SHADOW;

  public static final int WARNING = 0xFF6060;

  public static final int FAILURE = 0xAA0000;

  public static final int ACCENT = 0xC43A2E;

  public static final int DISABLED = SUNKEN_FILL;

  public static final int BUTTON_TEXT = BEVEL_HIGHLIGHT;

  public static final int BUTTON_TEXT_DISABLED = 0xA0A0A0;

  public static final int FIELD_TEXT = 0xE0E0E0;

  public static final int FIELD_HINT = 0x808080;

  public static final int FIELD_REFUSED = WARNING;

  public static final int HOVER = 0x80FFFFFF;

  public static final int COUNT = BEVEL_HIGHLIGHT;

  private VanillaPalette() {}

  public static int opaque(int colour) {
    return 0xFF000000 | colour;
  }
}
