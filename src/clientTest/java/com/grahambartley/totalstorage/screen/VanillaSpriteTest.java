package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.gui.DrawContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class VanillaSpriteTest {
  private static final String SPRITE_ROOT = "/assets/minecraft/textures/gui/sprites/";

  @ParameterizedTest
  @EnumSource(VanillaSprite.class)
  void everySpriteNamesATextureMinecraftShips(VanillaSprite sprite) throws IOException {
    try (InputStream texture =
        VanillaSpriteTest.class.getResourceAsStream(SPRITE_ROOT + sprite.spritePath() + ".png")) {
      assertNotNull(texture, sprite + " names a sprite Minecraft does not ship");
    }
  }

  @ParameterizedTest
  @EnumSource(VanillaSprite.class)
  void everySpriteIsReadFromMinecraftsOwnNamespace(VanillaSprite sprite) {
    assertEquals("minecraft", sprite.id().getNamespace());
    assertEquals(sprite.spritePath(), sprite.id().getPath());
  }

  @ParameterizedTest(name = "active={0}, hovered={1}")
  @CsvSource({
    "true, false, BUTTON",
    "true, true, BUTTON_HIGHLIGHTED",
    "false, false, BUTTON_DISABLED",
    "false, true, BUTTON_DISABLED"
  })
  void aButtonLooksDisabledWhateverTheMouseDoesAndLitWhenHovered(
      boolean active, boolean hovered, VanillaSprite expected) {
    assertEquals(expected, VanillaSprite.button(active, hovered));
  }

  @ParameterizedTest(name = "focused={0}")
  @CsvSource({"true, TEXT_FIELD_HIGHLIGHTED", "false, TEXT_FIELD"})
  void aFieldIsHighlightedWhileItHasFocus(boolean focused, VanillaSprite expected) {
    assertEquals(expected, VanillaSprite.field(focused));
  }

  @Test
  void drawingASpriteStretchesItsTextureOverTheBoxGiven() {
    final DrawContext context = mock(DrawContext.class);

    VanillaSprite.SLOT.draw(context, 3, 4, 18, 18);

    verify(context).drawGuiTexture(VanillaSprite.SLOT.id(), 3, 4, 18, 18);
  }
}
