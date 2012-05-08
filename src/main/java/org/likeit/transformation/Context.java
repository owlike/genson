package org.likeit.transformation;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.likeit.transformation.deserialization.DeserializerProvider;
import org.likeit.transformation.serialization.BeanView;
import org.likeit.transformation.serialization.SerializerProvider;
import org.likeit.transformation.stream.ObjectReader;
import org.likeit.transformation.stream.ObjectWriter;

public class Context {
	private final List<Class<? extends BeanView<?>>> views;
	private final SerializerProvider serializerProvider;
	private final DeserializerProvider deserializerProvider;
	
	public Context(SerializerProvider serializerProvider, 
			DeserializerProvider deserializerProvider,
			List<Class<? extends BeanView<?>>> views) {
		this.serializerProvider = serializerProvider;
		this.deserializerProvider = deserializerProvider;
		this.views = new ArrayList<Class<? extends BeanView<?>>>();
		if ( views != null ) this.views.addAll(views);
	}
	
	public List<Class<? extends BeanView<?>>> views() {
		return views;
	}
	
	/**
	 * Ne doit pas etre rappele pour la valeur courrante, sinon ça va boucler indéfiniement
	 * @param obj
	 * @throws TransformationException 
	 */
	public <T> void serialize(T obj, Type type, ObjectWriter writer) throws TransformationException {
		try {
    		if ( obj != null ) serializerProvider.resolveSerializer(type).serialize(obj, type, writer, this);
    		else writer.valueNull();
		} catch (IOException e) {
			throw new TransformationException("Serialization error for type " + type, e);
		}
	}
	
	@SuppressWarnings("unchecked") // normalement c'est garanti par le provider que le type matche bien
	public <T> T deserialize(Type type, ObjectReader reader) throws TransformationException {
		try {
			if ( reader.value() != null ) return (T) deserializerProvider.resolveDeserializer(type).deserialize(type, reader, this);
			else return null;
		} catch (IOException ioe) {
			throw new TransformationException("Deserialization error for type " + type, ioe);
		}
	}
}
