package com.owlike.genson.reflect;

import com.owlike.genson.Context;

public interface RuntimePropertyFilter {
  RuntimePropertyFilter noFilter = new RuntimePropertyFilter() {
    @Override
    public boolean shouldInclude(BeanProperty property, Context ctx) {
      return true;
    }
  };

  boolean shouldInclude(BeanProperty property, Context ctx);
}

