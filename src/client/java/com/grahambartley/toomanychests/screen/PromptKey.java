package com.grahambartley.toomanychests.screen;

/** What a key press did to the custom amount prompt. */
public enum PromptKey {
  /** The player accepted the number they typed. */
  CONFIRM,
  /** The player backed out, so nothing is withdrawn. */
  CANCEL,
  /** The number changed, so the prompt stays up and redraws. */
  EDITED,
  /** The prompt had no use for the key, though it still swallows it while it is up. */
  IGNORED
}
