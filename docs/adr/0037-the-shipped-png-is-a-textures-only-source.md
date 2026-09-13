# ADR 0037: The shipped PNG is a texture's only source

- **Status:** Accepted
- **Date:** 2026-09-13

## Context

[ADR 0017](0017-terminal-gui-art-ships-as-atlas-sprites.md) settled how GUI art ships, and settled something else alongside it: every texture was committed twice, once as the PNG the game loads and once as a palette-mapped text file under `art/`, one character per pixel, rendered by `tools/gui_sprite.py`. The argument was that an art change should review as a diff showing which pixels moved rather than as "Binary file changed".

The argument is sound. The implementation had no way to stay honest, and 0017 said so in the record itself: "the repository holds the same art twice. The two can drift if someone edits a PNG directly ... nothing enforces it." No gradle task, test, or CI step ever compared a source to the PNG beside it.

By the time this was revisited the drift had already happened. The mod shipped 37 texture PNGs against 36 text sources: `textures/gui/sprites/icon/copy_tags.png` had been added with no source at all, and nothing anywhere reported it. The other 36 pairs did still reproduce their PNG byte for byte, which is the worst possible reading of the situation: the discipline held everywhere it was watched, and the one place nobody watched is the one that went.

Neither copy is what ships. `art/` had zero entries in the mod jar and zero in the sources jar, and nothing in `build.gradle` or the CI workflow read it.

## Decision

**A texture has exactly one committed form: the PNG under `assets/` that the game loads. `art/` is deleted.**

The choice was between enforcing the second copy and removing it. Enforcing it means a build step that re-renders every source and fails on a mismatch, which is a real gate and would have caught `copy_tags`. It also permanently taxes every texture change with a second file that must be kept in step, to protect a review experience rather than a runtime behaviour. The mod's textures are small, few, and rarely edited; there is not enough traffic here to pay for a gate.

Removing the copy costs the readable diff. That is the thing being traded away, knowingly, and it is recoverable per change: the authoring workflow is unchanged, since `tools/gui_sprite.py` takes its input path as an argument and renders a scratch file straight into place. A reviewer who wants to see which pixels moved can be shown the source in the pull request, where it is evidence attached to one change rather than a permanent second source of truth.

`tools/gui_sprite.py` stays. It is the renderer and the previewer, and its `zoom`, `sheet`, `slice` and `scales` modes are how a sprite is checked at GUI scale without launching the game.

## Consequences

There is one answer to "what does this texture look like", and it is the file the game reads. A PNG edited directly is now simply the texture, rather than a silent contradiction of a text file that still claims otherwise.

Nothing about the shipped artefact changes. The mod jar and the sources jar carry the same entries before and after, because they never carried `art/` at all.

Accepted drawback: a texture change reviews as "Binary file changed" unless its author attaches the source or a before-and-after image to the pull request. That is a habit, and habits are exactly what the previous arrangement proved cannot be relied on. The difference is that failing to attach evidence is visible in the review, whereas a stale text source was invisible by construction.

Accepted drawback: the palettes in [`../terminal-gui-sprites.md`](../terminal-gui-sprites.md) and [`../terminal-block-art.md`](../terminal-block-art.md) are no longer mechanically present in any source file. For the GUI sprites this costs little, because `TerminalSpriteTest` reads every shipped sprite and fails on a pixel that is not in the palette, so the constraint is enforced against the PNG rather than against a description of it. The block textures have no equivalent test, and their palette is documentation only.
