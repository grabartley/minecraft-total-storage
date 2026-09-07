package com.grahambartley.toomanychests.screen;

/**
 * What a key press did to the custom amount prompt.
 *
 * <p>Only the two endings are named. The prompt is modal, so every other key is swallowed whether
 * or not it changed the number, and a case telling those apart would be one nothing reads: whether
 * the number moved is already answered by the number itself.
 */
public enum PromptKey {
  /** The player accepted the number they typed. */
  CONFIRM,
  /** The player backed out, so nothing is withdrawn. */
  CANCEL,
  /** The prompt kept the key to itself, so nothing behind it hears it. */
  HANDLED
}
