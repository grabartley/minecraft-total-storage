package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.toomanychests.config.ConfigSetting;
import com.grahambartley.toomanychests.config.ConfigSettings;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;

class ServerConfigTextTest {
  private static final String PREFIX = "screen.too-many-chests.config.";

  private static ConfigSetting setting(String name) {
    return ConfigSettings.byName(name).orElseThrow();
  }

  private static TranslatableTextContent contentOf(Object text) {
    return (TranslatableTextContent) ((Text) text).getContent();
  }

  @Test
  void theAccessModeHintNamesEveryModeAndTheDefaultInTheTerminalsWords() {
    TranslatableTextContent bounds =
        contentOf(ServerConfigText.bounds(setting("defaultTerminalAccessMode")));
    TranslatableTextContent modes = contentOf(bounds.getArgs()[0]);

    assertEquals(PREFIX + "bounds", bounds.getKey());
    assertEquals(TerminalAccessMode.OWNER.displayName(), bounds.getArgs()[1]);
    assertEquals(PREFIX + "or", modes.getKey());
    assertEquals(
        Text.empty()
            .append(TerminalAccessMode.OWNER.displayName())
            .append(Text.translatableWithFallback(PREFIX + "separator", ", "))
            .append(TerminalAccessMode.TRUSTED.displayName()),
        modes.getArgs()[0]);
    assertEquals(TerminalAccessMode.OPEN.displayName(), modes.getArgs()[1]);
  }

  @Test
  void aSwitchHintReadsOnOrOff() {
    TranslatableTextContent bounds =
        contentOf(ServerConfigText.bounds(setting("sortButtonEnabled")));
    TranslatableTextContent choices = contentOf(bounds.getArgs()[0]);

    assertEquals(PREFIX + "or", choices.getKey());
    assertEquals(ScreenTexts.ON, choices.getArgs()[0]);
    assertEquals(ScreenTexts.OFF, choices.getArgs()[1]);
    assertEquals(ScreenTexts.ON, bounds.getArgs()[1]);
  }

  @Test
  void aPercentageHintShowsBothEndsAndTheDefaultAsPercentages() {
    ConfigSetting warning = setting("nearCapacityWarningPercent");
    TranslatableTextContent bounds = contentOf(ServerConfigText.bounds(warning));
    TranslatableTextContent range = contentOf(bounds.getArgs()[0]);

    assertEquals(PREFIX + "range", range.getKey());
    assertEquals(
        warning.displayValue(Integer.toString(warning.range().orElseThrow().min())),
        range.getArgs()[0]);
    assertEquals(
        warning.displayValue(Integer.toString(warning.range().orElseThrow().max())),
        range.getArgs()[1]);
    assertEquals(warning.displayValue(warning.defaultValue()), bounds.getArgs()[1]);
    assertEquals(PREFIX + "value.percent", contentOf(bounds.getArgs()[1]).getKey());
  }

  @Test
  void aPlainNumberHintShowsItsRangeAsNumbers() {
    ConfigSetting radius = setting("quickStackRadius");
    TranslatableTextContent range =
        contentOf(contentOf(ServerConfigText.bounds(radius)).getArgs()[0]);

    assertEquals(
        Text.literal(Integer.toString(radius.range().orElseThrow().min())), range.getArgs()[0]);
  }
}
