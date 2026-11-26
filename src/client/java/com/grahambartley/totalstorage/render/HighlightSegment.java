package com.grahambartley.totalstorage.render;

import java.util.Objects;
import net.minecraft.util.math.Vec3d;

public record HighlightSegment(Vec3d from, Vec3d to) {
  public HighlightSegment {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(to, "to");
    if (from.equals(to)) {
      throw new IllegalArgumentException("a segment cannot start and end at " + from);
    }
  }

  public Vec3d normal() {
    return to.subtract(from).normalize();
  }
}
