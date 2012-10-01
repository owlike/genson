package com.owlike.genson.reflect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.TransformationException;
import com.owlike.genson.TransformationRuntimeException;
import com.owlike.genson.reflect.BeanCreator.BeanCreatorProperty;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

/**
 * BeanDescriptors are used to serialize/deserialize objects based on their fields, methods and
 * constructors. By default it is supposed to work on JavaBeans, however it can be configured and
 * extended to support different kind of objects.
 * 
 * In most cases BeanDescriptors should not be used directly as it is used internally to support
 * objects not handled by the default Converters. The most frequent case when you will use directly
 * a BeanDescriptor is when you want to deserialize into an existing instance. Here is an example :
 * 
 * <pre>
 * Genson genson = new Genson();
 * BeanDescriptorProvider provider = genson.getBeanDescriptorFactory();
 * BeanDescriptor&lt;MyClass&gt; descriptor = provider.provide(MyClass.class, genson);
 * 
 * MyClass existingInstance = descriptor.deserialize(existingInstance, new JsonReader(&quot;{}&quot;),
 * 		new Context(genson));
 * </pre>
 * 
 * @see BeanDescriptorProvider
 * 
 * @author eugen
 * 
 * @param <T>
 *            type that this BeanDescriptor can serialize and deserialize.
 */
public class BeanDescriptor<T> implements Converter<T> {
	final Class<?> fromDeclaringClass;
	final Class<T> ofClass;
	final Map<String, PropertyMutator<T, ?>> mutableProperties;
	final List<PropertyAccessor<T, ?>> accessibleProperties;

	private final BeanCreator<T> _creator;
	private final boolean _noArgCtr;

	private final static Comparator<BeanProperty<?>> _readablePropsComparator = new Comparator<BeanProperty<?>>() {
		public int compare(BeanProperty<?> o1, BeanProperty<?> o2) {
			return o1.name.compareToIgnoreCase(o2.name);
		}
	};

	public BeanDescriptor(Class<T> forClass, Class<?> fromDeclaringClass,
			List<PropertyAccessor<T, ?>> readableBps,
			Map<String, PropertyMutator<T, ?>> writableBps, BeanCreator<T> creator) {
		this.ofClass = forClass;
		this.fromDeclaringClass = fromDeclaringClass;
		this._creator = creator;
		mutableProperties = writableBps;

		Collections.sort(readableBps, _readablePropsComparator);

		accessibleProperties = Collections.unmodifiableList(readableBps);
		if (_creator != null)
			_noArgCtr = _creator.parameters.size() == 0;
		else
			_noArgCtr = false;
	}

	public boolean isReadable() {
		return !accessibleProperties.isEmpty();
	}

	public boolean isWritable() {
		return _creator != null;
	}

	public void serialize(T obj, ObjectWriter writer, Context ctx) throws TransformationException,
			IOException {
		writer.beginObject();
		for (PropertyAccessor<T, ?> accessor : accessibleProperties) {
			accessor.serialize(obj, writer, ctx);
		}
		writer.endObject();
	}

	public T deserialize(ObjectReader reader, Context ctx) throws TransformationException,
			IOException {
		T bean = null;
		// optimization for default ctr
		if (_noArgCtr) {
			bean = _creator.create();
			deserialize(bean, reader, ctx);
		} else {
			if (_creator == null)
				throw new TransformationRuntimeException("No constructor has been found for type "
						+ ofClass);
			bean = _deserWithCtrArgs(reader, ctx);
		}
		return bean;
	}

	public void deserialize(T into, ObjectReader reader, Context ctx) throws IOException,
			TransformationException {
		reader.beginObject();
		for (; reader.hasNext();) {
			reader.next();
			String propName = reader.name();
			PropertyMutator<T, ?> mutator = mutableProperties.get(propName);
			if (mutator != null) {
				mutator.deserialize(into, reader, ctx);
			} else {
				// TODO make it configurable
				reader.skipValue();
			}
		}
		reader.endObject();
	}

	protected T _deserWithCtrArgs(ObjectReader reader, Context ctx) throws TransformationException,
			IOException {
		List<String> names = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();

		reader.beginObject();
		for (; reader.hasNext();) {
			reader.next();
			String propName = reader.name();
			PropertyMutator<T, ?> muta = mutableProperties.get(propName);

			if (muta != null) {
				Object param = muta.deserialize(reader, ctx);
				names.add(propName);
				values.add(param);
			} else {
				// TODO make it configurable
				reader.skipValue();
			}
		}

		int size = names.size();
		int settersToCallCnt = size - _creator.parameters.size();
		if (settersToCallCnt < 0) settersToCallCnt = 0;
		Object[] creatorArgs = new Object[_creator.parameters.size()];
		String[] newNames = new String[size];
		Object[] newValues = new Object[size];
		// TODO if field for ctr is missing what to do? make it also configurable...?
		for (int i = 0, j = 0; i < size; i++) {
			BeanCreatorProperty<T, ?> mp = _creator.parameters.get(names.get(i));
			if (mp != null) {
				creatorArgs[mp.index] = values.get(i);
			} else {
				newNames[j] = names.get(i);
				newValues[j] = values.get(i);
				j++;
			}
		}

		T bean = _creator.create(creatorArgs);
		for (int i = 0; i < size; i++) {
			@SuppressWarnings("unchecked")
			PropertyMutator<T, Object> property = (PropertyMutator<T, Object>) mutableProperties
					.get(newNames[i]);
			if (property != null) property.mutate(bean, newValues[i]);
		}
		reader.endObject();
		return bean;
	}

	public Class<T> getOfClass() {
		return ofClass;
	}

}