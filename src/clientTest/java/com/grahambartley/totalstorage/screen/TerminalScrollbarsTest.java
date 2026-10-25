package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.listing.ListingPage;
import com.grahambartley.totalstorage.listing.ListingStatus;
import com.grahambartley.totalstorage.screenhandler.FilterColumnLayout;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.ContainerRoleEntry;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

class TerminalScrollbarsTest {
  private final TerminalPrompts prompts = new TerminalPrompts(3);
  private final TerminalSettingsPanel settings = new TerminalSettingsPanel();
  private final TerminalFilterColumn filters = new TerminalFilterColumn();
  private final TerminalItemsPanel items = new TerminalItemsPanel();
  private final TerminalScrollbars scrollbars =
      new TerminalScrollbars(prompts, settings, filters, items);

  private static NetworkSettingsView viewOf(int total) {
    return NetworkSettingsView.answered(
        16,
        16,
        32,
        TerminalAccessMode.OWNER,
        true,
        true,
        List.of(),
        NetworkCapacity.EMPTY,
        0,
        total,
        IntStream.range(0, Math.min(total, TerminalSettingsLayout.ROWS))
            .mapToObj(
                index ->
                    new ContainerRoleEntry(
                        new BlockPos(index, 64, 0),
                        "",
                        ContainerRole.GENERAL,
                        List.of(),
                        Optional.empty(),
                        0,
                        0L,
                        0,
                        27,
                        index))
            .toList());
  }

  @Test
  void withNothingHeldADragAndAReleaseAreLeftToTheScreen() {
    assertFalse(scrollbars.dragTo(50));
    assertFalse(scrollbars.release());
  }

  @Test
  void aHeldSettingsScrollbarFollowsTheDragAndLetsGoOnRelease() {
    settings.show(SettingsSection.CONTAINERS);
    settings.fit(viewOf(20), 0);
    settings.grabScrollbar(TerminalSettingsLayout.LIST_Y);

    assertTrue(
        scrollbars.dragTo(TerminalSettingsLayout.LIST_Y + TerminalSettingsLayout.LIST_HEIGHT + 40));
    assertEquals(20 - TerminalSettingsLayout.ROWS, settings.scroll().firstIndex());
    assertTrue(scrollbars.release());
    assertFalse(settings.isDraggingScrollbar());
  }

  @Test
  void aHeldGridScrollerFollowsTheDragAndLetsGoOnRelease() {
    items.beginFrame(new ListingPage<>(1L, ListingStatus.LIVE, 0, 500, List.of()));
    items.grabScrollbar(TerminalScreenLayout.SCROLLBAR_Y + 1);

    assertTrue(
        scrollbars.dragTo(
            TerminalScreenLayout.SCROLLBAR_Y + TerminalScreenLayout.SCROLLBAR_HEIGHT));
    assertEquals(items.scroll().maxTopRow(), items.scroll().topRow());
    assertTrue(scrollbars.release());
    assertFalse(items.scrollbar().isDragging());
  }

  @Test
  void aHeldFilterScrollbarFollowsTheDragAndLetsGoOnRelease() {
    final List<NetworkTagEntry> tags =
        IntStream.range(0, FilterColumnLayout.ROWS + 6)
            .mapToObj(tag -> new NetworkTagEntry("tag" + tag, "Tag " + tag, true, 0, 1))
            .toList();
    filters.fit(new NetworkTagView(true, tags, Optional.empty(), Set.of()));
    filters.click(
        new FilterSelection(),
        FilterColumnLayout.SCROLLBAR_X,
        FilterColumnLayout.CONTENT_Y,
        GridClick.LEFT_BUTTON);

    assertTrue(
        scrollbars.dragTo(FilterColumnLayout.CONTENT_Y + FilterColumnLayout.CONTENT_HEIGHT + 20));
    assertEquals(filters.scroll().maxTopRow(), filters.scroll().topRow());
    assertTrue(scrollbars.release());
    assertFalse(filters.isDraggingScrollbar());
  }
}
