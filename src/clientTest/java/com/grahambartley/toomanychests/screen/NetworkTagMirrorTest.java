package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class NetworkTagMirrorTest {
  private static final int SYNC_ID = 4;

  private static NetworkTagView viewOf(String tag) {
    return new NetworkTagView(
        true, List.of(new NetworkTagEntry(tag, tag, false, 1, 2)), Optional.empty(), Set.of());
  }

  @AfterEach
  void close() {
    NetworkTagMirror.closed();
  }

  @Test
  void aScreenThatHasNotAskedYetHoldsAnUnansweredView() {
    assertFalse(NetworkTagMirror.view().answered());
    assertEquals(List.of(), NetworkTagMirror.view().tags());
  }

  @Test
  void anAnswerForTheOpenScreenIsKept() {
    NetworkTagMirror.opened(SYNC_ID);

    assertTrue(NetworkTagMirror.accept(SYNC_ID, viewOf("castle")));
    assertTrue(NetworkTagMirror.view().answered());
    assertEquals("castle", NetworkTagMirror.view().tags().get(0).id());
  }

  @Test
  void anAnswerForAnotherScreenIsIgnored() {
    NetworkTagMirror.opened(SYNC_ID);

    assertFalse(NetworkTagMirror.accept(SYNC_ID + 1, viewOf("castle")));
    assertEquals(List.of(), NetworkTagMirror.view().tags());
  }

  @Test
  void anAnswerArrivingAfterTheScreenClosedIsIgnored() {
    NetworkTagMirror.opened(SYNC_ID);
    NetworkTagMirror.closed();

    assertFalse(NetworkTagMirror.accept(SYNC_ID, viewOf("castle")));
    assertFalse(NetworkTagMirror.view().answered());
  }

  @Test
  void openingAgainDropsTheAnswerTheLastScreenWasShowing() {
    NetworkTagMirror.opened(SYNC_ID);
    NetworkTagMirror.accept(SYNC_ID, viewOf("castle"));

    NetworkTagMirror.opened(SYNC_ID + 1);

    assertFalse(NetworkTagMirror.view().answered());
    assertEquals(List.of(), NetworkTagMirror.view().tags());
  }

  @Test
  void anEmptyAnswerIsRefusedRatherThanClearingTheView() {
    NetworkTagMirror.opened(SYNC_ID);
    NetworkTagMirror.accept(SYNC_ID, viewOf("castle"));

    assertFalse(NetworkTagMirror.accept(SYNC_ID, null));
    assertEquals("castle", NetworkTagMirror.view().tags().get(0).id());
  }
}
