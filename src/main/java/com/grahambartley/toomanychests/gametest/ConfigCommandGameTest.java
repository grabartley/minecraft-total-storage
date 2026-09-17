package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.ConfigSettings;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.SortOrdering;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ConfigCommandGameTest implements FabricGameTest {
  private static final String BATCH = "server-config";
  private static final int TICK_LIMIT = 20;
  private static final int REFUSED_EXIT_CODE = -1;

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theConsoleCanMoveAWholeNumberSetting(TestContext context) {
    TooManyChestsConfig original = ServerConfigService.current();

    int result = runAsConsole(context, "toomanychests config set quickStackRadius 12");
    int live = ServerConfigService.current().quickStackRadius();
    restore(context, original);

    context.assertTrue(result > 0, "Setting quickStackRadius should report success");
    context.assertTrue(live == 12, "quickStackRadius should read 12 but read " + live);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theConsoleCanMoveATrueOrFalseSetting(TestContext context) {
    TooManyChestsConfig original = ServerConfigService.current();

    int result = runAsConsole(context, "toomanychests config set sortButtonEnabled false");
    boolean live = ServerConfigService.current().sortButtonEnabled();
    restore(context, original);

    context.assertTrue(result > 0, "Setting sortButtonEnabled should report success");
    context.assertTrue(!live, "sortButtonEnabled should read false");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theConsoleCanMoveASettingChosenFromAlternatives(TestContext context) {
    TooManyChestsConfig original = ServerConfigService.current();

    int result = runAsConsole(context, "toomanychests config set sortOrdering display_name");
    SortOrdering live = ServerConfigService.current().sortOrdering();
    restore(context, original);

    context.assertTrue(result > 0, "Setting sortOrdering should report success");
    context.assertTrue(
        live == SortOrdering.DISPLAY_NAME,
        "sortOrdering should read display_name but read " + live);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aValueOutsideTheRangeIsRefusedRatherThanClamped(TestContext context) {
    TooManyChestsConfig original = ServerConfigService.current();

    int result = runAsConsole(context, "toomanychests config set maxTerminalRadius 9999");
    int live = ServerConfigService.current().maxTerminalRadius();
    restore(context, original);

    context.assertTrue(result == 0, "An out of range value should report failure");
    context.assertTrue(
        live == original.maxTerminalRadius(),
        "A refused value should leave maxTerminalRadius at " + original.maxTerminalRadius());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void resettingOneSettingRestoresItsDefault(TestContext context) {
    TooManyChestsConfig original = ServerConfigService.current();

    runAsConsole(context, "toomanychests config set quickStackRadius 3");
    int result = runAsConsole(context, "toomanychests config reset quickStackRadius");
    int live = ServerConfigService.current().quickStackRadius();
    restore(context, original);

    context.assertTrue(result > 0, "Resetting quickStackRadius should report success");
    context.assertTrue(
        live == TooManyChestsConfig.DEFAULT_QUICK_STACK_RADIUS,
        "quickStackRadius should be back at its default but read " + live);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void resettingEverythingRestoresEveryDefault(TestContext context) {
    TooManyChestsConfig original = ServerConfigService.current();

    runAsConsole(context, "toomanychests config set quickStackRadius 3");
    runAsConsole(context, "toomanychests config set sortButtonEnabled false");
    int result = runAsConsole(context, "toomanychests config reset all");
    TooManyChestsConfig live = ServerConfigService.current();
    restore(context, original);

    context.assertTrue(result > 0, "Resetting everything should report success");
    context.assertTrue(
        live.equals(TooManyChestsConfig.defaults()),
        "Every setting should be back at its default but read " + live);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aNonOperatorIsRefusedTheSetSubcommand(TestContext context) {
    TooManyChestsConfig original = ServerConfigService.current();

    int result = runAsPlayer(context, "toomanychests config set quickStackRadius 12");
    int live = ServerConfigService.current().quickStackRadius();
    restore(context, original);

    context.assertTrue(result == 0, "A non-operator should be refused config set");
    context.assertTrue(
        live == original.quickStackRadius(),
        "A refused set should leave quickStackRadius at " + original.quickStackRadius());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aNonOperatorIsRefusedTheResetSubcommand(TestContext context) {
    context.assertTrue(
        runAsPlayer(context, "toomanychests config reset all") == 0,
        "A non-operator should be refused config reset");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRefusedSetSaysWhyRatherThanFailingToParse(TestContext context) {
    List<Text> said = new ArrayList<>();

    int result =
        run(
            context,
            server(context).getCommandSource().withLevel(0).withOutput(collecting(said)),
            "toomanychests config set quickStackRadius 12");

    context.assertTrue(result == 0, "A non-operator should be refused rather than unable to parse");
    context.assertTrue(
        said.stream().anyMatch(line -> line.getString().contains("only an operator")),
        "The refusal should say why, but said " + said.stream().map(Text::getString).toList());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aRefusedEligibilityEditSaysWhyRatherThanFailingToParse(TestContext context) {
    List<Text> said = new ArrayList<>();

    int result =
        run(
            context,
            server(context).getCommandSource().withLevel(0).withOutput(collecting(said)),
            "toomanychests config eligibility set minecraft:furnace true");

    context.assertTrue(result == 0, "A non-operator should be refused rather than unable to parse");
    context.assertTrue(
        said.stream().anyMatch(line -> line.getString().contains("only an operator")),
        "The refusal should say why, but said " + said.stream().map(Text::getString).toList());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aNonOperatorIsRefusedForcingContainerEligibility(TestContext context) {
    context.assertTrue(
        runAsPlayer(context, "toomanychests config eligibility set minecraft:furnace true") == 0,
        "A non-operator should be refused eligibility set");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aNonOperatorCanStillReadTheConfig(TestContext context) {
    context.assertTrue(
        runAsPlayer(context, "toomanychests config get quickStackRadius") > 0,
        "Reading a setting should be open to anyone");
    context.assertTrue(
        runAsPlayer(context, "toomanychests config list") > 0,
        "Listing the settings should be open to anyone");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theShortAliasReachesTheSameCommands(TestContext context) {
    TooManyChestsConfig original = ServerConfigService.current();

    int result = runAsConsole(context, "tmc config set quickStackRadius 7");
    int live = ServerConfigService.current().quickStackRadius();
    restore(context, original);

    context.assertTrue(result > 0, "The alias should reach config set");
    context.assertTrue(live == 7, "quickStackRadius should read 7 but read " + live);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anUnknownSettingIsRefusedWithoutTouchingTheConfig(TestContext context) {
    TooManyChestsConfig original = ServerConfigService.current();

    int result = runAsConsole(context, "toomanychests config set notASetting 3");
    TooManyChestsConfig live = ServerConfigService.current();
    restore(context, original);

    context.assertTrue(result == 0, "An unknown setting should report failure");
    context.assertTrue(live.equals(original), "An unknown setting should change nothing");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theConsoleCanForceAndUnforceContainerEligibility(TestContext context) {
    TooManyChestsConfig original = ServerConfigService.current();
    Identifier furnace = Identifier.ofVanilla("furnace");

    int forced =
        runAsConsole(context, "toomanychests config eligibility set minecraft:furnace true");
    boolean forcedEligible =
        ServerConfigService.current().containerEligibilityOverrideFor(furnace).orElse(false);
    int cleared = runAsConsole(context, "toomanychests config eligibility clear minecraft:furnace");
    boolean stillForced =
        ServerConfigService.current().containerEligibilityOverrideFor(furnace).isPresent();
    restore(context, original);

    context.assertTrue(forced > 0, "Forcing a block eligible should report success");
    context.assertTrue(forcedEligible, "The furnace should be forced eligible");
    context.assertTrue(cleared > 0, "Clearing the override should report success");
    context.assertTrue(!stillForced, "Nothing should be forcing the furnace afterwards");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everySettingNameIsSuggested(TestContext context) {
    List<String> suggested = suggestionsFor(context, "toomanychests config set ");

    context.assertTrue(
        suggested.containsAll(ConfigSettings.names()),
        "Every setting should be suggested but only " + suggested + " was");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theValuesSuggestedFollowTheSettingAlreadyNamed(TestContext context) {
    List<String> suggested = suggestionsFor(context, "toomanychests config set sortOrdering ");

    context.assertTrue(
        Set.copyOf(suggested).equals(Set.of("registry_id", "display_name")),
        "sortOrdering should suggest its own alternatives but suggested " + suggested);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theAliasSuggestsValuesForTheSettingAlreadyNamed(TestContext context) {
    List<String> suggested = suggestionsFor(context, "tmc config set quickStackRadius ");

    context.assertTrue(
        !suggested.isEmpty(),
        "The alias should suggest quickStackRadius values but suggested nothing");
    context.assertTrue(
        suggested.equals(suggestionsFor(context, "toomanychests config set quickStackRadius ")),
        "The alias should suggest exactly what the full name suggests, but suggested " + suggested);
    context.complete();
  }

  private static List<String> suggestionsFor(TestContext context, String partialCommand) {
    CommandDispatcher<ServerCommandSource> dispatcher =
        server(context).getCommandManager().getDispatcher();
    ServerCommandSource source = server(context).getCommandSource();
    return dispatcher
        .getCompletionSuggestions(dispatcher.parse(partialCommand, source))
        .join()
        .getList()
        .stream()
        .map(Suggestion::getText)
        .toList();
  }

  private static CommandOutput collecting(List<Text> said) {
    return new CommandOutput() {
      @Override
      public void sendMessage(Text message) {
        said.add(message);
      }

      @Override
      public boolean shouldReceiveFeedback() {
        return true;
      }

      @Override
      public boolean shouldTrackOutput() {
        return true;
      }

      @Override
      public boolean shouldBroadcastConsoleToOps() {
        return false;
      }
    };
  }

  private static int runAsConsole(TestContext context, String command) {
    return run(context, server(context).getCommandSource(), command);
  }

  private static int runAsPlayer(TestContext context, String command) {
    return run(context, server(context).getCommandSource().withLevel(0), command);
  }

  private static int run(TestContext context, ServerCommandSource source, String command) {
    try {
      return server(context).getCommandManager().getDispatcher().execute(command, source);
    } catch (CommandSyntaxException ex) {
      return REFUSED_EXIT_CODE;
    }
  }

  private static void restore(TestContext context, TooManyChestsConfig original) {
    ServerConfigService.update(server(context), original);
  }

  private static MinecraftServer server(TestContext context) {
    return context.getWorld().getServer();
  }
}
