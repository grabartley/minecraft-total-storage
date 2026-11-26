package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.screen.ConfigScreenAccess.Notice;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConfigScreenTextTest {
  private static final String PREFIX = "screen.total-storage.config.";

  private static TranslatableTextContent contentOf(Text text) {
    return (TranslatableTextContent) text.getContent();
  }

  @Test
  void theTitleNamesTheModWithoutSayingServer() {
    final Text title = ConfigScreenText.title();

    assertEquals(PREFIX + "title", contentOf(title).getKey());
    assertEquals("Total Storage settings", title.getString());
  }

  @ParameterizedTest
  @CsvSource({"NOT_CONNECTED, not-connected", "READ_ONLY, read-only", "SAVE_FAILED, save-failed"})
  void everyNoticeHasItsOwnLine(Notice notice, String key) {
    assertEquals(PREFIX + key, contentOf(ConfigScreenText.notice(notice).orElseThrow()).getKey());
  }

  @Test
  void aScreenWithNothingToExplainDrawsNoNotice() {
    assertTrue(ConfigScreenText.notice(Notice.NONE).isEmpty());
  }

  @ParameterizedTest
  @CsvSource({"true", "false"})
  void theDefaultLineCarriesTheValueItIsGiven(boolean on) {
    final Text value = ScreenTexts.onOrOff(on);
    final TranslatableTextContent line = contentOf(ConfigScreenText.defaultValue(value));

    assertEquals(PREFIX + "default", line.getKey());
    assertEquals(value, line.getArgs()[0]);
  }
}
