package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import java.util.function.Consumer;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public final class TerminalSettingsButtons {
  public static final int STEPPER_SIZE = TerminalSettingsLayout.BUTTON_HEIGHT;

  public static final int RADIUS_DOWN_X = TerminalSettingsLayout.BUTTON_X;

  public static final int RADIUS_UP_X =
      TerminalSettingsLayout.BUTTON_X + TerminalSettingsLayout.BUTTON_WIDTH - STEPPER_SIZE;

  public interface Edits {
    void access(TerminalAccessMode mode);

    void trusted();

    void radius(int blocks);
  }

  private final TerminalTextButton access;
  private final TerminalTextButton trusted;
  private final TerminalTextButton radiusDown;
  private final TerminalTextButton radiusUp;

  private NetworkSettingsView view = NetworkSettingsView.pending();

  public TerminalSettingsButtons(
      Consumer<ClickableWidget> adder, int originX, int originY, Edits edits) {
    access =
        new TerminalTextButton(
            originX + TerminalSettingsLayout.BUTTON_X,
            originY + TerminalSettingsLayout.settingY(TerminalSetting.ACCESS.row()),
            TerminalSettingsLayout.BUTTON_WIDTH,
            TerminalSettingsLayout.BUTTON_HEIGHT,
            Text.empty(),
            () -> edits.access(AccessModeCycle.next(view.accessMode())),
            () -> edits.access(AccessModeCycle.previous(view.accessMode())));
    trusted =
        new TerminalTextButton(
            originX + TerminalSettingsLayout.BUTTON_X,
            originY + TerminalSettingsLayout.settingY(TerminalSetting.TRUSTED.row()),
            TerminalSettingsLayout.BUTTON_WIDTH,
            TerminalSettingsLayout.BUTTON_HEIGHT,
            Text.empty(),
            edits::trusted);
    final int radiusY = originY + TerminalSettingsLayout.settingY(TerminalSetting.RADIUS.row());
    radiusDown =
        new TerminalTextButton(
            originX + RADIUS_DOWN_X,
            radiusY,
            STEPPER_SIZE,
            STEPPER_SIZE,
            Text.literal("-"),
            () -> edits.radius(view.radius() - 1));
    radiusUp =
        new TerminalTextButton(
            originX + RADIUS_UP_X,
            radiusY,
            STEPPER_SIZE,
            STEPPER_SIZE,
            Text.literal("+"),
            () -> edits.radius(view.radius() + 1));
    radiusDown.setHint(TerminalScreenText.of("settings.radius.down"));
    radiusUp.setHint(TerminalScreenText.of("settings.radius.up"));
    adder.accept(access);
    adder.accept(trusted);
    adder.accept(radiusDown);
    adder.accept(radiusUp);
  }

  public void update(NetworkSettingsView shown, boolean visible) {
    view = shown;
    access.visible = visible;
    trusted.visible = visible;
    radiusDown.visible = visible;
    radiusUp.visible = visible;
    final boolean editable = shown.answered() && shown.mayAdminister();
    access.active = editable;
    trusted.active = editable;
    radiusDown.active = editable && shown.radius() > shown.minRadius();
    radiusUp.active = editable && shown.radius() < shown.maxRadius();
    access.show(accessLabel(shown), false);
    trusted.show(TerminalSettingsText.trustedSummary(shown), false);
    final Text refused = shown.answered() && !shown.mayAdminister() ? notYours() : null;
    access.setHint(refused != null ? refused : TerminalScreenText.of("settings.access.hint"));
    trusted.setHint(refused);
  }

  private static Text notYours() {
    return TerminalScreenText.of("settings.not_yours");
  }

  private static Text accessLabel(NetworkSettingsView shown) {
    return shown.answered()
        ? shown.accessMode().displayName()
        : TerminalScreenText.of("settings.pending");
  }
}
