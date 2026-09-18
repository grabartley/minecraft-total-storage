package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TagSetFileTest {
  private static final NetworkTagSet CASTLE =
      new NetworkTagSet(List.of(NetworkTag.custom("castle", "Castle", Set.of("minecraft:stone"))));

  @Test
  void aTagSetSurvivesBeingWrittenAndReadBack(@TempDir Path folder) {
    final Path file = folder.resolve("sets").resolve("base.json");

    assertTrue(TagSetFile.save(file, CASTLE));

    final NetworkTagSet loaded = new NetworkTagSet(TagSetFile.load(file).orElseThrow().tags());
    assertEquals(1, loaded.custom().size());
    assertEquals("Castle", loaded.byId("castle").orElseThrow().displayName());
    assertTrue(loaded.byId("castle").orElseThrow().contains("minecraft:stone"));
  }

  @Test
  void onlyCustomTagsAreWrittenBecausePresetsAreWorkedOut(@TempDir Path folder) throws IOException {
    final Path file = folder.resolve("base.json");
    TagSetFile.save(file, CASTLE);

    final String written = Files.readString(file, StandardCharsets.UTF_8);

    assertFalse(written.contains("preset"));
  }

  @Test
  void aFileThatIsNotThereImportsNothing(@TempDir Path folder) {
    assertTrue(TagSetFile.load(folder.resolve("missing.json")).isEmpty());
  }

  @Test
  void aMalformedFileImportsNothingRatherThanHalfASet(@TempDir Path folder) throws IOException {
    final Path file = folder.resolve("broken.json");
    Files.writeString(file, "{ not json", StandardCharsets.UTF_8);

    assertTrue(TagSetFile.load(file).isEmpty());
  }

  @Test
  void aFileWithoutATagSetInItImportsNothing(@TempDir Path folder) throws IOException {
    final Path file = folder.resolve("other.json");
    Files.writeString(file, "{}", StandardCharsets.UTF_8);

    assertTrue(TagSetFile.load(file).isEmpty());
  }

  @Test
  void nothingIsWrittenWhenThereIsNowhereToWriteIt() {
    assertFalse(TagSetFile.save(null, CASTLE));
    assertFalse(TagSetFile.save(Path.of("ignored.json"), null));
  }
}
