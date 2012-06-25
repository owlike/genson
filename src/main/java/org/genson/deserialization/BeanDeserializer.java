package org.genson.deserialization;

import java.io.IOException;
import java.lang.reflect.Type;

import org.genson.Context;
import org.genson.Factory;
import org.genson.TransformationException;
import org.genson.reflect.BeanDescriptor;
import org.genson.stream.ObjectReader;


public class BeanDeserializer extends ChainedDeserializer {
	private final Factory<BeanDescriptor> descProvider;

	public BeanDeserializer(Factory<BeanDescriptor> descProvider) {
		this(descProvider, null);
	}

	public BeanDeserializer(Factory<BeanDescriptor> descProvider, Deserializer<Object> next) {
		super(next);
		this.descProvider = descProvider;
	}

	@Override
	protected Object handleDeserialization(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		if (type.equals(Object.class))
			return null;

		Object bean = NULL;
		BeanDescriptor descriptor = descProvider.create(type);
		if (descriptor.isWritable()) {
			bean = descriptor.deserialize(type, reader, ctx);
		}
		return bean;
	}

	public Factory<BeanDescriptor> getBeanDescriptorProvider() {
		return descProvider;
	}
}
