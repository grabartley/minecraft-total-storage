package com.grahambartley.toomanychests.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class CountedNounTest {
  private static final String PREFIX = "count.too-many-chests.";

  private static TranslatableTextContent contentOf(Text text) {
    return (TranslatableTextContent) text.getContent();
  }

  @ParameterizedTest
  @EnumSource(CountedNoun.class)
  void oneOfAnythingIsNamedUnderItsOwnKeyAndCarriesNoNumber(CountedNoun noun) {
    TranslatableTextContent one = contentOf(noun.of(1L));

    assertEquals(PREFIX + noun.id() + ".one", one.getKey());
    assertEquals(0, one.getArgs().length);
  }

  @ParameterizedTest
  @EnumSource(CountedNoun.class)
  void aCountOfNoneReadsAsThePluralRatherThanTheSingular(CountedNoun noun) {
    assertEquals(PREFIX + noun.id(), contentOf(noun.of(0L)).getKey());
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 2L, 9L, 108L, 1_000_000L})
  void anyCountButOneCarriesItsNumberIntoThePluralKey(long count) {
    TranslatableTextContent many = contentOf(CountedNoun.ITEMS.of(count));

    assertEquals(PREFIX + "items", many.getKey());
    assertEquals(count, many.getArgs()[0]);
  }

  @ParameterizedTest
  @EnumSource(CountedNoun.class)
  void thePluralAndTheSingularAreNeverTheSameKey(CountedNoun noun) {
    assertNotEquals(contentOf(noun.of(1L)).getKey(), contentOf(noun.of(2L)).getKey());
  }

  @Test
  void aFormattedAmountIsShownInsteadOfTheRawNumberWhenOneIsGiven() {
    TranslatableTextContent many = contentOf(CountedNoun.ITEMS.of("12.3k", 12_300L));

    assertEquals(PREFIX + "items", many.getKey());
    assertEquals("12.3k", many.getArgs()[0]);
  }

  @Test
  void aFormattedAmountOfOneStillReadsAsTheSingular() {
    assertEquals(PREFIX + "items.one", contentOf(CountedNoun.ITEMS.of("1", 1L)).getKey());
  }

  @Test
  void anAmountHasToBeGivenWhenOneIsAskedFor() {
    assertThrows(NullPointerException.class, () -> CountedNoun.ITEMS.of(null, 4L));
  }

  @ParameterizedTest
  @CsvSource({
    "ITEMS, items",
    "KINDS, kinds",
    "STACKS, stacks",
    "SLOTS, slots",
    "CONTAINERS, containers",
    "NEARBY_CONTAINERS, nearby-containers",
    "TAGS, tags",
    "CUSTOM_TAGS, custom-tags",
    "BLOCKS, blocks",
    "CHARACTERS, characters"
  })
  void everyNounKeepsTheKeyItsTranslationsAreFiledUnder(CountedNoun noun, String id) {
    assertEquals(id, noun.id());
  }

  @ParameterizedTest
  @EnumSource(CountedNoun.class)
  void noTwoNounsShareAKey(CountedNoun noun) {
    for (final CountedNoun other : CountedNoun.values()) {
      assertTrue(other == noun || !other.id().equals(noun.id()), other + " shares " + noun);
    }
  }
}
