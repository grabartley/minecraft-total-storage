package com.grahambartley.totalstorage.network;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PayloadCapLog {
  private static final Logger LOGGER = LoggerFactory.getLogger(PayloadCapLog.class);

  private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

  private PayloadCapLog() {}

  public static boolean reached(String cap, int sent, int held) {
    if (REPORTED.add(cap)) {
      LOGGER.info(
          "Sending the first {} of {} {} to fit one packet. This is the cap doing its job and"
              + " will not be logged again.",
          sent,
          held,
          cap);
      return true;
    }
    return false;
  }

  public static void clear() {
    REPORTED.clear();
  }
}
