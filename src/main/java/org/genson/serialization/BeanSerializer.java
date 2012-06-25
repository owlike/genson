package org.genson.serialization;

import java.io.IOException;
import java.lang.reflect.Type;

import org.genson.Context;
import org.genson.TransformationException;
import org.genson.reflect.BeanDescriptor;
import org.genson.reflect.BeanDescriptorProvider;
import org.genson.stream.ObjectWriter;


public class BeanSerializer extends ChainedSerializer {
	protected final BeanDescriptorProvider descProvider;

	public BeanSerializer(BeanDescriptorProvider descProvider) {
		this(descProvider, null);
	}

	public BeanSerializer(BeanDescriptorProvider descProvider, Serializer<Object> next) {
		super(next);
		this.descProvider = descProvider;
	}

	@Override
	protected boolean handleSerialization(Object target, Type type, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		boolean handled = false;
		if (type.equals(Object.class))
			return handled;
// TODO
		writer.beginObject();
		if (ctx.genson.isWithClassMetadata())
			writer.metadata("class", ctx.genson.aliasFor(target.getClass()));
		BeanDescriptor descriptor = descProvider.create(type);
		if (descriptor.isReadable()) {
			descriptor.serialize(target, type, writer, ctx);
			handled = true;
		}
		writer.endObject();

		return handled;
	}
}
