package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TagSetImportTest {
  private static final NetworkTagSet CASTLE_AND_FARM =
      new NetworkTagSet(
          List.of(
              NetworkTag.custom("castle", "Castle", Set.of("minecraft:stone")),
              NetworkTag.custom("farm", "Farm", Set.of("minecraft:wheat"))));

  private static final List<NetworkTag> MIXED =
      List.of(
          NetworkTag.custom("castle", "Castle", Set.of("minecraft:stone")),
          NetworkTag.custom(PresetTag.TOOLS.id(), "Tools", Set.of()),
          NetworkTag.custom("a".repeat(TagNameRule.MAX_LENGTH + 1), "Long", Set.of()));

  @Test
  void mergingBringsEveryCustomTagAcross() {
    final TagSetImportResult result =
        TagSetImport.into(NetworkTagSet.defaults(), CASTLE_AND_FARM, TagSetImportMode.MERGE);

    assertEquals(2, result.imported());
    assertEquals(0, result.skipped());
    assertEquals(2, result.tags().custom().size());
    assertTrue(result.tags().byId("castle").orElseThrow().contains("minecraft:stone"));
  }

  @Test
  void mergingKeepsTheTagsThatWereAlreadyThere() {
    final NetworkTagSet target =
        new NetworkTagSet(List.of(NetworkTag.custom("mine", "Mine", Set.of("minecraft:coal"))));

    final TagSetImportResult result =
        TagSetImport.into(target, CASTLE_AND_FARM, TagSetImportMode.MERGE);

    assertEquals(3, result.tags().custom().size());
    assertTrue(result.tags().contains("mine"));
  }

  @Test
  void replacingDropsWhatWasThereFirst() {
    final NetworkTagSet target =
        new NetworkTagSet(List.of(NetworkTag.custom("mine", "Mine", Set.of("minecraft:coal"))));

    final TagSetImportResult result =
        TagSetImport.into(target, CASTLE_AND_FARM, TagSetImportMode.REPLACE);

    assertEquals(2, result.tags().custom().size());
    assertFalse(result.tags().contains("mine"));
  }

  @Test
  void aTagThatExistsOnBothSidesGainsTheItemsItWasMissing() {
    final NetworkTagSet target =
        new NetworkTagSet(
            List.of(NetworkTag.custom("castle", "Castle", Set.of("minecraft:oak_planks"))));

    final TagSetImportResult result =
        TagSetImport.into(target, CASTLE_AND_FARM, TagSetImportMode.MERGE);

    final NetworkTag merged = result.tags().byId("castle").orElseThrow();
    assertTrue(merged.contains("minecraft:oak_planks"));
    assertTrue(merged.contains("minecraft:stone"));
  }

  @Test
  void anItemTheTargetWorldHasNeverSeenIsKeptRatherThanDropped() {
    final NetworkTagSet source =
        new NetworkTagSet(List.of(NetworkTag.custom("gears", "Gears", Set.of("create:cogwheel"))));

    final TagSetImportResult result =
        TagSetImport.into(NetworkTagSet.defaults(), source, TagSetImportMode.MERGE);

    assertTrue(result.tags().byId("gears").orElseThrow().contains("create:cogwheel"));
  }

  @Test
  void aSourceBeyondTheCustomTagLimitImportsWhatFitsAndCountsTheRest() {
    final List<NetworkTag> many = new ArrayList<>();
    for (int index = 0; index < NetworkTagSet.MAX_CUSTOM + 5; index++) {
      many.add(NetworkTag.custom("tag " + index));
    }

    final TagSetImportResult result =
        TagSetImport.into(
            NetworkTagSet.defaults(), new NetworkTagSet(many), TagSetImportMode.MERGE);

    assertEquals(NetworkTagSet.MAX_CUSTOM, result.imported());
    assertEquals(5, result.skipped());
  }

  @ParameterizedTest
  @EnumSource(TagSetImportMode.class)
  void importingNothingChangesNothingAndSaysSo(TagSetImportMode mode) {
    final TagSetImportResult result =
        TagSetImport.into(CASTLE_AND_FARM, NetworkTagSet.defaults(), mode);

    assertEquals(0, result.imported());
    assertEquals(0, result.skipped());
    assertFalse(result.broughtSomethingAcross());
  }

  @Test
  void aPresetIsNeverCopiedBecauseEveryNetworkAlreadyHasIt() {
    final TagSetImportResult result =
        TagSetImport.into(
            NetworkTagSet.defaults(), NetworkTagSet.defaults(), TagSetImportMode.REPLACE);

    assertEquals(0, result.imported());
    assertTrue(result.tags().custom().isEmpty());
  }

  @Test
  void anEntryThatNeverParsedIsCountedRatherThanVanishing() {
    final TagSetImportResult result =
        TagSetImport.into(
            NetworkTagSet.defaults(),
            new TagSetContents(CASTLE_AND_FARM.custom(), 2),
            TagSetImportMode.MERGE);

    assertEquals(2, result.imported());
    assertEquals(2, result.skipped());
    assertEquals(2, result.skips().countOf(TagSkipReason.MALFORMED));
  }

  @Test
  void everySkipIsCountedUnderTheReasonThatCausedIt() {
    final TagSetImportResult result =
        TagSetImport.into(
            NetworkTagSet.defaults(), new TagSetContents(MIXED, 2), TagSetImportMode.MERGE);

    assertEquals(1, result.imported());
    assertEquals(4, result.skipped());
    assertEquals(1, result.skips().countOf(TagSkipReason.PRESET_NAME));
    assertEquals(1, result.skips().countOf(TagSkipReason.TOO_LONG));
    assertEquals(2, result.skips().countOf(TagSkipReason.MALFORMED));
  }

  @Test
  void whatCameAcrossAndWhatWasSkippedAddUpToTheFileHandedOver() {
    final TagSetImportResult result =
        TagSetImport.into(
            NetworkTagSet.defaults(), new TagSetContents(MIXED, 2), TagSetImportMode.MERGE);

    assertEquals(5, result.imported() + result.skipped());
  }
}
