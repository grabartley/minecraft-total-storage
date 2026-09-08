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

/**
 * How a container reads in the settings list.
 *
 * <p>Keys are asserted rather than English, because the English is a fallback for a client whose
 * resource pack has not caught up and the key is what a translator binds to.
 */
class ContainerRowTextTest {
  private static final String PREFIX = "screen.too-many-chests.terminal.";

  private static ContainerRoleEntry entry(int variants, long items) {
    return new ContainerRoleEntry(
        new BlockPos(12, 64, -8), ContainerRole.GENERAL, Optional.empty(), variants, items, 4.0);
  }

  private static String keyOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] argumentsOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }

  /** "1 kinds" reads as a bug, and a Minecraft translation carries no plural rule to fix it. */
  @Test
  void aContainerHoldingOneKindIsNotDescribedInThePlural() {
    Text row = ContainerRowText.contents(entry(1, 32L));

    assertEquals(PREFIX + "settings.container.one", keyOf(row));
    assertEquals(1, argumentsOf(row).length);
  }

  @Test
  void aContainerHoldingSeveralKindsCountsThem() {
    Text row = ContainerRowText.contents(entry(2, 32L));

    assertEquals(PREFIX + "settings.container", keyOf(row));
    assertEquals(2, argumentsOf(row)[0]);
  }

  @Test
  void anEmptyContainerSaysSoRatherThanReadingAsZeroKinds() {
    assertEquals(
        PREFIX + "settings.container.empty", keyOf(ContainerRowText.contents(entry(0, 0L))));
  }

  /**
   * A row leads with what is in the container, because that is what a player recognises. The
   * coordinates are what they need only once they have found the row they mean, so those go to the
   * tooltip the screen draws over a hovered row.
   */
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

  /** Colour alone would leave the distinction unreadable, so the word carries it too. */
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
