package com.grahambartley.totalstorage.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.OptionalDouble;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public final class HighlightLines {
  private static final int BUFFER_SIZE = 1536;

  private static final RenderPhase.Layering NO_DEPTH_TEST =
      new RenderPhase.Layering(
          "total-storage:no_depth_test",
          RenderSystem::disableDepthTest,
          RenderSystem::enableDepthTest);

  private static final RenderLayer THROUGH_BLOCKS =
      RenderLayer.of(
          "total-storage:lines_through_blocks",
          VertexFormats.LINES,
          VertexFormat.DrawMode.LINES,
          BUFFER_SIZE,
          RenderLayer.MultiPhaseParameters.builder()
              .program(RenderPhase.LINES_PROGRAM)
              .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
              .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
              .writeMaskState(RenderPhase.COLOR_MASK)
              .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
              .layering(NO_DEPTH_TEST)
              .cull(RenderPhase.DISABLE_CULLING)
              .build(false));

  private HighlightLines() {}

  public static RenderLayer throughBlocks() {
    return THROUGH_BLOCKS;
  }
}
