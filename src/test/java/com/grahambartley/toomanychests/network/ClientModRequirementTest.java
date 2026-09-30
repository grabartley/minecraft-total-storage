package com.grahambartley.toomanychests.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;

class ClientModRequirementTest {
  @Test
  void aClientWithTheModIsLetIn() {
    assertTrue(ClientModRequirement.refusalFor(true).isEmpty());
  }

  @Test
  void aClientWithoutTheModIsToldInPlainEnglishWhyItCannotJoin() {
    TranslatableTextContent content =
        (TranslatableTextContent) ClientModRequirement.refusalFor(false).orElseThrow().getContent();

    assertEquals("multiplayer.too-many-chests.client_mod_required", content.getKey());
    assertEquals(
        "This server runs Too Many Chests. Install the same mod on your game to join.",
        content.getFallback());
  }
}
