package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class VanillaPaletteTest {
  private static final String CHEST = "/assets/minecraft/textures/gui/container/generic_54.png";

  private static final int HANDLED_SCREEN_TITLE_COLOUR = 4210752;

  private static final int ANVIL_PROBLEM_COLOUR = 16736352;

  private static final int PRESSABLE_WIDGET_INACTIVE_TEXT_COLOUR = 10526880;

  private static final int TEXT_FIELD_EDITABLE_COLOUR = 14737632;

  private static final int TEXT_FIELD_SUGGESTION_COLOUR = -8355712;

  private static final int HANDLED_SCREEN_SLOT_HIGHLIGHT = -2130706433;

  private static final BufferedImage CHEST_TEXTURE = chest();

  private static BufferedImage chest() {
    try (InputStream source = VanillaPaletteTest.class.getResourceAsStream(CHEST)) {
      assertNotNull(source, "Minecraft's own chest texture is not on the test classpath");
      return ImageIO.read(source);
    } catch (IOException failure) {
      throw new AssertionError("could not read " + CHEST, failure);
    }
  }

  private static int at(int x, int y) {
    return CHEST_TEXTURE.getRGB(x, y) & 0xFFFFFF;
  }

  static Stream<Arguments> chromeSampledFromTheChest() {
    return Stream.of(
        Arguments.of("the black edge around the window", VanillaPalette.OUTLINE, 88, 0),
        Arguments.of("the chassis highlight", VanillaPalette.BEVEL_HIGHLIGHT, 88, 1),
        Arguments.of("the chassis face", VanillaPalette.PANEL, 88, 10),
        Arguments.of("the chassis shadow", VanillaPalette.BEVEL_SHADOW, 174, 100),
        Arguments.of("a slot's top edge", VanillaPalette.SUNKEN_SHADOW, 7, 17),
        Arguments.of("a slot's floor", VanillaPalette.SUNKEN_FILL, 10, 20),
        Arguments.of("a slot's bottom right edge", VanillaPalette.BEVEL_HIGHLIGHT, 24, 20));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("chromeSampledFromTheChest")
  void everyChromeColourIsTheOneVanillaPaintsThere(String what, int colour, int x, int y) {
    assertEquals(at(x, y), colour, what + " does not match Minecraft's own chest texture");
  }

  @Test
  void labelTextIsTheColourVanillaWritesAContainerTitleIn() {
    assertEquals(HANDLED_SCREEN_TITLE_COLOUR, VanillaPalette.LABEL);
  }

  @Test
  void warningTextIsTheColourVanillaReportsAProblemIn() {
    assertEquals(ANVIL_PROBLEM_COLOUR, VanillaPalette.WARNING);
  }

  static Stream<Arguments> textColoursVanillaAlsoUses() {
    return Stream.of(
        Arguments.of("failure text", VanillaPalette.FAILURE, Formatting.DARK_RED.getColorValue()),
        Arguments.of("button text", VanillaPalette.BUTTON_TEXT, Formatting.WHITE.getColorValue()),
        Arguments.of(
            "disabled button text",
            VanillaPalette.BUTTON_TEXT_DISABLED,
            PRESSABLE_WIDGET_INACTIVE_TEXT_COLOUR),
        Arguments.of("field text", VanillaPalette.FIELD_TEXT, TEXT_FIELD_EDITABLE_COLOUR),
        Arguments.of(
            "a field hint", VanillaPalette.FIELD_HINT, TEXT_FIELD_SUGGESTION_COLOUR & 0xFFFFFF),
        Arguments.of("a refused field", VanillaPalette.FIELD_REFUSED, ANVIL_PROBLEM_COLOUR),
        Arguments.of("a muted label", VanillaPalette.MUTED, Formatting.DARK_GRAY.getColorValue()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("textColoursVanillaAlsoUses")
  void everyTextColourBorrowedFromVanillaIsTheOneVanillaUses(String what, int colour, int vanilla) {
    assertEquals(vanilla, colour, what + " does not match Minecraft's own colour");
  }

  @Test
  void theHoverWashIsTheTranslucentWhiteVanillaLaysOverAHoveredSlot() {
    assertEquals(HANDLED_SCREEN_SLOT_HIGHLIGHT, VanillaPalette.HOVER);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"0x000000, 0xFF000000", "0xC6C6C6, 0xFFC6C6C6", "0x40C6C6C6, 0xFFC6C6C6"})
  void anOpaqueColourHasFullAlphaAndKeepsItsRgb(String colour, String expected) {
    assertEquals(
        Integer.parseUnsignedInt(expected.substring(2), 16),
        VanillaPalette.opaque(Integer.parseUnsignedInt(colour.substring(2), 16)));
  }
}
