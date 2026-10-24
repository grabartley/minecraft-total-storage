package com.grahambartley.totalstorage.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkTagSetTest {
  @ParameterizedTest
  @EnumSource(PresetTag.class)
  void aFreshNetworkAlreadyKnowsEveryPreset(PresetTag preset) {
    assertTrue(NetworkTagSet.defaults().contains(preset.id()));
  }

  @Test
  void aFreshNetworkHasNoCustomTags() {
    assertTrue(NetworkTagSet.defaults().custom().isEmpty());
  }

  @Test
  void presetsComeFirstInFilterColumnOrderAndCustomTagsFollowByName() {
    NetworkTagSet tags = NetworkTagSet.defaults().withCustomTag("zinc").withCustomTag("alloys");

    List<String> ids = tags.all().stream().map(NetworkTag::id).toList();
    List<String> presetIds = PresetTag.all().stream().map(PresetTag::id).toList();

    assertEquals(presetIds, ids.subList(0, presetIds.size()));
    assertEquals(List.of("alloys", "zinc"), ids.subList(presetIds.size(), ids.size()));
  }

  @Test
  void twoNetworksConfiguredTheSameWayCompareEqualWhateverOrderBuiltThem() {
    NetworkTagSet forwards =
        NetworkTagSet.defaults()
            .withCustomTag("ores")
            .withCustomTag("wood_things")
            .withItem("ores", "minecraft:coal");
    NetworkTagSet backwards =
        NetworkTagSet.defaults()
            .withCustomTag("wood_things")
            .withCustomTag("ores")
            .withItem("ores", "minecraft:coal");

    assertEquals(forwards, backwards);
    assertEquals(forwards.hashCode(), backwards.hashCode());
  }

  @Test
  void aCustomTagIsRememberedAndCanBeRemoved() {
    NetworkTagSet tags = NetworkTagSet.defaults().withCustomTag("ores", "Ores");

    assertEquals(Optional.of("Ores"), tags.byId("ores").map(NetworkTag::displayName));
    assertFalse(tags.withoutTag("ores").contains("ores"));
  }

  @Test
  void addingATagThatAlreadyExistsLeavesTheOneAlreadyThere() {
    NetworkTagSet tags = NetworkTagSet.defaults().withCustomTag("ores", "Ores");

    assertSame(tags, tags.withCustomTag("ores", "Rocks"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void aBlankIdIsNotATag(String id) {
    assertSame(NetworkTagSet.defaults(), NetworkTagSet.defaults().withCustomTag(id));
  }

  @Test
  void aNullTagIsIgnoredRatherThanStored() {
    assertSame(NetworkTagSet.defaults(), NetworkTagSet.defaults().withTag(null));
    assertSame(NetworkTagSet.defaults(), NetworkTagSet.defaults().withCustomTag(null));
    assertSame(NetworkTagSet.defaults(), NetworkTagSet.defaults().withoutTag(null));
  }

  @Test
  void aPresetCannotBeRenamedRemovedOrAssignedItems() {
    String preset = PresetTag.WOOD.id();
    NetworkTagSet tags = NetworkTagSet.defaults();

    assertSame(tags, tags.withoutTag(preset));
    assertSame(tags, tags.withItem(preset, "minecraft:oak_log"));
    assertSame(tags, tags.withTag(NetworkTag.custom(preset, "My Wood")));
    assertTrue(tags.byId(preset).orElseThrow().immutable());
  }

  @Test
  void aStoredTagCannotShadowAPresetItCollidesWith() {
    NetworkTagSet tags =
        new NetworkTagSet(List.of(NetworkTag.custom(PresetTag.STONE.id(), "Not The Preset")));

    assertTrue(tags.byId(PresetTag.STONE.id()).orElseThrow().immutable());
    assertTrue(tags.custom().isEmpty());
  }

  @Test
  void anItemIsAssignedToATagAndCanBeUnassigned() {
    NetworkTagSet tags =
        NetworkTagSet.defaults().withCustomTag("ores").withItem("ores", "minecraft:coal");

    assertEquals(Set.of("ores"), tags.tagIdsFor("minecraft:coal"));
    assertEquals(Set.of(), tags.withoutItem("ores", "minecraft:coal").tagIdsFor("minecraft:coal"));
  }

  @Test
  void anItemResolvesToEveryTagItCarriesInCanonicalOrder() {
    NetworkTagSet tags =
        NetworkTagSet.defaults()
            .withCustomTag("zinc")
            .withCustomTag("alloys")
            .withCustomTag("building")
            .withItem("zinc", "minecraft:cobblestone")
            .withItem("building", "minecraft:cobblestone")
            .withItem("alloys", "minecraft:cobblestone");

    assertEquals(
        List.of("alloys", "building", "zinc"),
        List.copyOf(tags.tagIdsFor("minecraft:cobblestone")));
  }

  @Test
  void anItemWithNoTagsResolvesToNothingRatherThanNull() {
    assertEquals(Set.of(), NetworkTagSet.defaults().tagIdsFor("minecraft:cobblestone"));
    assertEquals(Set.of(), NetworkTagSet.defaults().tagIdsFor(null));
  }

  @Test
  void assigningToATagThatDoesNotExistChangesNothing() {
    NetworkTagSet tags = NetworkTagSet.defaults();

    assertSame(tags, tags.withItem("nonexistent", "minecraft:coal"));
    assertSame(tags, tags.withoutItem("nonexistent", "minecraft:coal"));
  }

  @Test
  void twoNetworksKeepTheirOwnTagsAndAssignments() {
    NetworkTagSet base = NetworkTagSet.defaults().withCustomTag("ores");
    NetworkTagSet mine = base.withItem("ores", "minecraft:coal");
    NetworkTagSet outpost = base.withCustomTag("crops").withItem("crops", "minecraft:wheat");

    assertEquals(Set.of("ores"), mine.tagIdsFor("minecraft:coal"));
    assertEquals(Set.of(), outpost.tagIdsFor("minecraft:coal"));
    assertFalse(mine.contains("crops"));
    assertEquals(Set.of(), base.tagIdsFor("minecraft:coal"));
  }

  @Test
  void renamingATagKeepsEveryItemItHeld() {
    NetworkTagSet tags =
        NetworkTagSet.defaults()
            .withCustomTag("base build")
            .withItem("base-build", "minecraft:cobblestone")
            .withItem("base-build", "minecraft:oak_planks");

    NetworkTagSet renamed = tags.withRenamedTag("base-build", "Castle");

    assertFalse(renamed.contains("base-build"));
    assertTrue(renamed.contains("castle"));
    assertEquals(
        Set.of("minecraft:cobblestone", "minecraft:oak_planks"),
        renamed.byId("castle").orElseThrow().items());
    assertEquals(Set.of("castle"), renamed.tagIdsFor("minecraft:cobblestone"));
  }

  @Test
  void renamingATagKeepsTheNameAsItWasWritten() {
    NetworkTagSet renamed =
        NetworkTagSet.defaults().withCustomTag("castle").withRenamedTag("castle", "  The Keep  ");

    assertEquals("The Keep", renamed.byId("the-keep").orElseThrow().displayName());
  }

  @Test
  void renamingATagOntoANameAlreadyTakenChangesNothing() {
    NetworkTagSet tags = NetworkTagSet.defaults().withCustomTag("castle").withCustomTag("keep");

    assertSame(tags, tags.withRenamedTag("castle", "keep"));
  }

  @Test
  void renamingATagToAPresetNameChangesNothing() {
    NetworkTagSet tags = NetworkTagSet.defaults().withCustomTag("castle");

    assertSame(tags, tags.withRenamedTag("castle", PresetTag.WOOD.id()));
  }

  @Test
  void renamingAPresetOrAnUnknownTagChangesNothing() {
    NetworkTagSet tags = NetworkTagSet.defaults().withCustomTag("castle");

    assertSame(tags, tags.withRenamedTag(PresetTag.WOOD.id(), "timber"));
    assertSame(tags, tags.withRenamedTag("nonexistent", "castle keep"));
  }

  @Test
  void renamingATagToABlankNameChangesNothing() {
    NetworkTagSet tags = NetworkTagSet.defaults().withCustomTag("castle");

    assertSame(tags, tags.withRenamedTag("castle", "   "));
  }

  @Test
  void deletingATagTakesItOffEveryItemThatCarriedIt() {
    NetworkTagSet tags =
        NetworkTagSet.defaults()
            .withCustomTag("castle")
            .withItem("castle", "minecraft:cobblestone");

    NetworkTagSet deleted = tags.withoutTag("castle");

    assertFalse(deleted.contains("castle"));
    assertEquals(Set.of(), deleted.tagIdsFor("minecraft:cobblestone"));
  }

  @Test
  void filingSeveralItemsAtOnceFilesEveryOneOfThem() {
    NetworkTagSet tags =
        NetworkTagSet.defaults()
            .withCustomTag("castle")
            .withItems("castle", List.of("minecraft:cobblestone", "minecraft:oak_planks"));

    assertEquals(
        Set.of("minecraft:cobblestone", "minecraft:oak_planks"),
        tags.byId("castle").orElseThrow().items());
  }

  @Test
  void filingSeveralItemsKeepsWhatTheTagAlreadyHeld() {
    NetworkTagSet tags =
        NetworkTagSet.defaults()
            .withCustomTag("castle")
            .withItem("castle", "minecraft:diamond")
            .withItems("castle", List.of("minecraft:cobblestone"));

    assertEquals(
        Set.of("minecraft:diamond", "minecraft:cobblestone"),
        tags.byId("castle").orElseThrow().items());
  }

  @Test
  void theSameItemNamedTwiceIsFiledOnce() {
    NetworkTagSet tags =
        NetworkTagSet.defaults()
            .withCustomTag("castle")
            .withItems(
                "castle",
                List.of("minecraft:cobblestone", "MINECRAFT:COBBLESTONE", "minecraft:cobblestone"));

    assertEquals(Set.of("minecraft:cobblestone"), tags.byId("castle").orElseThrow().items());
  }

  @Test
  void filingNothingAtAllChangesNothing() {
    NetworkTagSet tags = NetworkTagSet.defaults().withCustomTag("castle");

    assertSame(tags, tags.withItems("castle", List.of()));
    assertSame(tags, tags.withItems("castle", null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"nonexistent", "", "   "})
  void filingIntoATagThatIsNotThereChangesNothing(String tagId) {
    NetworkTagSet tags = NetworkTagSet.defaults().withCustomTag("castle");

    assertSame(tags, tags.withItems(tagId, List.of("minecraft:cobblestone")));
  }

  @ParameterizedTest
  @EnumSource(PresetTag.class)
  void filingIntoAPresetChangesNothingBecausePresetsAreWorkedOut(PresetTag preset) {
    NetworkTagSet tags = NetworkTagSet.defaults().withCustomTag("castle");

    assertSame(tags, tags.withItems(preset.id(), List.of("minecraft:cobblestone")));
  }

  @Test
  void filingOnlyItemsATagAlreadyHeldLeavesTheSetAsItWas() {
    NetworkTagSet tags =
        NetworkTagSet.defaults()
            .withCustomTag("castle")
            .withItem("castle", "minecraft:cobblestone");

    assertSame(tags, tags.withItems("castle", List.of("minecraft:cobblestone")));
  }

  @Test
  void aTagIsFoundByTheNameItWasMadeWithAsWellAsByItsId() {
    NetworkTagSet tags = NetworkTagSet.defaults().withCustomTag("Base Build");

    assertTrue(tags.contains("Base Build"), "the name it was made with does not find it");
    assertTrue(tags.contains("base-build"), "the id it was minted as does not find it");
    assertTrue(tags.byId("Base Build").isPresent());
    assertEquals("base-build", tags.byId("Base Build").orElseThrow().id());
  }

  @Test
  void everyEditFindsATagByTheNameItWasMadeWith() {
    NetworkTagSet tags =
        NetworkTagSet.defaults()
            .withCustomTag("Base Build")
            .withItem("Base Build", "minecraft:stone");

    assertTrue(tags.byId("base-build").orElseThrow().contains("minecraft:stone"));
    assertFalse(tags.withoutTag("Base Build").contains("base-build"));
    assertFalse(
        tags.withoutItem("Base Build", "minecraft:stone")
            .byId("base-build")
            .orElseThrow()
            .contains("minecraft:stone"));
  }

  @Test
  void aTagMadeTwiceUnderOneNameIsStillOneTag() {
    NetworkTagSet tags =
        NetworkTagSet.defaults().withCustomTag("Base Build").withCustomTag("base-build");

    assertEquals(1, tags.custom().size());
  }
}
