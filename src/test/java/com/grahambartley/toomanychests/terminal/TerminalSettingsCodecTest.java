package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TerminalSettingsCodecTest {
  private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
  private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
  private static final BlockPos BARREL = new BlockPos(3, 64, -7);
  private static final BlockPos CHEST = new BlockPos(-11, 12, 40);

  private static List<TerminalSettings> roundTripCases() {
    return List.of(
        TerminalSettings.defaults(),
        TerminalSettings.defaults().withRadius(1),
        TerminalSettings.defaults().withRadius(64),
        TerminalSettings.defaults().withOwner(ALICE),
        TerminalSettings.defaults().withAccessMode(TerminalAccessMode.OPEN),
        TerminalSettings.defaults().withTrusted(ALICE).withTrusted(BOB),
        TerminalSettings.defaults()
            .withContainerRole(BARREL, ContainerRole.EXCLUDED)
            .withContainerRole(CHEST, ContainerRole.FILTERED),
        TerminalSettings.defaults().withTag("ores").withTag("wood"),
        TerminalSettings.defaults()
            .withRadius(12)
            .withAccessMode(TerminalAccessMode.TRUSTED)
            .withOwner(ALICE)
            .withTrusted(BOB)
            .withContainerRole(BARREL, ContainerRole.EXCLUDED)
            .withTag("ores"));
  }

  @ParameterizedTest
  @MethodSource("roundTripCases")
  void settingsSurviveEncodingAndDecoding(TerminalSettings original) {
    assertEquals(original, decode(encode(original)));
  }

  @Test
  void twoEquallyConfiguredTerminalsEncodeIdenticallyWhateverOrderBuiltThem() {
    TerminalSettings forwards =
        TerminalSettings.defaults()
            .withTrusted(ALICE)
            .withTrusted(BOB)
            .withTag("wood")
            .withTag("ores");
    TerminalSettings backwards =
        TerminalSettings.defaults()
            .withTag("ores")
            .withTag("wood")
            .withTrusted(BOB)
            .withTrusted(ALICE);

    assertEquals(encode(forwards).toString(), encode(backwards).toString());
  }

  @Test
  void anEmptySaveDecodesToAFreshTerminal() {
    assertEquals(TerminalSettings.defaults(), decode(new JsonObject()));
  }

  @Test
  void aSavedRadiusOutsideTheAllowedRangeIsClampedOnRead() {
    JsonObject tampered = encode(TerminalSettings.defaults()).getAsJsonObject();
    tampered.addProperty("radius", 9000);

    assertEquals(TerminalSettings.MAX_RADIUS, decode(tampered).radius());
  }

  @Test
  void anUnrecognisedAccessModeIsReadAsOwnerRatherThanLosingTheTerminal() {
    JsonObject tampered = encode(TerminalSettings.defaults().withRadius(9)).getAsJsonObject();
    tampered.addProperty("accessMode", "everybody");

    TerminalSettings decoded = decode(tampered);

    assertEquals(TerminalAccessMode.OWNER, decoded.accessMode());
    assertEquals(9, decoded.radius());
  }

  @Test
  void anUnrecognisedContainerRoleIsReadAsTheDefaultAndForgotten() {
    JsonObject tampered =
        encode(TerminalSettings.defaults().withContainerRole(BARREL, ContainerRole.EXCLUDED))
            .getAsJsonObject();
    tampered
        .getAsJsonArray("containerRoles")
        .get(0)
        .getAsJsonObject()
        .addProperty("role", "readonly");

    assertTrue(decode(tampered).containerRoles().isEmpty());
  }

  @Test
  void aBlankSavedTagIsDroppedOnRead() {
    JsonObject tampered = encode(TerminalSettings.defaults().withTag("ores")).getAsJsonObject();
    tampered.getAsJsonArray("tags").add("   ");

    assertEquals(java.util.Set.of("ores"), decode(tampered).tags());
  }

  private static JsonElement encode(TerminalSettings settings) {
    return TerminalSettingsCodec.CODEC.encodeStart(JsonOps.INSTANCE, settings).getOrThrow();
  }

  private static TerminalSettings decode(JsonElement encoded) {
    return TerminalSettingsCodec.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
  }
}
