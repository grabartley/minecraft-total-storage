package com.grahambartley.toomanychests.render;

import com.grahambartley.toomanychests.terminal.HighlightedContainer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

public final class ContainerHighlightRenderer {
  private static final double SWELL = 0.002;

  private static final double INNER_INSET = 0.2;

  private ContainerHighlightRenderer() {}

  public static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> ContainerHighlights.tick());
    WorldRenderEvents.AFTER_ENTITIES.register(ContainerHighlightRenderer::render);
  }

  private static void render(WorldRenderContext context) {
    if (!ContainerHighlights.isShowing()) {
      return;
    }
    final VertexConsumerProvider consumers = context.consumers();
    final MatrixStack matrices = context.matrixStack();
    if (consumers == null || matrices == null) {
      return;
    }
    final Vec3d camera = context.camera().getPos();
    final VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
    final BlockView world = context.world();
    matrices.push();
    matrices.translate(-camera.x, -camera.y, -camera.z);
    for (final HighlightedContainer container : ContainerHighlights.showing()) {
      drawOutline(matrices, lines, world, container);
    }
    matrices.pop();
  }

  private static void drawOutline(
      MatrixStack matrices, VertexConsumer lines, BlockView world, HighlightedContainer container) {
    final int colour = HighlightColour.of(container.role());
    final Box footprint = ContainerOutline.of(world, container.container());
    drawBox(matrices, lines, footprint.expand(SWELL), colour);
    if (HighlightColour.isDoubled(container.role())) {
      drawBox(matrices, lines, footprint.expand(-INNER_INSET), colour);
    }
  }

  private static void drawBox(MatrixStack matrices, VertexConsumer lines, Box box, int colour) {
    WorldRenderer.drawBox(
        matrices,
        lines,
        box,
        HighlightColour.red(colour),
        HighlightColour.green(colour),
        HighlightColour.blue(colour),
        HighlightColour.alpha(colour));
  }
}
