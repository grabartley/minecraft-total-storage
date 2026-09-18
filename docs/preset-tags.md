# Preset Tag Membership

The thirteen preset tags are derived from vanilla and Fabric convention item tags, in
`src/main/resources/data/too-many-chests/tags/item/preset/`. `PresetTagResolver` asks the registry
which of them an item is in, and `PresetTagRules` falls back to Miscellaneous when none of them
claim it, so TAG-2 holds by construction: every item resolves to at least one preset tag.

The derivation is data, not code, which makes it easy to change and easy to get wrong in ways only a
player notices. This page records what the memberships are meant to promise, so a future change can
be checked against the intent rather than against whatever the file happened to say.

## The Promise Each Name Makes

| Tag | Holds | Does not hold |
| --- | --- | --- |
| Tools | Things you use on the world, and the utility blocks you use them at: pickaxes, shears, buckets, the crafting table, the furnace, the anvil, the enchanting table | Weapons, armour, shields, writing materials |
| Weapons and Armour | Anything you fight with or wear, including shields, bows, arrows and horse armour | Mining tools |
| Ores and Minerals | Ores, raw materials, ingots, nuggets, gems, dusts, and the mineral storage blocks made from them | Storage blocks made from anything that is not a mineral |
| Building Blocks | Blocks you build with, including the non-mineral storage blocks such as bone block, hay bale and dried kelp | Ores |
| Brewing | Potions, bottles, the brewing stand, and every brewing ingredient | |
| Redstone | Components, wiring and the blocks that respond to a signal | |
| Food | Anything carrying the `FOOD` data component, plus the tagged edibles | |
| Wood, Stone, Nature, Decoration, Mob Drops | As their names read | |
| Miscellaneous | The fallback. Anything no other preset claims | |

## Memberships Deliberately Corrected

These were found in the second pre-release playtest and fixed, with the reason recorded so they are
not re-added by someone reading the convention tags alone.

| Item | Was | Now | Why |
| --- | --- | --- | --- |
| Paper | Tools | Miscellaneous | A sheet of paper is not a tool. It was reaching Tools only because Tools listed it directly. Searching `#tools` and getting twenty-two sheets of paper is the result a player remembers. |
| Book, Writable Book, Written Book, Enchanted Book, Knowledge Book | Tools | Miscellaneous | Same reason as paper, which is what they are made of. |
| Glass Bottle | Tools and Brewing | Brewing | It already had a home that says what it is for. |
| Shield | Tools and Weapons and Armour | Weapons and Armour | Tools pulled in the `#c:tools` umbrella, which convention defines as including `#c:tools/shield`, `#c:tools/melee_weapon`, `#c:tools/bow` and the rest. The umbrella is gone and the genuinely tool-shaped subtags are listed individually, so a modded pickaxe still lands in Tools and a modded sword does not. |
| Bone Block | Ores and Minerals and Building Blocks | Building Blocks | Ores pulled in the `#c:storage_blocks` umbrella, which convention defines as including bone block, hay bale, dried kelp and slime. The umbrella is gone and the mineral storage blocks are listed as `#c:storage_blocks/<mineral>`, so a modded ingot block still lands in Ores and a modded crop block does not. |

## Memberships That Look Surprising And Are Correct

- **Bucket and Bowl are in Tools.** Both are containers you use on the world rather than things you
consume, and a bucket is the only way to move a liquid.
- **Fire Charge is in both Tools and Weapons and Armour.** It lights a portal and it is thrown at
things. Multi-membership is the model working, not a defect.
- **End Crystal, Respawn Anchor and Lodestone are in Tools.** Tools is this mod's home for utility
blocks as well as handheld tools, which is why the crafting table and the anvil are there.
- **Turtle Helmet is in both Brewing and Weapons and Armour.** It is armour that grants an effect,
and it is a brewing ingredient.

## Changing It

Adding to a preset tag means adding to its JSON file. Prefer a convention tag over a list of item
ids where one exists and says what you mean, and prefer a narrow convention subtag over an umbrella:
an umbrella tag is defined by somebody else and will grow in directions this mod did not choose. If
an umbrella is genuinely right, say so in the table above so the next reader does not re-open it.
