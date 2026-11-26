package com.grahambartley.totalstorage.config;

import java.util.Optional;

public interface ConfigOption<S> {
  String valueIn(S subject);

  Optional<S> withValue(S subject, String rawValue);
}
