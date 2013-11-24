package com.owlike.genson.ext.guava;

import com.owlike.genson.Genson;
import com.owlike.genson.ext.GensonBundle;

public class GuavaBundle extends GensonBundle {
    @Override
    public void configure(Genson.Builder builder) {
        builder.withConverterFactory(new OptionalConverter.OptionalConverterFactory());
    }
}
