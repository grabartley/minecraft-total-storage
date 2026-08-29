package com.grahambartley.toomanychests.compat;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TooManyChestsModMenuTest {

  @Test
  @DisplayName("Mod Menu is offered no config screen yet, so it shows no configure button")
  void offersNoConfigScreenYet() {
    assertNull(new TooManyChestsModMenu().getModConfigScreenFactory().create(null));
  }
}
