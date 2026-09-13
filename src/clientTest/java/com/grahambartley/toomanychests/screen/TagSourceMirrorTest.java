package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.network.TagSourcesPayload;
import com.grahambartley.toomanychests.terminal.OwnedTerminal;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TagSourceMirrorTest {
  private static final int SYNC_ID = 7;
  private static final List<OwnedTerminal> SOURCES =
      List.of(new OwnedTerminal(new BlockPos(1, 2, 3), 4));

  @AfterEach
  void forgetWhatTheServerSent() {
    TagSourceMirror.clear();
  }

  @Test
  void aScreenAsksForItsOwnSyncIdAndGetsWhatTheServerSent() {
    TagSourceMirror.accept(new TagSourcesPayload(SYNC_ID, SOURCES));

    assertEquals(SOURCES, TagSourceMirror.sourcesFor(SYNC_ID));
  }

  @Test
  void aListMeantForAnotherScreenIsNeverShownOnThisOne() {
    TagSourceMirror.accept(new TagSourcesPayload(SYNC_ID, SOURCES));

    assertTrue(TagSourceMirror.sourcesFor(SYNC_ID + 1).isEmpty());
  }

  @Test
  void aScreenThatHasBeenSentNothingShowsNothing() {
    assertTrue(TagSourceMirror.sourcesFor(SYNC_ID).isEmpty());
  }

  @Test
  void disconnectingForgetsEveryTerminalTheServerNamed() {
    TagSourceMirror.accept(new TagSourcesPayload(SYNC_ID, SOURCES));

    TagSourceMirror.clear();

    assertTrue(TagSourceMirror.sourcesFor(SYNC_ID).isEmpty());
  }

  @Test
  void aSecondListReplacesTheFirstRatherThanAddingToIt() {
    TagSourceMirror.accept(new TagSourcesPayload(SYNC_ID, SOURCES));

    TagSourceMirror.accept(new TagSourcesPayload(SYNC_ID, List.of()));

    assertTrue(TagSourceMirror.sourcesFor(SYNC_ID).isEmpty());
  }
}
