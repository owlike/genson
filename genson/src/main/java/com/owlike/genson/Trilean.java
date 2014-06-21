package com.owlike.genson;

/**
 * A boolean with 3 states : true, false and unknown.
 *
 * @author eugen
 */
public enum Trilean {
  TRUE() {
    @Override
    public boolean booleanValue() {
      return true;
    }
  },
  FALSE {
    @Override
    public boolean booleanValue() {
      return false;
    }
  },
  UNKNOWN {
    @Override
    public boolean booleanValue() {
      throw new IllegalStateException(
        "Unknown state can not be converter to a boolean, only TRUE AND FALSE can!");
    }
  };

  public static Trilean valueOf(boolean value) {
    return value ? TRUE : FALSE;
  }

  public abstract boolean booleanValue();
}
