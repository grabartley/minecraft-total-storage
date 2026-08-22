package com.grahambartley.toomanychests.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkIndexEntryTest {
  private static final StorageOrigin FIRST = TestOrigins.at(0);
  private static final StorageOrigin SECOND = TestOrigins.at(1);

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 4})
  void providerCountIsTheNumberOfOrigins(int count) {
    NetworkIndexEntry<String> entry =
        new NetworkIndexEntry<>("stone", 64L, List.of(TestOrigins.chain(count)));

    assertEquals(count, entry.providerCount());
  }

  @Test
  void isHeldByRecognisesAnOriginItWasBuiltWith() {
    NetworkIndexEntry<String> entry = new NetworkIndexEntry<>("stone", 64L, List.of(FIRST));

    assertTrue(entry.isHeldBy(FIRST));
    assertFalse(entry.isHeldBy(SECOND));
  }

  @Test
  void isHeldByComparesOriginsByValueRatherThanIdentity() {
    NetworkIndexEntry<String> entry = new NetworkIndexEntry<>("stone", 64L, List.of(FIRST));

    assertTrue(entry.isHeldBy(TestOrigins.at(0)));
  }

  @Test
  void originsAreCopiedSoLaterMutationCannotReachTheEntry() {
    List<StorageOrigin> mutable = new ArrayList<>(List.of(FIRST));
    NetworkIndexEntry<String> entry = new NetworkIndexEntry<>("stone", 64L, mutable);

    mutable.add(SECOND);

    assertEquals(1, entry.providerCount());
  }

  @Test
  void originsAreUnmodifiable() {
    NetworkIndexEntry<String> entry = new NetworkIndexEntry<>("stone", 64L, List.of(FIRST));

    assertThrows(UnsupportedOperationException.class, () -> entry.origins().add(SECOND));
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 1L, 64L, Long.MAX_VALUE})
  void anyNonNegativeCountIsAccepted(long count) {
    assertEquals(count, new NetworkIndexEntry<>("stone", count, List.of(FIRST)).totalCount());
  }

  @ParameterizedTest
  @ValueSource(longs = {-1L, Long.MIN_VALUE})
  void aNegativeCountIsRejected(long count) {
    List<StorageOrigin> origins = List.of(FIRST);

    assertThrows(
        IllegalArgumentException.class, () -> new NetworkIndexEntry<>("stone", count, origins));
  }

  @Test
  void nullVariantIsRejected() {
    List<StorageOrigin> origins = List.of(FIRST);

    assertThrows(NullPointerException.class, () -> new NetworkIndexEntry<>(null, 1L, origins));
  }

  @Test
  void nullOriginsAreRejected() {
    assertThrows(NullPointerException.class, () -> new NetworkIndexEntry<>("stone", 1L, null));
  }
}
