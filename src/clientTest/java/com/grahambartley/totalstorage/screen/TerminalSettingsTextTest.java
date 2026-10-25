package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TerminalSettingsTextTest {
  private static final String PREFIX = "screen.total-storage.terminal.";

  private static NetworkSettingsView view(
      int radius,
      int effectiveRadius,
      TerminalAccessMode mode,
      boolean mayAdminister,
      List<String> trusted) {
    return NetworkSettingsView.answered(
        radius,
        effectiveRadius,
        64,
        mode,
        mayAdminister,
        true,
        trusted,
        NetworkCapacity.EMPTY,
        0,
        0,
        List.of());
  }

  private static NetworkSettingsView unanswered(
      TerminalAccessMode mode, int radius, int effectiveRadius) {
    return new NetworkSettingsView(
        false,
        radius,
        effectiveRadius,
        64,
        mode,
        true,
        true,
        List.of(),
        NetworkCapacity.EMPTY,
        0,
        0,
        List.of());
  }

  private static String keyOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] argumentsOf(Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }

  static Stream<Arguments> trustedSummaries() {
    return Stream.of(
        Arguments.of("an unanswered view", NetworkSettingsView.pending(), "settings.pending"),
        Arguments.of(
            "a player who may not administer",
            view(16, 16, TerminalAccessMode.TRUSTED, false, List.of("Alex")),
            "settings.trusted.hidden.short"),
        Arguments.of(
            "an empty trusted list",
            view(16, 16, TerminalAccessMode.TRUSTED, true, List.of()),
            "settings.trusted.nobody"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("trustedSummaries")
  void theTrustedSummaryExplainsWhyNoNamesAreShown(
      String label, NetworkSettingsView view, String key) {
    assertEquals(PREFIX + key, keyOf(TerminalSettingsText.trustedSummary(view)));
  }

  @Test
  void theTrustedSummaryListsTheTrustedPlayersByName() {
    final Text summary =
        TerminalSettingsText.trustedSummary(
            view(16, 16, TerminalAccessMode.TRUSTED, true, List.of("Alex", "Steve")));

    assertInstanceOf(PlainTextContent.class, summary.getContent());
    assertEquals("Alex, Steve", summary.getString());
  }

  static Stream<Arguments> descriptions() {
    return Stream.of(
        Arguments.of(
            "access in any mode",
            TerminalSetting.ACCESS,
            view(16, 16, TerminalAccessMode.TRUSTED, true, List.of()),
            "settings.access.description"),
        Arguments.of(
            "trusted while the network is trusted only",
            TerminalSetting.TRUSTED,
            view(16, 16, TerminalAccessMode.TRUSTED, true, List.of()),
            "settings.trusted.description.active"),
        Arguments.of(
            "trusted while the network is open",
            TerminalSetting.TRUSTED,
            view(16, 16, TerminalAccessMode.OPEN, true, List.of()),
            "settings.trusted.description"),
        Arguments.of(
            "trusted before the server answers",
            TerminalSetting.TRUSTED,
            unanswered(TerminalAccessMode.TRUSTED, 16, 16),
            "settings.trusted.description"),
        Arguments.of(
            "a radius within the server cap",
            TerminalSetting.RADIUS,
            view(16, 16, TerminalAccessMode.OWNER, true, List.of()),
            "settings.radius.description"),
        Arguments.of(
            "a capped radius before the server answers",
            TerminalSetting.RADIUS,
            unanswered(TerminalAccessMode.OWNER, 32, 8),
            "settings.radius.description"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("descriptions")
  void eachSettingIsDescribedForTheCurrentState(
      String label, TerminalSetting setting, NetworkSettingsView view, String key) {
    assertEquals(PREFIX + key, keyOf(TerminalSettingsText.description(setting, view)));
  }

  @Test
  void aRadiusCappedByTheServerSaysHowFarItReallyReaches() {
    final Text description =
        TerminalSettingsText.description(
            TerminalSetting.RADIUS, view(32, 8, TerminalAccessMode.OWNER, true, List.of()));

    assertEquals(PREFIX + "settings.radius.description.capped", keyOf(description));
    assertArrayEquals(new Object[] {8}, argumentsOf(description));
  }

  @Test
  void theRadiusIsADashBeforeTheServerAnswers() {
    assertEquals("-", TerminalSettingsText.radiusValue(NetworkSettingsView.pending()).getString());
  }

  @ParameterizedTest(name = "radius {0} reaching {1}")
  @MethodSource("radiusValues")
  void theRadiusValueShowsHowFarItReachesOutOfTheMaximum(
      int radius, int effectiveRadius, int shown) {
    final Text value =
        TerminalSettingsText.radiusValue(
            view(radius, effectiveRadius, TerminalAccessMode.OWNER, true, List.of()));

    assertEquals(PREFIX + "settings.radius.value", keyOf(value));
    assertArrayEquals(new Object[] {shown, 64}, argumentsOf(value));
  }

  static Stream<Arguments> radiusValues() {
    return Stream.of(Arguments.of(16, 16, 16), Arguments.of(32, 8, 8));
  }
}
