package com.grahambartley.toomanychests;

import com.grahambartley.toomanychests.screenhandler.TerminalOpenDataCodec;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
  public static final ScreenHandlerType<TerminalScreenHandler> TERMINAL =
      Registry.register(
          Registries.SCREEN_HANDLER,
          Identifier.of(TooManyChests.MOD_ID, "terminal"),
          new ExtendedScreenHandlerType<>(
              TerminalScreenHandler::new, TerminalOpenDataCodec.INSTANCE));

  public static void initialize() {}
}
