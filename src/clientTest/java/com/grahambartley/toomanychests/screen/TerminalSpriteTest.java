package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalSpriteTest {
  private static final String CHEST = "/assets/minecraft/textures/gui/container/generic_54.png";

  private static final int CHEST_WIDTH = 176;
  private static final int CHEST_HEIGHT = 222;
  private static final int CHEST_SLOT_X = 7;
  private static final int CHEST_SLOT_Y = 17;
  private static final int SLOT_SIZE = 18;

  private static final int TRANSPARENT = 0;

  private static final Set<Integer> VANILLA_COLOURS =
      Set.of(
          VanillaPalette.OUTLINE,
          VanillaPalette.SUNKEN_SHADOW,
          VanillaPalette.BEVEL_SHADOW,
          VanillaPalette.SUNKEN_FILL,
          VanillaPalette.PANEL,
          VanillaPalette.BEVEL_HIGHLIGHT,
          VanillaPalette.LABEL,
          VanillaPalette.ACCENT,
          VanillaPalette.ACCENT_LIGHT);

  private static BufferedImage read(String path) {
    try (InputStream source = TerminalSpriteTest.class.getResourceAsStream(path)) {
      assertNotNull(source, path + " is not on the classpath");
      return ImageIO.read(source);
    } catch (IOException failure) {
      throw new AssertionError("could not read " + path, failure);
    }
  }

  private static BufferedImage textureOf(TerminalSprite sprite) {
    return read("/" + sprite.texturePath());
  }

  private static JsonObject scalingOf(TerminalSprite sprite) {
    final String path = "/" + sprite.texturePath() + ".mcmeta";
    try (InputStream source = TerminalSpriteTest.class.getResourceAsStream(path)) {
      if (source == null) {
        return null;
      }
      return JsonParser.parseReader(new InputStreamReader(source, StandardCharsets.UTF_8))
          .getAsJsonObject()
          .getAsJsonObject("gui")
          .getAsJsonObject("scaling");
    } catch (IOException failure) {
      throw new AssertionError("could not read " + path, failure);
    }
  }

  private static BufferedImage nineSliced(BufferedImage source, int width, int height, int border) {
    final BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        out.setRGB(
            x,
            y,
            source.getRGB(
                sourceIndex(x, width, source.getWidth(), border),
                sourceIndex(y, height, source.getHeight(), border)));
      }
    }
    return out;
  }

  private static int sourceIndex(int drawn, int drawnSize, int sourceSize, int border) {
    if (drawn < border) {
      return drawn;
    }
    if (drawn >= drawnSize - border) {
      return sourceSize - (drawnSize - drawn);
    }
    return border + (drawn - border) % (sourceSize - 2 * border);
  }

  private static void assertSamePixels(
      BufferedImage drawn, BufferedImage vanilla, int vanillaX, int vanillaY, String what) {
    for (int y = 0; y < drawn.getHeight(); y++) {
      for (int x = 0; x < drawn.getWidth(); x++) {
        assertEquals(
            vanilla.getRGB(vanillaX + x, vanillaY + y),
            drawn.getRGB(x, y),
            what + " differs from Minecraft's own texture at " + x + "," + y);
      }
    }
  }

  @ParameterizedTest
  @EnumSource(TerminalSprite.class)
  void everySpriteInTheCatalogueShipsAsATexture(TerminalSprite sprite) {
    final BufferedImage texture = textureOf(sprite);

    assertTrue(texture.getWidth() > 0 && texture.getHeight() > 0, sprite + " is an empty texture");
  }

  @ParameterizedTest
  @EnumSource(TerminalSprite.class)
  void everyNineSlicedSpriteDeclaresTheSizeItActuallyIs(TerminalSprite sprite) {
    final JsonObject scaling = scalingOf(sprite);
    if (scaling == null) {
      return;
    }
    final BufferedImage texture = textureOf(sprite);
    final int border = scaling.get("border").getAsInt();

    assertEquals(texture.getWidth(), scaling.get("width").getAsInt(), sprite + " declares a width");
    assertEquals(
        texture.getHeight(), scaling.get("height").getAsInt(), sprite + " declares a height");
    assertTrue(
        2 * border < Math.min(texture.getWidth(), texture.getHeight()),
        sprite + " has a border with no centre left inside it");
  }

  @ParameterizedTest
  @EnumSource(TerminalSprite.class)
  void noSpritePaintsAColourVanillaWouldNot(TerminalSprite sprite) {
    final BufferedImage texture = textureOf(sprite);

    for (int y = 0; y < texture.getHeight(); y++) {
      for (int x = 0; x < texture.getWidth(); x++) {
        final int pixel = texture.getRGB(x, y);
        if ((pixel >>> 24) == TRANSPARENT) {
          continue;
        }
        assertTrue(
            VANILLA_COLOURS.contains(pixel & 0xFFFFFF),
            sprite
                + " paints "
                + String.format("#%06X", pixel & 0xFFFFFF)
                + " at "
                + x
                + ","
                + y
                + ", which is not a colour Minecraft uses");
      }
    }
  }

  @ParameterizedTest
  @EnumSource(TerminalSprite.class)
  void everySpriteKeepsItsMarkingsOutOfTheBandsThatTile(TerminalSprite sprite) {
    final JsonObject scaling = scalingOf(sprite);
    if (scaling == null) {
      return;
    }
    final BufferedImage texture = textureOf(sprite);
    final int border = scaling.get("border").getAsInt();
    final BufferedImage wide =
        nineSliced(texture, 4 * texture.getWidth(), 4 * texture.getHeight(), border);

    for (int y = border; y < wide.getHeight() - border; y++) {
      for (int x = border; x < wide.getWidth() - border; x++) {
        assertEquals(
            wide.getRGB(border, y),
            wide.getRGB(x, y),
            sprite
                + " repeats a marking across its middle when stretched, at "
                + x
                + ","
                + y
                + "; anything that must stay put belongs in the border");
      }
    }
  }

  @Test
  void aSlotIsVanillasSlotPixelForPixel() {
    final int border = scalingOf(TerminalSprite.SUNKEN).get("border").getAsInt();
    final BufferedImage drawn =
        nineSliced(textureOf(TerminalSprite.SUNKEN), SLOT_SIZE, SLOT_SIZE, border);

    assertSamePixels(drawn, read(CHEST), CHEST_SLOT_X, CHEST_SLOT_Y, "a slot");
  }

  @Test
  void theWindowChassisIsVanillasChassisPixelForPixel() {
    final int border = scalingOf(TerminalSprite.PANEL).get("border").getAsInt();
    final BufferedImage drawn =
        nineSliced(textureOf(TerminalSprite.PANEL), CHEST_WIDTH, CHEST_HEIGHT, border);
    final BufferedImage chest = read(CHEST);

    for (int y = 0; y < CHEST_HEIGHT; y++) {
      for (int x = 0; x < CHEST_WIDTH; x++) {
        if (x >= border && x < CHEST_WIDTH - border && y >= border && y < CHEST_HEIGHT - border) {
          continue;
        }
        assertEquals(
            chest.getRGB(x, y),
            drawn.getRGB(x, y),
            "the window chassis differs from Minecraft's own chest at " + x + "," + y);
      }
    }
  }
}
