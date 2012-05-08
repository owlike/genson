package org.likeit.transformation.deserialization;

import java.io.IOException;
import java.lang.reflect.Type;

import org.likeit.transformation.Context;
import org.likeit.transformation.TransformationException;
import org.likeit.transformation.stream.ObjectReader;

public abstract class ChainedDeserializer implements Deserializer<Object> {
	private Deserializer<Object> _next;
	
	public ChainedDeserializer() {
		
	}
	
	protected ChainedDeserializer(Deserializer<Object> next) {
		this._next = next;
	}
	
	/*
	 * TODO attention si la valeur deserialize vaut null dans un cas normal, boucle infinie
	 */
	@Override
	public final Object deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException, IOException {
		Object value = handleDeserialization(type, reader, ctx);
		if ( value == null ) {
			if ( _next != null ) _next.deserialize(type, reader, ctx);
			else 
				throw new TransformationException("Could not deserialize type " + type);
		}
		
		return value;
	}
	
	/**
	 * Attention renvoie le serializer en argument pour pouvoir chainer dessus des operations
	 * @param <T>
	 * @param serializer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Deserializer<?>> T withNext(T deserializer) {
		if ( _next != null ) throw new IllegalStateException();
		
		_next = (Deserializer<Object>) deserializer;
		
		return deserializer;
	}
	
	protected abstract Object handleDeserialization(Type type, ObjectReader reader, Context ctx) throws TransformationException, IOException;
}

