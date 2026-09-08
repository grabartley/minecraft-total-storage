package com.grahambartley.toomanychests.network;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Says once that a list the server sends had to be shortened, and then stays quiet.
 *
 * <p>The caps themselves are what keep a packet bounded however much a network holds, so hitting
 * one is not an error. It is worth telling a pack author about exactly once: a network big enough
 * to reach a cap reaches it on every request, and a line per occurrence turns the signal into the
 * noise it was supposed to rise above. A player with the settings panel open would produce one
 * every time the screen refreshed.
 *
 * <p>Remembered per cap rather than as a single flag, so a server that trips two different ones
 * hears about both.
 */
public final class PayloadCapLog {
  private static final Logger LOGGER = LoggerFactory.getLogger(PayloadCapLog.class);

  private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

  private PayloadCapLog() {}

  /**
   * Reports a cap the first time it is reached.
   *
   * @param cap what was capped, which is the key a repeat is recognised by
   * @param sent how many the packet carried
   * @param held how many there really were
   * @return whether this was the first time, which is the whole rule and so is worth returning
   *     rather than only acting on
   */
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

  /** Forgets what has been reported, so a fresh server reports afresh. */
  public static void clear() {
    REPORTED.clear();
  }
}
