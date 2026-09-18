package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.terminal.ContainerNamingService;
import com.grahambartley.toomanychests.terminal.ContainerRoleEntry;
import com.grahambartley.toomanychests.terminal.ContainerRoleService;
import com.grahambartley.toomanychests.terminal.RoleChangeOutcome;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import com.grahambartley.toomanychests.terminal.TerminalSettingsCodec;
import com.mojang.serialization.Codec;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ContainerNamingServiceGameTest implements FabricGameTest {
  private static final String BATCH = "container-naming";
  private static final int TICK_LIMIT = 60;

  private final ContainerNamingService names = new ContainerNamingService();
  private final ContainerRoleService roles = new ContainerRoleService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aContainerNobodyHasNamedHasNoName(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    context.assertTrue(
        names.nameOf(context.getWorld(), terminal(context), east(context)).isEmpty(),
        "A container starts without a name");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theOwnerCanNameAContainerAndTheRowCarriesIt(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    RoleChangeOutcome outcome = rename(context, owner, "Potions");

    context.assertTrue(outcome == RoleChangeOutcome.CHANGED, "The name should have been written");
    context.assertTrue(
        "Potions".equals(rowFor(context).name()),
        "The row should carry the name, got '" + rowFor(context).name() + "'");
    context.assertTrue(rowFor(context).isNamed(), "A named row should say it is named");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNameIsTrimmedBeforeItIsStored(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    rename(context, owner, "  Ores   and ingots  ");

    context.assertTrue(
        "Ores and ingots"
            .equals(names.nameOf(context.getWorld(), terminal(context), east(context))),
        "The name should have been tidied before storing");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anEmptyNameTakesTheNameBackOff(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    rename(context, owner, "Potions");

    RoleChangeOutcome outcome = rename(context, owner, "");

    context.assertTrue(outcome == RoleChangeOutcome.CHANGED, "Clearing a name should be a change");
    context.assertTrue(
        names.nameOf(context.getWorld(), terminal(context), east(context)).isEmpty(),
        "The name should be gone");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aStrangerCannotNameSomebodyElsesContainer(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity stranger = NetworkOperationFixture.standingPlayer(context);

    RoleChangeOutcome outcome =
        names.rename(
            context.getWorld(),
            terminal(context),
            TerminalCaller.of(stranger, false),
            east(context),
            "Mine now");

    context.assertTrue(outcome == RoleChangeOutcome.DENIED, "A stranger named it, got " + outcome);
    context.assertTrue(
        names.nameOf(context.getWorld(), terminal(context), east(context)).isEmpty(),
        "A refused name was written anyway");
    NetworkOperationFixture.finish(context, owner, stranger);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNameSurvivesBeingWrittenToDiskAndReadBack(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    rename(context, owner, "Potions");

    TerminalSettings reloaded = roundTrip(settingsOf(context));

    context.assertTrue(
        "Potions".equals(reloaded.nameFor(east(context))),
        "The name should survive a reload, got '" + reloaded.nameFor(east(context)) + "'");
    NetworkOperationFixture.finish(context, owner);
  }

  private RoleChangeOutcome rename(TestContext context, ServerPlayerEntity owner, String name) {
    return names.rename(
        context.getWorld(), terminal(context), TerminalCaller.of(owner), east(context), name);
  }

  private ContainerRoleEntry rowFor(TestContext context) {
    final List<ContainerRoleEntry> rows = roles.containers(context.getWorld(), terminal(context));
    return rows.stream()
        .filter(row -> row.container().equals(east(context)))
        .findFirst()
        .orElseThrow();
  }

  private static TerminalSettings settingsOf(TestContext context) {
    return context.<TerminalBlockEntity>getBlockEntity(NetworkOperationFixture.TERMINAL).settings();
  }

  private static TerminalSettings roundTrip(TerminalSettings settings) {
    final Codec<TerminalSettings> codec = TerminalSettingsCodec.CODEC;
    final NbtElement written = codec.encodeStart(NbtOps.INSTANCE, settings).getOrThrow();
    return codec.parse(NbtOps.INSTANCE, written).getOrThrow();
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }

  private static BlockPos east(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.EAST_BARREL);
  }
}
