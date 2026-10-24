package com.grahambartley.totalstorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NoCommentsRuleTest {

  private static final String CONFIG = "config/checkstyle/checkstyle.xml";
  private static final String FIXTURE = "/comment-gate/CommentGateFixture.java.txt";
  private static final String RULE_ID = "NoComments";
  private static final String STANDARD = "docs/standards.md";

  private static final List<Arguments> COMMENTS =
      List.of(
          Arguments.of(3, "javadoc on the type"),
          Arguments.of(6, "a plain line comment"),
          Arguments.of(19, "a trailing comment after code"),
          Arguments.of(21, "a bare double slash carrying no text"),
          Arguments.of(23, "a block comment"),
          Arguments.of(25, "an inline block comment after code"));

  private static final List<Arguments> LITERALS =
      List.of(
          Arguments.of(7, "a url whose slashes sit inside a string"),
          Arguments.of(9, "block comment markers inside a string"),
          Arguments.of(11, "a double quote character literal"),
          Arguments.of(13, "an escaped quote followed by slashes inside a string"),
          Arguments.of(15, "the opening delimiter of a text block"),
          Arguments.of(16, "comment markers inside a text block"),
          Arguments.of(17, "the closing delimiter of a text block"));

  @TempDir static Path workspace;

  private static List<AuditEvent> violations;

  static Stream<Arguments> comments() {
    return COMMENTS.stream();
  }

  static Stream<Arguments> literals() {
    return LITERALS.stream();
  }

  @BeforeAll
  static void auditTheFixture() throws Exception {
    File config = new File(CONFIG);
    assertTrue(config.isFile(), CONFIG + " is missing; the gate has no configuration to enforce");
    violations = audit(config, writeFixtureToDisk());
  }

  @ParameterizedTest(name = "line {0}, {1}")
  @MethodSource("comments")
  void everyCommentFormIsReported(int line, String form) {
    assertTrue(
        reportedLines().contains(line),
        "line " + line + " is " + form + ", and the gate failed to report it");
  }

  @ParameterizedTest(name = "line {0}, {1}")
  @MethodSource("literals")
  void noStringLiteralIsMistakenForAComment(int line, String form) {
    assertFalse(
        reportedLines().contains(line),
        "line " + line + " is " + form + ", and the gate wrongly reported it as a comment");
  }

  @Test
  void theGateReportsEveryCommentAndNothingElse() {
    assertEquals(
        COMMENTS.size(),
        violations.size(),
        "expected exactly "
            + COMMENTS.size()
            + " violations, got "
            + violations.size()
            + " at lines "
            + reportedLines());
  }

  @Test
  void everyViolationCarriesTheRuleIdSoCiOutputIsTraceable() {
    assertFalse(violations.isEmpty(), "no violations were produced, so the id proves nothing");
    assertTrue(
        violations.stream().allMatch(event -> RULE_ID.equals(event.getModuleId())),
        "a violation reached the build without the " + RULE_ID + " id");
  }

  @Test
  void everyViolationNamesTheStandardItEnforces() {
    assertFalse(violations.isEmpty(), "no violations were produced, so the message proves nothing");
    assertTrue(
        violations.stream().allMatch(event -> event.getMessage().contains(STANDARD)),
        "a violation reached the build without pointing at " + STANDARD);
  }

  private static Set<Integer> reportedLines() {
    return violations.stream().map(AuditEvent::getLine).collect(Collectors.toSet());
  }

  private static File writeFixtureToDisk() throws IOException {
    Path target = workspace.resolve("CommentGateFixture.java");
    try (InputStream source = NoCommentsRuleTest.class.getResourceAsStream(FIXTURE)) {
      assertNotNull(source, FIXTURE + " is missing from the test resources");
      Files.copy(source, target);
    }
    return target.toFile();
  }

  private static List<AuditEvent> audit(File config, File subject) throws Exception {
    Configuration configuration =
        ConfigurationLoader.loadConfiguration(
            config.getPath(), new PropertiesExpander(System.getProperties()));
    Checker checker = new Checker();
    List<AuditEvent> collected = new ArrayList<>();
    try {
      checker.setModuleClassLoader(Checker.class.getClassLoader());
      checker.configure(configuration);
      checker.addListener(new Collector(collected));
      checker.process(List.of(subject));
    } finally {
      checker.destroy();
    }
    return collected;
  }

  private record Collector(List<AuditEvent> collected) implements AuditListener {

    @Override
    public void addError(AuditEvent event) {
      collected.add(event);
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {
      throw new IllegalStateException("checkstyle failed to audit the fixture", throwable);
    }

    @Override
    public void auditStarted(AuditEvent event) {}

    @Override
    public void auditFinished(AuditEvent event) {}

    @Override
    public void fileStarted(AuditEvent event) {}

    @Override
    public void fileFinished(AuditEvent event) {}
  }
}
