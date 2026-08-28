package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServerConfigChangesTest {
  private final List<TooManyChestsConfig> heard = new ArrayList<>();
  private final ServerConfigListener recorder = (server, config) -> heard.add(config);

  @BeforeEach
  @AfterEach
  void startFromNoSubscribers() {
    ServerConfigChanges.clear();
    heard.clear();
  }

  @Test
  void aSubscriberHearsThePublishedConfig() {
    ServerConfigChanges.subscribe(recorder);

    ServerConfigChanges.publish(null, TestConfigs.everythingNonDefault());

    assertEquals(List.of(TestConfigs.everythingNonDefault()), heard);
  }

  @Test
  void everySubscriberHearsTheSameChange() {
    List<TooManyChestsConfig> second = new ArrayList<>();
    ServerConfigChanges.subscribe(recorder);
    ServerConfigChanges.subscribe((server, config) -> second.add(config));

    ServerConfigChanges.publish(null, TooManyChestsConfig.defaults());

    assertEquals(1, heard.size());
    assertEquals(1, second.size());
    assertEquals(2, ServerConfigChanges.listenerCount());
  }

  @Test
  void subscribingTwiceStillOnlyDeliversOnce() {
    ServerConfigChanges.subscribe(recorder);
    ServerConfigChanges.subscribe(recorder);

    ServerConfigChanges.publish(null, TooManyChestsConfig.defaults());

    assertEquals(1, heard.size());
    assertEquals(1, ServerConfigChanges.listenerCount());
  }

  @Test
  void anUnsubscribedListenerHearsNothingFurther() {
    ServerConfigChanges.subscribe(recorder);
    ServerConfigChanges.unsubscribe(recorder);

    ServerConfigChanges.publish(null, TooManyChestsConfig.defaults());

    assertTrue(heard.isEmpty());
    assertFalse(ServerConfigChanges.isSubscribed(recorder));
  }

  @Test
  void aListenerMayUnsubscribeWhileBeingTold() {
    ServerConfigChanges.subscribe(
        new ServerConfigListener() {
          @Override
          public void serverConfigChanged(
              net.minecraft.server.MinecraftServer server, TooManyChestsConfig config) {
            ServerConfigChanges.unsubscribe(this);
            heard.add(config);
          }
        });

    ServerConfigChanges.publish(null, TooManyChestsConfig.defaults());

    assertEquals(1, heard.size());
    assertEquals(0, ServerConfigChanges.listenerCount());
  }

  @Test
  void publishingNothingTellsNobody() {
    ServerConfigChanges.subscribe(recorder);

    ServerConfigChanges.publish(null, null);

    assertTrue(heard.isEmpty());
  }

  @Test
  void theConfigArrivesUnchangedRatherThanCopied() {
    TooManyChestsConfig published = TestConfigs.everythingNonDefault();
    List<TooManyChestsConfig> seen = new ArrayList<>();
    ServerConfigChanges.subscribe((server, config) -> seen.add(config));

    ServerConfigChanges.publish(null, published);

    assertSame(published, seen.get(0));
  }

  @Test
  void clearingLeavesNobodySubscribed() {
    ServerConfigChanges.subscribe(recorder);

    ServerConfigChanges.clear();

    assertEquals(0, ServerConfigChanges.listenerCount());
  }
}
