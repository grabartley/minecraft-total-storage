package com.grahambartley.toomanychests.listing;

import java.util.List;
import java.util.Objects;

public record ListingWindow(int offset, int length) {

  public static final int MAX_LENGTH = 128;

  public static final ListingWindow EMPTY = new ListingWindow(0, 0);

  public ListingWindow {
    offset = Math.max(0, offset);
    length = Math.max(0, Math.min(MAX_LENGTH, length));
  }

  public static ListingWindow of(int offset, int length) {
    return new ListingWindow(offset, length);
  }

  public int end() {
    return (int) Math.min(Integer.MAX_VALUE, (long) offset + (long) length);
  }

  public boolean isEmpty() {
    return length == 0;
  }

  public <T> List<T> sliceOf(List<T> all) {
    Objects.requireNonNull(all, "all");
    if (length == 0 || offset >= all.size()) {
      return List.of();
    }
    return List.copyOf(all.subList(offset, Math.min(all.size(), end())));
  }
}
