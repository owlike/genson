package com.owlike.genson.functional;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

import com.owlike.genson.Context;
import com.owlike.genson.Genson;

public class ContextTest {
  private final Context ctx = new Context(new Genson());

  @Test
  public void testStore() {
    assertNull(ctx.store("key", 1));
    assertEquals(1, ctx.store("key", 2));
    assertNull(ctx.store("key2", 1));
  }

  @Test
  public void testGet() {
    ctx.store("key", new String[]{"value"});
    assertArrayEquals(new String[]{"value"}, ctx.get("key", String[].class));
    try {
      ctx.get("key", List.class);
      fail();
    } catch (ClassCastException cce) {
    }
  }

  @Test
  public void testRemove() {
    ctx.store("key", new String[]{"value"});
    assertNull(ctx.remove("key2", String.class));
    try {
      ctx.remove("key", Integer[].class);
      fail();
    } catch (ClassCastException cce) {

    }

    assertArrayEquals(new String[]{"value"}, ctx.remove("key", String[].class));
  }
}
