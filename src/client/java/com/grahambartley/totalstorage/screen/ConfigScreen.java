package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.access.ServerSettingsAccessRule;
import com.grahambartley.totalstorage.config.ClientConfigMirror;
import com.grahambartley.totalstorage.config.ClientPreferencesStore;
import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.network.ModNetworkingClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import org.jetbrains.annotations.Nullable;

public final class ConfigScreen extends Screen {
  private static final int HEADER_HEIGHT = 44;
  private static final int FOOTER_HEIGHT = 36;
  private static final int BUTTON_WIDTH = 100;
  private static final int BUTTON_HEIGHT = 20;
  private static final int BUTTON_GAP = 4;
  private static final int TITLE_TOP = 12;
  private static final int NOTICE_TOP = 26;
  private static final int NOTICE_COLOUR = 0xFF000000 | VanillaPalette.BUTTON_TEXT_DISABLED;
  private static final int FAILURE_COLOUR = 0xFF000000 | VanillaPalette.WARNING;

  @Nullable private final Screen parent;
  private final ServerConfigEdits edits;
  private final ClientPreferenceEdits preferences;
  private ConfigScreenAccess access;
  private boolean preferencesSaveFailed;

  public ConfigScreen(@Nullable Screen parent) {
    super(ConfigScreenText.title());
    this.parent = parent;
    this.edits = new ServerConfigEdits(ClientConfigMirror.current());
    this.preferences = new ClientPreferenceEdits(ClientPreferencesStore.current());
    this.access = currentAccess();
  }

  @Nullable
  public Screen parent() {
    return parent;
  }

  private static ConfigScreenAccess currentAccess() {
    return new ConfigScreenAccess(ClientConfigMirror.hasReceived(), canEdit());
  }

  private static boolean canEdit() {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client == null || client.player == null) {
      return false;
    }
    final boolean ownsTheWorld = client.isInSingleplayer();
    return ServerSettingsAccessRule.mayEdit(
        client.player.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL),
        !ownsTheWorld,
        ownsTheWorld);
  }

  @Override
  protected void init() {
    final ConfigList list =
        addDrawableChild(
            new ConfigList(client, width, height - HEADER_HEIGHT - FOOTER_HEIGHT, HEADER_HEIGHT));
    list.addPreferences(preferences);
    if (access.showsServerSettings()) {
      list.addServerSettings(edits, access.serverControlsActive());
    }
    final int footerTop = height - FOOTER_HEIGHT + (FOOTER_HEIGHT - BUTTON_HEIGHT) / 2;
    final int left = width / 2 - BUTTON_WIDTH - BUTTON_GAP / 2;
    addDrawableChild(
        ButtonWidget.builder(ScreenTexts.DONE, button -> submitAndClose())
            .dimensions(left, footerTop, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build());
    addDrawableChild(
        ButtonWidget.builder(ScreenTexts.CANCEL, button -> close())
            .dimensions(left + BUTTON_WIDTH + BUTTON_GAP, footerTop, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build());
  }

  @Override
  public void tick() {
    final ConfigScreenAccess latest = currentAccess();
    final boolean adopted = edits.adopt(ClientConfigMirror.current());
    if (adopted || !latest.equals(access)) {
      access = latest;
      clearAndInit();
    }
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);
    context.drawCenteredTextWithShadow(textRenderer, title, width / 2, TITLE_TOP, 0xFFFFFFFF);
    final ConfigScreenAccess.Notice notice = access.notice(preferencesSaveFailed);
    final int colour =
        notice == ConfigScreenAccess.Notice.SAVE_FAILED ? FAILURE_COLOUR : NOTICE_COLOUR;
    ConfigScreenText.notice(notice)
        .ifPresent(
            text ->
                context.drawCenteredTextWithShadow(
                    textRenderer, text, width / 2, NOTICE_TOP, colour));
  }

  @Override
  public void close() {
    if (client != null) {
      client.setScreen(parent);
    }
  }

  private void submitAndClose() {
    if (preferences.isDirty() && !ClientPreferencesStore.save(preferences.edited())) {
      preferencesSaveFailed = true;
      return;
    }
    if (access.sendsServerEdits(edits.isDirty())) {
      ModNetworkingClient.sendConfigEdit(edits.edited());
    }
    close();
  }
}
