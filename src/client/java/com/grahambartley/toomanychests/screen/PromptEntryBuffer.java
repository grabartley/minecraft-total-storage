package com.grahambartley.toomanychests.screen;

public final class PromptEntryBuffer {
  private String typed;
  private boolean replacing;

  public PromptEntryBuffer(String initial) {
    typed = initial == null ? "" : initial;
  }

  public String text() {
    return typed;
  }

  public boolean isEmpty() {
    return typed.isEmpty();
  }

  public boolean isReplacing() {
    return replacing;
  }

  public boolean selectAll() {
    if (typed.isEmpty()) {
      return false;
    }
    replacing = true;
    return true;
  }

  public String candidate(char character) {
    return (replacing ? "" : typed) + character;
  }

  public void accept(String next) {
    typed = next == null ? "" : next;
    replacing = false;
  }

  public boolean backspace() {
    if (replacing) {
      typed = "";
      replacing = false;
      return true;
    }
    if (typed.isEmpty()) {
      return false;
    }
    typed = typed.substring(0, typed.length() - 1);
    return true;
  }
}
