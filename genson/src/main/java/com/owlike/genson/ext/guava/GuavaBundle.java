package com.owlike.genson.ext.guava;

import com.google.common.base.Optional;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.ext.GensonBundle;

public class GuavaBundle extends GensonBundle {
  @Override
  public void configure(GensonBuilder builder) {
    builder.useDefaultValue(Optional.absent(), Optional.class)
      .withConverterFactory(new OptionalConverter.OptionalConverterFactory());
  }
}
