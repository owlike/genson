package com.owlike.genson.convert;

import com.owlike.genson.Converter;
import com.owlike.genson.Genson;

public interface ContextualFactory<T> {
	public Converter<T> create(CreationContext context, Genson genson);
}
