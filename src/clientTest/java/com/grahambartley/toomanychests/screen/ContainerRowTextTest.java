package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
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
        new BlockPos(12, 64, -8), ContainerRole.GENERAL, variants, items, 4.0);
  }

  private static String keyOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] argumentsOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }

  /** A row has to be recognisable as a chest the player stood in front of, not just a position. */
  @Test
  void aStockedContainerReadsAsItsPositionAndWhatIsInIt() {
    Text row = ContainerRowText.position(entry(3, 168L));

    assertEquals(PREFIX + "settings.container", keyOf(row));
    assertEquals("12 64 -8", argumentsOf(row)[0]);
    assertEquals(3, argumentsOf(row)[1]);
  }

  @Test
  void anEmptyContainerSaysSoRatherThanReadingAsZeroKinds() {
    Text row = ContainerRowText.position(entry(0, 0L));

    assertEquals(PREFIX + "settings.container.empty", keyOf(row));
    assertEquals("12 64 -8", argumentsOf(row)[0]);
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
