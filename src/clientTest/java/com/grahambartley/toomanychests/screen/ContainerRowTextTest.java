package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ContainerRowTextTest {
  private static final String PREFIX = "screen.too-many-chests.terminal.";

  private static final String COUNT_PREFIX = "count.too-many-chests.";

  private static ContainerRoleEntry entry(int variants, long items) {
    return new ContainerRoleEntry(
        new BlockPos(12, 64, -8),
        ContainerRole.GENERAL,
        Optional.empty(),
        variants,
        items,
        0,
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
}
