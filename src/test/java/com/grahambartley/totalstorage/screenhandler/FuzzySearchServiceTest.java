package com.grahambartley.totalstorage.screenhandler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FuzzySearchServiceTest {
  private final FuzzySearchService service =
      new FuzzySearchService(new TerminalListingService(), new TerminalTagPanelService());

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void noPlayerHasNothingToChoose(boolean wanted) {
    assertFalse(service.choose(null, wanted));
  }

  @Test
  void theServiceNeedsSomewhereToSendTheNewResults() {
    assertThrows(
        NullPointerException.class,
        () -> new FuzzySearchService(null, new TerminalTagPanelService()));
    assertThrows(
        NullPointerException.class,
        () -> new FuzzySearchService(new TerminalListingService(), null));
  }
}
