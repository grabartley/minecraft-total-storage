package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.storage.ContainerAssignment;
import com.grahambartley.toomanychests.storage.ContainerAssignments;
import com.grahambartley.toomanychests.storage.ContainerFilter;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.tag.NetworkTag;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import com.grahambartley.toomanychests.tag.PresetTag;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalSettingsTest {
  private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
  private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
  private static final BlockPos BARREL = new BlockPos(3, 64, -7);
  private static final BlockPos CHEST = new BlockPos(-11, 12, 40);

  @ParameterizedTest
  @CsvSource({"-40, 1", "0, 1", "1, 1", "16, 16", "64, 64", "65, 64", "100000, 64"})
  void aRadiusIsClampedIntoTheRangeTheServerCanEverAllow(int given, int expected) {
    assertEquals(expected, TerminalSettings.defaults().withRadius(given).radius());
  }

  @Test
  void aFreshTerminalIsOwnerLockedWithNoOwnerAndNothingConfigured() {
    TerminalSettings defaults = TerminalSettings.defaults();

    assertEquals(TerminalAccessMode.OWNER, defaults.accessMode());
    assertEquals(Optional.empty(), defaults.owner());
    assertTrue(defaults.trusted().isEmpty());
    assertTrue(defaults.containers().isEmpty());
    assertTrue(defaults.tags().custom().isEmpty());
  }

  @Test
  void aNullAccessModeFallsBackToOwnerRatherThanBeingStored() {
    assertEquals(
        TerminalAccessMode.OWNER, TerminalSettings.defaults().withAccessMode(null).accessMode());
  }

  @Test
  void theRecordedOwnerIsRecognisedAndNobodyElseIs() {
    TerminalSettings owned = TerminalSettings.defaults().withOwner(ALICE);

    assertTrue(owned.isOwnedBy(ALICE));
    assertFalse(owned.isOwnedBy(BOB));
    assertFalse(owned.isOwnedBy(null));
  }

  @Test
  void clearingTheOwnerLeavesTheTerminalUnowned() {
    TerminalSettings cleared = TerminalSettings.defaults().withOwner(ALICE).withOwner(null);

    assertEquals(Optional.empty(), cleared.owner());
    assertFalse(cleared.isOwnedBy(ALICE));
  }

  @Test
  void trustingAPlayerIsRememberedAndRevokingItIsToo() {
    TerminalSettings trusted = TerminalSettings.defaults().withTrusted(ALICE);

    assertTrue(trusted.isTrusted(ALICE));
    assertFalse(trusted.isTrusted(BOB));
    assertFalse(trusted.withoutTrusted(ALICE).isTrusted(ALICE));
  }

  @Test
  void trustingTheSamePlayerTwiceChangesNothing() {
    TerminalSettings trusted = TerminalSettings.defaults().withTrusted(ALICE);

    assertSame(trusted, trusted.withTrusted(ALICE));
    assertSame(trusted, trusted.withTrusted(null));
    assertSame(trusted, trusted.withoutTrusted(BOB));
  }

  @Test
  void aTrustListBuiltInEitherOrderComparesEqual() {
    assertEquals(
        TerminalSettings.defaults().withTrusted(ALICE).withTrusted(BOB),
        TerminalSettings.defaults().withTrusted(BOB).withTrusted(ALICE));
  }

  @Test
  void anUnconfiguredContainerHasTheDefaultRole() {
    assertEquals(ContainerRole.DEFAULT, TerminalSettings.defaults().roleFor(BARREL));
    assertEquals(ContainerRole.DEFAULT, TerminalSettings.defaults().roleFor(null));
  }

  @Test
  void aRoleIsRememberedPerContainer() {
    TerminalSettings settings =
        TerminalSettings.defaults()
            .withContainerRole(BARREL, ContainerRole.EXCLUDED)
            .withContainerRole(CHEST, ContainerRole.FILTERED);

    assertEquals(ContainerRole.EXCLUDED, settings.roleFor(BARREL));
    assertEquals(ContainerRole.FILTERED, settings.roleFor(CHEST));
  }

  @Test
  void givingAContainerTheDefaultRoleForgetsItRatherThanRecordingIt() {
    TerminalSettings settings =
        TerminalSettings.defaults()
            .withContainerRole(BARREL, ContainerRole.EXCLUDED)
            .withContainerRole(BARREL, ContainerRole.GENERAL);

    assertTrue(settings.containers().isEmpty());
    assertEquals(TerminalSettings.defaults(), settings);
  }

  @Test
  void assigningARoleAContainerAlreadyHasChangesNothing() {
    TerminalSettings settings =
        TerminalSettings.defaults().withContainerRole(BARREL, ContainerRole.EXCLUDED);

    assertSame(settings, settings.withContainerRole(BARREL, ContainerRole.EXCLUDED));
    assertSame(settings, settings.withContainerRole(null, ContainerRole.EXCLUDED));
  }

  @Test
  void aNullRoleIsReadAsTheDefaultRatherThanBeingStored() {
    TerminalSettings settings =
        TerminalSettings.defaults()
            .withContainerRole(BARREL, ContainerRole.FILTERED)
            .withContainerRole(BARREL, null);

    assertEquals(ContainerRole.DEFAULT, settings.roleFor(BARREL));
    assertTrue(settings.containers().isEmpty());
  }

  @Test
  void roleAssignmentsAreOrderedByPositionWhateverOrderTheyArrivedIn() {
    List<BlockPos> forwards =
        List.copyOf(
            TerminalSettings.defaults()
                .withContainerRole(BARREL, ContainerRole.EXCLUDED)
                .withContainerRole(CHEST, ContainerRole.FILTERED)
                .containers()
                .byPosition()
                .keySet());
    List<BlockPos> backwards =
        List.copyOf(
            TerminalSettings.defaults()
                .withContainerRole(CHEST, ContainerRole.FILTERED)
                .withContainerRole(BARREL, ContainerRole.EXCLUDED)
                .containers()
                .byPosition()
                .keySet());

    assertEquals(forwards, backwards);
  }

  @Test
  void aTagIsRememberedAndCanBeRemoved() {
    TerminalSettings tagged = TerminalSettings.defaults().withTag("ores");

    assertTrue(tagged.hasTag("ores"));
    assertFalse(tagged.withoutTag("ores").hasTag("ores"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "\t"})
  void aBlankTagIsNotATag(String tag) {
    assertSame(TerminalSettings.defaults(), TerminalSettings.defaults().withTag(tag));
  }

  @Test
  void aTagIsTrimmedSoTheSameNameIsNeverStoredTwice() {
    TerminalSettings tagged = TerminalSettings.defaults().withTag("  Ores  ");

    assertEquals(List.of(NetworkTag.custom("ores", "Ores")), tagged.tags().custom());
    assertSame(tagged, tagged.withTag("ores"));
    assertTrue(tagged.hasTag(" ORES "));
  }

  @Test
  void aNullTagIsIgnoredOnBothSides() {
    assertSame(TerminalSettings.defaults(), TerminalSettings.defaults().withTag(null));
    assertSame(TerminalSettings.defaults(), TerminalSettings.defaults().withoutTag(null));
    assertFalse(TerminalSettings.defaults().hasTag(null));
  }

  @Test
  void nullsInsideACollectionAreDroppedRatherThanStored() {
    Set<UUID> trusted = new HashSet<>(Arrays.asList(ALICE, null));
    Map<BlockPos, ContainerAssignment> roles = new HashMap<>();
    roles.put(BARREL, null);
    roles.put(null, ContainerAssignment.of(ContainerRole.EXCLUDED));
    roles.put(CHEST, ContainerAssignment.of(ContainerRole.FILTERED));
    NetworkTagSet tags =
        new NetworkTagSet(
            Arrays.asList(NetworkTag.custom("ores"), null, NetworkTag.preset(PresetTag.WOOD)));

    TerminalSettings settings =
        new TerminalSettings(
            8,
            TerminalAccessMode.OPEN,
            Optional.empty(),
            trusted,
            new ContainerAssignments(roles),
            tags);

    assertEquals(Set.of(ALICE), settings.trusted());
    assertEquals(
        Map.of(CHEST, ContainerAssignment.of(ContainerRole.FILTERED)),
        settings.containers().byPosition());
    assertEquals(List.of(NetworkTag.custom("ores")), settings.tags().custom());
  }

  @Test
  void aNullOwnerOptionalIsReadAsNoOwner() {
    TerminalSettings settings =
        new TerminalSettings(
            8,
            TerminalAccessMode.OPEN,
            null,
            Set.of(),
            ContainerAssignments.empty(),
            NetworkTagSet.defaults());

    assertEquals(Optional.empty(), settings.owner());
  }

  @Test
  void everyStoredCollectionRefusesToBeChangedFromOutside() {
    TerminalSettings settings =
        TerminalSettings.defaults()
            .withTrusted(ALICE)
            .withContainerRole(BARREL, ContainerRole.EXCLUDED)
            .withTag("ores");

    assertThrows(UnsupportedOperationException.class, () -> settings.trusted().add(BOB));
    assertThrows(
        UnsupportedOperationException.class,
        () -> settings.containers().byPosition().put(CHEST, ContainerAssignment.DEFAULT));
    assertThrows(
        UnsupportedOperationException.class,
        () -> settings.tags().all().add(NetworkTag.custom("wood")));
  }

  @Test
  void twoTerminalsConfiguredTheSameWayCompareEqual() {
    TerminalSettings first =
        TerminalSettings.defaults()
            .withRadius(12)
            .withAccessMode(TerminalAccessMode.TRUSTED)
            .withOwner(ALICE)
            .withTrusted(BOB)
            .withContainerRole(BARREL, ContainerRole.EXCLUDED)
            .withTag("ores");
    TerminalSettings second =
        TerminalSettings.defaults()
            .withTag("ores")
            .withContainerRole(BARREL, ContainerRole.EXCLUDED)
            .withTrusted(BOB)
            .withOwner(ALICE)
            .withAccessMode(TerminalAccessMode.TRUSTED)
            .withRadius(12);

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    assertNotEquals(first, second.withRadius(11));
  }

  @Test
  void aFilteredContainerRemembersWhatItAccepts() {
    TerminalSettings settings =
        TerminalSettings.defaults()
            .withContainerRole(CHEST, ContainerRole.FILTERED)
            .withContainerFilter(CHEST, ContainerFilter.ofTags("ores"));

    assertEquals(ContainerFilter.ofTags("ores"), settings.filterFor(CHEST));
    assertEquals(
        ContainerAssignment.filtered(ContainerFilter.ofTags("ores")),
        settings.assignmentFor(CHEST));
  }

  @Test
  void aContainerNobodyConfiguredAcceptsNothingInParticular() {
    assertEquals(ContainerFilter.NONE, TerminalSettings.defaults().filterFor(CHEST));
    assertEquals(ContainerAssignment.DEFAULT, TerminalSettings.defaults().assignmentFor(CHEST));
  }

  @Test
  void forgettingAContainerDropsItsRoleAndItsFilter() {
    TerminalSettings settings =
        TerminalSettings.defaults()
            .withContainerRole(BARREL, ContainerRole.FILTERED)
            .withContainerFilter(BARREL, ContainerFilter.ofTags("ores"))
            .withContainerRole(CHEST, ContainerRole.EXCLUDED);

    TerminalSettings forgotten = settings.forgetContainer(BARREL);

    assertEquals(ContainerRole.DEFAULT, forgotten.roleFor(BARREL));
    assertEquals(ContainerFilter.NONE, forgotten.filterFor(BARREL));
    assertEquals(ContainerRole.EXCLUDED, forgotten.roleFor(CHEST));
  }

  @Test
  void forgettingAContainerNobodyConfiguredChangesNothing() {
    TerminalSettings settings =
        TerminalSettings.defaults().withContainerRole(CHEST, ContainerRole.EXCLUDED);

    assertSame(settings, settings.forgetContainer(BARREL));
    assertSame(settings, settings.forgetContainer(null));
  }
}
