package com.owlike.genson.ext.guava;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.ext.GensonBundle;

public class GuavaBundle extends GensonBundle {
    @Override
    public void configure(GensonBuilder builder) {
        builder.withConverterFactory(new OptionalConverter.OptionalConverterFactory());
    }
}
