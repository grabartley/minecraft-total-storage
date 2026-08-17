---
name: automated-qa
description: Programmatically drive the game client with a temporary in-process QA driver, capture framebuffer screenshots of the feature under test, verify them, and attach the evidence to the PR. Use for any change with a visible or interactive surface BEFORE handing off to manual QA.
---

# Automated QA

Drive the real game client programmatically, capture framebuffer screenshots of the feature under
test, verify them, and attach the evidence to the PR. Use this after implementing any change with a
visible or interactive surface (screens, HUD, rendering, in-world interactions) BEFORE handing off
to manual QA. Manual QA then confirms feel and edge cases instead of discovering basics.

Why in-process instead of OS automation: macOS input automation (System Events, cliclick) needs
accessibility permissions the agent shell usually lacks, and OS-level clicks are brittle against
window focus and Retina scaling. A temporary in-process driver has full deterministic control over
the client, needs no OS permissions, and captures pixel-exact framebuffer screenshots.

## The Temp Driver Pattern

All driver code is TEMPORARY and must never be committed. It exists only in the worktree during QA.

1. Create `src/client/java/com/grahambartley/toomanychests/<Feature>QaDriver.java` from
   `templates/QaDriver.java` in this skill directory. It is a tick-driven state machine registered
   on `ClientTickEvents.END_CLIENT_TICK`.
2. Register it with one line at the end of `TooManyChestsClient.onInitializeClient()`:
   `<Feature>QaDriver.register();`
3. After QA passes, revert both:
   `git checkout -- src/client/java/com/grahambartley/toomanychests/TooManyChestsClient.java`
   `rm src/client/java/com/grahambartley/toomanychests/<Feature>QaDriver.java`

## Capabilities Toolbox

- **World loading**: from the title screen call
  `client.createIntegratedServerLoader().start("New World", () -> {})` once
  `client.currentScreen instanceof TitleScreen` and ~60 ticks have passed (resources settled).
  Do NOT bother with loom `programArgs "--quickPlaySingleplayer", ...` — it is not picked up.
  The dev `run/saves/New World` world exists in every worktree because the `worktree` skill copies
  `run/`.
- **Server-side setup**: `client.getServer().execute(() -> ...)` reaches the integrated server.
  Spawn entities, set NBT/DataTracker state, tame to
  `server.getPlayerManager().getPlayerList().get(0)`, and trigger S2C packets exactly as
  production code would (e.g. calling the same `ModNetworking.send*` used by gameplay). This tests
  the real network round trip, not a mock.
- **Clicks and keys**: call `client.currentScreen.mouseClicked(sx, sy, 0)` / `keyPressed(...)`
  directly with SCALED screen coordinates. No cursor movement needed; this drives the same code
  path as a real click and real C2S packets flow.
- **Hover states**: hover rendering reads the real OS cursor, so move it with
  `GLFW.glfwSetCursorPos` plus the iterative settle loop in the template. Never trust a single
  set call: GLFW cursor space vs framebuffer size differs per display (Retina), so converge with
  the multiplicative feedback loop until `client.mouse` derives to the target scaled position.
- **Screenshots**: `ScreenshotRecorder.saveScreenshot(client.runDirectory, name + ".png",
  client.getFramebuffer(), text -> {})` writes to `run/screenshots/`. Log the derived mouse
  position alongside each shot so a failed hover is diagnosable from the log.
- **GUI scale sweeps**: `client.setScreen(null); client.options.getGuiScale().setValue(n);
  client.onResolutionChanged();` then re-trigger the screen. Capture at least scales 1, 2, and 4
  for anything with custom rendering.
- **Lifecycle**: print `[QA]`-prefixed markers for every step, a final `[QA] DONE`, catch every
  exception into `[QA] ERROR`, and end with `client.scheduleStop()` so the run terminates itself.

## Environment Prep (once per worktree)

- `run/options.txt`: set `pauseOnLostFocus:false` (MANDATORY — the client runs unfocused in the
  background and singleplayer would otherwise sit on the pause screen, which also blocks any
  screen-open packet handler that checks `currentScreen == null`). Pin `guiScale` to a known value
  so the first screenshots are deterministic.
- These are `run/` files, untracked; no cleanup needed beyond not committing them.
- Leave `recipe_viewers` off (the default). JEI and EMI draw full-height sidebars and a search bar
  over every screen, which lands in the captured framebuffer and makes the evidence unreadable.
  Only pass `-Precipe_viewers=true` when the recipe viewer integration is itself under test.

## Run Protocol

1. **Absolute paths only.** Every command runs with an explicit
   `cd /path/to/.claude/worktrees/too-many-chests-<branch> && ...`. The classic failure mode is the
   shell cwd silently resetting to the main checkout, which launches the OLD mod without the
   driver and "does nothing". If a run produces zero `[QA]` log lines, check cwd first. The tell is
   in the log itself: the Gradle `problems-report` path at the end names the checkout that actually
   built, so if it points at the main checkout rather than the worktree, that run proved nothing.
2. Compile and PROVE the driver is in the build before launching:
   `./gradlew compileClientJava` then confirm
   `ls build/classes/java/client/.../<Feature>QaDriver.class` exists and
   `javap -c -classpath build/classes/java/client com.grahambartley.toomanychests.TooManyChestsClient | grep -c QaDriver`
   returns non-zero. Do not launch until both check out.
3. Launch in the background with output to a log file:
   `./gradlew runClient > /tmp/qa-run.log 2>&1` (background).
4. Wait on the sentinels, never on time:
   `until grep -qE '\[QA\] (DONE|ERROR)|BUILD FAILED' /tmp/qa-run.log; do sleep 3; done`
5. Read the `[QA]` log lines, then Read every captured PNG and actually LOOK at it: sector
   alignment, text legibility, state highlights, scale behaviour. A green log with wrong pixels is
   a failed QA.

## The Dev World Is Mutable State, And A Dead Player Poisons It

`run/saves/New World` persists between runs. A driver that kills the player writes that death into
the save, and **every later run in that worktree inherits it**. This is the single most expensive
failure mode in this skill, because it does not look like a harness problem.

**Symptom.** The world renders normally, screenshots look fine, and inventory edits show up in the
hotbar, so the setup appears to have worked. But server-side, `getPlayerList().get(0)` returns a
player with `isRemoved() == true` and `world.getPlayers()` is empty. `TameableEntity.getOwner()`
resolves through `world.getPlayerByUuid(...)`, so it returns null, every `isOwner(player)` check
returns false, and interactions return `PASS` while consuming nothing. That is indistinguishable
from a genuine bug in the feature under test.

**Cause.** The saved player is at zero health. The driver's setup calls
`player.changeGameMode(GameMode.SURVIVAL)`, the player dies again on the very next tick, and the
entity is removed from the world while `PlayerManager` keeps handing out the dead reference.

**Diagnose it first, before touching feature code.** Log the player state at the moment of the
interaction, not just the outcome:

```java
System.out.println("[QA] isOwner=" + dog.isOwner(player)
    + " playerRemoved=" + player.isRemoved()
    + " worldPlayers=" + player.getServerWorld().getPlayers().size());
```

`playerRemoved=true` or `worldPlayers=0` means the harness is broken, not the feature.

**Fix.** Restore a clean save into the worktree, then re-run:

```bash
rm -rf <worktree>/run/saves/"New World"
cp -a <main-checkout>/run/saves/"New World" <worktree>/run/saves/
```

**Avoid causing it.** Two rules:

- A heightmap query against an ungenerated chunk answers with the world bottom. Any teleport that
  derives its Y from `world.getTopY(...)` must force generation first, or it drops the player into
  the void and corrupts the save:
  ```java
  world.getChunk(x >> 4, z >> 4);              // generate before querying
  final int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
  ```
- Restore the player in setup regardless: `setHealth(getMaxHealth())`, food to 20, `setAir`,
  `clearStatusEffects()`, `setFireTicks(0)`. It is three lines and it makes the run idempotent.

Prefer `player.requestTeleport(x, y, z)` for moving the player within one world. And do not trust
terrain for framing: flatten a small stage of grass with air above it so the shot composes the same
way on any seed, rather than discovering the camera is buried in a dirt cliff.

**The dev world is also populated, and its residents will do your test for you.** It already holds
tamed, registered dogs, so any driver that resolves "the player's pets" picks one of those rather
than the one it just spawned. Worse, a freshly spawned tamed dog runs `FollowOwnerGoal`, which
teleports it to its owner past twelve blocks: a recall test that waits a couple of seconds before
acting watches the dog arrive on its own and calls that a pass. Give any dog you spawn for a test
`setAiDisabled(true)`, and point the feature at it explicitly by id instead of letting it resolve.

## Publishing Evidence to the PR

GitHub's `user-attachments` uploads are not available via `gh`, so the evidence lives on the
dedicated orphan `images` branch, stored by PR number. NEVER commit screenshots to the PR branch
or main; binary evidence must not enter main's history.

NEVER force-push or delete the `images` branch: every PR description across the repo hot-links its
evidence from this branch by raw URL, so rewriting its history breaks images on every past PR at
once. Always add on top with normal commits; to replace a PR's evidence, overwrite the files in its
`pr-<number>/` directory in a new commit, which only ever affects that one PR.

1. Downscale the keeper screenshots: `sips -Z 1000 run/screenshots/qa_*.png --out <staging-dir>/`
2. Check out the `images` branch in a temporary worktree
   (`git worktree add <tmp-path> images`; if the branch does not exist yet, create it orphan with
   `git worktree add --orphan -b images <tmp-path>`), copy the screenshots into `pr-<number>/`,
   commit with `--no-verify` (the spotless pre-commit hook needs `./gradlew`, which does not exist
   on the codeless orphan branch), push `origin images`, then `git worktree remove` the temp path.
3. Embed them in the PR body via raw URLs:
   `https://raw.githubusercontent.com/grabartley/minecraft-too-many-chests/images/pr-<number>/<name>.png`
   placed next to the paragraph each illustrates, then `gh pr edit <num> --body-file ...`.
4. If a later run replaces the screenshots, overwrite the same file names in `pr-<number>/` and
   push again; the raw URLs track the images branch head, so the body only needs editing when the
   prose changes.

## Checklist Before Handoff to Manual QA

- [ ] Driver ran to `[QA] DONE` with zero `[QA] ERROR`
- [ ] Server-side interactions actually took effect; a `PASS` result with nothing consumed means
      checking `player.isRemoved()` before suspecting the feature
- [ ] Every screenshot visually verified by reading the PNG, at multiple GUI scales for rendering
- [ ] Server-side state assertions logged and correct (e.g. command/DataTracker values after a click)
- [ ] Temp driver + initializer hook reverted; `git status` shows only intended files
- [ ] Evidence pushed to the `images` branch under `pr-<number>/` and embedded in the PR body;
      nothing image-related committed to the PR branch

## Related Skills

- `build` — requires this skill before manual QA handoff
- `run-game-client` — plain manual launch, used when a human is driving
- `worktree` — provides the isolated `run/` directory this skill relies on
- `item-sprite`, authoring a flat item sprite; this skill verifies the result in the client
