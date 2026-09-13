package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ClientConfigMirror;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import org.jetbrains.annotations.Nullable;

public final class ServerConfigScreen extends Screen {
  private static final int HEADER_HEIGHT = 44;
  private static final int FOOTER_HEIGHT = 36;
  private static final int BUTTON_WIDTH = 100;
  private static final int BUTTON_HEIGHT = 20;
  private static final int BUTTON_GAP = 4;
  private static final int TITLE_TOP = 12;
  private static final int NOTICE_TOP = 26;
  private static final int NOTICE_COLOUR = 0xFFA0A0A0;

  @Nullable private final Screen parent;
  private final ServerConfigEdits edits;
  private final boolean editable;
  @Nullable private ServerConfigList list;

  public ServerConfigScreen(@Nullable Screen parent) {
    super(ServerConfigText.title());
    this.parent = parent;
    this.edits = new ServerConfigEdits(ClientConfigMirror.current());
    this.editable = canEdit();
  }

  @Nullable
  public Screen parent() {
    return parent;
  }

  private static boolean canEdit() {
    final MinecraftClient client = MinecraftClient.getInstance();
    return client != null
        && client.player != null
        && client.player.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL);
  }

  @Override
  protected void init() {
    final int footerTop = height - FOOTER_HEIGHT + (FOOTER_HEIGHT - BUTTON_HEIGHT) / 2;
    if (!ClientConfigMirror.hasReceived()) {
      addDrawableChild(
          ButtonWidget.builder(ScreenTexts.BACK, button -> close())
              .dimensions(width / 2 - BUTTON_WIDTH / 2, footerTop, BUTTON_WIDTH, BUTTON_HEIGHT)
              .build());
      return;
    }
    list =
        addDrawableChild(
            new ServerConfigList(
                client,
                width,
                height - HEADER_HEIGHT - FOOTER_HEIGHT,
                HEADER_HEIGHT,
                edits,
                editable));
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
    if (edits.adopt(ClientConfigMirror.current())) {
      clearAndInit();
    }
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);
    context.drawCenteredTextWithShadow(textRenderer, title, width / 2, TITLE_TOP, 0xFFFFFFFF);
    if (list == null) {
      context.drawCenteredTextWithShadow(
          textRenderer,
          ServerConfigText.notConnectedNotice(),
          width / 2,
          height / 2,
          NOTICE_COLOUR);
      return;
    }
    if (!editable) {
      context.drawCenteredTextWithShadow(
          textRenderer, ServerConfigText.readOnlyNotice(), width / 2, NOTICE_TOP, NOTICE_COLOUR);
    }
  }

  @Override
  public void close() {
    if (client != null) {
      client.setScreen(parent);
    }
  }

  private void submitAndClose() {
    if (editable && edits.isDirty()) {
      ModNetworkingClient.sendConfigEdit(edits.edited());
    }
    close();
  }
}
