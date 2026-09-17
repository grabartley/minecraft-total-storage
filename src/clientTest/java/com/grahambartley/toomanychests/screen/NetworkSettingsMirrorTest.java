package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.terminal.NetworkCapacity;
import com.grahambartley.toomanychests.terminal.NetworkSettingsView;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class NetworkSettingsMirrorTest {
  private static NetworkSettingsView answered(int radius) {
    return NetworkSettingsView.answered(
        radius,
        radius,
        32,
        TerminalAccessMode.OPEN,
        true,
        true,
        List.of(),
        NetworkCapacity.EMPTY,
        0,
        0,
        List.of());
  }

  @AfterEach
  void closeTheScreen() {
    NetworkSettingsMirror.closed();
  }

  @Test
  void anAnswerForTheOpenScreenIsTaken() {
    NetworkSettingsMirror.opened(7);

    assertTrue(NetworkSettingsMirror.accept(7, answered(20)));
    assertEquals(20, NetworkSettingsMirror.view().radius());
  }

  @Test
  void anAnswerForAnotherScreenIsDropped() {
    NetworkSettingsMirror.opened(7);

    assertFalse(NetworkSettingsMirror.accept(8, answered(20)));
    assertEquals(NetworkSettingsView.pending().radius(), NetworkSettingsMirror.view().radius());
  }

  @Test
  void anAnswerArrivingWithNoScreenOpenIsDropped() {
    NetworkSettingsMirror.closed();

    assertFalse(NetworkSettingsMirror.accept(7, answered(20)));
  }

  @Test
  void anAnswerWithNothingInItIsDropped() {
    NetworkSettingsMirror.opened(7);

    assertFalse(NetworkSettingsMirror.accept(7, null));
  }

  @Test
  void openingAScreenForgetsWhateverTheLastOneWasShowing() {
    NetworkSettingsMirror.opened(7);
    NetworkSettingsMirror.accept(7, answered(20));

    NetworkSettingsMirror.opened(8);

    assertEquals(NetworkSettingsView.pending().radius(), NetworkSettingsMirror.view().radius());
    assertTrue(NetworkSettingsMirror.accept(8, answered(30)));
    assertFalse(NetworkSettingsMirror.accept(7, answered(40)));
  }

  @Test
  void closingForgetsTheScreenAndWhatItHeld() {
    NetworkSettingsMirror.opened(7);
    NetworkSettingsMirror.accept(7, answered(20));

    NetworkSettingsMirror.closed();

    assertFalse(NetworkSettingsMirror.accept(7, answered(30)));
    assertEquals(NetworkSettingsView.pending().radius(), NetworkSettingsMirror.view().radius());
  }
}
