package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.ContainerRoleEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ContainerRowTextTest {
  private static final String PREFIX = "screen.total-storage.terminal.";

  private static final String COUNT_PREFIX = "count.total-storage.";

  private static ContainerRoleEntry entry(int variants, long items) {
    return named("", variants, items);
  }

  private static ContainerRoleEntry named(String name, int variants, long items) {
    return new ContainerRoleEntry(
        new BlockPos(12, 64, -8),
        name,
        ContainerRole.GENERAL,
        List.of(),
        Optional.empty(),
        variants,
        items,
        0,
        27,
        4.0);
  }

  private static ContainerRoleEntry filtered(List<String> accepted) {
    return new ContainerRoleEntry(
        new BlockPos(12, 64, -8),
        "",
        ContainerRole.FILTERED,
        accepted,
        Optional.empty(),
        1,
        1L,
        1,
        27,
        4.0);
  }

  private static String keyOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] argumentsOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }

  @Test
  void aContainerHoldingOneKindIsNotDescribedInThePlural() {
    Text row = ContainerRowText.contents(entry(1, 32L));

    assertEquals(COUNT_PREFIX + "kinds.one", keyOf(row));
    assertEquals(0, argumentsOf(row).length);
  }

  @Test
  void aContainerHoldingSeveralKindsCountsThem() {
    Text row = ContainerRowText.contents(entry(2, 32L));

    assertEquals(COUNT_PREFIX + "kinds", keyOf(row));
    assertEquals(2L, argumentsOf(row)[0]);
  }

  @Test
  void whatAContainerHoldsIsCountedForTheTooltipRatherThanCrowdingTheRow() {
    Text total = ContainerRowText.itemTotal(entry(51, 12_300L));

    assertEquals(COUNT_PREFIX + "items", keyOf(total));
    assertEquals("12.3k", argumentsOf(total)[0]);
  }

  @Test
  void aContainerHoldingASingleItemIsNotDescribedInThePlural() {
    assertEquals(COUNT_PREFIX + "items.one", keyOf(ContainerRowText.itemTotal(entry(1, 1L))));
  }

  @Test
  void anEmptyContainerSaysSoRatherThanReadingAsZeroKinds() {
    assertEquals(
        PREFIX + "settings.container.empty", keyOf(ContainerRowText.contents(entry(0, 0L))));
  }

  @Test
  void thePositionIsSaidSeparatelyForTheTooltip() {
    Text where = ContainerRowText.position(entry(3, 168L));

    assertEquals(PREFIX + "settings.container.at", keyOf(where));
    assertEquals("12 64 -8", argumentsOf(where)[0]);
  }

  @ParameterizedTest
  @EnumSource(ContainerRole.class)
  void everyRoleIsNamedUnderItsOwnKey(ContainerRole role) {
    assertEquals(PREFIX + "role." + role.id(), keyOf(ContainerRowText.role(role)));
  }

  @Test
  void noTwoRolesShareAWordOrAColour() {
    assertNotEquals(
        keyOf(ContainerRowText.role(ContainerRole.GENERAL)),
        keyOf(ContainerRowText.role(ContainerRole.EXCLUDED)));
    assertNotEquals(
        ContainerRowText.roleColour(ContainerRole.GENERAL),
        ContainerRowText.roleColour(ContainerRole.EXCLUDED));
    assertNotEquals(
        ContainerRowText.roleColour(ContainerRole.FILTERED),
        ContainerRowText.roleColour(ContainerRole.EXCLUDED));
  }

  @Test
  void aContainerWithNoRoleReadsAsTheDefaultOne() {
    assertEquals(
        keyOf(ContainerRowText.role(ContainerRole.DEFAULT)), keyOf(ContainerRowText.role(null)));
    assertEquals(
        ContainerRowText.roleColour(ContainerRole.DEFAULT), ContainerRowText.roleColour(null));
  }

  @Test
  void anUnnamedContainerIsLabelledByWhereItIs() {
    assertEquals("12 64 -8", ContainerRowText.label(entry(1, 1L)).getString());
  }

  @Test
  void aNamedContainerIsLabelledByItsName() {
    assertEquals("Potions", ContainerRowText.label(named("Potions", 1, 1L)).getString());
  }

  @Test
  void theChipOfAFilteredContainerCountsWhatItAccepts() {
    assertEquals(
        PREFIX + "role.filtered.count",
        keyOf(ContainerRowText.roleChip(filtered(List.of("ores", "wood")))));
  }

  @Test
  void theChipOfAnyOtherRoleIsJustItsName() {
    assertEquals(PREFIX + "role.general", keyOf(ContainerRowText.roleChip(entry(1, 1L))));
  }

  @Test
  void aFilteredContainerAcceptingNothingIsColouredAsAWarning() {
    assertEquals(VanillaPalette.WARNING, ContainerRowText.chipColour(filtered(List.of())));
  }

  @Test
  void aFilteredContainerThatAcceptsSomethingKeepsItsRoleColour() {
    assertEquals(
        ContainerRowText.roleColour(ContainerRole.FILTERED),
        ContainerRowText.chipColour(filtered(List.of("ores"))));
  }

  @Test
  void whatAFilteredContainerAcceptsIsListedRatherThanCounted() {
    assertEquals(
        "ores, wood",
        ContainerRowText.acceptedTags(filtered(List.of("ores", "wood")), tagsOf("ores", "wood"))
            .getString());
  }

  @Test
  void anAcceptedTagIsNamedTheWayTheEditorNamesIt() {
    assertEquals(
        "Ores and Minerals",
        ContainerRowText.acceptedTags(filtered(List.of("ores")), named("ores", "Ores and Minerals"))
            .getString());
  }

  @Test
  void aTagTheViewHasNotHeardOfFallsBackToItsId() {
    assertEquals(
        "ores", ContainerRowText.acceptedTags(filtered(List.of("ores")), tagsOf()).getString());
  }

  @Test
  void aFilteredContainerAcceptingNothingSaysSoRatherThanListingNothing() {
    assertEquals(
        PREFIX + "settings.container.filter.empty",
        keyOf(ContainerRowText.acceptedTags(filtered(List.of()), tagsOf())));
  }

  private static NetworkTagView tagsOf(String... ids) {
    return new NetworkTagView(
        true,
        java.util.Arrays.stream(ids).map(id -> new NetworkTagEntry(id, id, false, 0, 0)).toList(),
        Optional.empty(),
        Set.of());
  }

  private static NetworkTagView named(String id, String displayName) {
    return new NetworkTagView(
        true,
        List.of(new NetworkTagEntry(id, displayName, false, 0, 0)),
        Optional.empty(),
        Set.of());
  }
}
