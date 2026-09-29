package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.config.ConfigSetting;
import com.grahambartley.toomanychests.config.ConfigSettings;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ServerConfigTextTest {
  private static final String PREFIX = "screen.too-many-chests.config.";

  private static ConfigSetting setting(String name) {
    return ConfigSettings.byName(name).orElseThrow();
  }

  private static TranslatableTextContent contentOf(Object text) {
    return (TranslatableTextContent) ((Text) text).getContent();
  }

  @ParameterizedTest
  @ValueSource(strings = {"defaultTerminalAccessMode", "sortOrdering", "sortButtonEnabled"})
  void aChoiceHintNamesOnlyItsDefaultInThePlayersWords(String name) {
    ConfigSetting choice = setting(name);
    TranslatableTextContent hint = contentOf(ServerConfigText.bounds(choice));

    assertEquals(PREFIX + "default", hint.getKey());
    assertEquals(choice.displayValue(choice.defaultValue()), hint.getArgs()[0]);
  }

  @Test
  void theAccessModeHintUsesTheTerminalsOwnWords() {
    assertEquals(
        TerminalAccessMode.OWNER.displayName(),
        contentOf(ServerConfigText.bounds(setting("defaultTerminalAccessMode"))).getArgs()[0]);
  }

  @ParameterizedTest
  @ValueSource(strings = {"sortButtonEnabled", "quickStackEnabled"})
  void aSwitchHintReadsAsVanillasOnOrOff(String name) {
    Object shown = contentOf(ServerConfigText.bounds(setting(name))).getArgs()[0];

    assertTrue(shown.equals(ScreenTexts.ON) || shown.equals(ScreenTexts.OFF), shown.toString());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "maxTerminalRadius",
        "quickStackRadius",
        "nearCapacityWarningPercent",
        "movesPerTickBudget"
      })
  void aNumberHintShowsBothEndsAndTheDefaultInTheSettingsOwnUnits(String name) {
    ConfigSetting number = setting(name);
    TranslatableTextContent hint = contentOf(ServerConfigText.bounds(number));

    assertEquals(PREFIX + "bounds", hint.getKey());
    assertEquals(
        number.displayValue(Integer.toString(number.range().orElseThrow().min())),
        hint.getArgs()[0]);
    assertEquals(
        number.displayValue(Integer.toString(number.range().orElseThrow().max())),
        hint.getArgs()[1]);
    assertEquals(number.displayValue(number.defaultValue()), hint.getArgs()[2]);
  }
}
