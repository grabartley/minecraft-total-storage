package com.grahambartley.totalstorage.render;

import com.grahambartley.totalstorage.terminal.HighlightedContainer;
import java.util.List;
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

  private static final int DASHES_PER_EDGE = 3;

  private ContainerHighlightRenderer() {}

  public static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> ContainerHighlights.tick());
    WorldRenderEvents.AFTER_ENTITIES.register(ContainerHighlightRenderer::renderNetwork);
    WorldRenderEvents.AFTER_TRANSLUCENT.register(ContainerHighlightRenderer::renderLocated);
  }

  private static void renderNetwork(WorldRenderContext context) {
    if (ContainerHighlights.isShowing() && !ContainerHighlights.showsLocated()) {
      render(context, RenderLayer.getLines());
    }
  }

  private static void renderLocated(WorldRenderContext context) {
    if (!ContainerHighlights.showsLocated()) {
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
      case DASHED ->
          drawSegments(matrices, lines, DashedBox.of(footprint, DASHES_PER_EDGE), colour);
      case CROSSED -> {
        drawBox(matrices, lines, footprint, colour);
        drawSegments(matrices, lines, CrossedBox.of(footprint), colour);
      }
    }
  }

  private static void drawSegments(
      MatrixStack matrices, VertexConsumer lines, List<HighlightSegment> segments, int colour) {
    final MatrixStack.Entry entry = matrices.peek();
    final float red = HighlightColour.red(colour);
    final float green = HighlightColour.green(colour);
    final float blue = HighlightColour.blue(colour);
    final float alpha = HighlightColour.alpha(colour);
    for (final HighlightSegment segment : segments) {
      final Vec3d normal = segment.normal();
      for (final Vec3d end : List.of(segment.from(), segment.to())) {
        lines
            .vertex(entry, (float) end.x, (float) end.y, (float) end.z)
            .color(red, green, blue, alpha)
            .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);
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
