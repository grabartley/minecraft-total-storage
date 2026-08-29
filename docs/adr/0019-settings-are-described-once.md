# ADR 0019: Settings are described once, not wired twice

- **Status:** Accepted
- **Date:** 2026-08-29

## Context

The shared standards require that every option is reachable from an OP-gated command line, because a server owner over SSH with no client mod installed has no other way in. A settings screen is a convenience on top, never the only door.

The canonical implementation in Dogs Unleashed spells the command tree out by hand: a literal Brigadier node per setting, a handler per setting, a help line per setting, and a status line per setting. Adding a setting means editing the record, the serialiser, the command tree, the help text, the status text, and later the screen. Six edits, and the compiler asks for exactly one of them.

Nothing fails when the other five are forgotten. The setting persists and loads correctly; it is simply unreachable from a console, which is precisely the failure the standard exists to prevent, and it is invisible until an operator goes looking for a setting nobody wired up.

## Decision

Settings are described once, in `ConfigSettings`, and the command tree reads that description rather than restating it.

A `ConfigSetting` knows its name, how to read its value out of a configuration, its default, the values worth suggesting, what it will accept in words, and how to produce a configuration with it moved. `BoundedIntConfigSetting`, `BooleanConfigSetting`, and `EnumConfigSetting` cover every shape the record currently holds.

The command tree is therefore four nodes rather than one per setting: `list`, `get <setting>`, `set <setting> <value>`, and `reset <setting> | all`. Tab completion for both the setting name and its value reads the same list. `ConfigBuilder` exists so a description can move one field without restating the other ten.

Container eligibility overrides stay outside the list. They are not one value but a set keyed by block, so they get their own subtree under `config eligibility` rather than a setting whose name changes with the world's block list.

Values typed at a command are refused, naming the range, rather than clamped. Values read from a file are still clamped, because a hand edit must never stop a server starting, while an operator at a console is present to be told they got it wrong.

## Consequences

A setting added to the record and left out of `ConfigSettings` is absent from `list`, from `get`, from `set`, from tab completion, and from any future screen, all at once. That is a loud failure rather than a quiet one, and `ConfigSettingsTest` catches it by resetting every described setting on a fixture with every field moved and asserting the result is the defaults.

Accepted drawback: values arrive as strings and each setting parses its own. A per-setting Brigadier node would let `IntegerArgumentType.integer(min, max)` reject an out-of-range number during parsing, with the range shown in the client's red preview as it is typed. Here the number parses and the handler refuses it, so the operator finds out on submit rather than while typing. The trade is worth it: the error names the range either way, and the alternative is the six-edit problem above.

Accepted drawback: a setting whose value is not a word, such as one taking a sentence or a list, does not fit `set <setting> <value>` as written. Nothing in the record needs that today, and a setting that did would earn its own subtree the way eligibility overrides did.

Accepted drawback: this diverges from Dogs Unleashed, which the standards name as the reference implementation. The standards also say that when the document and the code disagree the code is probably right; this is a case where the newer code is. The divergence is worth carrying rather than hand-wiring a tree to match.
