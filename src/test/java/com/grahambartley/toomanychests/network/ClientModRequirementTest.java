package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;

class ClientModRequirementTest {
  @Test
  void aClientWithTheModIsLetIn() {
    assertTrue(ClientModRequirement.refusalFor(true).isEmpty());
  }

  @Test
  void aClientWithoutTheModIsToldWhyItCannotJoin() {
    Text refusal = ClientModRequirement.refusalFor(false).orElseThrow();

    assertEquals(ClientModRequirement.refusal(), refusal);
  }

  @Test
  void theRefusalCarriesPlainEnglishForAClientThatCannotTranslateIt() {
    TranslatableTextContent content =
        (TranslatableTextContent) ClientModRequirement.refusal().getContent();

    assertEquals("multiplayer.too-many-chests.client_mod_required", content.getKey());
    assertTrue(content.getFallback().contains("Too Many Chests"));
  }
}
