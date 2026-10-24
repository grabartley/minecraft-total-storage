package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class TerminalSpriteTest {
  private static final String CHEST = "/assets/minecraft/textures/gui/container/generic_54.png";

  private static final String SPRITE_ROOT = "/assets/total-storage/textures/gui/sprites";

  private static final String NINE_SLICED = "chrome";

  private static final int CHEST_WIDTH = 176;
  private static final int CHEST_HEIGHT = 222;
  private static final int CHEST_SLOT_X = 7;
  private static final int CHEST_SLOT_Y = 17;
  private static final int SLOT_SIZE = 18;

  private static final int STRETCH_FACTOR = 4;

  private static final int TRANSPARENT = 0;

  private static final Set<Integer> VANILLA_COLOURS =
      Set.of(
          VanillaPalette.OUTLINE,
          VanillaPalette.SUNKEN_SHADOW,
          VanillaPalette.BEVEL_SHADOW,
          VanillaPalette.SUNKEN_FILL,
          VanillaPalette.PANEL,
          VanillaPalette.BEVEL_HIGHLIGHT,
          VanillaPalette.LABEL);

  private static BufferedImage read(String path) {
    try (InputStream source = TerminalSpriteTest.class.getResourceAsStream(path)) {
      assertNotNull(source, path + " is not on the classpath");
      return ImageIO.read(source);
    } catch (IOException failure) {
      throw new UncheckedIOException("could not read " + path, failure);
    }
  }

  private static BufferedImage readFile(Path file) {
    try (InputStream source = Files.newInputStream(file)) {
      return ImageIO.read(source);
    } catch (IOException failure) {
      throw new UncheckedIOException("could not read " + file, failure);
    }
  }

  private static String relativeName(Path sprite) {
    return spriteRoot().relativize(sprite).toString().replace(java.io.File.separatorChar, '/');
  }

  private static BufferedImage textureOf(TerminalSprite sprite) {
    return read(SPRITE_ROOT + "/" + sprite.spritePath() + ".png");
  }

  private static Path spriteRoot() {
    try {
      final var located = TerminalSpriteTest.class.getResource(SPRITE_ROOT);
      assertNotNull(located, SPRITE_ROOT + " is not on the classpath");
      return Path.of(located.toURI());
    } catch (URISyntaxException failure) {
      throw new IllegalStateException("could not resolve " + SPRITE_ROOT, failure);
    }
  }

  static Stream<Path> everySpriteTheModShips() {
    try (Stream<Path> tree = Files.walk(spriteRoot())) {
      final List<Path> pngs =
          tree.filter(path -> path.getFileName().toString().endsWith(".png")).sorted().toList();
      assertTrue(
          pngs.size() >= TerminalSprite.values().length,
          "the sprite tree holds fewer textures than the catalogue names");
      return pngs.stream();
    } catch (IOException failure) {
      throw new UncheckedIOException("could not walk " + SPRITE_ROOT, failure);
    }
  }

  private static JsonObject scalingOf(String texturePath) {
    final Path meta = spriteRoot().resolve(texturePath + ".mcmeta");
    if (!Files.exists(meta)) {
      return null;
    }
    try (InputStream source = Files.newInputStream(meta)) {
      return JsonParser.parseReader(new InputStreamReader(source, StandardCharsets.UTF_8))
          .getAsJsonObject()
          .getAsJsonObject("gui")
          .getAsJsonObject("scaling");
    } catch (IOException failure) {
      throw new UncheckedIOException("could not read " + meta, failure);
    }
  }

  private static JsonObject scalingOf(TerminalSprite sprite) {
    return scalingOf(sprite.spritePath() + ".png");
  }

  static Stream<Path> everyNineSlicedSprite() {
    final List<Path> sliced =
        everySpriteTheModShips().filter(sprite -> scalingOf(relativeName(sprite)) != null).toList();
    assertFalse(sliced.isEmpty(), "nothing the mod ships declares nine-slice scaling");
    return sliced.stream();
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

  @ParameterizedTest
  @EnumSource(TerminalSprite.class)
  void everySpriteInTheCatalogueShipsAsATexture(TerminalSprite sprite) {
    final BufferedImage texture = textureOf(sprite);

    assertTrue(texture.getWidth() > 0 && texture.getHeight() > 0, sprite + " is an empty texture");
  }

  @ParameterizedTest
  @MethodSource("everySpriteTheModShips")
  void everySpriteTheModShipsStaysInsideVanillasPalette(Path sprite) {
    final BufferedImage texture = readFile(sprite);

    for (int y = 0; y < texture.getHeight(); y++) {
      for (int x = 0; x < texture.getWidth(); x++) {
        final int pixel = texture.getRGB(x, y);
        if ((pixel >>> 24) == TRANSPARENT) {
          continue;
        }
        assertTrue(
            VANILLA_COLOURS.contains(pixel & 0xFFFFFF),
            relativeName(sprite)
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
  @MethodSource("everySpriteTheModShips")
  void aSpriteIsNineSlicedWhenAndOnlyWhenItsSizeIsNotKnownInAdvance(Path sprite) {
    final String relative = relativeName(sprite);
    final boolean declaresScaling = scalingOf(relative) != null;

    assertEquals(
        relative.startsWith(NINE_SLICED),
        declaresScaling,
        relative
            + (declaresScaling
                ? " declares scaling but is drawn at its own size"
                : " is drawn at a size nobody knows in advance and declares no scaling"));
  }

  @ParameterizedTest
  @MethodSource("everyNineSlicedSprite")
  void everyNineSlicedSpriteDeclaresTheSizeItActuallyIs(Path sprite) {
    final String name = relativeName(sprite);
    final JsonObject scaling = scalingOf(name);
    final BufferedImage texture = readFile(sprite);
    final int border = scaling.get("border").getAsInt();

    assertEquals(texture.getWidth(), scaling.get("width").getAsInt(), name + " declares a width");
    assertEquals(
        texture.getHeight(), scaling.get("height").getAsInt(), name + " declares a height");
    assertTrue(
        2 * border < Math.min(texture.getWidth(), texture.getHeight()),
        name + " has a border with no centre left inside it");
  }

  @ParameterizedTest
  @MethodSource("everyNineSlicedSprite")
  void everySpriteKeepsItsMarkingsOutOfTheBandsThatTile(Path sprite) {
    final String name = relativeName(sprite);
    final BufferedImage texture = readFile(sprite);
    final int border = scalingOf(name).get("border").getAsInt();
    final BufferedImage wide =
        nineSliced(
            texture,
            STRETCH_FACTOR * texture.getWidth(),
            STRETCH_FACTOR * texture.getHeight(),
            border);

    for (int y = 0; y < wide.getHeight(); y++) {
      for (int x = 0; x < wide.getWidth(); x++) {
        final boolean fixedColumn = x < border || x >= wide.getWidth() - border;
        final boolean fixedRow = y < border || y >= wide.getHeight() - border;
        if (fixedColumn && fixedRow) {
          continue;
        }
        final int settled = fixedColumn ? wide.getRGB(x, border) : wide.getRGB(border, y);
        assertEquals(
            settled,
            wide.getRGB(x, y),
            name
                + " repeats a marking inside a band that tiles, at "
                + x
                + ","
                + y
                + "; anything that must stay put belongs in a corner");
      }
    }
  }

  @Test
  void aSlotIsVanillasSlotPixelForPixel() {
    final int border = scalingOf(TerminalSprite.SUNKEN).get("border").getAsInt();
    final BufferedImage drawn =
        nineSliced(textureOf(TerminalSprite.SUNKEN), SLOT_SIZE, SLOT_SIZE, border);
    final BufferedImage chest = read(CHEST);

    for (int y = 0; y < SLOT_SIZE; y++) {
      for (int x = 0; x < SLOT_SIZE; x++) {
        assertEquals(
            chest.getRGB(CHEST_SLOT_X + x, CHEST_SLOT_Y + y),
            drawn.getRGB(x, y),
            "a slot differs from Minecraft's own at " + x + "," + y);
      }
    }
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
