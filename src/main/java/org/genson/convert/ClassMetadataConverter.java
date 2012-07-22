package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;

import org.genson.Context;
import org.genson.Converter;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.annotation.HandleClassMetadata;
import org.genson.reflect.TypeUtil;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;
import org.genson.stream.ValueType;

/**
 * Converter responsible of writing and reading @class metadata. This is useful if you want to be
 * able to deserialize all serialized objects without knowing their concrete type. Metadata is
 * written only in objects (never in arrays or literals) and is always the first element in the
 * object. Most default converters are annotated with @HandleClassMetada indicating that they will
 * not have class metadata written nor use it during deserialization. This feature is disabled by
 * default, to enable it use {@link org.genson.Genson.Builder#setWithClassMetadata(boolean)
 * Builder.setWithClassMetadata(true)}. Genson provides also a aliases mechanism that will replace
 * the class name with the value of your alias in the generated stream. You should use it as it is
 * more "secure" and provides you more flexibility. Indeed if you change the name or package of your
 * class you will still be able to deserialize to it. An example allowing to serialize a object and then deserialize it back
 * without knowing its type would be:
 * 
 * <pre>
 * class Foo {
 * }
 * 
 * Genson genson = new Genson.Builder().setWithClassMetadata(true).addAlias("foo", Foo.class).create();
 * String json = genson.serialize(new Foo());
 * // json value will be {&quot;@class&quot;:&quot;Foo&quot;}
 * Foo foo = (Foo) genson.deserialize(json, Object.class);
 * </pre>
 * 
 * @see org.genson.stream.ObjectWriter#writeMetadata(String, String) ObjectWriter.metadata(key,
 *      value)
 * @see org.genson.stream.ObjectReader#metadata(String) ObjectReader.metadata("class")
 * @see org.genson.Genson#aliasFor(Class) Genson.aliasFor(Class)
 * 
 * @author eugen
 * 
 * @param <T>
 */
public class ClassMetadataConverter<T> extends ConverterDecorator<T> {
	public static class ClassMetadataConverterFactory extends ChainedFactory {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Converter<?> create(Type type, Genson genson, Converter<?> nextConverter) {
			if (nextConverter == null)
				throw new IllegalArgumentException(
						"nextConverter must be not null for ClassMetadataConverter, "
								+ "as ClassMetadataConverter can not be the last converter in the chain!");

			if (genson.isWithClassMetadata()
					&& !ConverterDecorator.toAnnotatedElement(nextConverter).isAnnotationPresent(
							HandleClassMetadata.class))
				return new ClassMetadataConverter(TypeUtil.getRawClass(type), nextConverter);
			else
				return nextConverter;
		}
	}

	private final Class<T> tClass;

	public ClassMetadataConverter(Class<T> tClass, Converter<T> delegate) {
		super(delegate);
		this.tClass = tClass;
	}

	@Override
	public void serialize(T obj, ObjectWriter writer, Context ctx) throws TransformationException,
			IOException {
		writer.beginNextObjectMetadata()
				.writeMetadata("class", ctx.genson.aliasFor(obj.getClass()));
		wrapped.serialize(obj, writer, ctx);
	}

	@Override
	public T deserialize(ObjectReader reader, Context ctx) throws TransformationException,
			IOException {
		if (ValueType.OBJECT.equals(reader.getValueType())) {
			String className = reader.nextObjectMetadata().metadata("class");
			if (className != null) {
				try {
					Class<?> classFromMetadata = ctx.genson.classFor(className);
					if (!classFromMetadata.equals(tClass)) {
						Converter<T> deser = ctx.genson.provideConverter(classFromMetadata);
						return deser.deserialize(reader, ctx);
					}
				} catch (ClassNotFoundException e) {
					throw new TransformationException(
							"Could not use @class metadata, no such class: " + className);
				}
			}
		}
		return wrapped.deserialize(reader, ctx);
	}

}
