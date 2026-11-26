package com.grahambartley.totalstorage.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.totalstorage.screen.ConfigScreen;
import com.grahambartley.totalstorage.screen.ConfigScreenText;
import java.util.stream.Stream;
import net.minecraft.client.gui.screen.Screen;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TotalStorageModMenuTest {
  static Stream<Arguments> openers() {
    return Stream.of(Arguments.of((Screen) null), Arguments.of(new ConfigScreen(null)));
  }

  @ParameterizedTest
  @MethodSource("openers")
  void modMenuIsHandedTheSettingsScreenWithItsTitleAndOpener(Screen opener) {
    final Screen built = new TotalStorageModMenu().getModConfigScreenFactory().create(opener);

    assertEquals(ConfigScreen.class, built.getClass());
    assertEquals(ConfigScreenText.title().getString(), built.getTitle().getString());
    assertSame(opener, ((ConfigScreen) built).parent());
  }
}
