package com.grahambartley.totalstorage.command;

import com.grahambartley.totalstorage.config.ConfigSetting;
import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.RoleTally;
import com.grahambartley.totalstorage.terminal.TerminalRadius;
import com.grahambartley.totalstorage.text.CountedNoun;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class CommandMessages {
  private static final String PREFIX = "command.total-storage.";

  private CommandMessages() {}

  public static Text helpHeader() {
    return line("help.header", "Total Storage commands:");
  }

  public static Text helpConfig() {
    return line(
        "help.config",
        "/ts config list | get <setting> | set <setting> <value> | reset <setting> | reset all");
  }

  public static Text helpEligibility() {
    return line(
        "help.eligibility",
        "/ts config eligibility list | set <block> <true|false> | clear <block> | clear all");
  }

  public static Text helpTerminal() {
    return line(
        "help.terminal", "/ts terminal <pos> radius [blocks] reads or sets one terminal's reach");
  }

  public static Text helpTerminalAccess() {
    return line(
        "help.terminal.access",
        "/ts terminal <pos> access [owner|trusted|open] reads or sets who may use it");
  }

  public static Text helpTerminalTrust() {
    return line(
        "help.terminal.trust",
        "/ts terminal <pos> trust | trust <players> | untrust <players> edits its trust list");
  }

  public static Text helpSort() {
    return line(
        "help.sort",
        "/ts sort sorts the container you have open or are looking at, or else your inventory");
  }

  public static Text helpQuickStack() {
    return line(
        "help.quickstack", "/ts quickstack tops up matching stacks in the containers around you");
  }

  public static Text quickStackDisabled() {
    return line("quickstack.disabled", "Quick stack is switched off on this server");
  }

  public static Text alreadySorted() {
    return line("sort.unchanged", "That was already sorted");
  }

  public static Text configHeader() {
    return line("config.header", "Total Storage settings:");
  }

  public static Text settingLine(ConfigSetting setting, TotalStorageConfig config) {
    return line(
        "config.entry",
        "%s = %s (default %s)",
        setting.name(),
        setting.valueIn(config),
        setting.defaultValue());
  }

  public static Text settingValue(ConfigSetting setting, TotalStorageConfig config) {
    return line("config.value", "%s = %s", setting.name(), setting.valueIn(config));
  }

  public static Text settingSet(ConfigSetting setting, String value) {
    return line("config.set", "Set %s to %s", setting.name(), value);
  }

  public static Text settingReset(ConfigSetting setting, String value) {
    return line("config.reset", "Reset %s to its default of %s", setting.name(), value);
  }

  public static Text everySettingReset() {
    return line("config.reset.all", "Reset every Total Storage setting to its default");
  }

  public static Text unknownSetting(String name, String known) {
    return line("config.unknown", "Unknown setting '%s'. Settings are: %s", name, known);
  }

  public static Text invalidValue(ConfigSetting setting, String value) {
    return line(
        "config.invalid",
        "'%s' is not a value %s takes. Expected %s",
        value,
        setting.name(),
        setting.allowedValues());
  }

  public static Text settingsRefused() {
    return line(
        "config.refused", "These are this server's settings, and only an operator can change them");
  }

  public static Text updateFailed() {
    return line(
        "config.failed", "Could not save the Total Storage config, see the server log for why");
  }

  public static Text eligibilityHeader() {
    return line("eligibility.header", "Container eligibility overrides:");
  }

  public static Text eligibilityEmpty() {
    return line(
        "eligibility.empty", "No container eligibility overrides are set, so the rules decide");
  }

  public static Text eligibilityLine(String blockId, boolean eligible) {
    return line(
        "eligibility.entry", "%s is forced %s", blockId, eligible ? "eligible" : "ineligible");
  }

  public static Text eligibilitySet(String blockId, boolean eligible) {
    return line("eligibility.set", "Forced %s %s", blockId, eligible ? "eligible" : "ineligible");
  }

  public static Text eligibilityCleared(String blockId) {
    return line("eligibility.cleared", "Stopped forcing eligibility for %s", blockId);
  }

  public static Text eligibilityUnusableBlock(String blockId) {
    return line("eligibility.unusable", "'%s' does not name a block", blockId);
  }

  public static Text eligibilityNotOverridden(String blockId) {
    return line("eligibility.missing", "Nothing was forcing eligibility for %s", blockId);
  }

  public static Text everyEligibilityCleared() {
    return line("eligibility.cleared.all", "Cleared every container eligibility override");
  }

  public static Text noTerminalAt(BlockPos pos) {
    return line("terminal.missing", "There is no storage terminal at %s", at(pos));
  }

  public static Text terminalRadius(BlockPos pos, TerminalRadius radius) {
    return line(
        "terminal.radius", "The terminal at %s has a radius of %s", at(pos), radius.blocks());
  }

  public static Text terminalRadiusHeldDown(BlockPos pos, TerminalRadius radius) {
    return line(
        "terminal.radius.held",
        "The terminal at %s is set to a radius of %s and reaches %s, the server maximum",
        at(pos),
        radius.requested(),
        radius.blocks());
  }

  public static Text terminalRadiusSet(BlockPos pos, TerminalRadius radius) {
    return line(
        "terminal.radius.set",
        "Set the terminal at %s to a radius of %s",
        at(pos),
        radius.blocks());
  }

  public static Text terminalRadiusClamped(BlockPos pos, TerminalRadius radius) {
    return line(
        "terminal.radius.clamped",
        "A radius of %s is above the server maximum, so the terminal at %s was set to %s",
        radius.requested(),
        at(pos),
        radius.blocks());
  }

  public static Text terminalAccessMode(BlockPos pos, TerminalAccessMode mode) {
    return line("terminal.access", "The terminal at %s is set to %s access", at(pos), mode.id());
  }

  public static Text terminalAccessModeSet(BlockPos pos, TerminalAccessMode mode) {
    return line("terminal.access.set", "Set the terminal at %s to %s access", at(pos), mode.id());
  }

  public static Text terminalAccessModeUnchanged(BlockPos pos, TerminalAccessMode mode) {
    return line(
        "terminal.access.unchanged",
        "The terminal at %s was already set to %s access",
        at(pos),
        mode.id());
  }

  public static Text unknownAccessMode(String value, String known) {
    return line(
        "terminal.access.unknown", "'%s' is not an access mode. Modes are: %s", value, known);
  }

  public static Text terminalNotYours(BlockPos pos) {
    return line("terminal.denied", "The terminal at %s is not yours to configure", at(pos));
  }

  public static Text terminalTrustHeader(BlockPos pos) {
    return line("terminal.trust.header", "The terminal at %s trusts:", at(pos));
  }

  public static Text terminalTrustEmpty(BlockPos pos) {
    return line("terminal.trust.empty", "The terminal at %s trusts nobody but its owner", at(pos));
  }

  public static Text terminalTrustEntry(String player) {
    return line("terminal.trust.entry", "- %s", player);
  }

  public static Text terminalTrusted(String player, BlockPos pos) {
    return line("terminal.trust.added", "%s can now use the terminal at %s", player, at(pos));
  }

  public static Text terminalAlreadyTrusted(String player, BlockPos pos) {
    return line("terminal.trust.kept", "%s could already use the terminal at %s", player, at(pos));
  }

  public static Text terminalUntrusted(String player, BlockPos pos) {
    return line(
        "terminal.trust.removed", "%s can no longer use the terminal at %s", player, at(pos));
  }

  public static Text terminalNotTrusted(String player, BlockPos pos) {
    return line(
        "terminal.trust.missing",
        "%s was not on the trust list for the terminal at %s",
        player,
        at(pos));
  }

  public static Text sortButtonDisabled() {
    return line("sort.button-disabled", "The sort button is switched off on this server");
  }

  public static Text sorted() {
    return line("sort.sorted", "Sorted");
  }

  public static Text inventorySorted() {
    return line("sort.inventory.sorted", "Sorted your inventory, as nothing in view can be sorted");
  }

  public static Text inventoryAlreadySorted() {
    return line(
        "sort.inventory.unchanged",
        "Your inventory was already sorted, and nothing in view can be sorted");
  }

  public static Text inventoryNothingToSort() {
    return line(
        "sort.inventory.empty",
        "Your inventory has nothing to sort, and nothing in view can be sorted");
  }

  public static Text notOpenableToSort() {
    return line("sort.unopenable", "You could not open that, so it was not sorted");
  }

  public static Text nothingToSort() {
    return line("sort.empty", "There is nothing in there to sort");
  }

  public static Text sortRefused() {
    return line("sort.refused", "That is not something this mod will sort");
  }

  public static Text sortTooFast() {
    return line("sort.cooldown", "Give the last sort a moment before asking again");
  }

  public static Text sortFailed() {
    return line("sort.failed", "The sort was rolled back and nothing moved");
  }

  public static Text helpTerminalInfo() {
    return line(
        "help.terminal.info",
        "/ts terminal <pos> info summarises one terminal, and rescan makes it look again");
  }

  public static Text helpTerminalRole() {
    return line(
        "help.terminal.role",
        "/ts terminal <pos> role <container> [general|filtered|excluded] reads or sets what the"
            + " network may do to a container");
  }

  public static Text helpTerminalName() {
    return line(
        "help.terminal.name",
        "/ts terminal <pos> name <container> [set <name>|clear] reads or sets what a container is"
            + " called");
  }

  public static Text helpTerminalFilter() {
    return line(
        "help.terminal.filter",
        "/ts terminal <pos> filter <container> [add|remove <tag>|clear] reads or edits what a"
            + " Filtered container accepts");
  }

  public static Text helpTerminalHighlight() {
    return line(
        "help.terminal.highlight",
        "/ts terminal <pos> highlight outlines the containers it is holding");
  }

  public static Text terminalInfoHeader(BlockPos pos) {
    return line("terminal.info.header", "The terminal at %s:", at(pos));
  }

  public static Text terminalInfoPower(boolean powered) {
    return powered
        ? line("terminal.info.powered", "- Powered, so its network is running")
        : line("terminal.info.unpowered", "- Unpowered, so it is holding nothing");
  }

  public static Text terminalInfoRadius(TerminalRadius radius) {
    return radius.clamped()
        ? line(
            "terminal.info.radius.held",
            "- Set to a radius of %s, reaching %s under the server maximum",
            radius.requested(),
            radius.blocks())
        : line("terminal.info.radius", "- Radius %s", radius.blocks());
  }

  public static Text terminalInfoAccess(TerminalAccessMode mode) {
    return line("terminal.info.access", "- %s access", mode.id());
  }

  public static Text terminalInfoWithheld() {
    return line(
        "terminal.info.withheld", "- what it is holding is only for players who may open it");
  }

  public static Text terminalInfoContainers(RoleTally tally) {
    return line(
        "terminal.info.containers",
        "- %s: %s general, %s filtered, %s excluded",
        CountedNoun.CONTAINERS.of(tally.total()),
        tally.general(),
        tally.filtered(),
        tally.excluded());
  }

  public static Text containerRole(BlockPos container, ContainerRole role) {
    return line("container.role", "The container at %s is %s", at(container), role.id());
  }

  public static Text containerRoleSet(BlockPos container, ContainerRole role) {
    return line("container.role.set", "Made the container at %s %s", at(container), role.id());
  }

  public static Text containerRoleUnchanged(BlockPos container, ContainerRole role) {
    return line(
        "container.role.unchanged", "The container at %s was already %s", at(container), role.id());
  }

  public static Text containerNotReached(BlockPos terminal, BlockPos container) {
    return line(
        "container.role.unreachable",
        "The terminal at %s does not reach the container at %s",
        at(terminal),
        at(container));
  }

  public static Text unknownRole(String requested, String roles) {
    return line("container.role.unknown", "'%s' is not a role. Roles are: %s", requested, roles);
  }

  public static Text terminalRescanned(BlockPos pos, int containers) {
    return line(
        "terminal.rescanned",
        "The terminal at %s scanned its region again and found %s",
        at(pos),
        CountedNoun.CONTAINERS.of(containers));
  }

  public static Text terminalUnpowered(BlockPos pos) {
    return line(
        "terminal.unpowered",
        "The terminal at %s has no power, so it has no region to walk",
        at(pos));
  }

  public static Text terminalHighlighted(BlockPos pos, int containers) {
    return line(
        "terminal.highlighted",
        "Showing the %s the terminal at %s holds",
        CountedNoun.CONTAINERS.of(containers),
        at(pos));
  }

  public static Text highlightNeedsAPlayer() {
    return line("terminal.highlight.console", "A highlight has to be shown to a player");
  }

  private static String at(BlockPos pos) {
    return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
  }

  private static Text line(String key, String english, Object... arguments) {
    return Text.translatableWithFallback(PREFIX + key, english, arguments);
  }
}
