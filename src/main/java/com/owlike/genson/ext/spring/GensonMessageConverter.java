package com.owlike.genson.ext.spring;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.owlike.genson.Context;
import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import com.owlike.genson.ThreadLocalHolder;
import com.owlike.genson.TransformationException;
import com.owlike.genson.annotation.WithBeanView;
import com.owlike.genson.stream.ObjectWriter;

public class GensonMessageConverter extends AbstractHttpMessageConverter<Object> {

	private final Genson genson;

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	public GensonMessageConverter() {
		this(new Genson.Builder().setHtmlSafe(true).setSkipNull(true).setWithBeanViewConverter(true).create());
	}

	public GensonMessageConverter(Genson genson) {
		super(new MediaType("application", "json", DEFAULT_CHARSET));
		this.genson = genson;
	}

	@Override
	protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		MethodParameter mp = ThreadLocalHolder.get("__GENSON$method_param", MethodParameter.class);
		try {
			WithBeanView ann = null;
			Type type = clazz;
			if (mp != null) {
				ann = mp.getMethodAnnotation(WithBeanView.class);
				type = mp.getGenericParameterType();
			}
			
			GenericType<?> genericType = GenericType.of(type);
			
			if (ann != null)
				return genson.deserialize(genericType,
						genson.createReader(inputMessage.getBody()),
						new Context(genson, Arrays.asList(ann.views())));
			else
				return genson.deserialize(genericType,
						genson.createReader(inputMessage.getBody()), new Context(genson));
		} catch (TransformationException e) {
			throw new IOException("Could not deserialize type " + clazz.getName());
		}
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void writeInternal(Object t, HttpOutputMessage outputMessage) throws IOException,
			HttpMessageNotWritableException {
		try {
			ObjectWriter writer = genson.createWriter(outputMessage.getBody());
			MethodParameter mp = ThreadLocalHolder.get("__GENSON$return_param", MethodParameter.class);
			WithBeanView ann = mp != null ? mp.getMethodAnnotation(WithBeanView.class) : null;
			if (ann != null)
				genson.serialize(t, writer, ann.views());
			else
				genson.serialize(t, writer);
			writer.flush();
		} catch (TransformationException e) {
			throw new IOException("Could not serialize type " + t.getClass(), e);
		}
	}
}
