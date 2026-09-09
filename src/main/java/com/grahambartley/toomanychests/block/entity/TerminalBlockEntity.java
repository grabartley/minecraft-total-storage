package com.grahambartley.toomanychests.block.entity;

import com.grahambartley.toomanychests.ModBlockEntities;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import com.grahambartley.toomanychests.terminal.TerminalSettingsCodec;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalBlockEntity extends BlockEntity {
  private static final String NBT_SETTINGS = "Settings";
  private static final Logger LOGGER = LoggerFactory.getLogger(TerminalBlockEntity.class);

  private TerminalSettings settings = TerminalSettings.defaults();

  public TerminalBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.TERMINAL, pos, state);
  }

  public TerminalSettings settings() {
    return settings;
  }

  public void setSettings(TerminalSettings updated) {
    this.settings = Objects.requireNonNullElseGet(updated, TerminalSettings::defaults);
    markDirty();
  }

  public void updateSettings(UnaryOperator<TerminalSettings> change) {
    setSettings(change.apply(settings));
  }

  @Override
  protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.writeNbt(nbt, registryLookup);
    TerminalSettingsCodec.CODEC
        .encodeStart(NbtOps.INSTANCE, settings)
        .resultOrPartial(
            error -> LOGGER.error("Could not write terminal settings at {}: {}", pos, error))
        .ifPresent(encoded -> nbt.put(NBT_SETTINGS, encoded));
  }

  @Override
  protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
    super.readNbt(nbt, registryLookup);
    if (!nbt.contains(NBT_SETTINGS)) {
      settings = TerminalSettings.defaults();
      return;
    }
    settings =
        TerminalSettingsCodec.CODEC
            .parse(NbtOps.INSTANCE, nbt.get(NBT_SETTINGS))
            .resultOrPartial(
                error -> LOGGER.warn("Could not read terminal settings at {}: {}", pos, error))
            .orElseGet(TerminalSettings::defaults);
  }
}
