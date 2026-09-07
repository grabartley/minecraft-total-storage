package com.grahambartley.toomanychests.listing;

import java.util.List;
import java.util.Objects;

/**
 * The slice of a listing a screen is currently showing.
 *
 * <p>A window is the whole reason a terminal can hold more than a screen handler could sync. The
 * client asks for the rows it is about to draw and nothing else, so what a listing costs is set by
 * the size of the grid rather than by the size of the network.
 *
 * <p>Both bounds are clamped rather than rejected, because the offset and length arrive from a
 * client and per ADR 0009 nothing a client sends is trusted to be sane. An offset past the end of
 * the results is not an error, it is an empty page: the results can shrink between a scroll and the
 * request it produces.
 *
 * @param offset how many matching rows to skip
 * @param length how many rows to return, never more than {@link #MAX_LENGTH}
 */
public record ListingWindow(int offset, int length) {
  /**
   * The most rows one page may carry. Chosen so a page is comfortably larger than the grid can show
   * at once and still far below the size of a packet, which is what keeps a response bounded no
   * matter what a client asks for.
   */
  public static final int MAX_LENGTH = 128;

  public static final ListingWindow EMPTY = new ListingWindow(0, 0);

  public ListingWindow {
    offset = Math.max(0, offset);
    length = Math.max(0, Math.min(MAX_LENGTH, length));
  }

  public static ListingWindow of(int offset, int length) {
    return new ListingWindow(offset, length);
  }

  /** The first row past this window, saturated rather than wrapped past the end of an int. */
  public int end() {
    return (int) Math.min(Integer.MAX_VALUE, (long) offset + (long) length);
  }

  public boolean isEmpty() {
    return length == 0;
  }

  public boolean contains(int index) {
    return index >= offset && index < end();
  }

  /** The rows this window covers, which is empty rather than out of bounds past the end. */
  public <T> List<T> sliceOf(List<T> all) {
    Objects.requireNonNull(all, "all");
    if (length == 0 || offset >= all.size()) {
      return List.of();
    }
    return List.copyOf(all.subList(offset, Math.min(all.size(), end())));
  }
}
