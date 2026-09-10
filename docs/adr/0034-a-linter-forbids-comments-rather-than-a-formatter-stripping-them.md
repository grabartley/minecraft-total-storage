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

## Verifying The Gate

The failure mode worth guarding against is a gate that silently stops gating: a mistyped query matches nothing, reports zero violations, and reads exactly like a clean tree. Nothing in the build detects that, so re-prove the gate by hand after any edit to the query.

Drop this into any source set and run `./gradlew checkstyleMain`. It must report exactly six violations, on lines 3, 6, 15, 17, 19 and 21, and none on the four string literals:

```java
package com.grahambartley.toomanychests;

/** Javadoc on the type. */
public final class CommentGateFixture {

	// a plain line comment
	private static final String URL = "https://example.com/a//b";

	private static final String STAR = "/* not a comment */";

	private static final char QUOTE = '"';

	private static final String ESCAPED = "a \" // still a string";

	private static final int TRAILING = 1; // a trailing comment

	//

	/* a block comment */
	public int value() {
		return 42; /* an inline block */
	}
}
```

A run that reports five rather than six is the `TodoComment` failure above: the bare `//` on line 17 is the one that gets missed.

## Consequences

A comment cannot reach `main`. It fails locally on `./gradlew check` and in CI, at `file:line:column`, tagged `[NoComments]`, with the violation message naming the standard and pointing at this directory for where reasoning belongs instead.

The drawbacks accepted:

- **The build fails instead of fixing itself.** There is no `spotlessApply` equivalent that removes the comment for you. Someone who writes one has to delete it themselves. This is deliberate: the moment a tool is allowed to rewrite source unattended, a bug in it becomes a silent code deletion, and that trade was the whole reason for choosing a linter.
- **Consumers of the published sources jar get no hover documentation.** The build publishes sources via `withSourcesJar()`, and a reader outside this repository has no javadoc and no access to these records unless they find the repository. The API surface is small and the names are long, which is the bet being made.
- **A genuinely surprising piece of code has nowhere in-file to explain itself.** That pressure is intended. It is meant to push the explanation into a record here, or push the code into a shape that does not need one, and it will occasionally be the wrong answer for a truly obscure workaround.
- **A second tool now runs on every build.** Checkstyle is configured with exactly one rule and is not a general style gate. Growing it into one is a separate decision, and it should be resisted while Spotless and `googleJavaFormat` own formatting.
- **Only this repository is covered.** The standard is shared across the five mods in the family, and the other four still hold the rule by memory alone.
