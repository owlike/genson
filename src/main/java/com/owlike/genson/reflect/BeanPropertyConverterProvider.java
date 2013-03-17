package com.owlike.genson.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.ThreadLocalHolder;
import com.owlike.genson.convert.CreationContext;

// TODO 1) a better name? 2) CreationContext and BeanProperty represent similar things does it make sense to have those 2 classes?
public final class BeanPropertyConverterProvider {
	private final static String CONTEXT_KEY = "__GENSON$CREATION_CONTEXT";
	// TODO ugly...
	private final Genson genson;
	
	public BeanPropertyConverterProvider(Genson genson) {
		this.genson = genson;
	}
	
	// TODO ugly signature...
	public Converter<?> provide(String name, Type type, Annotation[] annotations, Class<?> declaringClass) {
		CreationContext parent = ThreadLocalHolder.get(CONTEXT_KEY, CreationContext.class);
		try {
			CreationContext currentContext = new CreationContext(parent, type, name, name, annotations, declaringClass);
			ThreadLocalHolder.store(CONTEXT_KEY, currentContext);
			return genson.provideConverter(type);
		} finally {
			ThreadLocalHolder.store(CONTEXT_KEY, parent);
		}
	}
}
