package org.likeit.transformation.spring;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import org.likeit.transformation.ObjectTransformer;
import org.likeit.transformation.TransformationException;
import org.likeit.transformation.annotation.JsonBeanView;
import org.likeit.transformation.stream.JsonWriter;
import org.likeit.transformation.stream.ObjectWriter;

public class JsonMessageConverter extends AbstractHttpMessageConverter<Object> {

    private ObjectTransformer transformer = new ObjectTransformer.Builder().setHtmlSafe(true).setSkipNull(true).create();

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public JsonMessageConverter(){
        super(new MediaType("application", "json", DEFAULT_CHARSET));
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz,
                                  HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {

       return null;

    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return ExtendedReqRespBodyMethodProcessor.getCurrentMethodParameter() != null;
    }

	@SuppressWarnings("unchecked")
	@Override
    protected void writeInternal(Object t, 
                                 HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

    	ObjectWriter ow = new JsonWriter(new PrintWriter(outputMessage.getBody()), true, true);
    	try {
    		JsonBeanView ann = ExtendedReqRespBodyMethodProcessor.getCurrentMethodParameter().getMethodAnnotation(JsonBeanView.class);
			if ( ann != null ) transformer.serialize(t, ow, ann.views());
			else transformer.serialize(t, ow);
			ow.flush();
		} catch (TransformationException e) {
			throw new IOException("Erreur serialization du type " + t.getClass(), e);
		}
    }
}

