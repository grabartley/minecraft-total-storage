package com.grahambartley.toomanychests.filter;

import com.grahambartley.toomanychests.tag.NetworkTagLookup;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public final class NetworkFilterCandidates implements Function<ItemVariant, FilterCandidate> {
  private final FilterCandidateFactory presets;
  private final NetworkTagLookup custom;

  public NetworkFilterCandidates(NetworkTagSet tags) {
    this(new FilterCandidateFactory(), new NetworkTagLookup(tags));
  }

  public NetworkFilterCandidates(FilterCandidateFactory presets, NetworkTagLookup custom) {
    this.presets = Objects.requireNonNull(presets, "presets");
    this.custom = Objects.requireNonNull(custom, "custom");
  }

  @Override
  public FilterCandidate apply(ItemVariant variant) {
    return of(variant);
  }

  public FilterCandidate of(ItemVariant variant) {
    final FilterCandidate candidate = presets.of(variant);
    return candidate.plusTags(custom.tagIdsFor(candidate.identifier()));
  }
}
