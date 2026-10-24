package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.listing.ListingRow;
import java.util.Arrays;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ListingTooltipTest {
  private static List<Object> argumentsOf(Text line) {
    return Arrays.asList(((TranslatableTextContent) line.getContent()).getArgs());
  }

  private static String keyOf(Text line) {
    return ((TranslatableTextContent) line.getContent()).getKey();
  }

  @Test
  void theTooltipCarriesTheRealFigureRatherThanTheCompactedOne() {
    List<Text> lines = ListingTooltip.extraLines(new ListingRow<>("cobblestone", 12_399L, 1));

    assertEquals(1, lines.size());
    assertEquals("screen.total-storage.terminal.total", keyOf(lines.get(0)));
    assertTrue(
        argumentsOf(lines.get(0)).contains("12,399"),
        "The exact total should be the argument, got " + argumentsOf(lines.get(0)));
  }

  @DisplayName("how many containers hold it is only said when it is worth saying")
  @ParameterizedTest(name = "{0} containers produces {1} lines")
  @CsvSource({"0, 1", "1, 1", "2, 2", "12, 2"})
  void namesTheContainersOnlyWhenThereIsMoreThanOne(int providerCount, int expectedLines) {
    List<Text> lines =
        ListingTooltip.extraLines(new ListingRow<>("cobblestone", 300L, providerCount));

    assertEquals(expectedLines, lines.size());
  }

  @Test
  void aSpreadStackNamesHowManyContainersHoldIt() {
    List<Text> lines = ListingTooltip.extraLines(new ListingRow<>("cobblestone", 300L, 12));

    assertEquals("screen.total-storage.terminal.containers", keyOf(lines.get(1)));
    assertTrue(
        argumentsOf(lines.get(1)).contains(12),
        "The container count should be the argument, got " + argumentsOf(lines.get(1)));
  }

  @Test
  void aRowThatIsNotThereIsRefusedRatherThanDrawnAsBlankLines() {
    assertThrows(NullPointerException.class, () -> ListingTooltip.extraLines(null));
  }
}
