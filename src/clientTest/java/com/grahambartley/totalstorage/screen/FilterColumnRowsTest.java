package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FilterColumnRowsTest {
  private static final NetworkTagEntry ORES = preset("ores");
  private static final NetworkTagEntry WOOD = preset("wood");
  private static final NetworkTagEntry LOOT = custom("loot");
  private static final NetworkTagEntry FARM = custom("farm");

  private static NetworkTagEntry preset(String id) {
    return new NetworkTagEntry(id, id, true, 0, 0);
  }

  private static NetworkTagEntry custom(String id) {
    return new NetworkTagEntry(id, id, false, 0, 0);
  }

  private static FilterColumnRows rowsOf(NetworkTagEntry... tags) {
    return FilterColumnRows.of(new NetworkTagView(true, List.of(tags), Optional.empty(), Set.of()));
  }

  @Test
  void presetsComeFirstThenADividerThenCustomTagsInTheirOrder() {
    final FilterColumnRows rows = rowsOf(LOOT, ORES, FARM, WOOD);

    assertEquals(5, rows.size());
    assertEquals(ORES, rows.tagAt(0));
    assertEquals(WOOD, rows.tagAt(1));
    assertTrue(rows.at(2).isDivider());
    assertEquals(LOOT, rows.tagAt(3));
    assertEquals(FARM, rows.tagAt(4));
  }

  @Test
  void aNetworkWithOnlyPresetsHasNoDivider() {
    final FilterColumnRows rows = rowsOf(ORES, WOOD);

    assertEquals(2, rows.size());
    assertFalse(rows.at(0).isDivider());
    assertFalse(rows.at(1).isDivider());
  }

  @Test
  void aNetworkWithOnlyCustomTagsStartsWithTheDivider() {
    final FilterColumnRows rows = rowsOf(LOOT);

    assertEquals(2, rows.size());
    assertTrue(rows.at(0).isDivider());
    assertEquals(LOOT, rows.tagAt(1));
  }

  @Test
  void aNetworkWithNoTagsHasNoRows() {
    assertEquals(0, rowsOf().size());
  }

  @Test
  void theDividerCarriesNoTag() {
    assertNull(rowsOf(ORES, LOOT).tagAt(1));
  }

  @ParameterizedTest(name = "index {0}")
  @ValueSource(ints = {-1, 3, 10})
  void anIndexOutsideTheRowsFindsNothing(int index) {
    final FilterColumnRows rows = rowsOf(ORES, LOOT);

    assertNull(rows.at(index));
    assertNull(rows.tagAt(index));
  }

  @Test
  void laterChangesToTheGivenListDoNotReachTheRows() {
    final List<FilterColumnRows.Row> given =
        new ArrayList<>(List.of(new FilterColumnRows.Row(ORES)));
    final FilterColumnRows rows = new FilterColumnRows(given);

    given.add(new FilterColumnRows.Row(WOOD));

    assertEquals(1, rows.size());
  }
}
