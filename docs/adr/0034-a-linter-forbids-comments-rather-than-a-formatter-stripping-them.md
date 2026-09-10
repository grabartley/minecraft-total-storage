# ADR 0034: A linter forbids comments rather than a formatter stripping them

- **Status:** Accepted
- **Date:** 2026-09-10

## Context

[`../standards.md`](../standards.md) has always said the source carries no comments at all, not explanatory ones, not javadoc, not "why" ones. Until now that rule was prose. Nothing checked it, and it held only because everyone writing here happened to remember it.

An audit of all 545 Java files across the four source sets found the rule already perfectly kept: not one `//` and not one `/*` anywhere. That is the good case and it is exactly the case worth protecting, because a rule kept by memory alone fails silently the first time someone new, or something automated, does not share the memory.

Spotless already runs on every build, so it was the obvious place to put the enforcement. It turns out to be the wrong one. Spotless has no assertion primitive: every step is a rewriter, and `spotlessCheck` means nothing more than "run the rewriter and diff against disk". Its only comment-aware steps point the other way, `licenseHeader` inserting a comment and `googleJavaFormat().formatJavadoc()` preserving and reflowing javadoc. Enforcing through Spotless therefore requires a custom step that deletes comments from source files, which needs a hand-written literal-aware lexer, because `"https://example.com"`, `'"'` and `"/* not a comment */"` all defeat naive matching.

That lexer is the problem. When it is right it silently rewrites your file, which is pleasant. When it is wrong it silently eats a line of code inside a branch nobody tests, and the result still compiles, still passes the suite, and reaches review as a deletion buried in a diff nobody reads line by line.

## Decision

Checkstyle fails the build on any comment. It never edits source.

[`../../config/checkstyle/checkstyle.xml`](../../config/checkstyle/checkstyle.xml) carries one rule, a `MatchXpath` query over the parsed comment nodes:

```xml
<property name="query" value="//SINGLE_LINE_COMMENT | //BLOCK_COMMENT_BEGIN"/>
```

The query runs against Checkstyle's real Java AST, so a string literal is never mistaken for a comment and no regular expression is involved anywhere. `SINGLE_LINE_COMMENT` covers `//`, and `BLOCK_COMMENT_BEGIN` covers both `/* */` and `/** */`, so javadoc is not a special case and gets no exemption.

The Gradle `checkstyle` plugin attaches a task per source set, and `check` picks all four up on its own. CI already runs `./gradlew check`, so no workflow change was needed.

`MatchXpath` was chosen over the built-in `TodoComment` with a catch-all format, which was tried first and rejected. Checkstyle hands `TodoComment` the content `"\n"` for a bare `//`, and `"\n".split("\n")` in Java returns an empty array, so an empty comment matches nothing and passes. `MatchXpath` sees the node itself and does not care what is inside it.

## The Gate Tests Itself

The failure mode worth guarding against is a gate that silently stops gating: a mistyped query matches nothing, reports zero violations, and reads exactly like a clean tree. A verification procedure that lives in a document and relies on someone remembering to run it is the same class of failure this record exists to remove.

`NoCommentsRuleTest` runs Checkstyle against the real `config/checkstyle/checkstyle.xml`, over a fixture at `src/test/resources/comment-gate/CommentGateFixture.java.txt`. The fixture is a resource rather than a source file so the gate cannot audit its own test data.

It asserts that each of the six comment forms is reported, that none of the seven literal lines is, and that the total is exactly six, so a query matching too much fails as loudly as one matching too little. The fixture covers a text block holding `//`, `/*` and `*/`, which is the case a regular expression or a hand-written lexer gets wrong.

`config/checkstyle/checkstyle.xml` is declared an input of the `test` task. Without that, editing the query leaves the test up to date and Gradle skips it, which is precisely the silent failure being guarded against.

The test proves the **query**, not the **wiring**. Repointing `checkstyle.configFile` at another file, setting `ignoreFailures`, or adding a source set nobody attaches a task to all leave the suite green while the gate stops covering the code. The query is the part that fails silently and subtly; the wiring fails visibly, in the build file, where review catches it.

## Consequences

A comment cannot reach `main`. It fails locally on `./gradlew check` and in CI, at `file:line:column`, tagged `[NoComments]`, with the violation message naming the standard and pointing at this directory for where reasoning belongs instead.

The drawbacks accepted:

- **The build fails instead of fixing itself.** There is no `spotlessApply` equivalent that removes the comment for you. Someone who writes one has to delete it themselves. This is deliberate: the moment a tool is allowed to rewrite source unattended, a bug in it becomes a silent code deletion, and that trade was the whole reason for choosing a linter.
- **Consumers of the published sources jar get no hover documentation.** The build publishes sources via `withSourcesJar()`, and a reader outside this repository has no javadoc and no access to these records unless they find the repository. The API surface is small and the names are long, which is the bet being made.
- **A genuinely surprising piece of code has nowhere in-file to explain itself.** That pressure is intended. It is meant to push the explanation into a record here, or push the code into a shape that does not need one, and it will occasionally be the wrong answer for a truly obscure workaround.
- **The unit tests resolve a newer Guava than the mod runs against.** Checkstyle on the test classpath pulls Guava 33.2.0-jre, lifting Minecraft's 32.1.2-jre for `testRuntimeClasspath` only. It is test-scoped and never published, and the logic under test is pure functions over plain data by the standard in `../standards.md`, so the exposure is small. It is a real difference between what the tests exercise and what ships, and the cost of a self-test that runs the same Checkstyle the gate does.
- **A second tool now runs on every build.** Checkstyle is configured with exactly one rule and is not a general style gate. Growing it into one is a separate decision, and it should be resisted while Spotless and `googleJavaFormat` own formatting.
- **A comment-only file with no type declaration is not caught.** A `.java` file whose entire content is `// orphan`, with no `package` statement and no type, audits clean. Anything carrying a `package` line is caught, `package-info.java` javadoc included, so this is unreachable in a real source tree, but it is a hole rather than a guarantee.
- **The wider family is in a worse state than this record can fix.** `docs/standards.md` is nominally shared across the five mods, and has in practice diverged into four copies, two of them absent. Dogs Unleashed and Loot Lock carry no `docs/standards.md` at all, and hold comments in 158 of 364 and 43 of 119 Java files respectively. More Arrows and Teleport Effects state the opposite rule, that comments explain why when the reason is genuinely surprising, while Teleport Effects holds comments in 158 of 225 files and More Arrows in none. More Doors states a no-comments rule and keeps it. Bringing the other four into line, and reconciling the document itself, is separate work and is not attempted here.
