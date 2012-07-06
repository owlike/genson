package org.genson.reflect;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genson.Context;
import org.genson.TransformationException;
import org.genson.convert.Converter;
import org.genson.reflect.BeanCreator.BeanCreatorProperty;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;

public class BeanDescriptor<T> implements Converter<T> {

	final Class<T> ofClass;
	final List<BeanCreator<T>> _creators;
	final Map<String, PropertyMutator<T, ?>> mutableProperties;
	final List<PropertyAccessor<T, ?>> accessibleProperties;

	private final Map<List<String>, BeanCreator<T>> namesCreatorCache;
	private final BeanCreator<T> _emptyCtr;

	private final static Comparator<BeanProperty<?>> _readablePropsComparator = new Comparator<BeanProperty<?>>() {
		@Override
		public int compare(BeanProperty<?> o1, BeanProperty<?> o2) {
			return o1.name.compareToIgnoreCase(o2.name);
		}
	};

	// private final static Comparator<BeanCreator> _beanCreatorsComparator = new
	// Comparator<BeanCreator>() {
	// @Override
	// public int compare(BeanCreator o1, BeanCreator o2) {
	// return o1.parameters.size() - o2.parameters.size();
	// }
	// };

	public BeanDescriptor(Class<T> forClass, List<PropertyAccessor<T, ?>> readableBps,
			Map<String, PropertyMutator<T, ?>> writableBps, List<BeanCreator<T>> creators) {
		this.ofClass = forClass;
		this._creators = creators;
		namesCreatorCache = new HashMap<List<String>, BeanCreator<T>>();
		mutableProperties = writableBps;

		Collections.sort(creators);
		Collections.sort(readableBps, _readablePropsComparator);

		accessibleProperties = Collections.unmodifiableList(readableBps);
		if (_creators.size() > 0 && _creators.get(0).parameters.size() == 0) {
			// lets look if all properties have a field or method mutator, this means that we can
			// use the no arg beancreator (constructor or method) and still set all the properties
			boolean ok = true;
			// we look if there exists property that is only present as ctr arg, if so we will have
			for (PropertyMutator<T, ?> muta : mutableProperties.values())
				if (muta instanceof BeanCreatorProperty) {
					ok = false;
					break;
				}
			if (ok)
				_emptyCtr = _creators.get(0);
			else
				_emptyCtr = null;
		} else
			_emptyCtr = null;
	}

	public boolean isReadable() {
		return !accessibleProperties.isEmpty();
	}

	public boolean isWritable() {
		return !_creators.isEmpty();
	}

	@Override
	public void serialize(T obj, Type type, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		writer.beginObject();
		for (PropertyAccessor<T, ?> accessor : accessibleProperties) {
			accessor.serialize(obj, writer, ctx);
		}
		writer.endObject();
	}

	@Override
	public T deserialize(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		T bean = null;
		// optimization for default ctr
		if (_emptyCtr != null) {
			bean = _emptyCtr.create();
			deserialize(bean, reader, ctx);
		} else
			bean = _deserWithCtrArgs(type, reader, ctx);
		return bean;
	}

	public void deserialize(T into, ObjectReader reader, Context ctx) throws IOException, TransformationException {
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
	
	protected T _deserWithCtrArgs(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
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

		BeanCreator<T> creator = namesCreatorCache.get(names);
		if (creator == null) {
			int bestMatch = -1;
			for (BeanCreator<T> c : _creators) {
				int cnt = c.contains(names);
				if (cnt > bestMatch) {
					creator = c;
					bestMatch = cnt;
				}
			}
			namesCreatorCache.put(names, creator);
		}

		int size = names.size();
		int settersToCallCnt = size - creator.parameters.size();
		if (settersToCallCnt < 0)
			settersToCallCnt = 0;
		Object[] creatorArgs = new Object[creator.parameters.size()];
		String[] newNames = new String[size];
		Object[] newValues = new Object[size];
		// TODO if field for ctr is missing what to do?
		for (int i = 0, j = 0; i < size; i++) {
			BeanCreatorProperty<T, ?> mp = creator.parameters.get(names.get(i));
			if (mp != null) {
				creatorArgs[mp.index] = values.get(i);
			} else {
				newNames[j] = names.get(i);
				newValues[j] = values.get(i);
				j++;
			}
		}

		T bean = creator.create(creatorArgs);
		for (int i = 0; i < size; i++) {
			@SuppressWarnings("unchecked")
			PropertyMutator<T, Object> property = (PropertyMutator<T, Object>) mutableProperties.get(newNames[i]);
			if (property != null)
				property.mutate(bean, newValues[i]);
		}
		reader.endObject();
		return bean;
	}

}