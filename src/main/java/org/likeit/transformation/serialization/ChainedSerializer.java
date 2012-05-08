package org.likeit.transformation.serialization;

import java.io.IOException;
import java.lang.reflect.Type;

import org.likeit.transformation.Context;
import org.likeit.transformation.TransformationException;
import org.likeit.transformation.stream.ObjectWriter;

public abstract class ChainedSerializer implements Serializer<Object> {
	private Serializer<Object> _next;
	
	public ChainedSerializer() {
		
	}
	
	protected ChainedSerializer(Serializer<Object> next) {
		this._next = next;
	}
	
	@Override
	public final void serialize(Object obj, Type type, ObjectWriter writer, Context ctx) throws TransformationException, IOException {
		boolean handled = handleSerialization(obj, type, writer, ctx);
		if ( !handled ) {
			if ( _next != null ) _next.serialize(obj, type, writer, ctx);
			else 
				throw new TransformationException("Could not serialize type " + obj.getClass());
		}
		// OK
	}
	
	/**
	 * Attention renvoie le serializer en argument pour pouvoir chainer dessus des operations
	 * @param <T>
	 * @param serializer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Serializer<?>> T withNext(T serializer) {
		if ( _next != null ) throw new IllegalStateException();
		
		_next = (Serializer<Object>) serializer;
		
		return serializer;
	}
	
	protected abstract boolean handleSerialization(Object obj, Type type, ObjectWriter writer, Context ctx) throws TransformationException, IOException;
}
