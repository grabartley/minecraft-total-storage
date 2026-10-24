package com.grahambartley.totalstorage.config;

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
    assertEquals(TotalStorageConfig.defaults(), ClientConfigMirror.current());
    assertFalse(ClientConfigMirror.hasReceived());
  }

  @Test
  void whatTheServerSentIsWhatTheClientReads() {
    TotalStorageConfig sent = serverSaid(32);

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

    assertEquals(TotalStorageConfig.defaults(), ClientConfigMirror.current());
    assertFalse(ClientConfigMirror.hasReceived());
  }

  @Test
  void aForgedMirrorCannotRaiseTheRadiusTheServerEnforces() {
    ClientConfigMirror.accept(serverSaid(TotalStorageConfig.MAX_TERMINAL_RADIUS_MAX));

    assertEquals(
        TotalStorageConfig.MAX_TERMINAL_RADIUS_MAX,
        ClientConfigMirror.current().maxTerminalRadius());
    assertEquals(
        TotalStorageConfig.DEFAULT_MAX_TERMINAL_RADIUS,
        ServerConfigService.current().maxTerminalRadius());
    assertNotEquals(ClientConfigMirror.current(), ServerConfigService.current());
  }

  private static TotalStorageConfig serverSaid(int maxTerminalRadius) {
    return ConfigBuilder.from(TotalStorageConfig.defaults())
        .maxTerminalRadius(maxTerminalRadius)
        .build();
  }
}
