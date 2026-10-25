package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.terminal.NetworkTagEntry;
import com.grahambartley.totalstorage.terminal.NetworkTagView;
import java.util.ArrayList;
import java.util.List;

public record FilterColumnRows(List<Row> rows) {
  public record Row(NetworkTagEntry tag) {
    public boolean isDivider() {
      return tag == null;
    }
  }

  public FilterColumnRows {
    rows = List.copyOf(rows);
  }

  public static FilterColumnRows of(NetworkTagView view) {
    final List<Row> rows = new ArrayList<>();
    final List<NetworkTagEntry> custom = new ArrayList<>();
    for (final NetworkTagEntry tag : view.tags()) {
      if (tag.immutable()) {
        rows.add(new Row(tag));
      } else {
        custom.add(tag);
      }
    }
    if (!custom.isEmpty()) {
      rows.add(new Row(null));
      custom.forEach(tag -> rows.add(new Row(tag)));
    }
    return new FilterColumnRows(rows);
  }

  public int size() {
    return rows.size();
  }

  public Row at(int index) {
    return index < 0 || index >= rows.size() ? null : rows.get(index);
  }

  public NetworkTagEntry tagAt(int index) {
    final Row row = at(index);
    return row == null ? null : row.tag();
  }
}
