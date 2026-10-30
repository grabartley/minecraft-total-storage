package com.grahambartley.totalstorage.render;

import com.grahambartley.totalstorage.terminal.HighlightedContainer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
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

  private static final int DASHES_PER_EDGE = 3;

  private ContainerHighlightRenderer() {}

  public static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> ContainerHighlights.tick());
    ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(
        (client, world) -> ContainerHighlights.clear());
    WorldRenderEvents.AFTER_ENTITIES.register(ContainerHighlightRenderer::renderBehindBlocks);
    WorldRenderEvents.BEFORE_DEBUG_RENDER.register(ContainerHighlightRenderer::renderOverBlocks);
  }

  private static void renderBehindBlocks(WorldRenderContext context) {
    if (ContainerHighlights.isShowing() && !ContainerHighlights.showsThroughBlocks()) {
      render(context, RenderLayer.getLines());
    }
  }

  private static void renderOverBlocks(WorldRenderContext context) {
    if (!ContainerHighlights.showsThroughBlocks()) {
      return;
    }
    render(context, HighlightLines.throughBlocks());
    if (context.consumers() instanceof VertexConsumerProvider.Immediate immediate) {
      immediate.draw(HighlightLines.throughBlocks());
    }
  }

  private static void render(WorldRenderContext context, RenderLayer layer) {
    final VertexConsumerProvider consumers = context.consumers();
    final MatrixStack matrices = context.matrixStack();
    if (consumers == null || matrices == null) {
      return;
    }
    final Vec3d camera = context.camera().getPos();
    final VertexConsumer lines = consumers.getBuffer(layer);
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
    final Box footprint = ContainerOutline.of(world, container.container()).expand(SWELL);
    switch (HighlightColour.shapeOf(container.role())) {
      case SOLID -> drawBox(matrices, lines, footprint, colour);
      case DASHED -> DashedBox.draw(matrices, lines, footprint, DASHES_PER_EDGE, colour);
      case DOUBLED -> {
        drawBox(matrices, lines, footprint, colour);
        drawBox(matrices, lines, footprint.expand(-INNER_INSET), colour);
      }
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
