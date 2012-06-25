package org.genson.serialization;

import java.io.IOException;
import java.lang.reflect.Type;

import org.genson.Context;
import org.genson.TransformationException;
import org.genson.stream.ObjectWriter;

/**
 * Same as {@link org.genson.deserialization.ChainedDeserializer ChainedDeserializer}.
 * 
 * @see org.genson.deserialization.ChainedDeserializer ChainedDeserializer
 * 
 * @author eugen
 * 
 */
public abstract class ChainedSerializer implements Serializer<Object> {
	private Serializer<Object> _next;

	public ChainedSerializer() {

	}

	protected ChainedSerializer(Serializer<Object> next) {
		this._next = next;
	}

	@Override
	public final void serialize(final Object obj, final Type type, final ObjectWriter writer,
			final Context ctx) throws TransformationException, IOException {
		boolean handled = handleSerialization(obj, type, writer, ctx);
		if (!handled) {
			if (_next != null)
				_next.serialize(obj, type, writer, ctx);
			else
				throw new TransformationException("Could not serialize type " + obj.getClass());
		}
		// OK
	}

	/**
	 * Attention renvoie le serializer en argument pour pouvoir chainer dessus des operations
	 * 
	 * @param <T>
	 * @param serializer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Serializer<?>> T withNext(T serializer) {
		if (_next != null)
			throw new IllegalStateException();

		_next = (Serializer<Object>) serializer;

		return serializer;
	}

	/**
	 * 
	 * @param obj
	 * @param type
	 * @param writer
	 * @param ctx
	 * @return true if this serializer handled this object or false otherwise.
	 * 
	 * @throws TransformationException
	 * @throws IOException
	 * @see {@link org.genson.deserialization.ChainedDeserializer ChainedDeserializer}
	 */
	protected abstract boolean handleSerialization(Object obj, Type type, ObjectWriter writer,
			Context ctx) throws TransformationException, IOException;
}
