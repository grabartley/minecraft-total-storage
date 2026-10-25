package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.operation.TerminalNoticeTone;
import net.minecraft.text.Text;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalNoticeMirrorTest {
  private static final int SYNC_ID = 7;
  private static final int ANOTHER_SCREEN = 8;

  private static final Text HEADLINE = Text.literal("Skipped 2 containers");
  private static final Text MESSAGE = Text.literal("Sorted 9 containers, skipped 2");

  @AfterEach
  void forgetTheNotice() {
    TerminalNoticeMirror.clear();
  }

  @Test
  void aTerminalWithNoNoticeShowsNothing() {
    assertFalse(TerminalNoticeMirror.isShowingOn(SYNC_ID));
    assertNull(TerminalNoticeMirror.message());
  }

  @ParameterizedTest
  @EnumSource(value = TerminalNoticeTone.class, names = "DONE", mode = EnumSource.Mode.EXCLUDE)
  void aResultThatFellShortIsShownWithItsToneAndHeadline(TerminalNoticeTone tone) {
    TerminalNoticeMirror.accept(SYNC_ID, tone, HEADLINE, MESSAGE);

    assertTrue(TerminalNoticeMirror.isShowingOn(SYNC_ID));
    assertEquals(tone, TerminalNoticeMirror.tone());
    assertEquals(HEADLINE, TerminalNoticeMirror.headline());
    assertEquals(MESSAGE, TerminalNoticeMirror.message());
  }

  @Test
  void aSuccessShowsNothing() {
    TerminalNoticeMirror.accept(SYNC_ID, TerminalNoticeTone.DONE, HEADLINE, MESSAGE);

    assertFalse(TerminalNoticeMirror.isShowingOn(SYNC_ID));
  }

  @Test
  void aNoticeWithoutAHeadlineLeadsWithItsMessage() {
    TerminalNoticeMirror.accept(SYNC_ID, TerminalNoticeTone.FAILED, null, MESSAGE);

    assertEquals(MESSAGE, TerminalNoticeMirror.headline());
  }

  @Test
  void aNoticeForAnotherScreenIsNotShownOnThisOne() {
    TerminalNoticeMirror.accept(ANOTHER_SCREEN, TerminalNoticeTone.PARTIAL, HEADLINE, MESSAGE);

    assertFalse(TerminalNoticeMirror.isShowingOn(SYNC_ID));
  }

  @Test
  void aNoticeCountsDownAndThenGoes() {
    TerminalNoticeMirror.accept(SYNC_ID, TerminalNoticeTone.PARTIAL, HEADLINE, MESSAGE);

    for (int tick = 0; tick < TerminalNoticeMirror.SHOWN_TICKS; tick++) {
      TerminalNoticeMirror.tick();
    }

    assertFalse(TerminalNoticeMirror.isShowingOn(SYNC_ID));
    assertNull(TerminalNoticeMirror.message());
    assertEquals(TerminalNoticeTone.DONE, TerminalNoticeMirror.tone());
  }

  @Test
  void aFreshNoticeReplacesTheOneBeforeItAndRestartsTheClock() {
    TerminalNoticeMirror.accept(SYNC_ID, TerminalNoticeTone.PARTIAL, HEADLINE, MESSAGE);
    TerminalNoticeMirror.tick();
    final Text later = Text.literal("No room for that");

    TerminalNoticeMirror.accept(SYNC_ID, TerminalNoticeTone.FAILED, later, later);

    assertEquals(later, TerminalNoticeMirror.message());
    assertEquals(TerminalNoticeTone.FAILED, TerminalNoticeMirror.tone());
    assertEquals(TerminalNoticeMirror.SHOWN_TICKS, TerminalNoticeMirror.ticksLeft());
  }

  @Test
  void tickingWithNothingToShowIsHarmless() {
    TerminalNoticeMirror.tick();

    assertFalse(TerminalNoticeMirror.isShowingOn(SYNC_ID));
  }

  @Test
  void aNoticeWithNoMessageOrToneIsIgnored() {
    TerminalNoticeMirror.accept(SYNC_ID, TerminalNoticeTone.FAILED, HEADLINE, null);
    TerminalNoticeMirror.accept(SYNC_ID, null, HEADLINE, MESSAGE);

    assertFalse(TerminalNoticeMirror.isShowingOn(SYNC_ID));
  }
}
