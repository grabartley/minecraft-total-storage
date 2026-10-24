package com.grahambartley.totalstorage.filter;

import java.util.List;
import java.util.Set;

final class TestCandidates {
  static final FilterCandidate COBBLESTONE =
      new FilterCandidate(
          "minecraft:cobblestone", "Cobblestone", Set.of("building_blocks", "stone"));
  static final FilterCandidate DARK_OAK_CHEST =
      new FilterCandidate("minecraft:dark_oak_chest", "Dark Oak Chest", Set.of("wood", "storage"));
  static final FilterCandidate IRON_PICKAXE =
      new FilterCandidate("minecraft:iron_pickaxe", "Iron Pickaxe", Set.of("tools"));
  static final FilterCandidate COOKED_BEEF =
      new FilterCandidate("minecraft:cooked_beef", "Steak", Set.of("food", "mob_drops"));
  static final FilterCandidate COGWHEEL =
      new FilterCandidate("create:cogwheel", "Cogwheel", Set.of("redstone", "wood"));
  static final FilterCandidate CREATE_WRENCH =
      new FilterCandidate("create:wrench", "Wrench", Set.of("tools"));
  static final FilterCandidate UNTAGGED =
      new FilterCandidate("some_mod:mystery_widget", "Mystery Widget", Set.of());

  private TestCandidates() {}

  static List<FilterCandidate> all() {
    return List.of(
        COBBLESTONE, DARK_OAK_CHEST, IRON_PICKAXE, COOKED_BEEF, COGWHEEL, CREATE_WRENCH, UNTAGGED);
  }
}
