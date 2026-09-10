package com.grahambartley.toomanychests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NoCommentsRuleTest {

  private static final String CONFIG = "config/checkstyle/checkstyle.xml";
  private static final String FIXTURE = "/comment-gate/CommentGateFixture.java.txt";
  private static final int EXPECTED_VIOLATIONS = 6;

  private static List<AuditEvent> violations;

  @BeforeAll
  static void auditTheFixture() throws Exception {
    File config = new File(CONFIG);
    assertTrue(config.isFile(), CONFIG + " is missing; the gate has no configuration to enforce");
    violations = audit(config, writeFixtureToDisk());
  }

  @ParameterizedTest
  @ValueSource(ints = {3, 6, 19, 21, 23, 25})
  void everyCommentFormIsReported(int line) {
    assertTrue(
        reportedLines().contains(line),
        "line " + line + " carries a comment the gate failed to report");
  }

  @ParameterizedTest
  @ValueSource(ints = {7, 9, 11, 13, 15, 16, 17})
  void noStringLiteralIsMistakenForAComment(int line) {
    assertFalse(
        reportedLines().contains(line),
        "line " + line + " is a string literal the gate wrongly reported as a comment");
  }

  @Test
  void theGateReportsEveryCommentAndNothingElse() {
    assertEquals(
        EXPECTED_VIOLATIONS,
        violations.size(),
        "expected exactly " + EXPECTED_VIOLATIONS + " violations, got " + reportedLines());
  }

  @Test
  void everyViolationCarriesTheRuleIdSoCiOutputIsTraceable() {
    assertTrue(
        violations.stream().allMatch(event -> "NoComments".equals(event.getModuleId())),
        "a violation reached the build without the NoComments id");
  }

  private static Set<Integer> reportedLines() {
    return violations.stream().map(AuditEvent::getLine).collect(Collectors.toSet());
  }

  private static File writeFixtureToDisk() throws IOException {
    Path target = Files.createTempDirectory("comment-gate").resolve("CommentGateFixture.java");
    try (InputStream source = NoCommentsRuleTest.class.getResourceAsStream(FIXTURE)) {
      assertTrue(source != null, FIXTURE + " is missing from the test resources");
      Files.copy(source, target);
    }
    target.toFile().deleteOnExit();
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
