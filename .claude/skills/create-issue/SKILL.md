---
name: create-issue
description: Create GitHub issues for too-many-chests and link them to the project board. Use when asked to create, draft, scope, or file an issue in this repo.
---

# Create Issue

Use this skill when the user wants a new GitHub issue created for `grabartley/minecraft-too-many-chests`.

## Defaults

1. Leave every new issue unassigned unless the user gives an assignee.
2. Add every new issue to project `Minecraft Too Many Chests` (project 1).
3. Set the project status from `Board Status`, not to a fixed value.
4. Keep issue titles brief and concise.
5. Write issue bodies so the work can be implemented from the issue alone.
6. Treat every issue as public. Do not expose local file paths, private notes, or machine-specific details.
7. Apply the asset labels described in `Asset Labels`.
8. Attach the issue to an `[Epic]` parent when it is part of a larger effort, per `Epics And Sub-Issues`.
9. Never set a milestone by hand. See `Milestones Are Automated`.

## Public Repo Rules

- Never mention local paths like `~/Downloads/...`, `/Users/...`, or other workstation-specific locations.
- Never mention that a file existed only on a local machine, in a temp folder, or in private notes.
- If design artifacts or reference docs exist outside the repo, refer to them by stable artifact name only, and summarize the needed details directly in the issue body.
- Do not rely on "see local mock" or "see attached file on disk" as the only implementation guidance.
- If an issue references behavior in code, include the relevant repo path and summarize the behavior in plain language.

## Research First

Before writing the issue body, decide whether the request is already well scoped.

Use the request directly when it already includes:
- Clear goal
- Exact scope
- Acceptance criteria
- Enough implementation detail to work from

Research the codebase first when any of these are missing:
- The concrete class, screen, command, packet, or subsystem is unclear
- The request mentions a bug but not the root area of code
- The issue needs file paths, current behavior, or technical constraints
- The issue should call out risks, dependencies, or out-of-scope boundaries

Research tools to use:
- `glob` to find likely files
- `grep` to find symbols, strings, or call sites
- `read` to inspect the exact implementation and behavior
- `gh issue list --state all --limit 200 --json number,title` to avoid duplicate issues when needed

Do the research before authoring the issue so the final description is self-contained.

## Duplicate Check

Before creating a new issue, always check whether one already exists for the same work.

Minimum check:

```bash
gh issue list --repo grabartley/minecraft-too-many-chests --state all --limit 200 --json number,title
```

If the request is broad, ambiguous, or likely to overlap with earlier work, also search by keyword in issue titles and bodies before creating anything new.

Do not create a duplicate issue when an open or closed issue already covers the same scope closely enough. Instead:
- return the existing issue URL
- explain the overlap briefly
- only create a new issue if the user still wants a separate tracking artifact

## Asset Labels

Art and code are worked by different people, so every issue that touches an asset must declare which side of that split it sits on. Apply exactly one of these, or neither.

| Label | Apply when | Example |
|---|---|---|
| `requires art` | The issue is implementation work that cannot ship until an art or audio asset it does **not** produce exists. | `[Build] Add Dog Whistle item` needs the whistle model and texture. |
| `art` | The issue **produces** the asset: a model, texture, animation, sprite, icon, or sound. | `[Art] Dog Whistle 3D model and texture` |

Rules:

- Never apply both to one issue. If a single issue would need both, it is two issues.
- Do not apply `requires art` when the asset already exists in the repo. Say so in `Out Of Scope` instead, for example "No new texture art, the PNG already exists".
- Do not apply `requires art` for UI drawn with `DrawContext` primitives. This repo's screens are drawn in code and `assets/too-many-chests/textures/gui/` does not exist, so a new screen is usually not blocked on art.
- Sound and music count as assets. A ticket that wires up bark audio someone else must record or source gets `requires art`.
- When you create a `requires art` issue and its art counterpart does not exist yet, follow the art dependency workflow: propose the art scope to Graham first and only create the `[Art]` issue assigned to `vitoriavedanaa` once he agrees.

Verify the labels exist before using them:

```bash
gh label list --repo grabartley/minecraft-too-many-chests
```

## Board Status

Status answers one question: can someone pick this up today?

| Status | Apply when |
|---|---|
| `Backlog` | The issue is blocked on an art or audio asset that does not exist yet. In practice this is every issue carrying `requires art`. |
| `Ready` | Nothing blocks it. This includes `[Art]` issues, because the artist can start immediately, and implementation issues that need no asset. |

Rules:

- Do not park a `requires art` issue in `Ready`. A column that mixes startable work with work waiting on an asset stops being a queue.
- When the blocking `[Art]` issue closes, move its `requires art` counterpart from `Backlog` to `Ready`.
- An explicit status from the user always wins over this rule.
- `[Epic]` parents get `Backlog` on creation and move to `In progress` once any child is being worked.

## Epics And Sub-Issues

Feature-track grouping uses **native sub-issues under an `[Epic]` parent**, not milestones and not labels. GitHub allows one milestone per issue, which makes milestones a partition rather than a grouping, so they are reserved for releases.

Attaching to an epic is the default. Leaving an issue unparented is the exception, and needs a reason you would be willing to write down.

For every new issue:

1. Find the `[Epic]` parent it belongs to:
   ```bash
   gh issue list --repo grabartley/minecraft-too-many-chests --label epic --state open \
     --json number,title --jq '.[] | "#\(.number) \(.title)"'
   ```
2. If no existing epic fits and the work clearly spans several issues, create the `[Epic]` parent first: `epic` label, project status `Backlog`, and a body covering `## Goal`, `## Why These Ship Together`, and `## Sequencing`.
3. Attach the new issue as a sub-issue of that parent, using the child's database id, not its number:
   ```bash
   CHILD_ID=$(gh api repos/grabartley/minecraft-too-many-chests/issues/<child-number> --jq .id)
   gh api -X POST repos/grabartley/minecraft-too-many-chests/issues/<parent-number>/sub_issues \
     -F sub_issue_id="$CHILD_ID"
   ```
4. Verify the link, passing `per_page=100`:
   ```bash
   gh api "repos/grabartley/minecraft-too-many-chests/issues/<parent-number>/sub_issues?per_page=100" \
     --jq '.[] | "#\(.number) \(.title)"'
   ```
   The endpoint pages at 30 by default. On a long-running epic a child that linked successfully is absent from the default response, which reads as a failed link and invites a pointless retry.
5. Add the new issue to the epic body's `## Sequencing` section as well. The sub-issue link drives the GitHub UI; the body is what a human reads to understand ordering.

Leave an issue unparented only when it is genuinely standalone polish. A one-issue epic is noise, but so is a backlog of orphans that never surface in the planning view.

Epics pair a build issue with its art issue under the same parent, so the art queue and the work it blocks stay visible together.

## Milestones Are Automated

**Never pass `--milestone` when creating or editing an issue.**

A milestone means "this shipped in version X.Y.Z", nothing else. The release pipeline in `.github/workflows/cicd.yml` owns them end to end: on a `workflow_dispatch` release it creates the `vX.Y.Z` milestone, applies it to every issue closed by a pull request merged since the previous release, and closes it.

Consequences to respect:

- An open issue should have no milestone. If one does, it was set by hand and is wrong.
- Milestones are applied on release, not on merge. An issue merged to `main` stays unmilestoned until the next version ships.
- The issue is only picked up if a pull request closes it. Make sure the implementing PR uses a closing keyword such as `Closes #<number>`, which the `pr` skill already does.
- `[Epic]` parents usually never carry a milestone, because they are closed manually rather than by a pull request. That is correct and expected.

## Title Guidance

- Keep titles short.
- Match the repo's existing prefix style when appropriate.
- Prefer formats like:
- `[Build] <short feature or implementation target>`
- `[Bug] <short bug summary>`
- `[Docs] <short docs task>`
- `[Research] <short investigation target>`
- `[Art] <short asset deliverable>`
- `[Epic] <short feature track>`
- Do not cram acceptance criteria or implementation notes into the title.
- Prefer `[Epic]` over `[Tracking]` for umbrella issues. `[Tracking]` is legacy and appears only on older issues.

## Body Standards

Every issue body should be public-safe and detailed enough that someone can implement the work from the issue alone.

Include the sections that fit the task:
- `## Goal` or `## Context`
- `## Files` when repo locations are known and useful
- `## Current Behavior` when describing a bug or refactor target
- `## Target Behavior` or `## Scope`
- `## Specific Changes` or `## Tasks`
- `## Acceptance Criteria`
- `## Out Of Scope`

Body rules:
- Use repo-relative paths only, never local absolute paths.
- Summarize any external mock or design details directly in the issue.
- State defaults, ordering, labels, and behavioral constraints explicitly.
- Include enough acceptance criteria to verify the work.
- If implementation should preserve existing behavior in some area, say that explicitly.
- If the request is about code changes, include the likely file path or tell the implementer how to find it.

## Creation Workflow

1. Capture the request.
2. Check for an existing issue covering the same work.
3. Research the codebase if needed so the issue can stand on its own.
4. Draft a concise title.
5. Draft the body in markdown.
6. Write the body to a temp file to preserve formatting and avoid shell quoting problems.
7. Decide the asset label: `requires art`, `art`, or neither.
8. Create the issue with `gh issue create`, passing the asset label if one applies.
9. If the user provided an assignee, assign the issue.
10. Attach the issue to its `[Epic]` parent as a sub-issue, and verify the link with `per_page=100`.
11. Add the issue to project 1.
12. Set project status per `Board Status`: `Backlog` when blocked on art, `Ready` otherwise.
13. Return the issue URL, assignment state, status, label, and epic parent that were applied.

## GitHub Commands

Create the issue:

```bash
gh issue create \
--repo grabartley/minecraft-too-many-chests \
--title "<brief title>" \
--body-file .claude/tmp/issue-body.md \
--label "requires art"
```

Drop the `--label` flag when no asset label applies. Do not add `--milestone`; the release pipeline owns milestones.

If the user provided an assignee, either include it at creation time or assign it immediately after:

```bash
gh issue edit <issue-number> \
--repo grabartley/minecraft-too-many-chests \
--add-assignee <github-login>
```

Add it to the project:

```bash
gh project item-add 1 --owner grabartley --url <issue-url>
```

Fetch project field metadata when needed:

```bash
gh project field-list 1 --owner grabartley --format json
```

Current project metadata for fast use:
- Project id: `PVT_kwHOAQYbq84BOF-N`
- Status field id: `PVTSSF_lAHOAQYbq84BOF-Nzg85WjI`
- `Backlog` option id: `f75ad846`
- `Ready` option id: `61e4505c`
- `In progress` option id: `47fc9ee4`
- `QA Testing` option id: `df73e18b`
- `Done` option id: `98236657`

Look up the added project item for the issue, then set the status:

```bash
gh api graphql -f query='query($owner:String!, $repo:String!, $number:Int!) { repository(owner:$owner, name:$repo) { issue(number:$number) { projectItems(first:20) { nodes { id project { id } } } } } }' -f owner=grabartley -f repo=too-many-chests -F number=<issue-number>

gh project item-edit \
--id <project-item-id> \
--project-id PVT_kwHOAQYbq84BOF-N \
--field-id PVTSSF_lAHOAQYbq84BOF-Nzg85WjI \
--single-select-option-id 61e4505c
```

`61e4505c` is `Ready`. Swap in `f75ad846` for `Backlog` when the issue is blocked on art, per `Board Status`, or the matching option id when the user asked for a specific status.

## Final Checks

Before creating the issue, confirm:
- A duplicate check was completed.
- The title is concise.
- The description is public-safe.
- The body contains enough detail to implement the work without local context.
- Repo paths are relative and accurate.
- External references are summarized in the body.
- The asset label decision was made deliberately, including the decision to apply neither.

After creating the issue, confirm:
- The issue is unassigned unless the user requested an assignee.
- The issue was added to project `Minecraft Too Many Chests`.
- The project status matches `Board Status`: `Backlog` when blocked on art, `Ready` otherwise.
- The returned URL opens the created issue.
- At most one of `requires art` and `art` is applied.
- The issue is a sub-issue of an `[Epic]` parent, or is genuinely standalone.
- The sub-issue link was verified with `per_page=100`, not assumed from the POST succeeding.
- **No milestone is set.** Milestones are applied by the release pipeline on publish.

## Related Skills

- `build`, use this before implementation when build work does not already have a GitHub issue
- `pr`, use after implementation is complete and a linked issue already exists
