package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientConfigMirrorTest {
  @BeforeEach
  @AfterEach
  void startFromNothingReceived() {
    ClientConfigMirror.clear();
  }

  @Test
  void aClientThatHasHeardNothingYetReadsTheShippedDefaults() {
    assertEquals(TooManyChestsConfig.defaults(), ClientConfigMirror.current());
    assertFalse(ClientConfigMirror.hasReceived());
  }

  @Test
  void whatTheServerSentIsWhatTheClientReads() {
    TooManyChestsConfig sent = serverSaid(32);

    ClientConfigMirror.accept(sent);

    assertSame(sent, ClientConfigMirror.current());
    assertTrue(ClientConfigMirror.hasReceived());
  }

  @Test
  void aLaterSyncReplacesTheEarlierOne() {
    ClientConfigMirror.accept(serverSaid(32));

    ClientConfigMirror.accept(serverSaid(8));

    assertEquals(8, ClientConfigMirror.current().maxTerminalRadius());
  }

  @Test
  void leavingAServerDropsItsSettingsRatherThanCarryingThemToTheNext() {
    ClientConfigMirror.accept(serverSaid(32));

    ClientConfigMirror.clear();

    assertEquals(TooManyChestsConfig.defaults(), ClientConfigMirror.current());
    assertFalse(ClientConfigMirror.hasReceived());
  }

  @Test
  void aForgedMirrorCannotRaiseTheRadiusTheServerEnforces() {
    ClientConfigMirror.accept(serverSaid(TooManyChestsConfig.MAX_TERMINAL_RADIUS_MAX));

    assertEquals(
        TooManyChestsConfig.MAX_TERMINAL_RADIUS_MAX,
        ClientConfigMirror.current().maxTerminalRadius());
    assertEquals(
        TooManyChestsConfig.DEFAULT_MAX_TERMINAL_RADIUS,
        ServerConfigService.current().maxTerminalRadius());
    assertNotEquals(ClientConfigMirror.current(), ServerConfigService.current());
  }

  private static TooManyChestsConfig serverSaid(int maxTerminalRadius) {
    return ConfigBuilder.from(TooManyChestsConfig.defaults())
        .maxTerminalRadius(maxTerminalRadius)
        .build();
  }
}
