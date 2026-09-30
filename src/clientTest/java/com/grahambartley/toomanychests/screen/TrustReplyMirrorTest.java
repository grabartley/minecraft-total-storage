package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.access.TrustEditOutcome;
import com.grahambartley.toomanychests.network.TerminalTrustReplyPayload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TrustReplyMirrorTest {
  private static final TerminalTrustReplyPayload REPLY =
      new TerminalTrustReplyPayload(3, "Alex", TrustEditOutcome.ADDED);

  @AfterEach
  void forget() {
    TrustReplyMirror.clear();
  }

  @Test
  void aReplyIsHandedToTheScreenItWasFor() {
    TrustReplyMirror.accept(REPLY);

    assertEquals(REPLY, TrustReplyMirror.take(3).orElseThrow());
  }

  @Test
  void aReplyIsHandedOverOnceOnly() {
    TrustReplyMirror.accept(REPLY);
    TrustReplyMirror.take(3);

    assertTrue(TrustReplyMirror.take(3).isEmpty());
  }

  @Test
  void aReplyForAnotherScreenIsNotHandedOver() {
    TrustReplyMirror.accept(REPLY);

    assertTrue(TrustReplyMirror.take(4).isEmpty());
    assertEquals(REPLY, TrustReplyMirror.take(3).orElseThrow());
  }

  @Test
  void clearingForgetsTheReply() {
    TrustReplyMirror.accept(REPLY);

    TrustReplyMirror.clear();

    assertTrue(TrustReplyMirror.take(3).isEmpty());
  }
}
