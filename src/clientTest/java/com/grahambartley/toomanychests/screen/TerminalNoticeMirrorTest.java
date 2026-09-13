package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.text.Text;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TerminalNoticeMirrorTest {
  private static final int SYNC_ID = 7;
  private static final int ANOTHER_SCREEN = 8;

  private static final Text NOTICE = Text.literal("Sorted 3 containers");

  @AfterEach
  void forgetTheNotice() {
    TerminalNoticeMirror.clear();
  }

  @Test
  void aTerminalWithNoNoticeShowsNothing() {
    assertFalse(TerminalNoticeMirror.isShowingOn(SYNC_ID));
    assertNull(TerminalNoticeMirror.message());
  }

  @Test
  void aNoticeIsShownOnTheScreenItWasSentFor() {
    TerminalNoticeMirror.accept(SYNC_ID, NOTICE, false);

    assertTrue(TerminalNoticeMirror.isShowingOn(SYNC_ID));
    assertEquals(NOTICE, TerminalNoticeMirror.message());
  }

  @Test
  void aNoticeForAnotherScreenIsNotShownOnThisOne() {
    TerminalNoticeMirror.accept(ANOTHER_SCREEN, NOTICE, false);

    assertFalse(TerminalNoticeMirror.isShowingOn(SYNC_ID));
  }

  @Test
  void aQuestionStaysUpForLongerThanAnAnswerlessNotice() {
    TerminalNoticeMirror.accept(SYNC_ID, NOTICE, true);
    int asking = TerminalNoticeMirror.ticksLeft();
    TerminalNoticeMirror.accept(SYNC_ID, NOTICE, false);

    assertTrue(asking > TerminalNoticeMirror.ticksLeft());
    assertEquals(TerminalNoticeMirror.SHOWN_TICKS, TerminalNoticeMirror.ticksLeft());
  }

  @Test
  void aNoticeCountsDownAndThenGoes() {
    TerminalNoticeMirror.accept(SYNC_ID, NOTICE, false);

    for (int tick = 0; tick < TerminalNoticeMirror.SHOWN_TICKS; tick++) {
      TerminalNoticeMirror.tick();
    }

    assertFalse(TerminalNoticeMirror.isShowingOn(SYNC_ID));
    assertNull(TerminalNoticeMirror.message());
  }

  @Test
  void aFreshNoticeReplacesTheOneBeforeIt() {
    TerminalNoticeMirror.accept(SYNC_ID, NOTICE, true);
    TerminalNoticeMirror.tick();
    Text later = Text.literal("Gathered 40 items");

    TerminalNoticeMirror.accept(SYNC_ID, later, false);

    assertEquals(later, TerminalNoticeMirror.message());
    assertFalse(TerminalNoticeMirror.needsAnAnswer());
  }

  @Test
  void tickingWithNothingToShowIsHarmless() {
    TerminalNoticeMirror.tick();

    assertFalse(TerminalNoticeMirror.isShowingOn(SYNC_ID));
  }

  @Test
  void aNoticeWithNoMessageIsIgnored() {
    TerminalNoticeMirror.accept(SYNC_ID, null, true);

    assertFalse(TerminalNoticeMirror.isShowingOn(SYNC_ID));
  }
}
