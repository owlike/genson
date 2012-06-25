package org.genson.deserialization;

import java.io.IOException;
import java.lang.reflect.Type;

import org.genson.Context;
import org.genson.TransformationException;
import org.genson.stream.ObjectReader;

/**
 * The mechanism of using one deserializer or another at runtime based on the context is provided by
 * classes that extend ChainedSerializer. The {@link org.genson.deserializer.BeanViewDeserializer
 * BeanViewDeserializer} is the perfect example, if a view is present it will apply it, otherwise it
 * will let the chain continue until a Serializer handles that object. ChainedDeserializers are used
 * if no other Deserializer could be provided for this type.
 * 
 * @see org.genson.deserializer.BeanViewDeserializer BeanViewDeserializer
 * 
 * @author eugen
 * 
 */
public abstract class ChainedDeserializer implements Deserializer<Object> {
	private Deserializer<Object> _next;

	/**
	 * Object used to indicate that this deserializer could not handle the deserialization.
	 */
	protected final static Object NULL = new Object();

	public ChainedDeserializer() {

	}

	protected ChainedDeserializer(Deserializer<Object> next) {
		this._next = next;
	}

	@Override
	public final Object deserialize(final Type type, final ObjectReader reader, final Context ctx)
			throws TransformationException, IOException {
		Object value = handleDeserialization(type, reader, ctx);
		if (value == NULL) {
			if (_next != null)
				value = _next.deserialize(type, reader, ctx);
			else
				throw new TransformationException("Could not deserialize ValueToken "
						+ reader.getValueType().name() + " to java type " + type);
		}

		return value;
	}

	/**
	 * Attention renvoie le serializer en argument pour pouvoir chainer dessus des operations
	 * 
	 * @param <T>
	 * @param serializer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Deserializer<?>> T withNext(T deserializer) {
		if (_next != null)
			throw new IllegalStateException();

		_next = (Deserializer<Object>) deserializer;

		return deserializer;
	}

	/**
	 * Implementations of this method must try to handle the deserialization, if this deserializer
	 * can not handle it (not the right type or the input does not match) then it must return the
	 * {@link #NULL} object, so deserialization can be delegated to the next deserializer.
	 * 
	 * @param type of the deserialized object
	 * @param reader from which to read the input data
	 * @param ctx is the current Context for the deserialization
	 * @return the deserialized object or the {@link #NULL} object if it could not be done
	 * @throws TransformationException
	 * @throws IOException
	 */
	protected abstract Object handleDeserialization(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException;
}
